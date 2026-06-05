<template>
  <div class="student-list">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="姓名">
          <el-input v-model="query.name" placeholder="搜索学生姓名" clearable @clear="handleSearch" />
        </el-form-item>
        <el-form-item label="年级">
          <el-select v-model="query.grade" placeholder="全部年级" clearable @clear="handleSearch">
            <el-option-group label="小学">
              <el-option v-for="i in 6" :key="'P'+i" :label="`小学${i}年级`" :value="`PRIMARY_${i}`" />
            </el-option-group>
            <el-option-group label="初中">
              <el-option v-for="i in 3" :key="'J'+i" :label="`初${i}`" :value="`JUNIOR_${i}`" />
            </el-option-group>
            <el-option-group label="高中">
              <el-option v-for="i in 3" :key="'S'+i" :label="`高${i}`" :value="`SENIOR_${i}`" />
            </el-option-group>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部状态" clearable @clear="handleSearch">
            <el-option label="在读" value="ACTIVE" />
            <el-option label="暂停" value="PAUSED" />
            <el-option label="毕业" value="GRADUATED" />
            <el-option label="流失" value="LOST" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button type="success" @click="showAddForm">
            <el-icon><Plus /></el-icon>新增学生
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 学生列表 -->
    <el-card style="margin-top: 16px">
      <el-table :data="students" v-loading="loading" stripe>
        <el-table-column prop="name" label="姓名" width="100">
          <template #default="{ row }">
            <el-link type="primary" @click="goToDetail(row.id)">{{ row.name }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="gender" label="性别" width="70">
          <template #default="{ row }">
            {{ genderMap[row.gender] || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="grade" label="年级" width="100">
          <template #default="{ row }">
            {{ gradeMap[row.grade] || row.grade }}
          </template>
        </el-table-column>
        <el-table-column prop="subjects" label="科目" min-width="150">
          <template #default="{ row }">
            <el-tag v-for="s in row.subjects" :key="s" size="small" style="margin-right: 4px">
              {{ s }}
            </el-tag>
            <span v-if="!row.subjects?.length">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="parentName" label="家长" width="90" />
        <el-table-column prop="parentPhone" label="家长电话" width="120" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="statusTagType[row.status]" size="small">
              {{ statusMap[row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="enrollmentDate" label="入学日期" width="110" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="showEditForm(row)">编辑</el-button>
            <el-button type="primary" link @click="goToDetail(row.id)">详情</el-button>
            <el-popconfirm title="确定删除该学生吗？" @confirm="handleDelete(row.id)">
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

    <!-- 新增/编辑弹窗 -->
    <StudentForm
      :visible="formVisible"
      :student="currentStudent"
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
import { getStudents, createStudent, updateStudent, deleteStudent } from '@/api/student'
import type { Student, StudentQuery, StudentRequest } from '@/api/student'
import StudentForm from './components/StudentForm.vue'

const router = useRouter()
const loading = ref(false)
const students = ref<Student[]>([])
const total = ref(0)
const formVisible = ref(false)
const currentStudent = ref<Student | null>(null)

const query = reactive<StudentQuery>({
  name: '',
  grade: '',
  status: '',
  page: 1,
  size: 20,
})

const genderMap: Record<string, string> = {
  MALE: '男',
  FEMALE: '女',
  OTHER: '其他',
}

const gradeMap: Record<string, string> = {
  PRIMARY_1: '小学1年级', PRIMARY_2: '小学2年级', PRIMARY_3: '小学3年级',
  PRIMARY_4: '小学4年级', PRIMARY_5: '小学5年级', PRIMARY_6: '小学6年级',
  JUNIOR_1: '初一', JUNIOR_2: '初二', JUNIOR_3: '初三',
  SENIOR_1: '高一', SENIOR_2: '高二', SENIOR_3: '高三',
}

const statusMap: Record<string, string> = {
  ACTIVE: '在读',
  PAUSED: '暂停',
  GRADUATED: '毕业',
  LOST: '流失',
}

const statusTagType: Record<string, string> = {
  ACTIVE: 'success',
  PAUSED: 'warning',
  GRADUATED: 'info',
  LOST: 'danger',
}

async function fetchStudents() {
  loading.value = true
  try {
    const res = await getStudents(query)
    students.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  fetchStudents()
}

function showAddForm() {
  currentStudent.value = null
  formVisible.value = true
}

function showEditForm(student: Student) {
  currentStudent.value = student
  formVisible.value = true
}

async function handleFormSubmit(data: StudentRequest) {
  try {
    if (currentStudent.value) {
      await updateStudent(currentStudent.value.id, data)
      ElMessage.success('学生信息已更新')
    } else {
      await createStudent(data)
      ElMessage.success('学生已创建')
    }
    formVisible.value = false
    fetchStudents()
  } catch (error) {
    // 错误已在拦截器中处理
  }
}

async function handleDelete(id: number) {
  try {
    await deleteStudent(id)
    ElMessage.success('学生已删除')
    fetchStudents()
  } catch (error) {
    // 错误已在拦截器中处理
  }
}

function goToDetail(id: number) {
  router.push(`/students/${id}`)
}

onMounted(() => {
  fetchStudents()
})
</script>

<style scoped>
.search-card :deep(.el-card__body) {
  padding-bottom: 0;
}
</style>
