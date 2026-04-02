<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
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
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
        >写文章</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="articleList" style="width: 100%">
      <el-table-column label="文章 ID" align="center" prop="id" width="80" />
      <el-table-column label="标题" align="center" prop="title" :show-overflow-tooltip="true" width="200" />
      <el-table-column label="摘要" align="center" prop="summary" :show-overflow-tooltip="true" width="300">
        <template slot-scope="scope">
          <span>{{ scope.row.summary ? (scope.row.summary.length > 50 ? scope.row.summary.substring(0, 50) + '...' : scope.row.summary) : '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="审核状态" align="center" prop="status" width="100">
        <template slot-scope="scope">
          <el-tag :type="getStatusTagType(scope.row.status)" size="small">{{ formatStatus(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="120" fixed="right">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleView(scope.row)"
          >查看</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 新增文章对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="800px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入文章标题" />
        </el-form-item>
        <el-form-item label="摘要" prop="summary">
          <el-input v-model="form.summary" type="textarea" placeholder="请输入文章摘要" :rows="3" />
        </el-form-item>
        <el-form-item label="文章内容" prop="contentHtml">
          <div id="article-editor-wrapper" style="border: 1px solid #ccc;" v-if="open">
            <Toolbar
              :editor="editor"
              :defaultConfig="toolbarConfig"
              :mode="mode"
              style="border-bottom: 1px solid #ccc; white-space: nowrap; overflow-x: auto;"
            />
            <Editor
              v-model="form.contentHtml"
              :defaultConfig="editorConfig"
              :mode="mode"
              @onCreated="handleEditorCreated"
              @onDestroyed="handleEditorDestroyed"
              style="height: 400px; overflow-y: auto;"
            />
          </div>
          <div v-else style="height: 400px;">页面加载中...</div>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">发 布</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 查看文章对话框 -->
    <el-dialog title="文章详情" :visible.sync="viewOpen" width="800px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="文章 ID">{{ viewData.id }}</el-descriptions-item>
        <el-descriptions-item label="标题">{{ viewData.title }}</el-descriptions-item>
        <el-descriptions-item label="摘要" :span="2">{{ viewData.summary || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审核状态">
          <el-tag :type="getStatusTagType(viewData.status)" size="small">{{ formatStatus(viewData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ parseTime(viewData.createTime, '{y}-{m}-{d} {h}:{i}:{s}') }}
        </el-descriptions-item>
      </el-descriptions>
      <el-divider>文章内容</el-divider>
      <div v-html="viewData.contentHtml" style="line-height: 1.8; padding: 20px 0;"></div>
    </el-dialog>
  </div>
</template>

<script>
import { listArticle, addArticle } from "@/api/system/article";
import "@wangeditor/editor/dist/css/style.css";
import { Editor, Toolbar } from '@wangeditor/editor-for-vue';
import { getToken } from '@/utils/auth';

export default {
  name: "AlgorithmWrite",
  components: {
    Editor,
    Toolbar
  },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 文章表格数据
      articleList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查看文章弹出层
      viewOpen: false,
      // 查看文章数据
      viewData: {},
      // wangEditor Vue2 组件实例
      editor: null,
      // 编辑器配置
      editorConfig: {
        placeholder: '请输入文章内容...',
      },
      // 工具栏配置
      toolbarConfig: {
        toolbarKeys: [
          'headerSelect',
          'bold',
          'italic',
          'underline',
          'color',
          'bgColor',
          'fontSize',
          'fontFamily',
          'lineHeight',
          '|',
          'justifyLeft',
          'justifyCenter',
          'justifyRight',
          'justifyJustify',
          '|',
          'bulletedList',
          'numberedList',
          'todo',
          '|',
          'insertLink',
          'insertImage',
          'insertTable',
          'codeBlock',
          '|',
          'undo',
          'redo'
        ]
      },
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: null,
        createBy: null  // 当前登录用户
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        title: [
          { required: true, message: "标题不能为空", trigger: "blur" }
        ],
        summary: [
          { required: true, message: "摘要不能为空", trigger: "blur" }
        ],
        contentHtml: [
          { required: true, message: "文章内容不能为空", trigger: "blur" }
        ],
      }
    };
  },
  created() {
    // 获取当前登录用户
    const userInfo = JSON.parse(getToken() ? atob(getToken().split('.')[1] || '{}') : '{}');
    this.queryParams.createBy = userInfo.username || this.$store.getters.name;
    this.getList();
  },
  beforeDestroy() {
    this.editor = null;
  },
  watch: {
    open(val) {
      if (val) {
        this.$nextTick(() => {
          setTimeout(() => {
            if (this.editor && this.form.contentHtml) {
              this.editor.setHtml(this.form.contentHtml);
            }
          }, 50);
        });
      }
    }
  },
  methods: {
    handleEditorCreated(editorInstance) {
      this.editor = editorInstance;
    },

    handleEditorDestroyed() {
      this.editor = null;
    },

    /** 查询文章列表（仅显示当前用户的文章） */
    getList() {
      this.loading = true;
      listArticle(this.queryParams).then(response => {
        this.articleList = response.rows;
        this.total = response.total;
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
      // 重置后保留 createBy 过滤
      const createBy = this.queryParams.createBy;
      this.queryParams = {
        pageNum: 1,
        pageSize: 10,
        title: null,
        createBy: createBy
      };
      this.getList();
    },

    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },

    // 表单重置
    reset() {
      this.form = {
        title: null,
        summary: null,
        contentHtml: null,
        contentText: null
      };
      if (this.editor) {
        this.editor.setHtml('<p><br></p>');
      }
      this.resetForm("form");
    },

    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "写文章";
    },

    /** 查看文章 */
    handleView(row) {
      this.viewData = row;
      this.viewOpen = true;
    },

    /** 提交按钮（仅新增） */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          // 从编辑器获取最新内容
          let contentHtml = '';
          let contentText = '';

          if (this.editor) {
            contentHtml = this.editor.getHtml();
            contentText = this.editor.getText();
          } else {
            contentHtml = this.form.contentHtml || '';
            contentText = '';
          }

          // 构建提交数据
          const submitData = {
            title: String(this.form.title || '').trim(),
            summary: String(this.form.summary || '').trim(),
            contentHtml: String(contentHtml),
            contentText: String(contentText),
            status: '0' // 默认待审核
          };

          // 序列化验证
          try {
            JSON.stringify(submitData);
          } catch (e) {
            console.error('JSON 序列化失败:', e);
            this.$modal.msgError('数据格式化失败，请检查输入内容');
            return;
          }

          addArticle(submitData, {
            headers: {
              'repeatSubmit': false
            }
          }).then(response => {
            this.$modal.msgSuccess("发布成功，等待审核");
            this.open = false;
            this.getList();
          }).catch(error => {
            console.error('发布失败:', error);
            this.$modal.msgError('发布失败：' + (error.message || '未知错误'));
          });
        }
      });
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

<style scoped>
/* 文章详情样式 */
::v-deep .el-descriptions__label {
  width: 100px;
}
</style>
