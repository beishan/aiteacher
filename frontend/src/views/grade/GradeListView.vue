<template>
  <div class="grade-list">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="科目">
          <el-select v-model="query.subject" placeholder="全部科目" clearable @clear="handleSearch">
            <el-option label="语文" value="语文" />
            <el-option label="数学" value="数学" />
            <el-option label="英语" value="英语" />
            <el-option label="物理" value="物理" />
            <el-option label="化学" value="化学" />
          </el-select>
        </el-form-item>
        <el-form-item label="考试类型">
          <el-select v-model="query.examType" placeholder="全部类型" clearable @clear="handleSearch">
            <el-option label="月考" value="MONTHLY" />
            <el-option label="期中考试" value="MIDTERM" />
            <el-option label="期末考试" value="FINAL" />
            <el-option label="联考" value="JOINT" />
            <el-option label="模拟考试" value="MOCK" />
            <el-option label="自测" value="SELF_TEST" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button type="success" @click="showAddForm">
            <el-icon><Plus /></el-icon>录入成绩
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 成绩列表 -->
    <el-card style="margin-top: 16px">
      <el-table :data="exams" v-loading="loading" stripe>
        <el-table-column prop="studentName" label="学生" width="80" />
        <el-table-column prop="examName" label="考试名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="examType" label="类型" width="90">
          <template #default="{ row }">
            {{ examTypeMap[row.examType] || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="subject" label="科目" width="70" />
        <el-table-column prop="examDate" label="日期" width="100" />
        <el-table-column label="分数" width="120">
          <template #default="{ row }">
            <span :class="getScoreClass(row.percentage)">{{ row.score }}/{{ row.fullScore }}</span>
          </template>
        </el-table-column>
        <el-table-column label="得分率" width="80">
          <template #default="{ row }">
            <span :class="getScoreClass(row.percentage)">{{ row.percentage }}%</span>
          </template>
        </el-table-column>
        <el-table-column prop="classRank" label="班级排名" width="80">
          <template #default="{ row }">{{ row.classRank || '-' }}</template>
        </el-table-column>
        <el-table-column prop="gradeRank" label="年级排名" width="80">
          <template #default="{ row }">{{ row.gradeRank || '-' }}</template>
        </el-table-column>
        <el-table-column prop="analysis" label="分析" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="showEditForm(row)">编辑</el-button>
            <el-popconfirm title="确定删除该成绩吗？" @confirm="handleDelete(row.id)">
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

    <!-- 成绩表单弹窗 -->
    <GradeForm
      :visible="formVisible"
      :exam="currentExam"
      @close="formVisible = false"
      @submit="handleFormSubmit"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getExams, createExam, updateExam, deleteExam } from '@/api/exam'
import type { ExamRecord, ExamQuery, ExamRequest } from '@/api/exam'
import GradeForm from './components/GradeForm.vue'

const loading = ref(false)
const exams = ref<ExamRecord[]>([])
const total = ref(0)
const formVisible = ref(false)
const currentExam = ref<ExamRecord | null>(null)

const query = reactive<ExamQuery>({
  subject: '',
  examType: '',
  page: 1,
  size: 20,
})

const examTypeMap: Record<string, string> = {
  MONTHLY: '月考',
  MIDTERM: '期中',
  FINAL: '期末',
  JOINT: '联考',
  MOCK: '模拟',
  SELF_TEST: '自测',
}

function getScoreClass(percentage: number | null) {
  if (!percentage) return ''
  if (percentage >= 90) return 'score-excellent'
  if (percentage >= 80) return 'score-good'
  if (percentage >= 60) return 'score-pass'
  return 'score-fail'
}

async function fetchExams() {
  loading.value = true
  try {
    const res = await getExams(query)
    exams.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    // handled
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  fetchExams()
}

function showAddForm() {
  currentExam.value = null
  formVisible.value = true
}

function showEditForm(exam: ExamRecord) {
  currentExam.value = exam
  formVisible.value = true
}

async function handleFormSubmit(data: ExamRequest) {
  try {
    if (currentExam.value) {
      await updateExam(currentExam.value.id, data)
      ElMessage.success('成绩已更新')
    } else {
      await createExam(data)
      ElMessage.success('成绩已录入')
    }
    formVisible.value = false
    fetchExams()
  } catch (error) {
    // handled
  }
}

async function handleDelete(id: number) {
  try {
    await deleteExam(id)
    ElMessage.success('成绩已删除')
    fetchExams()
  } catch (error) {
    // handled
  }
}

onMounted(() => {
  fetchExams()
})
</script>

<style scoped>
.search-card :deep(.el-card__body) {
  padding-bottom: 0;
}

.score-excellent {
  color: #67c23a;
  font-weight: 600;
}

.score-good {
  color: #409eff;
}

.score-pass {
  color: #e6a23c;
}

.score-fail {
  color: #f56c6c;
  font-weight: 600;
}
</style>
