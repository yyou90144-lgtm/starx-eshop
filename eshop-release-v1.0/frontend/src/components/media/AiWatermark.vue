<template>
  <div class="ai-wm" :class="[`ai-wm--${size}`, { 'ai-wm--block': block }]">
    <span v-if="size === 'lg'" class="ai-wm__badge" aria-label="AI生成内容">AI生成</span>
    <slot />
    <span v-if="size !== 'lg'" class="ai-wm__badge" aria-label="AI生成内容">AI生成</span>
  </div>
</template>

<script setup>
defineProps({
  /** sm: 列表缩略图；md: 详情主图；lg: 详情长图区域 */
  size: {
    type: String,
    default: 'sm',
  },
  block: {
    type: Boolean,
    default: false,
  },
})
</script>

<style scoped>
.ai-wm {
  position: relative;
  display: inline-block;
  max-width: 100%;
  vertical-align: top;
  line-height: 0;
  overflow: hidden;
}

.ai-wm--block {
  display: block;
  width: 100%;
}

.ai-wm :deep(img) {
  display: block;
  width: 100%;
  max-width: 100%;
  height: auto;
}

.ai-wm__badge {
  position: absolute;
  right: 6px;
  bottom: 6px;
  z-index: 3;
  padding: 2px 6px;
  border-radius: 4px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  background: rgba(20, 20, 20, 0.45);
  color: rgba(255, 255, 255, 0.95);
  font-family: "PingFang SC", "Microsoft YaHei", -apple-system, BlinkMacSystemFont, sans-serif;
  font-size: 10px;
  font-weight: 650;
  letter-spacing: 0.06em;
  line-height: 1.35;
  pointer-events: none;
  user-select: none;
  white-space: nowrap;
}

.ai-wm--md .ai-wm__badge {
  right: 10px;
  bottom: 10px;
  padding: 3px 8px;
  font-size: 11px;
}

/* 长图：水印吸顶在右上角，滚动全程可见 */
.ai-wm--lg {
  overflow: visible;
}

.ai-wm--lg .ai-wm__badge {
  position: sticky;
  top: 12px;
  float: right;
  right: auto;
  bottom: auto;
  margin: 12px 12px -40px 0;
  padding: 4px 10px;
  font-size: 12px;
  background: rgba(20, 20, 20, 0.5);
}
</style>
