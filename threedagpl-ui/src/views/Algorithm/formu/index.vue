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

    <!-- 文章详情对话框 -->
    <el-dialog
      title="文章详情"
      :visible.sync="dialogVisible"
      width="800px"
      append-to-body
      class="article-detail-dialog"
    >
      <div class="article-detail" v-if="currentArticle">
        <div class="article-detail-header">
          <h2 class="article-detail-title">{{ currentArticle.title }}</h2>
          <div class="article-detail-meta">
            <el-tag :type="getStatusTagType(currentArticle.status)" size="mini">
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
          </div>
        </div>
        <el-divider></el-divider>
        <div class="article-detail-summary" v-if="currentArticle.summary">
          <h4>摘要</h4>
          <p>{{ currentArticle.summary }}</p>
        </div>
        <el-divider></el-divider>
        <div class="article-detail-content">
          <h4>正文</h4>
          <div class="article-content-html" v-html="currentArticle.contentHtml"></div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listArticle } from "@/api/system/article";

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
        status: '1' // 默认只显示已发布的文章
      },
      // 文章详情对话框
      dialogVisible: false,
      // 当前查看的文章
      currentArticle: null
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
      this.dialogVisible = true;
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

/* 文章详情对话框样式 */
.article-detail-dialog {
  ::v-deep .el-dialog__body {
    padding: 0;
  }
}

.article-detail {
  padding: 24px;
  max-height: 70vh;
  overflow-y: auto;
}

.article-detail-header {
  margin-bottom: 20px;
}

.article-detail-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
  line-height: 1.4;
}

.article-detail-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 14px;
  color: #909399;
}

.article-detail-summary {
  h4 {
    font-size: 16px;
    color: #303133;
    margin: 0 0 12px 0;
  }

  p {
    font-size: 14px;
    color: #606266;
    line-height: 1.8;
    margin: 0;
    padding: 12px 16px;
    background-color: #f5f7fa;
    border-radius: 4px;
  }
}

.article-detail-content {
  h4 {
    font-size: 16px;
    color: #303133;
    margin: 0 0 12px 0;
  }
}

.article-content-html {
  font-size: 15px;
  line-height: 1.8;
  color: #303133;

  ::v-deep {
    p {
      margin: 12px 0;
    }

    img {
      max-width: 100%;
      height: auto;
      border-radius: 4px;
    }

    h1, h2, h3, h4, h5, h6 {
      margin: 20px 0 12px;
      color: #303133;
    }

    ul, ol {
      padding-left: 20px;
    }

    blockquote {
      margin: 16px 0;
      padding: 12px 16px;
      background-color: #f5f7fa;
      border-left: 4px solid #409EFF;
      border-radius: 4px;
    }

    code {
      padding: 2px 6px;
      background-color: #f5f7fa;
      border-radius: 3px;
      font-family: Consolas, Monaco, 'Andale Mono', monospace;
    }

    pre {
      margin: 16px 0;
      padding: 16px;
      background-color: #282c34;
      border-radius: 6px;
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
      margin: 16px 0;

      th, td {
        border: 1px solid #dcdfe6;
        padding: 10px 14px;
      }

      th {
        background-color: #f5f7fa;
        font-weight: 600;
      }
    }
  }
}
</style>
