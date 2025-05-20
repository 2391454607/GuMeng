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
</style> 