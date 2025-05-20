<script setup>
import { Editor } from 'bytemd';
import { ref, watch, defineEmits, onMounted } from 'vue';

const props = defineProps({
  value: String,//内容控件的值
  plugins: Array,//插件数组
  sanitize: Object,//策略模式
  remarkRehype: Object,//文档备注介入选项
  mode: String,//模式
  previewDebounce: Number,//指定debounce时间
  placeholder: String,//占位符
  editorConfig: Object, //编辑配置
  locale: Object,//语言类
  uploadImages: Function,//支持上传图片(指定如何上次图片)
  split: Boolean,//是否启用分屏模式
});
const emit = defineEmits(["change"]);

//当前编辑器的el
const el = ref(null);
const editorRef = ref(null);

//监听props的值，去除为undefined的值
watch(() => props, newValue => {
  const copy = { ...newValue };
  for (let k in copy) {
    if (copy[k] === undefined) {
      delete copy[k];
    }
  }
  editorRef.value.$set(copy);
}, {
  deep: true
});

//监听编辑器值的变化
onMounted(() => {
  const editor = new Editor({
    target: el.value,
    props
  });
  editor.$on("change", e => {
    emit("change", e.detail.value);
  });
  editorRef.value = editor;
});
</script>

<template>
  <div ref="el" class="md-editor">
  </div>
</template>

<style>
.md-editor {
  height: 100%;
}

.bytemd {
  height: 100% !important;
}
</style> 