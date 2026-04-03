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
                <div class="message-text" v-html="renderMarkdown(msg.content)"></div>
                <div v-if="msg.knowledgeContext" class="message-context">
                  <el-collapse>
                    <el-collapse-item title="查看参考知识">
                      <div class="context-content" v-html="renderMarkdown(msg.knowledgeContext)"></div>
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
                  <i class="el-icon-loading"></i> 正在思考,请稍候...
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
import MarkdownIt from 'markdown-it';
import 'highlight.js/styles/atom-one-dark.css';

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
      },
      // Markdown缓存
      markdownCache: new Map(),
      // Markdown解析器
      md: new MarkdownIt({
        html: true,
        linkify: true,
        typographer: true,
        breaks: true,
        highlight: function (str, lang) {
          if (lang && hljs.getLanguage(lang)) {
            try {
              return '<pre class="hljs"><code>' +
                     hljs.highlight(str, { language: lang, ignoreIllegals: true }).value +
                     '</code></pre>';
            } catch (__) {}
          }
          return '<pre class="hljs"><code>' + md.utils.escapeHtml(str) + '</code></pre>';
        }
      })
    };
  },
  created() {
    this.getHistoryList();
  },
  methods: {
    /** 渲染Markdown */
    renderMarkdown(text) {
      if (!text) return '';
      // 检查缓存
      if (this.markdownCache.has(text)) {
        return this.markdownCache.get(text);
      }
      // 渲染并缓存
      const rendered = this.md.render(text);
      this.markdownCache.set(text, rendered);
      return rendered;
    },

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
        console.log('AI响应成功');
        const data = response.data;
        console.log('AI答案数据:', data);
        
        // 确保数据存在
        if (!data || !data.answer) {
          throw new Error('响应数据格式错误');
        }
        
        // 先设置 loading 为 false,再添加消息
        this.loading = false;
        
        this.messageList.push({
          type: "ai",
          content: data.answer,
          knowledgeContext: data.knowledgeContext
        });
        
        console.log('AI回答已添加到列表, loading状态:', this.loading);

        this.$nextTick(() => {
          this.scrollToBottom();
        });

        // 暂时注释,测试是否导致无限循环
        // this.getHistoryList();
      }).catch(error => {
        console.error('AI问答失败:', error);
        
        // 先设置 loading 为 false
        this.loading = false;
        
        this.messageList.push({
          type: "ai",
          content: "抱歉,请求失败,请稍后重试。错误信息:" + (error.message || "未知错误")
        });
        
        console.log('错误消息已添加, loading状态:', this.loading);

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
          max-width: 70%;
          padding: 12px 15px;
          border-radius: 8px;
          
          .message-text {
            font-size: 14px;
            color: #303133;
            line-height: 1.8;
            word-break: break-word;
            
            // Markdown样式
            ::v-deep {
              h1, h2, h3, h4, h5, h6 {
                margin: 16px 0 10px;
                font-weight: 600;
                line-height: 1.4;
                
                &:first-child {
                  margin-top: 0;
                }
              }
              
              h1 { font-size: 20px; }
              h2 { font-size: 18px; }
              h3 { font-size: 16px; }
              
              p {
                margin: 10px 0;
                line-height: 1.8;
              }
              
              ul, ol {
                margin: 10px 0;
                padding-left: 20px;
              }
              
              li {
                margin: 5px 0;
                line-height: 1.6;
              }
              
              code {
                padding: 2px 6px;
                margin: 0 2px;
                background: #f5f7fa;
                border-radius: 3px;
                font-family: 'Consolas', 'Monaco', monospace;
                font-size: 13px;
                color: #e96900;
              }
              
              pre {
                margin: 12px 0;
                padding: 12px;
                background: #282c34;
                border-radius: 6px;
                overflow-x: auto;
                
                code {
                  padding: 0;
                  margin: 0;
                  background: none;
                  color: #abb2bf;
                  font-size: 13px;
                  line-height: 1.6;
                }
              }
              
              blockquote {
                margin: 12px 0;
                padding: 8px 12px;
                border-left: 4px solid #409eff;
                background: #f5f7fa;
                color: #606266;
              }
              
              table {
                border-collapse: collapse;
                width: 100%;
                margin: 12px 0;
                font-size: 13px;
                
                th, td {
                  border: 1px solid #dcdfe6;
                  padding: 8px 12px;
                  text-align: left;
                }
                
                th {
                  background: #f5f7fa;
                  font-weight: 600;
                }
                
                tr:nth-child(even) {
                  background: #fafafa;
                }
              }
              
              a {
                color: #409eff;
                text-decoration: none;
                
                &:hover {
                  text-decoration: underline;
                }
              }
              
              strong {
                font-weight: 600;
                color: #303133;
              }
              
              em {
                font-style: italic;
                color: #606266;
              }
              
              hr {
                margin: 16px 0;
                border: none;
                border-top: 1px solid #dcdfe6;
              }
            }
          }
          
          .message-context {
            margin-top: 12px;
            padding-top: 12px;
            border-top: 1px dashed #dcdfe6;
            
            ::v-deep .el-collapse {
              border: none;
              
              .el-collapse-item__header {
                font-size: 12px;
                color: #909399;
                background: transparent;
                border: none;
              }
              
              .el-collapse-item__wrap {
                background: transparent;
                border: none;
              }
              
              .el-collapse-item__content {
                padding-bottom: 10px;
                
                .context-content {
                  font-size: 12px;
                  color: #606266;
                  line-height: 1.6;
                  max-height: 300px;
                  overflow-y: auto;
                  word-break: break-word;
                  
                  // Markdown样式
                  ::v-deep {
                    h1, h2, h3, h4, h5, h6 {
                      margin: 12px 0 8px;
                      font-size: 14px;
                    }
                    
                    p {
                      margin: 8px 0;
                    }
                    
                    code {
                      padding: 2px 4px;
                      background: #f5f7fa;
                      border-radius: 2px;
                      font-size: 11px;
                    }
                    
                    pre {
                      margin: 8px 0;
                      padding: 8px;
                      background: #f5f7fa;
                      border-radius: 4px;
                      overflow-x: auto;
                      
                      code {
                        padding: 0;
                        background: none;
                        font-size: 11px;
                      }
                    }
                  }
                }
              }
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
