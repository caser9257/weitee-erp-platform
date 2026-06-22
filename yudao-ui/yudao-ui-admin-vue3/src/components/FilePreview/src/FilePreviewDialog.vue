<template>
  <el-dialog
    v-model="visible"
    :title="file?.name || '文件预览'"
    width="85%"
    top="5vh"
    destroy-on-close
    @closed="handleClosed"
  >
    <div class="file-preview">
      <!-- 图片预览 -->
      <div v-if="isImage" class="file-preview__image">
        <el-image
          :src="file?.url"
          fit="contain"
          :preview-src-list="[file?.url]"
          :initial-index="0"
          class="file-preview__img"
        />
      </div>

      <!-- PDF 预览 -->
      <div v-else-if="isPdf" class="file-preview__pdf">
        <iframe
          :src="file?.url"
          class="file-preview__iframe"
          frameborder="0"
        ></iframe>
      </div>

      <!-- Office 预览 -->
      <div v-else-if="isOffice" class="file-preview__office">
        <iframe
          :src="officePreviewUrl"
          class="file-preview__iframe"
          frameborder="0"
        ></iframe>
      </div>

      <!-- 视频预览 -->
      <div v-else-if="isVideo" class="file-preview__video">
        <video
          :src="file?.url"
          controls
          class="file-preview__video-player"
        >
          您的浏览器不支持视频播放
        </video>
      </div>

      <!-- 音频预览 -->
      <div v-else-if="isAudio" class="file-preview__audio">
        <div class="file-preview__audio-info">
          <Icon icon="ep:microphone" size="48" class="text-primary-500" />
          <div class="file-preview__name">{{ file?.name }}</div>
        </div>
        <audio
          :src="file?.url"
          controls
          class="file-preview__audio-player"
        >
          您的浏览器不支持音频播放
        </audio>
      </div>

      <!-- 文本文件预览 -->
      <div v-else-if="isText" class="file-preview__text">
        <div v-if="textLoading" class="file-preview__loading">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>加载中...</span>
        </div>
        <pre v-else class="file-preview__text-content">{{ textContent }}</pre>
      </div>

      <!-- 代码文件预览 -->
      <div v-else-if="isCode" class="file-preview__code">
        <div v-if="textLoading" class="file-preview__loading">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>加载中...</span>
        </div>
        <pre v-else class="file-preview__code-content"><code>{{ textContent }}</code></pre>
      </div>

      <!-- DXF/DWG 工程图纸预览 -->
      <div v-else-if="isCad" class="file-preview__cad">
        <div v-if="cadLoading" class="file-preview__loading">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>正在解析工程图纸...</span>
        </div>
        <div v-else-if="cadSvg" class="file-preview__cad-svg" v-html="cadSvg"></div>
        <div v-else class="file-preview__cad-fallback">
          <div class="file-preview__engineering-info">
            <Icon icon="ep:scale-to-original" size="64" class="text-primary-500" />
            <div class="file-preview__name">{{ file?.name }}</div>
            <div class="file-preview__format-badge">CAD 工程图纸</div>
            <div class="file-preview__meta">
              <span v-if="file?.size">大小：{{ formatSize(file.size) }}</span>
            </div>
            <div class="file-preview__actions">
              <el-button type="primary" @click="download">
                <Icon icon="ep:download" class="mr-5px" />
                下载原始文件
              </el-button>
              <el-button @click="openInExternalViewer">
                <Icon icon="ep:link" class="mr-5px" />
                在线查看器
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- Gerber PCB 文件预览 -->
      <div v-else-if="isGerber" class="file-preview__gerber">
        <div v-if="gerberLoading" class="file-preview__loading">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>正在解析 PCB 文件...</span>
        </div>
        <div v-else-if="gerberSvg" class="file-preview__gerber-svg" v-html="gerberSvg"></div>
        <div v-else class="file-preview__gerber-fallback">
          <div class="file-preview__engineering-info">
            <Icon icon="ep:cpu" size="64" class="text-success-500" />
            <div class="file-preview__name">{{ file?.name }}</div>
            <div class="file-preview__format-badge">PCB 设计文件</div>
            <div class="file-preview__layer-info" v-if="gerberLayer">
              <el-tag size="small">{{ gerberLayer }}</el-tag>
            </div>
            <div class="file-preview__meta">
              <span v-if="file?.size">大小：{{ formatSize(file.size) }}</span>
            </div>
            <div class="file-preview__actions">
              <el-button type="primary" @click="download">
                <Icon icon="ep:download" class="mr-5px" />
                下载原始文件
              </el-button>
              <el-button @click="openInExternalViewer">
                <Icon icon="ep:link" class="mr-5px" />
                在线查看器
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 3D 模型预览 -->
      <div v-else-if="is3DModel" class="file-preview__3d">
        <div v-if="modelLoading" class="file-preview__loading">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>正在加载 3D 模型...</span>
        </div>
        <div v-else ref="threeContainer" class="file-preview__3d-container"></div>
        <div class="file-preview__3d-controls" v-if="!modelLoading">
          <el-button-group>
            <el-button size="small" @click="reset3DView">
              <Icon icon="ep:refresh" />
            </el-button>
            <el-button size="small" @click="toggle3DWireframe">
              <Icon icon="ep:grid" />
            </el-button>
          </el-button-group>
        </div>
      </div>

      <!-- EDA/原理图文件预览 -->
      <div v-else-if="isEDA" class="file-preview__eda">
        <div class="file-preview__engineering-info">
          <Icon icon="ep:circuit-board" size="64" class="text-warning-500" />
          <div class="file-preview__name">{{ file?.name }}</div>
          <div class="file-preview__format-badge">{{ edaFormatName }}</div>
          <div class="file-preview__meta">
            <span v-if="file?.size">大小：{{ formatSize(file.size) }}</span>
          </div>
          <el-alert
            type="info"
            :closable="false"
            show-icon
            class="mt-4"
          >
            <template #title>
              <span>此文件需要使用专业 EDA 软件打开</span>
            </template>
            <template #default>
              <div class="text-sm text-slate-500">
                <div v-if="file?.name?.endsWith('.schdoc')">Altium Designer 原理图文件</div>
                <div v-else-if="file?.name?.endsWith('.pcbdoc')">Altium Designer PCB 文件</div>
                <div v-else-if="file?.name?.endsWith('.brd')">Eagle PCB 文件</div>
                <div v-else-if="file?.name?.endsWith('.sch')">原理图文件</div>
                <div v-else-if="file?.name?.endsWith('.kicad_pcb')">KiCad PCB 文件</div>
                <div v-else-if="file?.name?.endsWith('.kicad_sch')">KiCad 原理图文件</div>
              </div>
            </template>
          </el-alert>
          <div class="file-preview__actions">
            <el-button type="primary" @click="download">
              <Icon icon="ep:download" class="mr-5px" />
              下载文件
            </el-button>
          </div>
        </div>
      </div>

      <!-- 其他文件 -->
      <div v-else class="file-preview__other">
        <div class="file-preview__info">
          <div class="file-preview__icon">
            <Icon :icon="fileIcon" size="48" />
          </div>
          <div class="file-preview__details">
            <div class="file-preview__name">{{ file?.name }}</div>
            <div class="file-preview__meta">
              <span v-if="file?.size">大小：{{ formatSize(file.size) }}</span>
              <span v-if="file?.createTime">上传时间：{{ file.createTime }}</span>
            </div>
          </div>
        </div>
        <div class="file-preview__actions">
          <el-button type="primary" @click="download">
            <Icon icon="ep:download" class="mr-5px" />
            下载文件
          </el-button>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="file-preview__footer">
        <el-button @click="visible = false">关闭</el-button>
        <el-button v-if="!isImage && !isPdf && !isOffice" type="primary" @click="download">
          <Icon icon="ep:download" class="mr-5px" />
          下载
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import request from '@/config/axios'

defineOptions({ name: 'FilePreviewDialog' })

interface FileInfo {
  name: string
  url: string
  size?: number
  type?: string
  createTime?: string
}

const visible = ref(false)
const file = ref<FileInfo | null>(null)
const textContent = ref('')
const textLoading = ref(false)

// CAD 相关
const cadLoading = ref(false)
const cadSvg = ref('')

// Gerber 相关
const gerberLoading = ref(false)
const gerberSvg = ref('')
const gerberLayer = ref('')

// 3D 相关
const threeContainer = ref<HTMLElement | null>(null)
const modelLoading = ref(false)
let threeScene: any = null
let wireframeMode = false

// ============ 文件类型判断 ============

const isImage = computed(() => {
  if (!file.value) return false
  const ext = getExtension(file.value.name)
  return ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg', 'ico', 'tiff'].includes(ext)
})

const isPdf = computed(() => {
  if (!file.value) return false
  return getExtension(file.value.name) === 'pdf'
})

const isOffice = computed(() => {
  if (!file.value) return false
  const ext = getExtension(file.value.name)
  return ['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx'].includes(ext)
})

const isVideo = computed(() => {
  if (!file.value) return false
  const ext = getExtension(file.value.name)
  return ['mp4', 'webm', 'ogg', 'mov', 'avi', 'wmv', 'mkv'].includes(ext)
})

const isAudio = computed(() => {
  if (!file.value) return false
  const ext = getExtension(file.value.name)
  return ['mp3', 'wav', 'ogg', 'aac', 'flac', 'm4a', 'wma'].includes(ext)
})

const isText = computed(() => {
  if (!file.value) return false
  const ext = getExtension(file.value.name)
  return ['txt', 'csv', 'log', 'ini', 'conf', 'yml', 'yaml', 'xml', 'json', 'md', 'properties'].includes(ext)
})

const isCode = computed(() => {
  if (!file.value) return false
  const ext = getExtension(file.value.name)
  return ['js', 'ts', 'jsx', 'tsx', 'vue', 'java', 'py', 'go', 'rs', 'c', 'cpp', 'h', 'hpp', 'css', 'scss', 'less', 'html', 'sql', 'sh', 'bat', 'ps1'].includes(ext)
})

// CAD 工程图纸
const isCad = computed(() => {
  if (!file.value) return false
  const ext = getExtension(file.value.name)
  return ['dxf', 'dwg'].includes(ext)
})

// Gerber PCB 文件
const isGerber = computed(() => {
  if (!file.value) return false
  const ext = getExtension(file.value.name)
  return ['gbr', 'gbl', 'gtl', 'gbs', 'gts', 'gbo', 'gto', 'gm1', 'drl', 'ger'].includes(ext)
})

// 3D 模型
const is3DModel = computed(() => {
  if (!file.value) return false
  const ext = getExtension(file.value.name)
  return ['step', 'stp', 'iges', 'igs', 'stl', 'obj', '3ds', 'fbx'].includes(ext)
})

// EDA 文件
const isEDA = computed(() => {
  if (!file.value) return false
  const ext = getExtension(file.value.name)
  return ['schdoc', 'pcbdoc', 'brd', 'sch', 'kicad_pcb', 'kicad_sch', 'kicad_pro', 'pro'].includes(ext)
})

const edaFormatName = computed(() => {
  if (!file.value) return ''
  const ext = getExtension(file.value.name)
  const map: Record<string, string> = {
    schdoc: 'Altium Designer 原理图',
    pcbdoc: 'Altium Designer PCB',
    brd: 'Eagle PCB',
    sch: '原理图',
    kicad_pcb: 'KiCad PCB',
    kicad_sch: 'KiCad 原理图',
    kicad_pro: 'KiCad 项目',
    pro: '项目文件'
  }
  return map[ext] || 'EDA 文件'
})

// Office 在线预览 URL
const officePreviewUrl = computed(() => {
  if (!file.value?.url) return ''
  return `https://view.officeapps.live.com/op/embed.aspx?src=${encodeURIComponent(file.value.url)}`
})

const fileIcon = computed(() => {
  if (!file.value) return 'ep:document'
  const ext = getExtension(file.value.name)
  const iconMap: Record<string, string> = {
    pdf: 'ep:document',
    doc: 'ep:document', docx: 'ep:document',
    xls: 'ep:grid', xlsx: 'ep:grid',
    ppt: 'ep:picture', pptx: 'ep:picture',
    zip: 'ep:folder', rar: 'ep:folder', '7z': 'ep:folder',
    mp4: 'ep:video-camera', avi: 'ep:video-camera',
    mp3: 'ep:microphone', wav: 'ep:microphone',
    dwg: 'ep:scale-to-original', dxf: 'ep:scale-to-original',
    gbr: 'ep:cpu', gbl: 'ep:cpu', gtl: 'ep:cpu',
    step: 'ep:box', stl: 'ep:box', obj: 'ep:box',
    schdoc: 'ep:circuit-board', pcbdoc: 'ep:circuit-board',
    brd: 'ep:circuit-board', sch: 'ep:circuit-board',
  }
  return iconMap[ext] || 'ep:document'
})

// ============ 工具函数 ============

const getExtension = (filename: string): string => {
  return filename?.split('.').pop()?.toLowerCase() || ''
}

const formatSize = (bytes?: number) => {
  if (!bytes) return '-'
  const units = ['B', 'KB', 'MB', 'GB']
  let size = bytes
  let unitIndex = 0
  while (size >= 1024 && unitIndex < units.length - 1) {
    size /= 1024
    unitIndex++
  }
  return `${size.toFixed(2)} ${units[unitIndex]}`
}

// ============ 核心功能 ============

// 打开预览
const open = async (fileInfo: FileInfo) => {
  file.value = fileInfo
  visible.value = true

  // 根据文件类型加载内容
  const ext = getExtension(fileInfo.name)
  
  if (isText.value || isCode.value) {
    await loadTextContent(fileInfo.url)
  } else if (isCad.value) {
    await loadCadPreview(fileInfo.url, ext)
  } else if (isGerber.value) {
    await loadGerberPreview(fileInfo.url, ext)
  } else if (is3DModel.value) {
    await nextTick()
    await load3DModel(fileInfo.url, ext)
  }
}

// 加载文本内容
const loadTextContent = async (url: string) => {
  textLoading.value = true
  textContent.value = ''
  try {
    const response = await fetch(url)
    if (response.ok) {
      textContent.value = await response.text()
      if (textContent.value.length > 100000) {
        textContent.value = textContent.value.substring(0, 100000) + '\n\n... [文件内容过长，仅显示前 100000 字符]'
      }
    } else {
      textContent.value = '无法加载文件内容'
    }
  } catch (e) {
    textContent.value = '加载文件内容失败：' + (e as Error).message
  } finally {
    textLoading.value = false
  }
}

// 加载 DXF 预览
const loadCadPreview = async (url: string, ext: string) => {
  cadLoading.value = true
  cadSvg.value = ''
  try {
    if (ext === 'dxf') {
      // 使用 dxf-parser 解析 DXF 文件
      const response = await fetch(url)
      const text = await response.text()
      
      // 简单的 DXF 转 SVG（基础实现）
      // 生产环境建议使用 dxf-parser + three-dxf 库
      cadSvg.value = parseDxfToSvg(text)
    }
    // DWG 格式需要服务器端转换，这里显示下载提示
  } catch (e) {
    console.error('解析 CAD 文件失败', e)
  } finally {
    cadLoading.value = false
  }
}

// 简单的 DXF 转 SVG 解析器
const parseDxfToSvg = (dxfContent: string): string => {
  // 这是一个简化的 DXF 解析器，生产环境建议使用专业库
  const lines = dxfContent.split('\n')
  let minX = Infinity, minY = Infinity, maxX = -Infinity, maxY = -Infinity
  const entities: Array<{type: string, points: Array<{x: number, y: number}>}> = []
  let currentEntity: any = null
  let inEntities = false

  for (let i = 0; i < lines.length; i++) {
    const line = lines[i].trim()
    const nextLine = lines[i + 1]?.trim()

    if (line === 'ENTITIES') inEntities = true
    if (line === 'ENDSEC' && inEntities) break

    if (inEntities) {
      if (line === '0' && nextLine) {
        if (currentEntity) entities.push(currentEntity)
        currentEntity = { type: nextLine, points: [] }
      }
      if (currentEntity) {
        if (line === '10') currentEntity.points.push({ x: parseFloat(nextLine || '0'), y: 0 })
        if (line === '20' && currentEntity.points.length > 0) {
          currentEntity.points[currentEntity.points.length - 1].y = parseFloat(nextLine || '0')
        }
      }
    }
  }
  if (currentEntity) entities.push(currentEntity)

  // 计算边界
  entities.forEach(e => {
    e.points.forEach(p => {
      minX = Math.min(minX, p.x)
      minY = Math.min(minY, p.y)
      maxX = Math.max(maxX, p.x)
      maxY = Math.max(maxY, p.y)
    })
  })

  if (!isFinite(minX)) return ''

  const width = maxX - minX || 100
  const height = maxY - minY || 100
  const scale = Math.min(800 / width, 600 / height)

  let svg = `<svg xmlns="http://www.w3.org/2000/svg" width="${width * scale}" height="${height * scale}" viewBox="${minX} ${minY} ${width} ${height}">`
  svg += `<rect x="${minX}" y="${minY}" width="${width}" height="${height}" fill="white"/>`

  entities.forEach(e => {
    if (e.type === 'LINE' && e.points.length >= 2) {
      svg += `<line x1="${e.points[0].x}" y1="${e.points[0].y}" x2="${e.points[1].x}" y2="${e.points[1].y}" stroke="black" stroke-width="0.5"/>`
    } else if (e.type === 'CIRCLE' && e.points.length >= 1) {
      // 简化处理，实际需要读取半径
      svg += `<circle cx="${e.points[0].x}" cy="${e.points[0].y}" r="10" fill="none" stroke="black" stroke-width="0.5"/>`
    }
  })

  svg += '</svg>'
  return svg
}

// 加载 Gerber 预览
const loadGerberPreview = async (url: string, ext: string) => {
  gerberLoading.value = true
  gerberSvg.value = ''
  
  // 识别 Gerber 层
  const layerMap: Record<string, string> = {
    gtl: '顶层铜',
    gbl: '底层铜',
    gts: '顶层阻焊',
    gbs: '底层阻焊',
    gto: '顶层丝印',
    gbo: '底层丝印',
    gbr: 'Gerber',
    gm1: '机械层',
    drl: '钻孔',
    ger: 'Gerber'
  }
  gerberLayer.value = layerMap[ext] || 'PCB'

  try {
    // Gerber 解析需要 gerber-parser 库
    // 这里显示文件信息，建议使用专业 Gerber 查看器
    console.log('Gerber 文件需要专业解析库')
  } catch (e) {
    console.error('解析 Gerber 文件失败', e)
  } finally {
    gerberLoading.value = false
  }
}

// 加载 3D 模型
const load3DModel = async (url: string, ext: string) => {
  if (!threeContainer.value) return
  
  modelLoading.value = true
  try {
    // 动态加载 Three.js
    const THREE = await import('three')
    const { OrbitControls } = await import('three/examples/jsm/controls/OrbitControls.js')

    // 创建场景
    const scene = new THREE.Scene()
    scene.background = new THREE.Color(0xf5f5f5)

    // 创建相机
    const camera = new THREE.PerspectiveCamera(
      75,
      threeContainer.value.clientWidth / threeContainer.value.clientHeight,
      0.1,
      1000
    )

    // 创建渲染器
    const renderer = new THREE.WebGLRenderer({ antialias: true })
    renderer.setSize(threeContainer.value.clientWidth, threeContainer.value.clientHeight)
    threeContainer.value.appendChild(renderer.domElement)

    // 添加控制器
    const controls = new OrbitControls(camera, renderer.domElement)
    controls.enableDamping = true

    // 添加光源
    const ambientLight = new THREE.AmbientLight(0xffffff, 0.6)
    scene.add(ambientLight)
    const directionalLight = new THREE.DirectionalLight(0xffffff, 0.8)
    directionalLight.position.set(10, 10, 10)
    scene.add(directionalLight)

    // 根据文件类型加载模型
    if (ext === 'stl') {
      const { STLLoader } = await import('three/examples/jsm/loaders/STLLoader.js')
      const loader = new STLLoader()
      loader.load(url, (geometry) => {
        const material = new THREE.MeshStandardMaterial({ color: 0x00aaff })
        const mesh = new THREE.Mesh(geometry, material)
        scene.add(mesh)
        
        // 调整相机位置
        geometry.computeBoundingBox()
        const box = geometry.boundingBox!
        const center = box.getCenter(new THREE.Vector3())
        const size = box.getSize(new THREE.Vector3())
        const maxDim = Math.max(size.x, size.y, size.z)
        camera.position.set(center.x + maxDim, center.y + maxDim, center.z + maxDim)
        camera.lookAt(center)
        controls.target.copy(center)
      })
    } else if (ext === 'obj') {
      const { OBJLoader } = await import('three/examples/jsm/loaders/OBJLoader.js')
      const loader = new OBJLoader()
      loader.load(url, (object) => {
        scene.add(object)
      })
    } else {
      // STEP/IGES 等格式需要专业解析库
      // 显示一个占位立方体
      const geometry = new THREE.BoxGeometry(1, 1, 1)
      const material = new THREE.MeshStandardMaterial({ color: 0x00aaff })
      const cube = new THREE.Mesh(geometry, material)
      scene.add(cube)
    }

    // 保存场景引用
    threeScene = { scene, camera, renderer, controls }

    // 动画循环
    const animate = () => {
      requestAnimationFrame(animate)
      controls.update()
      renderer.render(scene, camera)
    }
    animate()

  } catch (e) {
    console.error('加载 3D 模型失败', e)
  } finally {
    modelLoading.value = false
  }
}

// 重置 3D 视图
const reset3DView = () => {
  if (threeScene) {
    threeScene.camera.position.set(5, 5, 5)
    threeScene.controls.target.set(0, 0, 0)
    threeScene.controls.update()
  }
}

// 切换线框模式
const toggle3DWireframe = () => {
  if (threeScene) {
    wireframeMode = !wireframeMode
    threeScene.scene.traverse((child: any) => {
      if (child.isMesh) {
        child.material.wireframe = wireframeMode
      }
    })
  }
}

// 打开外部查看器
const openInExternalViewer = () => {
  if (!file.value?.url) return
  // 使用免费的在线查看器
  const viewerUrl = `https://sharecad.org/cadframe/load?url=${encodeURIComponent(file.value.url)}`
  window.open(viewerUrl, '_blank')
}

// 关闭
const handleClosed = () => {
  file.value = null
  textContent.value = ''
  cadSvg.value = ''
  gerberSvg.value = ''
  
  // 清理 3D 场景
  if (threeScene) {
    threeScene.renderer.dispose()
    threeScene = null
  }
  if (threeContainer.value) {
    threeContainer.value.innerHTML = ''
  }
}

// 下载
const download = () => {
  if (!file.value?.url) return
  const link = document.createElement('a')
  link.href = file.value.url
  link.download = file.value.name
  link.click()
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.file-preview {
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.file-preview__image {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.file-preview__img {
  max-height: 70vh;
  max-width: 100%;
}

.file-preview__pdf,
.file-preview__office {
  width: 100%;
  height: 70vh;
}

.file-preview__iframe {
  width: 100%;
  height: 100%;
  border: none;
}

.file-preview__video {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.file-preview__video-player {
  max-width: 100%;
  max-height: 70vh;
}

.file-preview__audio {
  width: 100%;
  padding: 40px 20px;
  text-align: center;
}

.file-preview__audio-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.file-preview__audio-player {
  width: 100%;
  max-width: 400px;
}

.file-preview__text,
.file-preview__code {
  width: 100%;
  max-height: 70vh;
  overflow: auto;
}

.file-preview__text-content,
.file-preview__code-content {
  padding: 16px;
  margin: 0;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-wrap: break-word;
  background: var(--erp-slate-50);
  border-radius: 8px;
  color: var(--erp-slate-800);
}

.file-preview__code-content {
  background: var(--erp-slate-900);
  color: var(--erp-slate-100);
}

.file-preview__loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 40px;
  color: var(--erp-slate-500);
}

// CAD 预览
.file-preview__cad,
.file-preview__gerber {
  width: 100%;
  min-height: 400px;
}

.file-preview__cad-svg,
.file-preview__gerber-svg {
  width: 100%;
  display: flex;
  justify-content: center;
  padding: 16px;
  background: white;
  border-radius: 8px;
  overflow: auto;

  :deep(svg) {
    max-width: 100%;
    height: auto;
  }
}

.file-preview__cad-fallback,
.file-preview__gerber-fallback {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

// 3D 预览
.file-preview__3d {
  width: 100%;
  height: 70vh;
  position: relative;
}

.file-preview__3d-container {
  width: 100%;
  height: 100%;
  border-radius: 8px;
  overflow: hidden;

  :deep(canvas) {
    display: block;
  }
}

.file-preview__3d-controls {
  position: absolute;
  bottom: 16px;
  left: 16px;
  z-index: 10;
}

// 工程文件通用信息
.file-preview__engineering-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 40px 20px;
  text-align: center;
}

.file-preview__format-badge {
  display: inline-block;
  padding: 4px 12px;
  background: var(--erp-primary-50);
  color: var(--erp-primary-600);
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.file-preview__layer-info {
  margin-top: 4px;
}

.file-preview__other {
  width: 100%;
  text-align: center;
  padding: 40px 20px;
}

.file-preview__info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.file-preview__icon {
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--erp-slate-50);
  border-radius: 12px;
  color: var(--erp-primary-600);
}

.file-preview__details {
  text-align: center;
}

.file-preview__name {
  font-size: 18px;
  font-weight: 600;
  color: var(--erp-slate-900);
  margin-bottom: 8px;
}

.file-preview__meta {
  display: flex;
  gap: 16px;
  font-size: 14px;
  color: var(--erp-slate-500);
}

.file-preview__actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

.file-preview__footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

// EDA 文件
.file-preview__eda {
  width: 100%;
}
</style>
