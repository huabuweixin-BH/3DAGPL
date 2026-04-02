<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
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
          v-hasPermi="['system:article:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['system:article:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-top"
          size="mini"
          :disabled="multiple"
          @click="handlePublish"
          v-hasPermi="['system:article:edit']"
        >上架</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-bottom"
          size="mini"
          :disabled="multiple"
          @click="handleReject"
          v-hasPermi="['system:article:edit']"
        >下架</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:article:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['system:article:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="articleList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="文章 ID" align="center" prop="id" width="80" />
      <el-table-column label="创建者" align="center" prop="createBy" width="120" />
      <el-table-column label="标题" align="center" prop="title" :show-overflow-tooltip="true" width="150" />
      <el-table-column label="摘要" align="center" prop="summary" :show-overflow-tooltip="true" width="200">
        <template slot-scope="scope">
          <span>{{ scope.row.summary ? (scope.row.summary.length > 50 ? scope.row.summary.substring(0, 50) + '...' : scope.row.summary) : '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="封面图地址" align="center" prop="coverUrl" :show-overflow-tooltip="true" width="200">
        <template slot-scope="scope">
          <span>{{ scope.row.coverUrl ? (scope.row.coverUrl.length > 30 ? scope.row.coverUrl.substring(0, 30) + '...' : scope.row.coverUrl) : '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="富文本 HTML" align="center" prop="contentHtml" :show-overflow-tooltip="true" width="200">
        <template slot-scope="scope">
          <span>{{ scope.row.contentHtml ? (scope.row.contentHtml.length > 50 ? scope.row.contentHtml.substring(0, 50) + '...' : scope.row.contentHtml) : '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="纯文本内容" align="center" prop="contentText" :show-overflow-tooltip="true" width="200">
        <template slot-scope="scope">
          <span>{{ scope.row.contentText ? (scope.row.contentText.length > 50 ? scope.row.contentText.substring(0, 50) + '...' : scope.row.contentText) : '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="审核状态" align="center" prop="status" width="100">
        <template slot-scope="scope">
          <el-tag :type="getStatusTagType(scope.row.status)" size="small">{{ formatStatus(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="是否置顶" align="center" prop="isTop" width="80">
        <template slot-scope="scope">
          <el-tag :type="scope.row.isTop === '1' || scope.row.isTop === true ? 'danger' : 'info'" size="small">{{ scope.row.isTop === '1' || scope.row.isTop === true ? '是' : '否' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="是否公开" align="center" prop="isVisible" width="80">
        <template slot-scope="scope">
          <el-tag :type="scope.row.isVisible === '1' || scope.row.isVisible === true ? 'success' : 'info'" size="small">{{ scope.row.isVisible === '1' || scope.row.isVisible === true ? '是' : '否' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" :show-overflow-tooltip="true" width="150">
        <template slot-scope="scope">
          <span>{{ scope.row.remark ? (scope.row.remark.length > 30 ? scope.row.remark.substring(0, 30) + '...' : scope.row.remark) : '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="文章特征向量" align="center" prop="embedding" :show-overflow-tooltip="true" width="150">
        <template slot-scope="scope">
          <span>{{ formatEmbedding(scope.row.embedding) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150" fixed="right">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['system:article:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:article:remove']"
          >删除</el-button>
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

    <!-- 添加或修改文章对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="800px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入文章标题" />
        </el-form-item>
        <el-form-item label="摘要" prop="summary">
          <el-input v-model="form.summary" type="textarea" placeholder="请输入文章摘要" />
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
              style="height: 320px; overflow-y: auto;"
            />
          </div>
          <div v-else style="height: 320px;">页面加载中...</div>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listArticle, getArticle, delArticle, addArticle, updateArticle, publishArticle, rejectArticle } from "@/api/system/article";
import "@wangeditor/editor/dist/css/style.css";
import { Editor, Toolbar } from '@wangeditor/editor-for-vue';

export default {
  name: "Article",
  components: {
    Editor,
    Toolbar
  },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
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
      // wangEditor Vue2 组件实例
      editor: null,
      // 编辑器内容（HTML）
      editorContent: '<p><br></p>',
      // 编辑器配置
      editorConfig: {
        placeholder: '请输入内容...',
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
        summary: null,
        coverUrl: null,
        contentHtml: null,
        contentText: null,
        status: null,
        isTop: null,
        isVisible: null,
        embedding: null
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
          { required: true, message: "富文本HTML不能为空", trigger: "blur" }
        ],
      }
    };
  },
  created() {
    this.getList();
  },
  beforeDestroy() {
    // nothing special for wrapper component
  },
  watch: {
    open(val) {
      if (val) {
        this.$nextTick(() => {
          // Dialog 渲染延迟环境下，延迟一点再赋值防止编辑器未完全激活
          setTimeout(() => {
            this.editorContent = this.form.contentHtml || '<p><br></p>';
          }, 50);
        });
      }
    }
  },
  methods: {
    handleEditorCreated(editorInstance) {
      this.editor = Object.seal(editorInstance);
    },

    handleEditorDestroyed() {
      this.editor = null;
    },
    /** 查询文章列表 */
    getList() {
      this.loading = true;
      listArticle(this.queryParams).then(response => {
        this.articleList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        id: null,
        title: null,
        summary: null,
        coverUrl: null,
        contentHtml: null,
        contentText: null,
        status: null,
        isTop: null,
        isVisible: null,
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null,
        remark: null,
        embedding: null
      };
      this.editorContent = '';
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加文章";
      this.editorContent = '';
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getArticle(id).then(response => {
        this.form = response.data;
        this.editorContent = response.data.contentHtml || '';
        this.open = true;
        this.title = "修改文章";
      });
    },
    /** 提交按钮 */
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

          // 构建干净的提交数据对象，只包含有值的字段
          const submitData = {};
          
          // ID 字段（如果有）
          if (this.form.id) {
            submitData.id = Number(this.form.id);
          }
          
          // 必填字段
          submitData.title = String(this.form.title || '').trim();
          submitData.summary = String(this.form.summary || '').trim();
          submitData.contentHtml = String(contentHtml);
          submitData.contentText = String(contentText);
          
          // 可选字段 - 只有有值时才添加
          if (this.form.coverUrl) {
            submitData.coverUrl = String(this.form.coverUrl).trim();
          }
          if (this.form.status !== null && this.form.status !== undefined && this.form.status !== '') {
            submitData.status = String(this.form.status);
          }
          // 布尔值字段：正确处理字符串 "true"/"false" 和布尔值
          if (this.form.isTop !== null && this.form.isTop !== undefined && this.form.isTop !== '') {
            submitData.isTop = this.form.isTop === true || this.form.isTop === 'true' ? '1' : '0';
          }
          if (this.form.isVisible !== null && this.form.isVisible !== undefined && this.form.isVisible !== '') {
            submitData.isVisible = this.form.isVisible === true || this.form.isVisible === 'true' ? '1' : '0';
          }
          if (this.form.remark) {
            submitData.remark = String(this.form.remark).trim();
          }
          // embedding 字段：如果是对象则序列化，否则直接使用
          if (this.form.embedding !== null && this.form.embedding !== undefined && this.form.embedding !== '') {
            submitData.embedding = typeof this.form.embedding === 'object' 
              ? JSON.stringify(this.form.embedding) 
              : String(this.form.embedding);
          }
          
          // 调试日志：打印要提交的数据对象
          console.log('准备提交的数据对象:', submitData);
          console.log('提交数据的键:', Object.keys(submitData));
          console.log('contentHtml 长度:', submitData.contentHtml.length);
          console.log('contentText 长度:', submitData.contentText.length);

          // 尝试序列化数据，捕获可能的错误
          let serializedData;
          try {
            serializedData = JSON.stringify(submitData);
            console.log('✅ 前端序列化成功');
            console.log('📝 序列化后的数据:', serializedData);
            console.log('📊 数据大小:', serializedData.length, '字节');

            // 尝试反序列化验证数据完整性
            const testData = JSON.parse(serializedData);
            console.log('✅ 前端反序列化验证通过');
            console.log('🔍 验证后的数据键:', Object.keys(testData));
            console.log('🔍 验证后的 contentHtml:', testData.contentHtml);
            console.log('🔍 验证后的 contentText:', testData.contentText);
          } catch (e) {
            console.error('❌ JSON 序列化失败:', e);
            console.error('问题数据:', submitData);
            this.$modal.msgError('数据格式化失败，请检查输入内容');
            return;
          }

          if (this.form.id != null) {
            // 禁用防重复提交检查
            updateArticle(submitData, {
              headers: {
                'repeatSubmit': false
              }
            }).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            }).catch(error => {
              console.error('更新失败:', error);
              this.$modal.msgError('保存失败：' + (error.message || '未知错误'));
            });
          } else {
            // 禁用防重复提交检查
            addArticle(submitData, {
              headers: {
                'repeatSubmit': false
              }
            }).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            }).catch(error => {
              console.error('新增失败:', error);
              this.$modal.msgError('保存失败：' + (error.message || '未知错误'));
            });
          }
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$modal.confirm('是否确认删除文章编号为"' + ids + '"的数据项？').then(function() {
        return delArticle(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 上架按钮操作 */
    handlePublish() {
      const ids = this.ids;
      this.$modal.confirm('是否确认上架选中的 ' + ids.length + ' 篇文章？').then(() => {
        return publishArticle(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("上架成功");
      }).catch(() => {});
    },
    /** 下架按钮操作 */
    handleReject() {
      const ids = this.ids;
      this.$modal.confirm('是否确认下架选中的 ' + ids.length + ' 篇文章？').then(() => {
        return rejectArticle(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("下架成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('system/article/export', {
        ...this.queryParams
      }, `article_${new Date().getTime()}.xlsx`)
    },
    /** 格式化状态显示 */
    formatStatus(status) {
      if (status === null || status === undefined || status === '') {
        return '未知';
      }
      const statusMap = {
        '0': '待审核',
        '1': '已通过',
        '2': '已拒绝',
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
    },
    /** 格式化特征向量显示 */
    formatEmbedding(embedding) {
      if (embedding === null || embedding === undefined || embedding === '') {
        return '-';
      }
      try {
        // 如果是字符串，尝试解析为数组
        const arr = typeof embedding === 'string' ? JSON.parse(embedding) : embedding;
        if (Array.isArray(arr)) {
          if (arr.length === 0) {
            return '[]';
          } else if (arr.length <= 5) {
            // 短数组直接显示
            return '[' + arr.map(item => typeof item === 'number' ? item.toFixed(2) : item).join(', ') + ']';
          } else {
            // 长数组只显示前几个元素和维度信息
            const preview = arr.slice(0, 3).map(item => typeof item === 'number' ? item.toFixed(2) : item).join(', ');
            return `[${preview}, ... ${arr.length}维]`;
          }
        }
        // 非数组类型，转换为字符串并截断
        const str = String(embedding);
        return str.length > 30 ? str.substring(0, 30) + '...' : str;
      } catch (e) {
        // 解析失败时直接显示字符串
        const str = String(embedding);
        return str.length > 30 ? str.substring(0, 30) + '...' : str;
      }
    }
  }
};
</script>
