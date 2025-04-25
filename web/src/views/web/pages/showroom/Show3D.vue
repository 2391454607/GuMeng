<script setup>
import * as THREE from "three";
import {onMounted, onUnmounted, ref} from "vue";
import { OrbitControls } from "three/examples/jsm/controls/OrbitControls.js";
import { DRACOLoader } from "three/examples/jsm/loaders/DRACOLoader.js";
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js';
import { useRouter, useRoute } from 'vue-router';  // 添加 useRoute
import {getModelDetail} from "@/api/web/Model.js";

const router = useRouter();
const route = useRoute();

// 添加状态变量
const loading = ref(false);
const progress = ref(0);
const error = ref(false);
const errorMessage = ref('');

// 根据id获取模型
// 添加模型路径验证函数
const validateModelUrl = (url) => {
  if (!url) return false;
  // 检查是否是有效的URL格式或有效的文件路径
  return url.match(/^(http|https):\/\//) || url.match(/\.(gltf|glb)$/i);
};

// 修改随机延迟函数
const getRandomDelay = () => {
  return Math.floor(Math.random() * (3000 - 2000 + 1) + 2000); // 2000-3000ms
};

// 修改 getModel 方法中的错误处理部分
const getModel = async () => {
  loading.value = true;
  progress.value = 0;
  error.value = false;
  
  try {
    const modelId = route.query.modelId;
    console.log('模型ID:', modelId);
    const res = await getModelDetail({modelId: modelId});
    console.log('接口返回数据:', res);
    
    if (res.code === 200 && res.data && Array.isArray(res.data) && res.data.length > 0) {
      const modelData = res.data[0];
      console.log('模型数据:', modelData);
      
      if (modelData.modelUrl && validateModelUrl(modelData.modelUrl)) {
        // 直接使用返回的路径
        const modelUrl = modelData.modelUrl;
        console.log('模型URL:', modelUrl);
        loadModel(modelUrl);
      } else {
        // 模拟加载进度
        let currentProgress = 0;
        const progressInterval = setInterval(() => {
          if (currentProgress < 95) {
            currentProgress += 5;
            progress.value = currentProgress;
          } else {
            clearInterval(progressInterval);
            setTimeout(() => {
              loading.value = false;
              error.value = true;
              errorMessage.value = '模型加载失败';
            }, 3000);
          }
        }, 100);
      }
    } else {
      // 模拟加载进度
      let currentProgress = 0;
      const delay = getRandomDelay();
      const steps = Math.floor(delay / 100); // 计算需要多少步骤达到95%
      const increment = Math.ceil(95 / steps); // 每步增加的进度

      const progressInterval = setInterval(() => {
        if (currentProgress < 95) {
          currentProgress = Math.min(95, currentProgress + increment);
          progress.value = currentProgress;
        } else {
          clearInterval(progressInterval);
          setTimeout(() => {
            loading.value = false;
            error.value = true;
            errorMessage.value = '未找到模型数据';
          }, delay - (steps * 100)); // 减去已经用掉的时间
        }
      }, 100);
    }
  } catch (err) {
    // 模拟加载进度
    let currentProgress = 0;
    const delay = getRandomDelay();
    const steps = Math.floor(delay / 100);
    const increment = Math.ceil(95 / steps);

    const progressInterval = setInterval(() => {
      if (currentProgress < 95) {
        currentProgress = Math.min(95, currentProgress + increment);
        progress.value = currentProgress;
      } else {
        clearInterval(progressInterval);
        setTimeout(() => {
          loading.value = false;
          error.value = true;
          errorMessage.value = '获取模型失败';
          console.error('获取模型失败:', err);
        }, delay - (steps * 100));
      }
    }, 100);
  }
};

// 修改 loadModel 方法中的错误处理部分
const loadModel = (modelUrl) => {
  loading.value = true;
  progress.value = 0;
  error.value = false;
  
  try {
    const loader = new GLTFLoader();
    const dracoLoader = new DRACOLoader();
    dracoLoader.setDecoderPath("/draco/");
    loader.setDRACOLoader(dracoLoader);
    
    loader.load(
      modelUrl,
      (gltf) => {
        progress.value = 100;
        setTimeout(() => {
          loading.value = false;
          const model = gltf.scene;
          
          // 计算模型包围盒
          const box = new THREE.Box3().setFromObject(model);
          const center = box.getCenter(new THREE.Vector3());
          const size = box.getSize(new THREE.Vector3());
          
          // 将模型居中并放置在网格上方
          model.position.x = -center.x;
          model.position.y = -center.y + size.y / 2;
          model.position.z = -center.z;
          
          // 调整相机位置以适应模型大小
          const maxDim = Math.max(size.x, size.y, size.z);
          const fov = camera.fov * (Math.PI / 180);
          const cameraZ = Math.abs(maxDim / Math.tan(fov / 2));
          camera.position.set(maxDim * 0.8, maxDim * 0.8, cameraZ * 0.8);
          camera.lookAt(new THREE.Vector3(0, 0, 0));
          
          scene.add(model);
          
          // 更新控制器目标点
          controls.target.set(0, size.y / 3, 0);
          controls.update();
        }, 500);
      },
      (progressEvent) => {
        if (progressEvent.lengthComputable) {
          const percentComplete = Math.round((progressEvent.loaded / progressEvent.total) * 95);
          progress.value = percentComplete;
        }
      },
      (error) => {
        // 模拟加载进度
        let currentProgress = progress.value;
        const delay = getRandomDelay();
        const steps = Math.floor(delay / 100);
        const increment = Math.ceil((95 - currentProgress) / steps);

        const progressInterval = setInterval(() => {
          if (currentProgress < 95) {
            currentProgress = Math.min(95, currentProgress + increment);
            progress.value = currentProgress;
          } else {
            clearInterval(progressInterval);
            setTimeout(() => {
              loading.value = false;
              error.value = true;
              errorMessage.value = `模型加载失败: ${error.message || '请检查模型文件是否正确'}`;
              console.error('模型加载错误:', error);
            }, delay - (steps * 100));
          }
        }, 100);
      }
    );
  } catch (err) {
    // 捕获的错误也添加延迟
    setTimeout(() => {
      loading.value = false;
      error.value = true;
      errorMessage.value = '模型加载过程出错';
      console.error('加载过程错误:', err);
    }, 3000);
  }
};

let controls;
let canvasDom = ref(null);
let renderer;
let camera;
let scene;

// 初始化场景
const initScene = () => {
  scene = new THREE.Scene();
  camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000);
  camera.position.set(0, 0, 0);
  
  renderer = new THREE.WebGLRenderer({
    antialias: true,
  });
  renderer.setSize(window.innerWidth, window.innerHeight);
};

// 初始化渲染
const initRenderer = () => {
  if (canvasDom.value && renderer) {
    canvasDom.value.appendChild(renderer.domElement);
    renderer.setClearColor("#000");
    scene.background = new THREE.Color("#1a1a1a");
    scene.environment = new THREE.Color("#1a1a1a");
    render();
  }
};

// 添加窗口大小变化处理函数
const handleResize = () => {
  if (camera && renderer) {
    // 更新相机宽高比
    camera.aspect = window.innerWidth / window.innerHeight;
    camera.updateProjectionMatrix();
    
    // 更新渲染器大小
    renderer.setSize(window.innerWidth, window.innerHeight);
  }
};

onMounted(() => {
  initScene();
  initRenderer();
  
  // 添加窗口大小变化监听
  window.addEventListener('resize', handleResize);
  
  // 添加控制器
  controls = new OrbitControls(camera, renderer.domElement);
  controls.enableDamping = true;
  controls.dampingFactor = 0.05;
  controls.rotateSpeed = 0.8;
  controls.zoomSpeed = 0.8;
  controls.panSpeed = 0.8;
  
  // 相机
  controls.enablePan = false;  // 平移
  controls.enableZoom = true; // 缩放
  controls.enableRotate = true; // 旋转
  
  controls.update();

  // 获取并加载模型
  getModel();

  // 添加灯光
  const light1 = new THREE.DirectionalLight(0xffffff, 1);
  light1.position.set(0, 0, 10);
  scene.add(light1);
  const light2 = new THREE.DirectionalLight(0xffffff, 1);
  light2.position.set(0, 0, -10);
  scene.add(light2);
  const light3 = new THREE.DirectionalLight(0xffffff, 1);
  light3.position.set(10, 0, 0);
  scene.add(light3);
  const light4 = new THREE.DirectionalLight(0xffffff, 1);
  light4.position.set(-10, 0, 0);
  scene.add(light4);
  const light5 = new THREE.DirectionalLight(0xffffff, 1);
  light5.position.set(0, 10, 0);
  scene.add(light5);
  // 修正灯光设置
  const light6 = new THREE.DirectionalLight(0xffffff, 1);
  light6.position.set(5, 10, 0);  // 修正：之前错误地使用了 light5
  scene.add(light6);
  
  const light7 = new THREE.DirectionalLight(0xffffff, 1);
  light7.position.set(0, 10, 5);  // 修正：之前错误地使用了 light5
  scene.add(light7);
  
  const light8 = new THREE.DirectionalLight(0xffffff, 1);
  light8.position.set(0, 10, -5);  // 修正：之前错误地使用了 light5
  scene.add(light8);
  
  const light9 = new THREE.DirectionalLight(0xffffff, 1);
  light9.position.set(-5, 10, 0);  // 修正：之前错误地使用了 light5
  scene.add(light9);

});

// 修改渲染函数，确保阻尼效果生效
const render = () => {
  renderer.render(scene, camera);
  controls && controls.update(); // 确保每帧更新控制器
  requestAnimationFrame(render);
}

const handleBack = () => {
  router.push('/showroom');
};

// 组件卸载时清理资源
onUnmounted(() => {
  // 移除窗口大小变化监听
  window.removeEventListener('resize', handleResize);
  
  if (renderer) {
    renderer.dispose();
  }
  if (controls) {
    controls.dispose();
  }
  
  // 添加额外清理
  if (scene) {
    scene.traverse((object) => {
      if (object.geometry) {
        object.geometry.dispose();
      }
      if (object.material) {
        if (Array.isArray(object.material)) {
          object.material.forEach(material => material.dispose());
        } else {
          object.material.dispose();
        }
      }
    });
  }
});
</script>

<template>
  <div class="home">
    <div ref="canvasDom" class="canvas-container"></div>
    
    <!-- 修改加载动画部分 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-content">
        <div class="loading-title">加载中...</div>
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: `${progress}%` }">
            <div class="progress-glow"></div>
          </div>
        </div>
        <div class="loading-text">{{ progress }}%</div>
      </div>
    </div>
    
    <!-- 添加错误提示 -->
    <div v-if="error" class="error-container">
      <div class="error-content">
        <icon-exclamation-circle-fill class="error-icon" />
        <div class="error-text">{{ errorMessage }}</div>
        <a-button type="outline" @click="handleBack">返回</a-button>
      </div>
    </div>
    
    <div class="back-button">
      <a-button type="primary" shape="circle" @click="handleBack">
        <template #icon><icon-arrow-left /></template>
      </a-button>
    </div>
  </div>
</template>

<!-- 修改错误提示部分的样式 -->
<style scoped>
*{
  margin: 0;
  padding: 0;
}

.home {
  position: relative;
  width: 100%;
  height: 100vh;
}

.canvas-container {
  width: 100%;
  height: 100%;
}

.back-button {
  position: fixed;
  top: 20px;
  left: 20px;
  z-index: 1000;
}

.back-button :deep(.arco-btn) {
  width: 40px;
  height: 40px;
  font-size: 18px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(8px);
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  transition: all 0.3s;
}

.back-button :deep(.arco-btn):hover {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.05);
}

.loading-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.loading-content {
  text-align: center;
  color: white;
  width: 300px;
  padding: 20px;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 12px;
  backdrop-filter: blur(8px);
}

.loading-title {
  font-size: 20px;
  margin-bottom: 20px;
  color: #fff;
}

.progress-bar {
  width: 100%;
  height: 8px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  overflow: hidden;
  position: relative;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #165DFF, #41B883);
  border-radius: 4px;
  transition: width 0.3s ease;
  position: relative;
}

.progress-glow {
  position: absolute;
  top: 0;
  right: 0;
  width: 20px;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3));
  filter: blur(2px);
}

.loading-text {
  margin-top: 12px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

.error-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  backdrop-filter: blur(8px);
}

.error-content {
  text-align: center;
  color: white;
  padding: 30px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(8px);
  max-width: 400px;
  width: 90%;
}

.error-icon {
  font-size: 48px;
  color: #F53F3F;
  margin-bottom: 16px;
  animation: pulse 2s infinite;
}

.error-text {
  font-size: 16px;
  line-height: 1.6;
  margin: 16px 0 24px;
  color: rgba(255, 255, 255, 0.9);
}

.error-content :deep(.arco-btn) {
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  padding: 8px 24px;
  font-size: 16px;
  transition: all 0.3s ease;
}

.error-content :deep(.arco-btn):hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.5);
  transform: translateY(-2px);
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}
</style>