<template>
  <div style="padding:24px">

    <!-- 标题区域 -->
    <el-card shadow="hover" style="margin-bottom:5px">
      <h2 style="text-align: center; margin: 0;">三维模型轻量化处理与图形学算法学习平台</h2>
    </el-card>

    <!-- 控制面板 -->
    <el-card shadow="hover" style="margin-bottom:20px">

      <el-row :gutter="20" align="middle" style="margin-left: 60px;">

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
        <el-col :span="3" v-if="vertexCount" style="display: flex; align-items: center; height: 40px;">
          <el-tag type="success">
            原始顶点: {{ originalCount }}
          </el-tag>
        </el-col>

        <!-- 目标顶点 -->
        <el-col :span="5" style="display: flex; align-items: center; height: 40px;">
          <span style="margin-right: 8px; font-size: 14px; color: #606266;">目标顶点:</span>
          <el-input-number
            v-model="targetCount"
            :min="0"
            placeholder="目标顶点"
            style="flex: 1;"
          />
        </el-col>

        <!-- 简化率 -->
        <el-col :span="5" style="display: flex; align-items: center; height: 40px;">
          <span style="margin-right: 8px; font-size: 14px; color: #606266;">简化率:</span>
          <el-input-number
            v-model="simplifyRate"
            :min="0"
            :max="1"
            :step="0.01"
            style="flex: 1;"
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
      <div class="three-scenes">
        <div class="scene-container">
          <h3>原始模型</h3>
          <div ref="leftContainer" class="canvas-container"></div>
        </div>
        <div class="scene-container">
          <h3>处理后模型</h3>
          <div ref="rightContainer" class="canvas-container"></div>
        </div>
      </div>
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
      // 左边场景（原始模型）
      sceneLeft: null,
      cameraLeft: null,
      rendererLeft: null,
      objMeshLeft: null,

      // 右边场景（处理后模型）
      sceneRight: null,
      cameraRight: null,
      rendererRight: null,
      objMeshRight: null,

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
        input: this.filePath,
        originalVertices: this.originalCount
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
              // 创建双面材质：实体 + 网格线框
              const solidMaterial = new THREE.MeshStandardMaterial({
                color: 0x44aaee,
                roughness: 0.5,
                metalness: 0.3,
                side: THREE.DoubleSide,
                flatShading: false
              })
              
              // 创建线框网格模型
              const wireframe = new THREE.LineSegments(
                new THREE.EdgesGeometry(c.geometry),
                new THREE.LineBasicMaterial({ 
                  color: 0x00ffff,
                  transparent: true,
                  opacity: 0.4
                })
              )

              // 应用实体材质
              c.material = solidMaterial
              
              // 添加线框作为子对象
              c.add(wireframe)
            }
          })

          if (this.objMeshRight) {
            this.sceneRight.remove(this.objMeshRight)
          }

          this.objMeshRight = obj
          this.sceneRight.add(obj)

          this.$message.success(
            `加载完成：${taskData.originalVertices || this.originalCount} → ${taskData.processedVertexCount}`
          )

        },

        undefined,

        () => {
          this.$message.error('模型加载失败')
        }
      )
    },

    // ================= 初始化 THREE 场景 =================
    initScenes() {
      this.initScene('left')
      this.initScene('right')
    },

    initScene(side) {
      const container = this.$refs[side + 'Container']

      const scene = new THREE.Scene()
      // 使用渐变感更好的深色背景
      scene.background = new THREE.Color(0x0a0a1a)

      const camera = new THREE.PerspectiveCamera(
        45,
        container.clientWidth / container.clientHeight,
        0.1,
        1000
      )
      camera.position.set(3, 3, 6)

      const renderer = new THREE.WebGLRenderer({ 
        antialias: true,
        alpha: false
      })
      renderer.setSize(container.clientWidth, container.clientHeight)
      renderer.setPixelRatio(window.devicePixelRatio)
      renderer.shadowMap.enabled = true
      renderer.shadowMap.type = THREE.PCFSoftShadowMap
      renderer.toneMapping = THREE.ACESFilmicToneMapping
      renderer.toneMappingExposure = 1.2

      container.appendChild(renderer.domElement)

      // ================= 增强光照系统 =================
      
      // 1. 主方向光（模拟太阳光）
      const mainLight = new THREE.DirectionalLight(0xffffff, 1.5)
      mainLight.position.set(5, 8, 5)
      mainLight.castShadow = true
      mainLight.shadow.mapSize.width = 2048
      mainLight.shadow.mapSize.height = 2048
      scene.add(mainLight)

      // 2. 补光（柔和的蓝色调）
      const fillLight = new THREE.DirectionalLight(0x88aaff, 0.6)
      fillLight.position.set(-5, 3, -3)
      scene.add(fillLight)

      // 3. 背光（边缘光效果）
      const backLight = new THREE.DirectionalLight(0xffa500, 0.4)
      backLight.position.set(0, 2, -8)
      scene.add(backLight)

      // 4. 半球光（天空和地面的自然过渡）
      const hemiLight = new THREE.HemisphereLight(0x87ceeb, 0x362d59, 0.5)
      scene.add(hemiLight)

      // 5. 环境光（基础照明）
      const ambientLight = new THREE.AmbientLight(0xffffff, 0.4)
      scene.add(ambientLight)

      // 6. 点光源（增加局部高光）
      const pointLight = new THREE.PointLight(0x00ffff, 0.5, 20)
      pointLight.position.set(0, 5, 0)
      scene.add(pointLight)

      // ================= 添加网格辅助地面 =================
      const gridHelper = new THREE.GridHelper(20, 20, 0x444444, 0x222222)
      scene.add(gridHelper)

      // 轨道控制器
      const controls = new OrbitControls(camera, renderer.domElement)
      controls.enableDamping = true
      controls.dampingFactor = 0.05
      controls.minDistance = 2
      controls.maxDistance = 50

      // 渲染循环
      const loop = () => {
        requestAnimationFrame(loop)
        controls.update()
        renderer.render(scene, camera)
      }

      loop()

      // 动态设置属性
      this['scene' + side.charAt(0).toUpperCase() + side.slice(1)] = scene
      this['camera' + side.charAt(0).toUpperCase() + side.slice(1)] = camera
      this['renderer' + side.charAt(0).toUpperCase() + side.slice(1)] = renderer
    },

    // ================= 上传并加载本地OBJ =================
    onFileChange(file) {

      const rawFile = file.raw
      if (!rawFile) return

      this.filePath = rawFile.name

      const reader = new FileReader()

      reader.onload = (ev) => {

        const obj = new OBJLoader().parse(ev.target.result)

        if (this.objMeshLeft) {
          this.sceneLeft.remove(this.objMeshLeft)
        }

        // 统计唯一顶点数（从OBJ文本直接解析，与MeshLab一致）
        // 使用正则表达式匹配所有 'v ' 开头的行，性能优于split
        const text = ev.target.result
        const vertexMatches = text.match(/^v\s+/gm)
        this.originalCount = vertexMatches ? vertexMatches.length : 0

        obj.traverse(c => {
          if (c.isMesh) {
            // 创建双面材质：实体 + 网格线框
            const solidMaterial = new THREE.MeshStandardMaterial({
              color: 0x44aaee,
              roughness: 0.5,
              metalness: 0.3,
              side: THREE.DoubleSide,
              flatShading: false
            })
            
            // 创建线框材质
            const wireframeMaterial = new THREE.MeshBasicMaterial({
              color: 0x00ffff,
              wireframe: false
            })

            // 创建线框网格模型
            const wireframe = new THREE.LineSegments(
              new THREE.EdgesGeometry(c.geometry),
              new THREE.LineBasicMaterial({ 
                color: 0x00ffff,
                transparent: true,
                opacity: 0.4
              })
            )

            // 应用实体材质
            c.material = solidMaterial
            
            // 添加线框作为子对象
            c.add(wireframe)
          }
        })

        this.objMeshLeft = obj
        this.sceneLeft.add(obj)

        this.$message.success(`已加载 ${this.originalCount} 个顶点`)
      }

      reader.readAsText(rawFile)
    }

  },

  mounted() {
    this.initScenes()
  }

}
</script>

<style scoped>
.three-scenes {
  display: flex;
  gap: 20px;
  min-height: 600px;
}

.scene-container {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.scene-container h3 {
  text-align: center;
  margin-bottom: 10px;
  color: #409EFF;
  font-weight: 600;
  text-shadow: 0 0 10px rgba(64, 158, 255, 0.3);
}

.canvas-container {
  width: 100%;
  flex: 1;
  min-height: 550px;
  border-radius: 8px;
  background: #000;
  box-shadow: inset 0 0 20px rgba(0, 0, 0, 0.5);
}
</style>
