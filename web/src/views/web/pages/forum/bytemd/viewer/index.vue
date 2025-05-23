<script setup>
import { getProcessor } from 'bytemd';
import { defineProps, ref, computed, nextTick, watch, onMounted, onUnmounted } from 'vue';

const props = defineProps(["value", "plugins", "sanitize", "remarkRehype"]);

const markdownBody = ref(null);
const cbs = ref([]);

//获取文档对象
const file = computed(() => {
  //同步处理文档对象的值
  return getProcessor(props).processSync(props.value || '');
});

const needUpdate = computed(() => {
  return [file.value, props.plugins, props.sanitize, props.remarkRehype];
});

watch(() => needUpdate.value, () => {
  off();
  nextTick(() => {
    on();
  });
}, {
  deep: true
});

onMounted(() => {
  on();
});

onUnmounted(() => {
  off();
});

const handleClick = e => {
  const target = e.target;
  if (target.tagName !== 'A') return;
  const href = target.getAttribute("href");
  if (!href || !href.startsWith('#')) return;
  const dest = markdownBody.value.querySelector('#user-content-' + href.slice(1));
  if (dest) dest.scrollIntoView();
};

const off = () => {
  if (cbs.value.length) {
    cbs.value.forEach(cb => cb && cb());
  }
};

const on = () => {
  if (props.plugins && file.value) {
    cbs.value = props.plugins.map(({ viewerEffect }) => viewerEffect && viewerEffect({ markdownBody: markdownBody.value, file: file.value }));
  }
};
</script>

<template>
  <div v-html="file.toString()" class="markdown-body" @click="handleClick" ref="markdownBody">
  </div>
</template>

<style>
.bytemd {
  height: 100% !important;
}

/* 增强预览区域样式 */
.markdown-body {
  font-family: "SimSun", "宋体", serif;
  color: #582F0E;
  padding: 15px;
}

.markdown-body h1,
.markdown-body h2,
.markdown-body h3,
.markdown-body h4,
.markdown-body h5,
.markdown-body h6 {
  color: #8C1F28;
  font-family: "STKaiti", "楷体", serif;
  margin-top: 24px;
  margin-bottom: 16px;
  font-weight: 600;
  border-bottom: 1px solid #E4D9C3;
  padding-bottom: 0.3em;
}

.markdown-body a {
  color: #8C1F28;
  text-decoration: none;
}

.markdown-body a:hover {
  text-decoration: underline;
}

.markdown-body blockquote {
  padding: 0 1em;
  color: #6B5B45;
  border-left: 0.25em solid #E4D9C3;
}

.markdown-body pre {
  background-color: #FFF7E9;
  border: 1px solid #E4D9C3;
  border-radius: 4px;
}

.markdown-body code {
  background-color: #FFF7E9;
  color: #8C1F28;
  padding: 0.2em 0.4em;
  border-radius: 3px;
  font-family: "SFMono-Regular", Consolas, "Liberation Mono", Menlo, monospace;
}

.markdown-body img {
  max-width: 100%;
  box-sizing: content-box;
  border-radius: 4px;
  border: 1px solid #E4D9C3;
  margin: 10px 0;
  display: block;
}
</style> 