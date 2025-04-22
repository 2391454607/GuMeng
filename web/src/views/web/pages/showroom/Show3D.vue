<script setup>
import * as THREE from "three";
import {onMounted, reactive, ref} from "vue";
import { OrbitControls } from "three/examples/jsm/controls/OrbitControls.js";
import { DRACOLoader } from "three/examples/jsm/loaders/DRACOLoader.js";
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js';


let controls;
let canvasDom = ref(null);
//创建场景
const scene = new THREE.Scene();
//创建相机
const camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000);
camera.position.set(0, 0, 0);
//创建渲染器
const renderer = new THREE.WebGLRenderer({
  //抗锯齿
  antialias: true,

});
renderer.setSize(window.innerWidth, window.innerHeight);

onMounted(() => {
  //把渲染器插入到dom中
  canvasDom.value.appendChild(renderer.domElement);
  //初始化渲染器，渲染背景
  renderer.setClearColor("#000");
  scene.background = new THREE.Color("#1a1a1a"); // 更改背景
  scene.environment = new THREE.Color("#1a1a1a"); // 保持环境色与背景一致
  render();

  //添加控制器
  controls = new OrbitControls(camera, renderer.domElement);
  // 添加阻尼效果
  controls.enableDamping = true; // 启用阻尼效果
  controls.dampingFactor = 0.05; // 设置阻尼系数
  controls.rotateSpeed = 0.8; // 调整旋转速度
  controls.zoomSpeed = 0.8; // 调整缩放速度
  controls.panSpeed = 0.8; // 调整平移速度
  controls.update();

  //添加模型
  const loader = new GLTFLoader();
  const dracoLoader = new DRACOLoader();
  dracoLoader.setDecoderPath("/public/draco/");
  loader.setDRACOLoader(dracoLoader);
  loader.load("/src/assets/3d/car.glb", (gltf) => {
    const car = gltf.scene;
    
    // 计算模型包围盒
    const box = new THREE.Box3().setFromObject(car);
    const center = box.getCenter(new THREE.Vector3());
    const size = box.getSize(new THREE.Vector3());
    
    // 将模型居中并放置在网格上方
    car.position.x = -center.x;
    car.position.y = -center.y + size.y / 2;
    car.position.z = -center.z;
    
    // 调整相机位置以适应模型大小
    const maxDim = Math.max(size.x, size.y, size.z);
    const fov = camera.fov * (Math.PI / 180);
    const cameraZ = Math.abs(maxDim / Math.tan(fov / 2));
    // 修改相机位置，减小距离系数
    camera.position.set(maxDim * 0.8, maxDim * 0.8, cameraZ * 0.8);
    camera.lookAt(new THREE.Vector3(0, 0, 0));
    
    scene.add(car);
    
    // 更新控制器目标点，稍微提高一点以更好地对准模型中心
    controls.target.set(0, size.y / 3, 0);
    controls.update();
  });

  //添加灯光
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
  const light6 = new THREE.DirectionalLight(0xffffff, 1);
  light5.position.set(5, 10, 0);
  scene.add(light6);
  const light7 = new THREE.DirectionalLight(0xffffff, 1);
  light5.position.set(0, 10, 5);
  scene.add(light7);
  const light8 = new THREE.DirectionalLight(0xffffff, 1);
  light5.position.set(0, 10, -5);
  scene.add(light8);
  const light9 = new THREE.DirectionalLight(0xffffff, 1);
  light5.position.set(-5, 10, 0);
  scene.add(light9);

});

// 修改渲染函数，确保阻尼效果生效
const render = () => {
  renderer.render(scene, camera);
  controls && controls.update(); // 确保每帧更新控制器
  requestAnimationFrame(render);
}
</script>

<template>
  <div class="home">
    <div ref="canvasDom" class="canvas-container"></div>
  </div>
</template>

<style scoped>
*{
  margin: 0;
  padding: 0;
}
</style>