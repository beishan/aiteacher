<template>
  <div class="material-list">
    <!-- 操作栏 -->
    <el-card>
      <div style="display: flex; justify-content: space-between; align-items: center">
        <div>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item @click="navigateToRoot">全部资料</el-breadcrumb-item>
            <el-breadcrumb-item
              v-for="folder in breadcrumb"
              :key="folder.id"
              @click="navigateToFolder(folder.id)"
            >
              {{ folder.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div>
          <el-input
            v-model="keyword"
            placeholder="搜索资料"
            style="width: 200px; margin-right: 8px"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button @click="showFolderForm">
            <el-icon><FolderAdd /></el-icon>新建文件夹
          </el-button>
          <el-button type="primary" @click="showUploadForm">
            <el-icon><Upload /></el-icon>上传资料
          </el-button>
          <el-button :type="showFavorite ? 'warning' : ''" @click="toggleFavoriteView">
            <el-icon><Star /></el-icon>收藏
          </el-button>
          <el-button-group style="margin-left: 8px">
            <el-button :type="viewMode === 'grid' ? 'primary' : ''" @click="viewMode = 'grid'">
              <el-icon><Grid /></el-icon>
            </el-button>
            <el-button :type="viewMode === 'list' ? 'primary' : ''" @click="viewMode = 'list'">
              <el-icon><List /></el-icon>
            </el-button>
            <el-button :type="viewMode === 'columns' ? 'primary' : ''" @click="viewMode = 'columns'">
              <el-icon><Operation /></el-icon>
            </el-button>
          </el-button-group>
        </div>
      </div>
    </el-card>

    <!-- 列表视图 -->
    <el-card v-if="viewMode === 'list'" style="margin-top: 16px">
      <el-table :data="materials" v-loading="loading" stripe @row-contextmenu="openContextMenu">
        <el-table-column prop="title" label="名称" min-width="200">
          <template #default="{ row }">
            <div class="name-cell" @click="handleClick(row)">
              <el-icon v-if="row.isPinned" :size="14" color="#E6A23C" style="margin-right: 4px"><Top /></el-icon>
              <el-icon v-if="row.isFolder" :size="20" :color="row.color || '#409EFF'"><Folder /></el-icon>
              <el-icon v-else :size="20" :color="fileTypeColor[row.fileType] || '#909399'">
                <Document />
              </el-icon>
              <span style="margin-left: 8px">{{ row.title }}</span>
              <el-icon
                v-if="row.isFavorite"
                :size="14"
                color="#E6A23C"
                style="margin-left: 4px"
              >
                <StarFilled />
              </el-icon>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="subject" label="科目" width="80">
          <template #default="{ row }">{{ row.subject || '-' }}</template>
        </el-table-column>
        <el-table-column prop="fileType" label="类型" width="80">
          <template #default="{ row }">
            {{ row.isFolder ? '文件夹' : (row.fileType?.toUpperCase() || '-') }}
          </template>
        </el-table-column>
        <el-table-column label="大小" width="100">
          <template #default="{ row }">
            {{ row.isFolder ? '-' : formatSize(row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="tags" label="标签" width="150">
          <template #default="{ row }">
            <el-tag v-for="tag in row.tags?.slice(0, 3)" :key="tag" size="small" style="margin-right: 4px">
              {{ tag }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="versionCount" label="版本" width="60" />
        <el-table-column prop="updatedAt" label="修改时间" width="160">
          <template #default="{ row }">
            {{ new Date(row.updatedAt).toLocaleString('zh-CN') }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button v-if="!row.isFolder" type="primary" link @click="handlePreview(row)">预览</el-button>
            <el-button v-if="isEditable(row)" type="success" link @click="openEditor(row)">在线编辑</el-button>
            <el-button type="warning" link @click="handlePinned(row)">
              {{ row.isPinned ? '取消置顶' : '置顶' }}
            </el-button>
            <el-button type="primary" link @click="handleFavorite(row)">
              {{ row.isFavorite ? '取消收藏' : '收藏' }}
            </el-button>
            <el-button type="info" link @click="showEditForm(row)">改信息</el-button>
            <el-popconfirm title="确定删除吗？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="materials.length === 0 && !loading" description="暂无资料" />
    </el-card>

    <!-- 文件夹视图 -->
    <div v-if="viewMode === 'grid'" class="grid-container" v-loading="loading">
      <!-- 空状态 -->
      <el-empty v-if="materials.length === 0 && !loading" description="暂无资料" />

      <!-- 文件夹 -->
      <template v-if="folders.length > 0">
        <div class="grid-section-title">文件夹</div>
        <div class="grid-items">
          <div
            v-for="item in folders"
            :key="item.id"
            class="grid-item folder-item"
            :style="item.color ? { '--folder-color': item.color, borderColor: item.color, borderWidth: '2px' } : {}"
            draggable="true"
            @dragstart="handleDragStart($event, item)"
            @dragend="handleDragEnd"
            @dragover="handleDragOver($event, item)"
            @dragleave="handleDragLeave"
            @drop="handleDrop($event, item)"
            @click="handleClick(item)"
            @contextmenu.prevent="openContextMenu($event, item)"
          >
            <div class="grid-item-icon">
              <el-icon :size="48" :color="item.color || '#409EFF'"><Folder /></el-icon>
            </div>
            <div class="grid-item-name">{{ item.title }}</div>
            <div class="grid-item-meta">
              <span>{{ item.childCount || 0 }} 个文件</span>
              <span>{{ new Date(item.updatedAt).toLocaleDateString('zh-CN') }}</span>
            </div>
            <!-- 置顶图标 -->
            <div v-if="item.isPinned" class="grid-pinned-icon">
              <el-icon :size="14" color="#E6A23C"><Top /></el-icon>
            </div>
            <!-- 收藏按钮 -->
            <div class="grid-favorite-icon" :class="{ 'is-active': item.isFavorite }" @click.stop="handleFavorite(item)">
              <el-icon :size="16" :color="item.isFavorite ? '#E6A23C' : '#c0c4cc'">
                <component :is="item.isFavorite ? StarFilled : Star" />
              </el-icon>
            </div>
            <!-- 操作按钮 -->
            <div class="grid-item-actions" @click.stop>
              <el-dropdown trigger="click">
                <el-icon :size="16"><MoreFilled /></el-icon>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="handlePinned(item)">
                      {{ item.isPinned ? '取消置顶' : '置顶' }}
                    </el-dropdown-item>
                    <el-dropdown-item @click="showEditForm(item)">编辑信息</el-dropdown-item>
                    <el-dropdown-item @click="handleDelete(item.id)">
                      <span style="color: #f56c6c">删除</span>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>
      </template>

      <!-- 文件 -->
      <template v-if="files.length > 0">
        <div class="grid-section-title">文件</div>
        <div class="grid-items">
          <div
            v-for="item in files"
            :key="item.id"
            class="grid-item file-item"
            draggable="true"
            @dragstart="handleDragStart($event, item)"
            @dragend="handleDragEnd"
            @contextmenu.prevent="openContextMenu($event, item)"
          >
            <div class="grid-item-icon file-icon" :style="{ backgroundColor: fileBgColor[item.fileType] || '#f5f5f5' }">
              <el-icon :size="36" :color="fileTypeColor[item.fileType] || '#909399'">
                <Document />
              </el-icon>
              <span class="file-ext">{{ item.fileType?.toUpperCase() }}</span>
            </div>
            <div class="grid-item-name">{{ item.title }}</div>
            <div class="grid-item-meta">
              <span>{{ formatSize(item.fileSize) }}</span>
              <span>{{ new Date(item.updatedAt).toLocaleDateString('zh-CN') }}</span>
            </div>
            <!-- 标签 -->
            <div class="grid-item-tags" v-if="item.tags?.length">
              <el-tag v-for="tag in item.tags.slice(0, 2)" :key="tag" size="small" type="info">
                {{ tag }}
              </el-tag>
            </div>
            <!-- 置顶图标 -->
            <div v-if="item.isPinned" class="grid-pinned-icon">
              <el-icon :size="14" color="#E6A23C"><Top /></el-icon>
            </div>
            <!-- 收藏按钮 -->
            <div class="grid-favorite-icon" :class="{ 'is-active': item.isFavorite }" @click.stop="handleFavorite(item)">
              <el-icon :size="16" :color="item.isFavorite ? '#E6A23C' : '#c0c4cc'">
                <component :is="item.isFavorite ? StarFilled : Star" />
              </el-icon>
            </div>
            <!-- 操作按钮 -->
            <div class="grid-item-actions" @click.stop>
              <el-dropdown trigger="click">
                <el-icon :size="16"><MoreFilled /></el-icon>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item v-if="!item.isFolder" @click="handlePreview(item)">预览</el-dropdown-item>
                    <el-dropdown-item v-if="isEditable(item)" @click="openEditor(item)">在线编辑</el-dropdown-item>
                    <el-dropdown-item @click="handlePinned(item)">
                      {{ item.isPinned ? '取消置顶' : '置顶' }}
                    </el-dropdown-item>
                    <el-dropdown-item @click="showEditForm(item)">编辑信息</el-dropdown-item>
                    <el-dropdown-item @click="handleDelete(item.id)">
                      <span style="color: #f56c6c">删除</span>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>
      </template>
    </div>

    <!-- 分栏视图 (macOS Finder 风格) -->
    <div v-if="viewMode === 'columns'" class="columns-container" v-loading="loading">
      <div class="columns-wrapper">
        <!-- 根栏 -->
        <div class="column-panel">
          <div class="column-header">
            <span class="column-title">全部资料</span>
          </div>
          <div
            class="column-content"
            @dragover.prevent="handleDragOver($event, { id: null, isFolder: true } as any)"
            @drop="handleDropToRoot"
          >
            <div
              v-for="item in rootMaterials"
              :key="item.id"
              class="column-item"
              :class="{
                'is-folder': item.isFolder,
                'is-active': item.isFolder && columnPath.includes(item.id),
                'is-file': !item.isFolder,
                'is-pinned': item.isPinned
              }"
              draggable="true"
              @dragstart="handleDragStart($event, item)"
              @dragend="handleDragEnd"
              @dragover="item.isFolder ? handleDragOver($event, item) : null"
              @dragleave="item.isFolder ? handleDragLeave : null"
              @drop="item.isFolder ? handleDrop($event, item) : null"
              @click="handleColumnClick(item, -1)"
              @contextmenu.prevent="openContextMenu($event, item)"
            >
              <div class="column-item-left">
                <el-icon v-if="item.isPinned" :size="12" color="#E6A23C"><Top /></el-icon>
                <el-icon :size="16" :color="item.isFolder ? (item.color || '#409EFF') : (fileTypeColor[item.fileType] || '#909399')">
                  <component :is="item.isFolder ? Folder : Document" />
                </el-icon>
                <span class="column-item-name">{{ item.title }}</span>
              </div>
              <div class="column-item-right">
                <span v-if="!item.isFolder" class="column-item-size">{{ formatSize(item.fileSize) }}</span>
                <el-icon v-if="item.isFolder" :size="12" color="#c0c4cc"><ArrowRight /></el-icon>
                <el-dropdown trigger="click" @click.stop>
                  <el-icon :size="14" class="column-item-more"><MoreFilled /></el-icon>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item v-if="!item.isFolder" @click="handlePreview(item)">预览</el-dropdown-item>
                      <el-dropdown-item v-if="isEditable(item)" @click="openEditor(item)">在线编辑</el-dropdown-item>
                      <el-dropdown-item @click="handlePinned(item)">
                        {{ item.isPinned ? '取消置顶' : '置顶' }}
                      </el-dropdown-item>
                      <el-dropdown-item @click="handleFavorite(item)">
                        {{ item.isFavorite ? '取消收藏' : '收藏' }}
                      </el-dropdown-item>
                      <el-dropdown-item @click="showEditForm(item)">编辑信息</el-dropdown-item>
                      <el-dropdown-item @click="handleDelete(item.id)">
                        <span style="color: #f56c6c">删除</span>
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
            <el-empty v-if="rootMaterials.length === 0 && !loading" :image-size="48" description="暂无资料" />
          </div>
        </div>

        <!-- 子栏 -->
        <div
          v-for="(col, index) in columns"
          :key="col.parentId"
          class="column-panel"
        >
          <div class="column-header">
            <span class="column-title">{{ col.title }}</span>
            <span class="column-count">{{ col.items.length }} 项</span>
          </div>
          <div class="column-content">
            <div
              v-for="item in col.items"
              :key="item.id"
              class="column-item"
              :class="{
                'is-folder': item.isFolder,
                'is-active': item.isFolder && columnPath.includes(item.id),
                'is-file': !item.isFolder,
                'is-pinned': item.isPinned
              }"
              draggable="true"
              @dragstart="handleDragStart($event, item)"
              @dragend="handleDragEnd"
              @dragover="item.isFolder ? handleDragOver($event, item) : null"
              @dragleave="item.isFolder ? handleDragLeave : null"
              @drop="item.isFolder ? handleDrop($event, item) : null"
              @click="handleColumnClick(item, index)"
              @contextmenu.prevent="openContextMenu($event, item)"
            >
              <div class="column-item-left">
                <el-icon v-if="item.isPinned" :size="12" color="#E6A23C"><Top /></el-icon>
                <el-icon :size="16" :color="item.isFolder ? (item.color || '#409EFF') : (fileTypeColor[item.fileType] || '#909399')">
                  <component :is="item.isFolder ? Folder : Document" />
                </el-icon>
                <span class="column-item-name">{{ item.title }}</span>
              </div>
              <div class="column-item-right">
                <span v-if="!item.isFolder" class="column-item-size">{{ formatSize(item.fileSize) }}</span>
                <el-icon v-if="item.isFolder" :size="12" color="#c0c4cc"><ArrowRight /></el-icon>
                <el-dropdown trigger="click" @click.stop>
                  <el-icon :size="14" class="column-item-more"><MoreFilled /></el-icon>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item v-if="!item.isFolder" @click="handlePreview(item)">预览</el-dropdown-item>
                      <el-dropdown-item v-if="isEditable(item)" @click="openEditor(item)">在线编辑</el-dropdown-item>
                      <el-dropdown-item @click="handlePinned(item)">
                        {{ item.isPinned ? '取消置顶' : '置顶' }}
                      </el-dropdown-item>
                      <el-dropdown-item @click="handleFavorite(item)">
                        {{ item.isFavorite ? '取消收藏' : '收藏' }}
                      </el-dropdown-item>
                      <el-dropdown-item @click="showEditForm(item)">编辑信息</el-dropdown-item>
                      <el-dropdown-item @click="handleDelete(item.id)">
                        <span style="color: #f56c6c">删除</span>
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
            <el-empty v-if="col.items.length === 0 && !loading" :image-size="48" description="空文件夹" />
          </div>
        </div>
      </div>
    </div>

    <!-- 资料表单弹窗 -->
    <MaterialForm
      :visible="formVisible"
      :material="currentMaterial"
      :is-folder="isFolderForm"
      :parent-id="currentParentId"
      @close="formVisible = false"
      @submit="handleFormSubmit"
    />

    <!-- 文件预览弹窗 -->
    <FilePreview
      :visible="previewVisible"
      :url="previewUrl"
      :title="previewTitle"
      :file-type="previewType"
      @close="previewVisible = false"
    />

    <!-- OnlyOffice 编辑器弹窗 -->
    <OnlyOfficeEditor
      v-if="editorVisible"
      :visible="editorVisible"
      :material-id="editorMaterialId!"
      :title="editorTitle"
      @close="handleEditorClose"
      @saved="handleEditorSaved"
    />

    <!-- 右键菜单 -->
    <ContextMenu
      :visible="contextMenuVisible"
      :x="contextMenuX"
      :y="contextMenuY"
      :material="contextMenuMaterial"
      @close="contextMenuVisible = false"
      @action="handleContextMenuAction"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import {
  Search, FolderAdd, Upload, Star, StarFilled, Folder, Document,
  Grid, List, MoreFilled, Top, Operation, ArrowRight,
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import {
  getMaterials,
  createMaterial,
  updateMaterial,
  deleteMaterial,
  toggleFavorite,
  togglePinned,
  moveMaterial,
  uploadMaterial,
  getPreviewUrl,
} from '@/api/material'
import type { Material, MaterialRequest } from '@/api/material'
import MaterialForm from './components/MaterialForm.vue'
import FilePreview from './components/FilePreview.vue'
import OnlyOfficeEditor from './components/OnlyOfficeEditor.vue'
import ContextMenu from './components/ContextMenu.vue'

const loading = ref(false)
const materials = ref<Material[]>([])
const currentParentId = ref<number | null>(null)
const breadcrumb = ref<{ id: number; title: string }[]>([])
const keyword = ref('')
const showFavorite = ref(false)
const formVisible = ref(false)
const isFolderForm = ref(false)
const currentMaterial = ref<Material | null>(null)
const viewMode = ref<'grid' | 'list' | 'columns'>('grid')
const previewVisible = ref(false)
const previewUrl = ref('')
const previewTitle = ref('')
const previewType = ref('')
const editorVisible = ref(false)
const editorMaterialId = ref<number | null>(null)
const editorTitle = ref('')

// 右键菜单状态
const contextMenuVisible = ref(false)
const contextMenuX = ref(0)
const contextMenuY = ref(0)
const contextMenuMaterial = ref<Material | null>(null)

// 拖拽状态
const dragSource = ref<Material | null>(null)
const dragOverTarget = ref<number | null>(null)

// 分栏视图相关状态
const columnPath = ref<number[]>([])
const columns = ref<{ parentId: number; title: string; items: Material[] }[]>([])
const columnMaterialsCache = ref<Map<number, Material[]>>(new Map())
const columnSelectedFolderId = ref<number | null>(null) // 分栏模式下当前选中的文件夹

// 文件夹和文件分开（用于网格视图）
const folders = computed(() => materials.value.filter(m => m.isFolder))
const files = computed(() => materials.value.filter(m => !m.isFolder))

// 分栏视图：根目录资料
const rootMaterials = computed(() => materials.value)

const fileTypeColor: Record<string, string> = {
  pdf: '#f56c6c',
  docx: '#409eff',
  xlsx: '#67c23a',
  pptx: '#e6a23c',
  jpg: '#e6a23c',
  png: '#e6a23c',
}

const fileBgColor: Record<string, string> = {
  pdf: '#fef0f0',
  docx: '#ecf5ff',
  xlsx: '#f0f9eb',
  pptx: '#fdf6ec',
  jpg: '#fdf6ec',
  png: '#fdf6ec',
}

function formatSize(bytes: number | null) {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

async function fetchMaterials() {
  loading.value = true
  try {
    const params: any = {
      parentId: currentParentId.value,
      size: 100,
    }
    if (keyword.value) params.keyword = keyword.value
    if (showFavorite.value) params.isFavorite = true

    const res = await getMaterials(params)
    materials.value = res.data.records
  } catch (error) {
    // handled
  } finally {
    loading.value = false
  }
}

// 分栏视图：获取子目录资料
async function fetchColumnMaterials(parentId: number): Promise<Material[]> {
  // 先检查缓存
  if (columnMaterialsCache.value.has(parentId)) {
    return columnMaterialsCache.value.get(parentId)!
  }

  try {
    const res = await getMaterials({ parentId, size: 100 })
    const items = res.data.records
    columnMaterialsCache.value.set(parentId, items)
    return items
  } catch (error) {
    return []
  }
}

// 分栏视图：点击处理
async function handleColumnClick(item: Material, columnIndex: number) {
  if (item.isFolder) {
    // 点击文件夹：展开子栏，并记录当前选中的文件夹
    columnSelectedFolderId.value = item.id

    const newPath = columnIndex < 0 ? [] : columnPath.value.slice(0, columnIndex + 1)
    newPath.push(item.id)
    columnPath.value = newPath

    // 加载子目录内容
    const items = await fetchColumnMaterials(item.id)

    // 更新列：移除点击列之后的所有列，然后添加新列
    const newColumns = columnIndex < 0 ? [] : columns.value.slice(0, columnIndex + 1)
    newColumns.push({
      parentId: item.id,
      title: item.title,
      items
    })
    columns.value = newColumns
  } else {
    // 点击文件：打开预览
    handlePreview(item)
  }
}

function handleSearch() {
  fetchMaterials()
}

function navigateToRoot() {
  currentParentId.value = null
  breadcrumb.value = []
  fetchMaterials()
}

function navigateToFolder(id: number) {
  const index = breadcrumb.value.findIndex(f => f.id === id)
  if (index >= 0) {
    breadcrumb.value = breadcrumb.value.slice(0, index + 1)
  }
  currentParentId.value = id
  fetchMaterials()
}

function handleClick(material: Material) {
  if (material.isFolder) {
    currentParentId.value = material.id
    breadcrumb.value.push({ id: material.id, title: material.title })
    fetchMaterials()
  }
}

function toggleFavoriteView() {
  showFavorite.value = !showFavorite.value
  fetchMaterials()
}

async function handleFavorite(material: Material) {
  try {
    await toggleFavorite(material.id)
    material.isFavorite = !material.isFavorite
    ElMessage.success(material.isFavorite ? '已收藏' : '已取消收藏')
  } catch (error) {
    // handled
  }
}

async function handlePinned(material: Material) {
  try {
    await togglePinned(material.id)
    material.isPinned = !material.isPinned
    ElMessage.success(material.isPinned ? '已置顶' : '已取消置顶')
    fetchMaterials()
  } catch (error) {
    // handled
  }
}

function showFolderForm() {
  currentMaterial.value = null
  isFolderForm.value = true
  formVisible.value = true
}

function showUploadForm() {
  currentMaterial.value = null
  isFolderForm.value = false
  formVisible.value = true
}

function showEditForm(material: Material) {
  currentMaterial.value = material
  isFolderForm.value = material.isFolder
  formVisible.value = true
}

async function handleFormSubmit(data: MaterialRequest & { file?: File }) {
  try {
    // 分栏模式下，设置正确的 parentId
    const submitData = { ...data }
    if (viewMode.value === 'columns' && columnSelectedFolderId.value) {
      submitData.parentId = columnSelectedFolderId.value
    }

    if (currentMaterial.value) {
      await updateMaterial(currentMaterial.value.id, submitData)
      ElMessage.success('已更新')
    } else if (submitData.file) {
      // 有文件时用上传接口
      const tagsStr = submitData.tags?.length ? submitData.tags.join(',') : undefined
      await uploadMaterial(submitData.file, {
        title: submitData.title,
        subject: submitData.subject,
        grade: submitData.grade,
        tags: tagsStr,
        parentId: submitData.parentId,
      })
      ElMessage.success('资料已上传')
    } else {
      await createMaterial(submitData)
      ElMessage.success('文件夹已创建')
    }
    formVisible.value = false

    // 刷新列表（只在非分栏模式下刷新根目录）
    if (viewMode.value !== 'columns') {
      fetchMaterials()
    }

    // 分栏模式下，清除缓存并刷新当前列
    if (viewMode.value === 'columns') {
      // 只清除当前选中文件夹的缓存
      if (columnSelectedFolderId.value) {
        columnMaterialsCache.value.delete(columnSelectedFolderId.value)
        const items = await fetchColumnMaterials(columnSelectedFolderId.value)
        const lastColumn = columns.value[columns.value.length - 1]
        if (lastColumn && lastColumn.parentId === columnSelectedFolderId.value) {
          lastColumn.items = items
        }
      }
    }
  } catch (error) {
    // handled
  }
}

// 预览文件
async function handlePreview(material: Material) {
  if (material.isFolder) return
  const type = material.fileType?.toLowerCase() || ''
  if (!['pdf', 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'jpg', 'jpeg', 'png', 'gif'].includes(type)) {
    ElMessage.warning('该文件类型暂不支持预览')
    return
  }
  try {
    const res = await getPreviewUrl(material.id)
    previewUrl.value = res.data
    previewTitle.value = material.title
    previewType.value = type
    previewVisible.value = true
  } catch (error) {
    ElMessage.error('获取预览链接失败')
  }
}

// 可编辑的文件类型
const editableTypes = ['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx']
function isEditable(material: Material) {
  return !material.isFolder && editableTypes.includes(material.fileType?.toLowerCase() || '')
}

// 打开 OnlyOffice 编辑器
function openEditor(material: Material) {
  editorMaterialId.value = material.id
  editorTitle.value = '编辑：' + material.title
  editorVisible.value = true
}

function handleEditorClose() {
  editorVisible.value = false
  editorMaterialId.value = null
}

function handleEditorSaved() {
  fetchMaterials()
}

async function handleDelete(id: number) {
  try {
    await deleteMaterial(id)
    ElMessage.success('已删除')
    fetchMaterials()
  } catch (error) {
    // handled
  }
}

// 打开右键菜单
function openContextMenu(e: MouseEvent, item: Material) {
  e.preventDefault()
  contextMenuMaterial.value = item
  contextMenuX.value = e.clientX
  contextMenuY.value = e.clientY
  contextMenuVisible.value = true
}

// 处理右键菜单操作
async function handleContextMenuAction(action: string, material: Material) {
  switch (action) {
    case 'open':
      if (material.isFolder) {
        handleClick(material)
      }
      break
    case 'preview':
      handlePreview(material)
      break
    case 'edit':
      openEditor(material)
      break
    case 'download':
      // 下载文件
      window.open(`/api/v1/materials/${material.id}/download`, '_blank')
      break
    case 'rename':
      showEditForm(material)
      break
    case 'color':
      showEditForm(material)
      break
    case 'pin':
      await handlePinned(material)
      break
    case 'favorite':
      await handleFavorite(material)
      break
    case 'assign':
      // TODO: 打开分配学生对话框
      ElMessage.info('分配学生功能开发中')
      break
    case 'copy':
      ElMessage.info('复制功能开发中')
      break
    case 'move':
      ElMessage.info('移动功能开发中')
      break
    case 'info':
      showEditForm(material)
      break
    case 'createSubfolder':
      currentMaterial.value = null
      isFolderForm.value = true
      formVisible.value = true
      if (material.isFolder) {
        currentParentId.value = material.id
      }
      break
    case 'upload':
      currentMaterial.value = null
      isFolderForm.value = false
      formVisible.value = true
      if (material.isFolder) {
        currentParentId.value = material.id
      }
      break
    case 'delete':
      await handleDelete(material.id)
      break
  }
}

// 拖拽处理函数
function handleDragStart(e: DragEvent, material: Material) {
  dragSource.value = material
  if (e.dataTransfer) {
    e.dataTransfer.effectAllowed = 'move'
    e.dataTransfer.setData('text/plain', String(material.id))
  }
  // 添加拖拽样式
  const target = e.target as HTMLElement
  target.classList.add('dragging')
}

function handleDragEnd(e: DragEvent) {
  dragSource.value = null
  dragOverTarget.value = null
  // 移除所有拖拽样式
  document.querySelectorAll('.dragging').forEach(el => el.classList.remove('dragging'))
  document.querySelectorAll('.drag-over').forEach(el => el.classList.remove('drag-over'))
}

function handleDragOver(e: DragEvent, material: Material) {
  e.preventDefault()
  if (!dragSource.value || !material.isFolder) return
  if (dragSource.value.id === material.id) return

  e.dataTransfer!.dropEffect = 'move'
  dragOverTarget.value = material.id

  // 添加拖拽悬停样式
  const target = e.target as HTMLElement
  target.closest('.grid-item, .column-item')?.classList.add('drag-over')
}

function handleDragLeave(e: DragEvent) {
  const target = e.target as HTMLElement
  target.closest('.grid-item, .column-item')?.classList.remove('drag-over')
}

async function handleDrop(e: DragEvent, targetMaterial: Material) {
  e.preventDefault()
  e.stopPropagation()
  dragOverTarget.value = null

  const source = dragSource.value
  if (!source || !targetMaterial.isFolder) return
  if (source.id === targetMaterial.id) return

  // 移除样式
  document.querySelectorAll('.drag-over').forEach(el => el.classList.remove('drag-over'))
  document.querySelectorAll('.dragging').forEach(el => el.classList.remove('dragging'))

  dragSource.value = null

  try {
    await moveMaterial(source.id, targetMaterial.id)
    ElMessage.success(`已移动 "${source.title}" 到 "${targetMaterial.title}"`)

    // 刷新列表
    await fetchMaterials()

    // 分栏模式下刷新
    if (viewMode.value === 'columns') {
      columnMaterialsCache.value.clear()
      if (columnSelectedFolderId.value) {
        const items = await fetchColumnMaterials(columnSelectedFolderId.value)
        const lastColumn = columns.value[columns.value.length - 1]
        if (lastColumn && lastColumn.parentId === columnSelectedFolderId.value) {
          lastColumn.items = items
        }
      }
    }
  } catch (error: any) {
    console.error('移动失败:', error)
    ElMessage.error(error?.message || '移动失败')
  }
}

// 拖拽到根目录
async function handleDropToRoot(e: DragEvent) {
  e.preventDefault()
  e.stopPropagation()
  dragOverTarget.value = null

  const source = dragSource.value
  if (!source) return
  if (!source.parentId) return // 已经在根目录

  document.querySelectorAll('.drag-over').forEach(el => el.classList.remove('drag-over'))
  document.querySelectorAll('.dragging').forEach(el => el.classList.remove('dragging'))

  dragSource.value = null

  try {
    await moveMaterial(source.id, null)
    ElMessage.success(`已移动 "${source.title}" 到根目录`)
    await fetchMaterials()

    if (viewMode.value === 'columns') {
      columnMaterialsCache.value.clear()
    }
  } catch (error: any) {
    console.error('移动失败:', error)
    ElMessage.error(error?.message || '移动失败')
  }
}

onMounted(() => {
  fetchMaterials()
})
</script>

<style scoped>
.name-cell {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.name-cell:hover {
  color: #409eff;
}

/* 网格视图 */
.grid-container {
  margin-top: 16px;
  min-height: 200px;
}

.grid-section-title {
  font-size: 13px;
  color: #909399;
  margin-bottom: 12px;
  padding-left: 4px;
}

.grid-items {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.grid-item {
  position: relative;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 16px 12px 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  transition: all 0.2s;
}

.grid-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.15);
}

.folder-item {
  position: relative;
  overflow: hidden;
}

.folder-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background-color: var(--folder-color, transparent);
}

.grid-item:hover .grid-item-actions {
  opacity: 1;
}

.grid-item-icon {
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.file-icon {
  width: 72px;
  height: 72px;
  border-radius: 8px;
  flex-direction: column;
  gap: 2px;
  position: relative;
}

.file-ext {
  font-size: 11px;
  font-weight: 600;
  color: #606266;
  position: absolute;
  bottom: 6px;
}

.grid-item-name {
  font-size: 13px;
  color: #303133;
  text-align: center;
  width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.4;
}

.grid-item-meta {
  font-size: 11px;
  color: #c0c4cc;
  margin-top: 4px;
  display: flex;
  gap: 8px;
}

.grid-item-tags {
  display: flex;
  gap: 4px;
  margin-top: 6px;
  flex-wrap: wrap;
  justify-content: center;
}

.grid-favorite-icon {
  position: absolute;
  top: 8px;
  right: 8px;
  cursor: pointer;
  padding: 2px;
  border-radius: 4px;
  transition: all 0.2s;
  opacity: 0;
}

.grid-item:hover .grid-favorite-icon {
  opacity: 1;
}

.grid-favorite-icon:hover {
  background: rgba(0, 0, 0, 0.06);
  transform: scale(1.2);
}

/* 已收藏的星标始终可见 */
.grid-item .grid-favorite-icon.is-active {
  opacity: 1;
}

.grid-item-actions {
  position: absolute;
  top: 8px;
  right: 32px;
  opacity: 0;
  transition: opacity 0.2s;
  cursor: pointer;
  color: #909399;
}

.grid-item-actions:hover {
  color: #409eff;
}

.grid-pinned-icon {
  position: absolute;
  top: 8px;
  left: 8px;
  cursor: pointer;
  padding: 2px;
  border-radius: 4px;
  background: rgba(230, 162, 60, 0.1);
}

/* 分栏视图 (macOS Finder 风格) */
.columns-container {
  margin-top: 16px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
  min-height: 400px;
}

.columns-wrapper {
  display: flex;
  height: 500px;
  overflow-x: auto;
}

.column-panel {
  min-width: 240px;
  max-width: 300px;
  border-right: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.column-panel:last-child {
  border-right: none;
  flex: 1;
  min-width: 280px;
}

.column-header {
  padding: 10px 16px;
  background: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.column-title {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.column-count {
  font-size: 11px;
  color: #909399;
}

.column-content {
  flex: 1;
  overflow-y: auto;
  padding: 4px 0;
}

.column-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 16px;
  cursor: pointer;
  transition: background-color 0.15s;
}

.column-item:hover {
  background-color: #f5f7fa;
}

.column-item.is-active {
  background-color: #ecf5ff;
}

.column-item.is-folder {
  font-weight: 500;
}

.column-item.is-pinned {
  background-color: #fdf6ec;
}

.column-item.is-pinned:hover {
  background-color: #faecd8;
}

.column-item-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}

.column-item-name {
  font-size: 13px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.column-item-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.column-item-size {
  font-size: 11px;
  color: #909399;
}

.column-item-more {
  opacity: 0;
  color: #909399;
  cursor: pointer;
  transition: opacity 0.15s;
}

.column-item:hover .column-item-more {
  opacity: 1;
}

.column-item-more:hover {
  color: #409eff;
}

/* 分栏视图滚动条样式 */
.columns-wrapper::-webkit-scrollbar {
  height: 6px;
}

.columns-wrapper::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 3px;
}

.columns-wrapper::-webkit-scrollbar-track {
  background: #f5f7fa;
}

.column-content::-webkit-scrollbar {
  width: 6px;
}

.column-content::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 3px;
}

.column-content::-webkit-scrollbar-track {
  background: #f5f7fa;
}

/* 拖拽样式 */
.grid-item.dragging,
.column-item.dragging {
  opacity: 0.5;
  transform: scale(0.95);
}

.grid-item.drag-over,
.column-item.drag-over {
  border-color: #409eff !important;
  background-color: #ecf5ff !important;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.grid-item[draggable="true"],
.column-item[draggable="true"] {
  cursor: grab;
}

.grid-item[draggable="true"]:active,
.column-item[draggable="true"]:active {
  cursor: grabbing;
}
</style>
