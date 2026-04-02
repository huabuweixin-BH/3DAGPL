<template>
  <div class="app-container forum-container">
    <!-- 搜索栏 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true" class="search-form">
      <el-form-item label="标题">
        <el-input
          v-model="queryParams.title"
          placeholder="请输入文章标题"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 文章列表 -->
    <div class="article-list" v-loading="loading">
      <div
        v-for="article in articleList"
        :key="article.id"
        class="article-card"
        @click="handleViewArticle(article)"
      >
        <div class="article-card-header">
          <h3 class="article-title">{{ article.title }}</h3>
          <el-tag :type="getStatusTagType(article.status)" size="mini" class="status-tag">
            {{ formatStatus(article.status) }}
          </el-tag>
        </div>
        <div class="article-card-body">
          <p class="article-summary">{{ article.summary || '暂无摘要' }}</p>
        </div>
        <div class="article-card-footer">
          <div class="article-meta">
            <span class="meta-item">
              <i class="el-icon-user"></i>
              {{ article.createBy || '未知用户' }}
            </span>
            <span class="meta-item">
              <i class="el-icon-time"></i>
              {{ parseTime(article.createTime, '{y}-{m}-{d} {h}:{i}:{s}') }}
            </span>
          </div>
          <el-button type="text" class="read-more">
            阅读全文 <i class="el-icon-arrow-right"></i>
          </el-button>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty v-if="articleList.length === 0 && !loading" description="暂无文章"></el-empty>
    </div>

    <!-- 分页 -->
    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 文章详情对话框（全屏） -->
    <el-dialog
      :title="''"
      :visible.sync="dialogVisible"
      width="100%"
      top="0"
      :fullscreen="true"
      append-to-body
      class="article-detail-dialog-fullscreen"
      :before-close="handleCloseDialog"
    >
      <div class="article-detail-container" v-if="currentArticle">
        <!-- 关闭按钮 -->
        <el-button class="close-dialog-btn" icon="el-icon-close" circle @click="handleCloseDialog"></el-button>
        
        <div class="article-detail-wrapper">
          <!-- 文章头部 -->
          <div class="article-detail-header">
            <h1 class="article-detail-title">{{ currentArticle.title }}</h1>
            <div class="article-detail-meta">
              <el-tag :type="getStatusTagType(currentArticle.status)" size="small">
                {{ formatStatus(currentArticle.status) }}
              </el-tag>
              <span class="meta-item">
                <i class="el-icon-user"></i>
                {{ currentArticle.createBy }}
              </span>
              <span class="meta-item">
                <i class="el-icon-time"></i>
                {{ parseTime(currentArticle.createTime, '{y}-{m}-{d} {h}:{i}:{s}') }}
              </span>
              <span class="meta-item" v-if="currentArticle.updateTime">
                <i class="el-icon-edit"></i>
                更新于 {{ parseTime(currentArticle.updateTime, '{y}-{m}-{d} {h}:{i}:{s}') }}
              </span>
            </div>
          </div>

          <!-- 摘要 -->
          <div class="article-detail-summary" v-if="currentArticle.summary">
            <div class="section-title">
              <i class="el-icon-document"></i>
              <span>摘要</span>
            </div>
            <p class="summary-content">{{ currentArticle.summary }}</p>
          </div>

          <!-- 正文 -->
          <div class="article-detail-content">
            <div class="section-title">
              <i class="el-icon-edit-outline"></i>
              <span>正文</span>
            </div>
            <div class="article-content-html" v-html="currentArticle.contentHtml"></div>
          </div>

          <el-divider></el-divider>

          <!-- 评论区 -->
          <div class="article-comments-section">
            <div class="section-title">
              <i class="el-icon-chat-dot-round"></i>
              <span>评论区</span>
              <span class="comment-count" v-if="comments.length > 0">({{ comments.length }})</span>
            </div>

            <!-- 发表评论 -->
            <div class="comment-form-wrapper">
              <el-input
                v-model="newComment.content"
                type="textarea"
                :rows="4"
                placeholder="写下你的评论..."
                class="comment-input"
              ></el-input>
              <div class="comment-form-actions">
                <el-button type="primary" @click="handlePostComment" :loading="postingComment">
                  发表评论
                </el-button>
                <el-button @click="() => newComment.content = ''">取消</el-button>
              </div>
            </div>

            <!-- 评论列表 -->
            <div class="comments-list">
              <div
                v-for="comment in comments"
                :key="comment.id"
                class="comment-item"
              >
                <div class="comment-avatar">
                  <i class="el-icon-user-solid"></i>
                </div>
                <div class="comment-content">
                  <div class="comment-header">
                    <span class="comment-author">{{ comment.userName || comment.createBy || '匿名用户' }}</span>
                    <span class="comment-time">
                      {{ parseTime(comment.createTime, '{y}-{m}-{d} {h}:{i}') }}
                    </span>
                  </div>
                  <div class="comment-text">
                    {{ comment.content }}
                  </div>
                  <div class="comment-actions">
                    <el-button type="text" size="mini" @click="handleReplyComment(comment)">
                      <i class="el-icon-chat-line-square"></i> 回复
                    </el-button>
                    <el-button
                      v-if="canDeleteComment(comment)"
                      type="text"
                      size="mini"
                      class="delete-btn"
                      @click="handleDeleteComment(comment)"
                    >
                      <i class="el-icon-delete"></i> 删除
                    </el-button>
                  </div>
                  <!-- 回复列表 -->
                  <div class="reply-list" v-if="comment.replies && comment.replies.length > 0">
                    <div
                      v-for="reply in comment.replies"
                      :key="reply.id"
                      class="reply-item"
                    >
                      <div class="reply-header">
                        <span class="reply-author">{{ reply.userName || reply.createBy || '匿名用户' }}</span>
                        <span class="reply-time">
                          {{ parseTime(reply.createTime, '{y}-{m}-{d} {h}:{i}') }}
                        </span>
                      </div>
                      <div class="reply-text">
                        <span v-if="reply.replyToUserName" class="reply-to">@{{ reply.replyToUserName }} </span>
                        {{ reply.content }}
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 空状态 -->
              <el-empty v-if="comments.length === 0" description="暂无评论，快来抢沙发吧~" :image-size="80"></el-empty>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listArticle } from "@/api/system/article";
import { listComments, addComment, delComment } from "@/api/system/comment";
import { getToken } from '@/utils/auth';

export default {
  name: "AlgorithmFormu",
  data() {
    return {
      // 加载状态
      loading: true,
      // 文章列表
      articleList: [],
      // 总条数
      total: 0,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: null,
        status: '1'
      },
      // 文章详情对话框
      dialogVisible: false,
      // 当前查看的文章
      currentArticle: null,
      // 评论列表
      comments: [],
      // 新评论
      newComment: {
        content: '',
        articleId: null,
        parentId: null,
        replyToUserId: null
      },
      // 发表评论加载中
      postingComment: false
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询文章列表 */
    getList() {
      this.loading = true;
      listArticle(this.queryParams).then(response => {
        this.articleList = response.rows;
        this.total = response.total;
        this.loading = false;
      }).catch(() => {
        this.loading = false;
      });
    },

    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },

    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.queryParams = {
        pageNum: 1,
        pageSize: 10,
        title: null,
        status: '1'
      };
      this.getList();
    },

    /** 查看文章详情 */
    handleViewArticle(article) {
      this.currentArticle = article;
      this.newComment.articleId = article.id;
      this.dialogVisible = true;
      // 加载评论
      this.loadComments(article.id);
    },

    /** 关闭对话框 */
    handleCloseDialog() {
      this.$confirm('确定要关闭文章详情吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.dialogVisible = false;
        this.currentArticle = null;
        this.comments = [];
        this.newComment = { content: '', articleId: null, parentId: null, replyToUserId: null };
      }).catch(() => {});
    },

    /** 加载评论 */
    loadComments(articleId) {
      this.comments = [];
      listComments(articleId).then(response => {
        const allComments = response.data || [];
        // 将评论按 parentId 分组，构建层级结构
        const mainComments = allComments.filter(c => !c.parentId || c.parentId === 0);
        const replies = allComments.filter(c => c.parentId && c.parentId > 0);
        
        // 将回复关联到主评论
        mainComments.forEach(main => {
          main.replies = replies.filter(r => r.parentId === main.id);
        });
        
        this.comments = mainComments;
      }).catch(() => {
        this.comments = [];
      });
    },

    /** 发表评论 */
    handlePostComment() {
      if (!this.newComment.content || !this.newComment.content.trim()) {
        this.$message.warning('请输入评论内容');
        return;
      }

      this.postingComment = true;

      const submitData = {
        articleId: this.newComment.articleId,
        content: this.newComment.content.trim(),
        parentId: this.newComment.parentId || 0,
        replyToUserId: this.newComment.replyToUserId,
        status: '0'
      };

      addComment(submitData).then(response => {
        this.$message.success('评论成功');
        // 重新加载评论
        this.loadComments(this.newComment.articleId);
        this.newComment = { content: '', articleId: null, parentId: null, replyToUserId: null };
        this.postingComment = false;
      }).catch(error => {
        this.$message.error('评论失败：' + (error.message || '未知错误'));
        this.postingComment = false;
      });
    },

    /** 回复评论 */
    handleReplyComment(comment) {
      this.newComment.parentId = comment.id;
      this.newComment.replyToUserId = comment.userId;
      this.$message.info(`回复 @${comment.userName || '用户'}`);
      // 滚动到评论框
      this.$nextTick(() => {
        const commentInput = document.querySelector('.comment-input textarea');
        if (commentInput) {
          commentInput.focus();
        }
      });
    },

    /** 删除评论 */
    handleDeleteComment(comment) {
      this.$confirm('确定要删除这条评论吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        delComment([comment.id]).then(() => {
          this.$message.success('删除成功');
          // 重新加载评论
          this.loadComments(this.currentArticle.id);
        }).catch(error => {
          this.$message.error('删除失败：' + (error.message || '未知错误'));
        });
      }).catch(() => {});
    },

    /** 判断是否可以删除评论 */
    canDeleteComment(comment) {
      // TODO: 根据当前用户权限判断
      return false;
    },

    /** 格式化状态显示 */
    formatStatus(status) {
      if (status === null || status === undefined || status === '') {
        return '未知';
      }
      const statusMap = {
        '0': '待审核',
        '1': '已发布',
        '2': '被拒绝',
        '3': '草稿'
      };
      return statusMap[String(status)] || status;
    },

    /** 获取状态标签类型 */
    getStatusTagType(status) {
      const typeMap = {
        '0': 'warning',
        '1': 'success',
        '2': 'danger',
        '3': 'info'
      };
      return typeMap[String(status)] || 'info';
    }
  }
};
</script>

<style lang="scss" scoped>
.forum-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.search-form {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.article-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.article-card {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px 24px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.08);
  border: 1px solid #e4e7ed;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px 0 rgba(0, 0, 0, 0.12);
    border-color: #409EFF;
  }
}

.article-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.article-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
  line-height: 1.5;
  transition: color 0.3s ease;

  .article-card:hover & {
    color: #409EFF;
  }
}

.status-tag {
  flex-shrink: 0;
  margin-left: 12px;
}

.article-card-body {
  margin-bottom: 16px;
}

.article-summary {
  font-size: 14px;
  color: #606266;
  line-height: 1.8;
  margin: 0;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.article-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

.article-meta {
  display: flex;
  gap: 20px;
  font-size: 13px;
  color: #909399;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;

  i {
    font-size: 14px;
  }
}

.read-more {
  color: #409EFF;
  font-size: 14px;
  padding: 0;

  &:hover {
    color: #66b1ff;
  }

  i {
    margin-left: 4px;
    transition: transform 0.3s ease;
  }

  .article-card:hover & i {
    transform: translateX(4px);
  }
}

/* 文章详情对话框样式 - 全屏 */
.article-detail-dialog-fullscreen {
  ::v-deep .el-dialog {
    height: 100vh !important;
    margin-top: 0 !important;
    max-height: 100vh !important;
  }

  ::v-deep .el-dialog__body {
    padding: 0;
    height: 100%;
  }

  ::v-deep .el-dialog__header {
    padding: 0;
  }
}

.close-dialog-btn {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 2000;
  background-color: rgba(0, 0, 0, 0.5);
  color: #fff;
  border: none;

  &:hover {
    background-color: rgba(0, 0, 0, 0.7);
  }
}

.article-detail-container {
  height: 100%;
  background-color: #f5f7fa;
  overflow-y: auto;
  padding: 60px 20px 20px;
}

.article-detail-wrapper {
  max-width: 900px;
  margin: 0 auto;
  background-color: #fff;
  border-radius: 8px;
  padding: 40px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.article-detail-header {
  margin-bottom: 32px;
}

.article-detail-title {
  font-size: 32px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 20px 0;
  line-height: 1.4;
}

.article-detail-meta {
  display: flex;
  align-items: center;
  gap: 20px;
  font-size: 14px;
  color: #909399;
  flex-wrap: wrap;

  .meta-item {
    display: flex;
    align-items: center;
    gap: 6px;

    i {
      font-size: 16px;
    }
  }
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid #f0f0f0;

  i {
    font-size: 20px;
    color: #409EFF;
  }

  .comment-count {
    font-size: 14px;
    color: #909399;
    font-weight: normal;
  }
}

.article-detail-summary {
  margin-bottom: 32px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
  border-left: 4px solid #409EFF;

  .summary-content {
    font-size: 15px;
    color: #606266;
    line-height: 1.8;
    margin: 0;
  }
}

.article-detail-content {
  margin-bottom: 32px;

  .article-content-html {
    font-size: 16px;
    line-height: 1.8;
    color: #303133;
    min-height: 200px;

    ::v-deep {
      p {
        margin: 16px 0;
      }

      img {
        max-width: 100%;
        height: auto;
        border-radius: 8px;
        margin: 16px 0;
      }

      h1, h2, h3, h4, h5, h6 {
        margin: 24px 0 16px;
        color: #303133;
        font-weight: 600;
      }

      h1 {
        font-size: 28px;
      }

      h2 {
        font-size: 24px;
      }

      h3 {
        font-size: 20px;
      }

      h4 {
        font-size: 18px;
      }

      ul, ol {
        padding-left: 24px;
        margin: 16px 0;
      }

      li {
        margin: 8px 0;
      }

      blockquote {
        margin: 20px 0;
        padding: 16px 20px;
        background-color: #f5f7fa;
        border-left: 4px solid #409EFF;
        border-radius: 4px;
        color: #606266;
      }

      code {
        padding: 3px 8px;
        background-color: #f5f7fa;
        border-radius: 4px;
        font-family: Consolas, Monaco, 'Andale Mono', monospace;
        font-size: 14px;
        color: #e74c3c;
      }

      pre {
        margin: 20px 0;
        padding: 20px;
        background-color: #282c34;
        border-radius: 8px;
        overflow-x: auto;

        code {
          padding: 0;
          background-color: transparent;
          color: #abb2bf;
        }
      }

      table {
        width: 100%;
        border-collapse: collapse;
        margin: 20px 0;

        th, td {
          border: 1px solid #dcdfe6;
          padding: 12px 16px;
          text-align: left;
        }

        th {
          background-color: #f5f7fa;
          font-weight: 600;
        }
      }

      hr {
        border: none;
        border-top: 1px solid #e4e7ed;
        margin: 24px 0;
      }
    }
  }
}

/* 评论区样式 */
.article-comments-section {
  margin-top: 20px;
}

.comment-form-wrapper {
  margin-bottom: 32px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 8px;

  .comment-input {
    margin-bottom: 12px;
  }

  .comment-form-actions {
    display: flex;
    gap: 12px;
  }
}

.comments-list {
  .comment-item {
    display: flex;
    gap: 16px;
    padding: 20px 0;
    border-bottom: 1px solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }
  }

  .comment-avatar {
    flex-shrink: 0;
    width: 50px;
    height: 50px;
    border-radius: 50%;
    background-color: #e6f7ff;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #409EFF;
    font-size: 24px;
  }

  .comment-content {
    flex: 1;
  }

  .comment-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
  }

  .comment-author {
    font-size: 15px;
    font-weight: 600;
    color: #303133;
  }

  .comment-time {
    font-size: 13px;
    color: #909399;
  }

  .comment-text {
    font-size: 14px;
    color: #606266;
    line-height: 1.6;
    margin-bottom: 12px;
  }

  .comment-actions {
    display: flex;
    gap: 12px;

    .delete-btn {
      color: #f56c6c;

      &:hover {
        color: #f78989;
      }
    }
  }

  .reply-list {
    margin-top: 16px;
    padding-left: 20px;
    border-left: 2px solid #f0f0f0;
  }

  .reply-item {
    padding: 12px 0;
    margin-top: 12px;

    &:first-child {
      margin-top: 0;
    }
  }

  .reply-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 6px;
  }

  .reply-author {
    font-size: 14px;
    font-weight: 500;
    color: #303133;
  }

  .reply-time {
    font-size: 12px;
    color: #909399;
  }

  .reply-text {
    font-size: 14px;
    color: #606266;
    line-height: 1.5;

    .reply-to {
      color: #409EFF;
      font-weight: 500;
    }
  }
}
</style>
