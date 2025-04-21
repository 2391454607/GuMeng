<script setup>
import * as THREE from "three";
import {onMounted, reactive, ref} from "vue";
import { OrbitControls } from "three/examples/jsm/controls/OrbitControls.js";
import { DRACOLoader } from "three/examples/jsm/loaders/DRACOLoader.js";
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js';
import {FBXLoader} from "three/examples/jsm/loaders/FBXLoader.js";


let controls;
let canvasDom = ref(null);
//创建场景
const scene = new THREE.Scene();
//创建相机
const camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000);
camera.position.set(0, 2, 6);
//创建渲染器
const renderer = new THREE.WebGLRenderer({
  //抗锯齿
  antialias: true,

});
renderer.setSize(window.innerWidth, window.innerHeight);

const render = ()=>{
  renderer.render(scene, camera);
  controls && controls.update();
  requestAnimationFrame(render);
}

onMounted(() => {
  //把渲染器插入到dom中
  canvasDom.value.appendChild(renderer.domElement);
  //初始化渲染器，渲染背景
  renderer.setClearColor("#000");
  scene.background = new THREE.Color("#ccc");
  scene.environment = new THREE.Color("#ccc");
  render();

  //添加网格
  const gridHelper = new THREE.GridHelper(10,10);
  gridHelper.opacity = 0.2;
  gridHelper.material.transparent = true;
  scene.add(gridHelper);

  //添加控制器
  controls = new OrbitControls(camera, renderer.domElement);
  controls.update();

  //添加模型
  const loader = new FBXLoader();
  loader.load('/src/assets/3d/car.fbx',
    (fbx) => {
      // FBX loader 直接返回模型对象，不需要访问 scene 属性
      scene.add(fbx);
    },
    (progress) => {
      console.log('加载进度:', (progress.loaded / progress.total * 100) + '%');
    },
    (error) => {
      console.error('模型加载错误:', error);
    }
  );
})

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