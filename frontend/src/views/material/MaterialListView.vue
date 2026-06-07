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
          </el-button-group>
        </div>
      </div>
    </el-card>

    <!-- 列表视图 -->
    <el-card v-if="viewMode === 'list'" style="margin-top: 16px">
      <el-table :data="materials" v-loading="loading" stripe>
        <el-table-column prop="title" label="名称" min-width="200">
          <template #default="{ row }">
            <div class="name-cell" @click="handleClick(row)">
              <el-icon v-if="row.isFolder" :size="20" color="#409EFF"><Folder /></el-icon>
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
        <el-table-column label="操作" width="270" fixed="right">
          <template #default="{ row }">
            <el-button v-if="!row.isFolder" type="primary" link @click="handlePreview(row)">预览</el-button>
            <el-button v-if="isEditable(row)" type="success" link @click="openEditor(row)">在线编辑</el-button>
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
            @click="handleClick(item)"
            @contextmenu.prevent="openContextMenu($event, item)"
          >
            <div class="grid-item-icon">
              <el-icon :size="48" color="#409EFF"><Folder /></el-icon>
            </div>
            <div class="grid-item-name">{{ item.title }}</div>
            <div class="grid-item-meta">
              <span>{{ item.childCount || 0 }} 个文件</span>
              <span>{{ new Date(item.updatedAt).toLocaleDateString('zh-CN') }}</span>
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
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import {
  Search, FolderAdd, Upload, Star, StarFilled, Folder, Document,
  Grid, List, MoreFilled,
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import {
  getMaterials,
  createMaterial,
  updateMaterial,
  deleteMaterial,
  toggleFavorite,
  uploadMaterial,
  getPreviewUrl,
} from '@/api/material'
import type { Material, MaterialRequest } from '@/api/material'
import MaterialForm from './components/MaterialForm.vue'
import FilePreview from './components/FilePreview.vue'
import OnlyOfficeEditor from './components/OnlyOfficeEditor.vue'

const loading = ref(false)
const materials = ref<Material[]>([])
const currentParentId = ref<number | null>(null)
const breadcrumb = ref<{ id: number; title: string }[]>([])
const keyword = ref('')
const showFavorite = ref(false)
const formVisible = ref(false)
const isFolderForm = ref(false)
const currentMaterial = ref<Material | null>(null)
const viewMode = ref<'grid' | 'list'>('grid')
const previewVisible = ref(false)
const previewUrl = ref('')
const previewTitle = ref('')
const previewType = ref('')
const editorVisible = ref(false)
const editorMaterialId = ref<number | null>(null)
const editorTitle = ref('')

// 文件夹和文件分开（用于网格视图）
const folders = computed(() => materials.value.filter(m => m.isFolder))
const files = computed(() => materials.value.filter(m => !m.isFolder))

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
    if (currentMaterial.value) {
      await updateMaterial(currentMaterial.value.id, data)
      ElMessage.success('已更新')
    } else if (data.file) {
      // 有文件时用上传接口
      const tagsStr = data.tags?.length ? data.tags.join(',') : undefined
      await uploadMaterial(data.file, {
        title: data.title,
        subject: data.subject,
        grade: data.grade,
        tags: tagsStr,
        parentId: data.parentId,
      })
      ElMessage.success('资料已上传')
    } else {
      await createMaterial(data)
      ElMessage.success('文件夹已创建')
    }
    formVisible.value = false
    fetchMaterials()
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

function openContextMenu(_e: MouseEvent, _item: Material) {
  // 预留右键菜单扩展
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
  left: 8px;
  opacity: 0;
  transition: opacity 0.2s;
  cursor: pointer;
  color: #909399;
}

.grid-item-actions:hover {
  color: #409eff;
}
</style>
