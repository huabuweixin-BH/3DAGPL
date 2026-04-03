<template>
  <div class="app-container ai-chat-container">
    <el-row :gutter="20">
      <!-- 左侧对话历史 -->
      <el-col :span="6">
        <div class="history-panel">
          <div class="history-header">
            <h3>对话历史</h3>
            <el-button type="primary" size="mini" @click="clearHistory">清空</el-button>
          </div>
          <div class="history-list" v-loading="historyLoading">
            <div
              v-for="item in historyList"
              :key="item.id"
              class="history-item"
              :class="{ active: currentHistoryId === item.id }"
              @click="selectHistory(item)"
            >
              <div class="history-question">{{ item.question }}</div>
              <div class="history-time">{{ item.createTime }}</div>
            </div>
            <el-empty v-if="!historyList.length" description="暂无对话历史"></el-empty>
          </div>
        </div>
      </el-col>

      <!-- 右侧对话区域 -->
      <el-col :span="18">
        <div class="chat-panel">
          <!-- 消息列表 -->
          <div class="message-list" ref="messageList">
            <div
              v-for="(msg, index) in messageList"
              :key="index"
              class="message-item"
              :class="msg.type"
            >
              <div class="message-avatar">
                <i :class="msg.type === 'user' ? 'el-icon-user' : 'el-icon-service'"></i>
              </div>
              <div class="message-content">
                <div class="message-text">{{ msg.content }}</div>
                <div v-if="msg.knowledgeContext" class="message-context">
                  <el-collapse>
                    <el-collapse-item title="查看参考知识">
                      <div class="context-content">{{ msg.knowledgeContext }}</div>
                    </el-collapse-item>
                  </el-collapse>
                </div>
              </div>
            </div>
            <div v-if="loading" class="message-item ai">
              <div class="message-avatar">
                <i class="el-icon-service"></i>
              </div>
              <div class="message-content">
                <div class="message-text">
                  <i class="el-icon-loading"></i> 正在思考...
                </div>
              </div>
            </div>
          </div>

          <!-- 输入区域 -->
          <div class="input-area">
            <el-form :model="queryParams" ref="queryForm" :inline="true">
              <el-form-item>
                <el-checkbox v-model="queryParams.useRag">使用知识库</el-checkbox>
              </el-form-item>
              <el-form-item v-if="queryParams.useRag">
                <span>TopK:</span>
                <el-input-number v-model="queryParams.topK" :min="1" :max="10" size="small"></el-input-number>
              </el-form-item>
            </el-form>
            <div class="input-box">
              <el-input
                v-model="question"
                type="textarea"
                :autosize="{ minRows: 2, maxRows: 6 }"
                placeholder="请输入您的问题..."
                @keyup.enter.native="handleChat"
                :disabled="loading"
              ></el-input>
              <el-button
                type="primary"
                @click="handleChat"
                :loading="loading"
                icon="el-icon-s-promotion"
              >
                发送
              </el-button>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { chatAi, listChatHistory, delChatHistory } from "@/api/system/aichat";

export default {
  name: "AiChat",
  data() {
    return {
      // 问题输入
      question: "",
      // 消息列表
      messageList: [],
      // 加载状态
      loading: false,
      // 历史加载状态
      historyLoading: false,
      // 当前选中的历史ID
      currentHistoryId: null,
      // 历史列表
      historyList: [],
      // 查询参数
      queryParams: {
        useRag: true,
        topK: 5
      }
    };
  },
  created() {
    this.getHistoryList();
  },
  methods: {
    /** 获取对话历史列表 */
    getHistoryList() {
      this.historyLoading = true;
      listChatHistory({ pageNum: 1, pageSize: 50 }).then(response => {
        this.historyList = response.rows || [];
        this.historyLoading = false;
      }).catch(() => {
        this.historyLoading = false;
      });
    },

    /** 选择历史记录 */
    selectHistory(item) {
      this.currentHistoryId = item.id;
      this.messageList = [
        { type: "user", content: item.question },
        { type: "ai", content: item.answer, knowledgeContext: null }
      ];
      this.$nextTick(() => {
        this.scrollToBottom();
      });
    },

    /** AI问答 */
    handleChat() {
      if (!this.question.trim()) {
        this.$message.warning("请输入问题");
        return;
      }

      // 添加用户消息
      this.messageList.push({
        type: "user",
        content: this.question
      });

      const questionText = this.question;
      this.question = "";
      this.loading = true;

      this.$nextTick(() => {
        this.scrollToBottom();
      });

      // 调用AI接口
      chatAi({
        question: questionText,
        useRag: this.queryParams.useRag,
        topK: this.queryParams.topK
      }).then(response => {
        const data = response.data;
        this.messageList.push({
          type: "ai",
          content: data.answer,
          knowledgeContext: data.knowledgeContext
        });
        this.loading = false;
        
        // 刷新历史列表
        this.getHistoryList();
        
        this.$nextTick(() => {
          this.scrollToBottom();
        });
      }).catch(error => {
        this.messageList.push({
          type: "ai",
          content: "抱歉,请求失败,请稍后重试。"
        });
        this.loading = false;
        
        this.$nextTick(() => {
          this.scrollToBottom();
        });
      });
    },

    /** 滚动到底部 */
    scrollToBottom() {
      const container = this.$refs.messageList;
      if (container) {
        container.scrollTop = container.scrollHeight;
      }
    },

    /** 清空历史 */
    clearHistory() {
      this.$confirm("确定要清空所有对话历史吗?", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(() => {
        const ids = this.historyList.map(item => item.id);
        delChatHistory(ids).then(() => {
          this.$message.success("清空成功");
          this.getHistoryList();
          this.messageList = [];
        });
      }).catch(() => {});
    }
  }
};
</script>

<style scoped lang="scss">
.ai-chat-container {
  height: calc(100vh - 120px);
  
  .history-panel {
    background: #fff;
    border-radius: 8px;
    padding: 20px;
    height: calc(100vh - 120px);
    display: flex;
    flex-direction: column;
    
    .history-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
      padding-bottom: 15px;
      border-bottom: 1px solid #e4e7ed;
      
      h3 {
        margin: 0;
        font-size: 18px;
        color: #303133;
      }
    }
    
    .history-list {
      flex: 1;
      overflow-y: auto;
      
      .history-item {
        padding: 12px 15px;
        margin-bottom: 10px;
        background: #f5f7fa;
        border-radius: 6px;
        cursor: pointer;
        transition: all 0.3s;
        
        &:hover {
          background: #e6f7ff;
        }
        
        &.active {
          background: #e6f7ff;
          border-left: 3px solid #409eff;
        }
        
        .history-question {
          font-size: 14px;
          color: #303133;
          margin-bottom: 8px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        
        .history-time {
          font-size: 12px;
          color: #909399;
        }
      }
    }
  }
  
  .chat-panel {
    background: #fff;
    border-radius: 8px;
    padding: 20px;
    height: calc(100vh - 120px);
    display: flex;
    flex-direction: column;
    
    .message-list {
      flex: 1;
      overflow-y: auto;
      padding: 20px 0;
      
      .message-item {
        display: flex;
        margin-bottom: 20px;
        
        &.user {
          flex-direction: row-reverse;
          
          .message-content {
            background: #e6f7ff;
            margin-right: 15px;
          }
        }
        
        &.ai {
          .message-content {
            background: #f5f7fa;
            margin-left: 15px;
          }
        }
        
        .message-avatar {
          width: 40px;
          height: 40px;
          border-radius: 50%;
          background: #409eff;
          color: #fff;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 20px;
          flex-shrink: 0;
        }
        
        .message-content {
          max-width: 60%;
          padding: 12px 15px;
          border-radius: 8px;
          
          .message-text {
            font-size: 14px;
            color: #303133;
            line-height: 1.6;
            white-space: pre-wrap;
            word-break: break-word;
          }
          
          .message-context {
            margin-top: 10px;
            padding-top: 10px;
            border-top: 1px dashed #dcdfe6;
            
            .context-content {
              font-size: 12px;
              color: #606266;
              line-height: 1.6;
              max-height: 200px;
              overflow-y: auto;
              white-space: pre-wrap;
            }
          }
        }
      }
    }
    
    .input-area {
      border-top: 1px solid #e4e7ed;
      padding-top: 20px;
      
      .el-form {
        margin-bottom: 15px;
      }
      
      .input-box {
        display: flex;
        gap: 10px;
        
        .el-textarea {
          flex: 1;
        }
        
        .el-button {
          align-self: flex-end;
        }
      }
    }
  }
}
</style>
