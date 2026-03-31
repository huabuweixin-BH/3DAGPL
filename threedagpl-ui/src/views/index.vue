<template>
  <div style="padding:24px">

    <div class="controls">
      <input type="file" @change="onFileChange" accept=".obj" />

      <span v-if="vertexCount">
        原始顶点: {{ originalCount }}
      </span>

      <input v-model.number="targetCount" type="number" placeholder="目标顶点数" style="width:120px" />

      <input v-model.number="simplifyRate" type="number"
        placeholder="简化率 (0-1)"
        style="width:120px; margin-left: 10px;"
        min="0" max="1" step="0.01" />

      <label style="margin-left: 20px; margin-right: 10px;">
        <input v-model="useOptim" type="checkbox" />
        启用价感知
      </label>

      <label style="margin-right: 10px;">
        <input v-model="useIsotropic" type="checkbox" />
        启用各向同性
      </label>

      <button @click="submitSimplify" :disabled="simplifying || !filePath">
        {{ simplifying ? '提交中...' : '提交简化' }}
      </button>

      <span v-if="submitMessage" style="margin-left: 20px; color: #44aaee;">
        {{ submitMessage }}
      </span>
    </div>

    <div ref="threeContainer" class="canvas-container"></div>

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

      simplifying: false,
      submitMessage: ''
    }
  },

  methods: {

    // ================= 提交任务 =================
    submitSimplify() {

      if (!this.filePath) {
        this.submitMessage = '请先上传模型文件'
        return
      }

      if (!this.targetCount && this.simplifyRate === 0.5) {
        this.submitMessage = '请输入目标顶点数或调整简化率'
        return
      }

      this.simplifying = true
      this.submitMessage = ''

      const payload = {
        input: this.filePath,
      }

      if (this.targetCount) payload.v = this.targetCount
      if (this.simplifyRate !== 0.5) payload.p = this.simplifyRate
      if (this.useOptim) payload.optim = true
      if (this.useIsotropic) payload.isotropic = true

      this.submitRequest(payload)
    },

    // ================= 请求后端 =================
    submitRequest(data) {

      request.post('/system/tasks/model', data).then(resp => {

        this.simplifying = false

        if (resp.code === 200) {

          this.submitMessage = '✓ 提交成功，任务ID: ' + resp.data?.taskNo

          // 🔥 关键：加载返回模型
          if (resp.data?.outputModelPath) {
            this.loadOutputModel(resp.data.outputModelPath, resp.data)
          }

        } else {
          this.submitMessage = '✗ 提交失败：' + (resp.msg || '未知错误')
        }

      }).catch(err => {
        this.simplifying = false
        this.submitMessage = '✗ 请求出错：' + (err.response?.data?.msg || err.message)
      })
    },

    // ================= 加载简化模型（核心修改） =================
    loadOutputModel(modelPath, taskData) {

      // ✅ 1. 提取文件名
      const fileName = modelPath.split('/').pop()

      // ✅ 2. 拼接 http-server 地址
      const fullPath = `http://localhost:82/${fileName}`

      console.log('加载模型URL：', fullPath)

      // ✅ 3. 直接用 THREE 加载（更推荐）
      const loader = new OBJLoader()

      loader.load(
        fullPath,

        (obj) => {

          // 清除旧模型
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

          const processedCount = taskData.processedVertexCount

          this.submitMessage =
            `✓ 已加载简化模型 | 原顶点: ${this.originalCount} → 简化后: ${processedCount}`

        },

        undefined,

        (err) => {
          this.submitMessage = '✗ 加载模型失败'
          console.error(err)
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

    // ================= 加载本地 OBJ =================
    onFileChange(e) {

      const file = e.target.files[0]
      if (!file) return

      this.filePath = file.name

      const reader = new FileReader()

      reader.onload = (ev) => {

        const obj = new OBJLoader().parse(ev.target.result)

        if (this.objMesh) {
          this.scene.remove(this.objMesh)
        }

        obj.traverse(c => {
          if (c.isMesh) {
            c.material = new THREE.MeshStandardMaterial({ color: 0x44aaee })

            this.originalCount = c.geometry.attributes.position.count
            this.vertexCount = this.originalCount
          }
        })

        this.objMesh = obj
        this.scene.add(obj)

        this.submitMessage = `已加载 ${this.originalCount} 个顶点`
      }

      reader.readAsText(file)
    }

  },

  mounted() {
    this.initThree()
  }

}
</script>

<style scoped>
.controls {
  margin-bottom: 20px;
  display: flex;
  gap: 15px;
  align-items: center;
  flex-wrap: wrap;
  padding: 15px;
  background: #f5f5f5;
  border-radius: 4px;
}

.controls input[type="file"] {
  padding: 6px;
  border: 1px solid #ccc;
  border-radius: 3px;
}

.controls input[type="number"] {
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 3px;
}

.controls input[type="checkbox"] {
  margin-right: 5px;
}

.controls label {
  display: flex;
  align-items: center;
  cursor: pointer;
  font-size: 14px;
}

.canvas-container {
  width: 100%;
  height: calc(100vh - 160px);
  border: 1px solid #ddd;
  border-radius: 4px;
  background: #000;
}

button {
  padding: 8px 16px;
  background: #44aaee;
  color: white;
  border: none;
  cursor: pointer;
  border-radius: 3px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

button:hover:not(:disabled) {
  background: #2898dd;
  box-shadow: 0 2px 8px rgba(68, 170, 238, 0.3);
}

button:disabled {
  background: #ccc;
  cursor: not-allowed;
}

span[style*="color"] {
  font-size: 14px;
  font-weight: 500;
}
</style>
