<template>
  <div class="homework-list">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部状态" clearable @clear="handleSearch">
            <el-option label="未提交" value="PENDING" />
            <el-option label="已提交" value="SUBMITTED" />
            <el-option label="已批改" value="GRADED" />
            <el-option label="已查看" value="REVIEWED" />
          </el-select>
        </el-form-item>
        <el-form-item label="科目">
          <el-select v-model="query.subject" placeholder="全部科目" clearable @clear="handleSearch">
            <el-option label="语文" value="语文" />
            <el-option label="数学" value="数学" />
            <el-option label="英语" value="英语" />
            <el-option label="物理" value="物理" />
            <el-option label="化学" value="化学" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button type="success" @click="showAddForm">
            <el-icon><Plus /></el-icon>布置作业
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 作业列表 -->
    <el-card style="margin-top: 16px">
      <el-table :data="homeworks" v-loading="loading" stripe>
        <el-table-column prop="title" label="作业标题" min-width="150">
          <template #default="{ row }">
            <el-link type="primary" @click="goToDetail(row.id)">{{ row.title }}</el-link>
          </template>
        </el-table-column>
        <el-table-column label="布置对象" width="120">
          <template #default="{ row }">
            {{ row.studentName || row.className || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="subject" label="科目" width="80" />
        <el-table-column prop="dueDate" label="截止日期" width="110" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusTagType[row.status]" size="small">
              {{ statusMap[row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="分数" width="80">
          <template #default="{ row }">
            {{ row.score || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="submissionCount" label="提交数" width="80" />
        <el-table-column prop="createdAt" label="创建时间" width="160">
          <template #default="{ row }">
            {{ new Date(row.createdAt).toLocaleString('zh-CN') }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="goToDetail(row.id)">详情</el-button>
            <el-button type="primary" link @click="showEditForm(row)">编辑</el-button>
            <el-popconfirm title="确定删除该作业吗？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0"
        style="margin-top: 16px; justify-content: flex-end"
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSearch"
        @current-change="handleSearch"
      />
    </el-card>

    <!-- 作业表单弹窗 -->
    <HomeworkForm
      :visible="formVisible"
      :homework="currentHomework"
      @close="formVisible = false"
      @submit="handleFormSubmit"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getHomeworks, createHomework, updateHomework, deleteHomework } from '@/api/homework'
import type { Homework, HomeworkQuery, HomeworkRequest } from '@/api/homework'
import HomeworkForm from './components/HomeworkForm.vue'

const router = useRouter()
const loading = ref(false)
const homeworks = ref<Homework[]>([])
const total = ref(0)
const formVisible = ref(false)
const currentHomework = ref<Homework | null>(null)

const query = reactive<HomeworkQuery>({
  status: '',
  subject: '',
  page: 1,
  size: 20,
})

const statusMap: Record<string, string> = {
  PENDING: '未提交',
  SUBMITTED: '已提交',
  GRADED: '已批改',
  REVIEWED: '已查看',
}

const statusTagType: Record<string, string> = {
  PENDING: 'info',
  SUBMITTED: 'warning',
  GRADED: 'success',
  REVIEWED: '',
}

async function fetchHomeworks() {
  loading.value = true
  try {
    const res = await getHomeworks(query)
    homeworks.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    // handled
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  fetchHomeworks()
}

function showAddForm() {
  currentHomework.value = null
  formVisible.value = true
}

function showEditForm(hw: Homework) {
  currentHomework.value = hw
  formVisible.value = true
}

async function handleFormSubmit(data: HomeworkRequest) {
  try {
    if (currentHomework.value) {
      await updateHomework(currentHomework.value.id, data)
      ElMessage.success('作业已更新')
    } else {
      await createHomework(data)
      ElMessage.success('作业已布置')
    }
    formVisible.value = false
    fetchHomeworks()
  } catch (error) {
    // handled
  }
}

async function handleDelete(id: number) {
  try {
    await deleteHomework(id)
    ElMessage.success('作业已删除')
    fetchHomeworks()
  } catch (error) {
    // handled
  }
}

function goToDetail(id: number) {
  router.push(`/homework/${id}`)
}

onMounted(() => {
  fetchHomeworks()
})
</script>

<style scoped>
.search-card :deep(.el-card__body) {
  padding-bottom: 0;
}
</style>
