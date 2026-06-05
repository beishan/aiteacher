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
        </div>
      </div>
    </el-card>

    <!-- 资料列表 -->
    <el-card style="margin-top: 16px">
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
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleFavorite(row)">
              {{ row.isFavorite ? '取消收藏' : '收藏' }}
            </el-button>
            <el-button type="primary" link @click="showEditForm(row)">编辑</el-button>
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

    <!-- 资料表单弹窗 -->
    <MaterialForm
      :visible="formVisible"
      :material="currentMaterial"
      :is-folder="isFolderForm"
      :parent-id="currentParentId"
      @close="formVisible = false"
      @submit="handleFormSubmit"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Search, FolderAdd, Upload, Star, StarFilled, Folder, Document } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import {
  getMaterials,
  createMaterial,
  updateMaterial,
  deleteMaterial,
  toggleFavorite,
} from '@/api/material'
import type { Material, MaterialRequest } from '@/api/material'
import MaterialForm from './components/MaterialForm.vue'

const loading = ref(false)
const materials = ref<Material[]>([])
const currentParentId = ref<number | null>(null)
const breadcrumb = ref<{ id: number; title: string }[]>([])
const keyword = ref('')
const showFavorite = ref(false)
const formVisible = ref(false)
const isFolderForm = ref(false)
const currentMaterial = ref<Material | null>(null)

const fileTypeColor: Record<string, string> = {
  pdf: '#f56c6c',
  docx: '#409eff',
  xlsx: '#67c23a',
  jpg: '#e6a23c',
  png: '#e6a23c',
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

async function handleFormSubmit(data: MaterialRequest) {
  try {
    if (currentMaterial.value) {
      await updateMaterial(currentMaterial.value.id, data)
      ElMessage.success('已更新')
    } else {
      await createMaterial(data)
      ElMessage.success(isFolderForm.value ? '文件夹已创建' : '资料已上传')
    }
    formVisible.value = false
    fetchMaterials()
  } catch (error) {
    // handled
  }
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
</style>
