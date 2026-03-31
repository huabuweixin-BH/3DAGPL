<template>
  <div style="padding:24px">

    <!-- 标题区域 -->
    <el-card shadow="hover" style="margin-bottom:5px">
      <h2 style="text-align: center; margin: 0;">三维模型轻量化处理与图形学算法学习平台</h2>
    </el-card>

    <!-- 控制面板 -->
    <el-card shadow="hover" style="margin-bottom:20px">

      <el-row :gutter="20" align="middle" style="margin-left: 120px;">

        <!-- 上传 -->
        <el-col :span="3">
          <el-upload
            :auto-upload="false"
            :show-file-list="false"
            accept=".obj"
            :on-change="onFileChange"
          >
            <el-button type="primary">上传OBJ模型</el-button>
          </el-upload>
        </el-col>

        <!-- 原始顶点 -->
        <el-col :span="4" v-if="vertexCount" style="display: flex; align-items: center; height: 40px;">
          <el-tag type="success">
            原始顶点: {{ originalCount }}
          </el-tag>
        </el-col>

        <!-- 目标顶点 -->
        <el-col :span="4">
          <el-input-number
            v-model="targetCount"
            :min="0"
            placeholder="目标顶点"
            style="width:100%"
          />
        </el-col>

        <!-- 简化率 -->
        <el-col :span="4">
          <el-input-number
            v-model="simplifyRate"
            :min="0"
            :max="1"
            :step="0.01"
            style="width:100%"
          />
        </el-col>

        <!-- 选项 -->
        <el-col :span="4" style="display: flex; align-items: center; height: 40px;">
          <el-checkbox v-model="useOptim">价感知</el-checkbox>
          <el-checkbox v-model="useIsotropic">各向同性</el-checkbox>
        </el-col>

        <!-- 提交按钮 -->
        <el-col :span="4">
          <el-button
            type="success"
            :loading="simplifying"
            @click="submitSimplify"
            style="width:80%"
          >
            {{ simplifying ? '提交中...' : '提交简化任务' }}
          </el-button>
        </el-col>

      </el-row>

    </el-card>

    <!-- Three.js 渲染区域 -->
    <el-card shadow="never">
      <div ref="threeContainer" class="canvas-container"></div>
    </el-card>

  </div>
</template>

<script>
import * as THREE from "three"
import { OBJLoader } from "three/examples/jsm/loaders/OBJLoader.js"
import { OrbitControls } from "three/examples/jsm/controls/OrbitControls.js"
import request from "@/utils/request"

export default {

  data() {
    return {
      scene: null,
      camera: null,
      renderer: null,

      objMesh: null,
      filePath: null,

      originalCount: 0,
      vertexCount: null,

      targetCount: null,
      simplifyRate: 0.5,
      useOptim: false,
      useIsotropic: false,

      simplifying: false
    }
  },

  methods: {

    // ================= 提交任务 =================
    submitSimplify() {

      if (!this.filePath) {
        this.$message.warning('请先上传模型文件')
        return
      }

      if (!this.targetCount && this.simplifyRate === 0.5) {
        this.$message.warning('请输入目标顶点数或调整简化率')
        return
      }

      this.simplifying = true

      const payload = {
        input: this.filePath
      }

      if (this.targetCount) payload.v = this.targetCount
      if (this.simplifyRate !== 0.5) payload.p = this.simplifyRate
      if (this.useOptim) payload.optim = true
      if (this.useIsotropic) payload.isotropic = true

      request.post('/system/tasks/model', payload).then(resp => {

        this.simplifying = false

        if (resp.code === 200) {

          this.$message.success('任务提交成功')

          if (resp.data?.outputModelPath) {
            this.loadOutputModel(resp.data.outputModelPath, resp.data)
          }

        } else {
          this.$message.error(resp.msg || '提交失败')
        }

      }).catch(() => {
        this.simplifying = false
        this.$message.error('请求失败')
      })
    },

    // ================= 加载简化模型 =================
    loadOutputModel(modelPath, taskData) {

      // 提取文件名
      const fileName = modelPath.split('/').pop()

      // 拼接 http-server 地址
      const fullPath = `http://localhost:82/${fileName}`

      console.log('加载模型URL：', fullPath)

      const loader = new OBJLoader()

      loader.load(
        fullPath,

        (obj) => {

          if (this.objMesh) {
            this.scene.remove(this.objMesh)
          }

          obj.traverse(c => {
            if (c.isMesh) {
              c.material = new THREE.MeshStandardMaterial({
                color: 0x44aaee,
                roughness: 0.7,
                metalness: 0.2
              })
            }
          })

          this.objMesh = obj
          this.scene.add(obj)

          this.$message.success(
            `加载完成：${this.originalCount} → ${taskData.processedVertexCount}`
          )

        },

        undefined,

        () => {
          this.$message.error('模型加载失败')
        }
      )
    },

    // ================= 初始化 THREE =================
    initThree() {

      const container = this.$refs.threeContainer

      this.scene = new THREE.Scene()
      this.scene.background = new THREE.Color(0x1a1a1a)

      this.camera = new THREE.PerspectiveCamera(
        45,
        container.clientWidth / container.clientHeight,
        0.1,
        1000
      )

      this.camera.position.set(2, 2, 5)

      this.renderer = new THREE.WebGLRenderer({ antialias: true })
      this.renderer.setSize(container.clientWidth, container.clientHeight)

      container.appendChild(this.renderer.domElement)

      const light = new THREE.DirectionalLight(0xffffff, 1)
      light.position.set(5, 5, 5)

      this.scene.add(light)
      this.scene.add(new THREE.AmbientLight(0xffffff, 0.6))

      new OrbitControls(this.camera, this.renderer.domElement)

      const loop = () => {
        requestAnimationFrame(loop)
        this.renderer.render(this.scene, this.camera)
      }

      loop()
    },

    // ================= 上传并加载本地OBJ =================
    onFileChange(file) {

      const rawFile = file.raw
      if (!rawFile) return

      this.filePath = rawFile.name

      const reader = new FileReader()

      reader.onload = (ev) => {

        const obj = new OBJLoader().parse(ev.target.result)

        if (this.objMesh) {
          this.scene.remove(this.objMesh)
        }

        obj.traverse(c => {
          if (c.isMesh) {
            c.material = new THREE.MeshStandardMaterial({
              color: 0x44aaee
            })

            this.originalCount = c.geometry.attributes.position.count
            this.vertexCount = this.originalCount
          }
        })

        this.objMesh = obj
        this.scene.add(obj)

        this.$message.success(`已加载 ${this.originalCount} 个顶点`)
      }

      reader.readAsText(rawFile)
    }

  },

  mounted() {
    this.initThree()
  }

}
</script>

<style scoped>
.canvas-container {
  width: 100%;
  height: calc(100vh - 220px);
  border-radius: 6px;
  background: #000;
}
</style>
