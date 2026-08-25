<template>
  <span class="macos-dock-glyph" :class="`macos-dock-glyph--${tone}`" aria-hidden="true">
    <span class="macos-dock-glyph__shine" />
    <el-icon class="macos-dock-glyph__symbol"><component :is="icon" /></el-icon>
  </span>
</template>

<script setup lang="ts">
import type { Component } from 'vue'

defineProps<{ icon: Component; tone: string }>()
</script>

<style scoped>
.macos-dock-glyph {
  position: relative;
  display: grid;
  width: 100%;
  height: 100%;
  place-items: center;
  overflow: hidden;
  isolation: isolate;
  border: 1px solid rgba(255,255,255,.78);
  border-radius: 27%;
  background: linear-gradient(145deg,#75d7ff,#1689e8 58%,#0755ae);
  box-shadow: 0 2px 5px rgba(15,47,83,.28), inset 0 1px 1px rgba(255,255,255,.74), inset 0 -1px 2px rgba(0,35,91,.34);
}
.macos-dock-glyph--indigo { background: linear-gradient(145deg,#a5adff,#646ee5 58%,#3b3ca9); }
.macos-dock-glyph--orange { background: linear-gradient(145deg,#ffd06e,#ef922c 58%,#a64a18); }
.macos-dock-glyph--violet { background: linear-gradient(145deg,#d3a0ff,#8a55dd 58%,#5130a3); }
.macos-dock-glyph--cyan { background: linear-gradient(145deg,#86e9f8,#27aeca 58%,#087288); }
.macos-dock-glyph--green { background: linear-gradient(145deg,#8aea9f,#2ab85a 58%,#117a36); }
.macos-dock-glyph--mint { background: linear-gradient(145deg,#76e8c8,#1eb290 58%,#08745f); }
.macos-dock-glyph--yellow { background: linear-gradient(145deg,#ffe78a,#eeb538 58%,#a56c12); }
.macos-dock-glyph--pink { background: linear-gradient(145deg,#ffacd0,#e45b9d 58%,#a72968); }
.macos-dock-glyph--teal { background: linear-gradient(145deg,#8be4df,#2cadac 58%,#0d7379); }
.macos-dock-glyph--slate { background: linear-gradient(145deg,#d2d9e1,#7d8c9e 58%,#455464); }
.macos-dock-glyph--gray { background: linear-gradient(145deg,#d6dce3,#8794a4 58%,#4c5968); }
.macos-dock-glyph__shine { position: absolute; inset: 1px 2px 48%; border-radius: 42% 42% 52% 52%; background: linear-gradient(180deg,rgba(255,255,255,.64),rgba(255,255,255,.05)); mix-blend-mode: screen; }
.macos-dock-glyph::after { position: absolute; right: 12%; bottom: 5%; left: 12%; height: 22%; border-radius: 50%; background: rgba(255,255,255,.18); filter: blur(4px); content: ''; }
.macos-dock-glyph__symbol { z-index: 1; width: 58%; height: 58%; color: rgba(255,255,255,.97); filter: drop-shadow(0 1.5px 1px rgba(14,42,68,.35)); }
.macos-dock-glyph__symbol :deep(svg) { width: 100%; height: 100%; shape-rendering: geometricPrecision; }
</style>
