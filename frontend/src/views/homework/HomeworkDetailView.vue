<template>
  <div class="homework-detail" v-loading="loading">
    <!-- 返回按钮 -->
    <el-page-header @back="$router.push('/homework')" style="margin-bottom: 20px">
      <template #content>
        <span class="page-title">{{ homework?.title || '作业详情' }}</span>
      </template>
      <template #extra>
        <el-tag :type="statusTagType[homework?.status || '']" style="margin-right: 12px">
          {{ statusMap[homework?.status || ''] }}
        </el-tag>
        <el-button type="primary" @click="showEditForm">编辑</el-button>
      </template>
    </el-page-header>

    <!-- 作业信息 -->
    <el-card>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="作业标题" :span="2">{{ homework?.title }}</el-descriptions-item>
        <el-descriptions-item label="布置对象">
          {{ homework?.studentName || homework?.className || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="科目">{{ homework?.subject || '-' }}</el-descriptions-item>
        <el-descriptions-item label="截止日期">{{ homework?.dueDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="评分方式">
          {{ scoreTypeMap[homework?.scoreType || ''] || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="作业内容" :span="2">
          <div style="white-space: pre-wrap">{{ homework?.content || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item v-if="homework?.score" label="分数">{{ homework?.score }}</el-descriptions-item>
        <el-descriptions-item v-if="homework?.comment" label="评语" :span="2">
          {{ homework?.comment }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 提交记录和批改 -->
    <el-card style="margin-top: 16px">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="提交记录" name="submissions">
          <div style="margin-bottom: 16px">
            <el-button type="success" @click="showSubmitForm">
              <el-icon><Upload /></el-icon>提交作业
            </el-button>
          </div>

          <el-table :data="submissions" stripe>
            <el-table-column prop="studentName" label="学生" width="100" />
            <el-table-column prop="submittedAt" label="提交时间" width="180">
              <template #default="{ row }">
                {{ new Date(row.submittedAt).toLocaleString('zh-CN') }}
              </template>
            </el-table-column>
            <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
            <el-table-column prop="files" label="附件" width="100">
              <template #default="{ row }">
                {{ row.files?.length || 0 }} 个文件
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 'REVIEWED' ? 'success' : 'info'" size="small">
                  {{ row.status === 'REVIEWED' ? '已查看' : '已提交' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="批改" name="grade">
          <el-form :model="gradeForm" label-width="80px" style="max-width: 500px">
            <el-form-item label="分数">
              <el-input v-model="gradeForm.score" placeholder="请输入分数" />
            </el-form-item>
            <el-form-item label="评语">
              <el-input
                v-model="gradeForm.comment"
                type="textarea"
                :rows="4"
                placeholder="请输入评语"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="gradeLoading" @click="handleGrade">
                提交批改
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 提交作业弹窗 -->
    <el-dialog v-model="submitVisible" title="提交作业" width="500px">
      <el-form :model="submitForm" label-width="80px">
        <el-form-item label="文字内容">
          <el-input
            v-model="submitForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入作业内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="submitVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Upload } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import {
  getHomework,
  getSubmissions,
  submitHomework,
  gradeHomework,
} from '@/api/homework'
import type { Homework, Submission, SubmissionRequest, GradeRequest } from '@/api/homework'

const route = useRoute()
const homeworkId = Number(route.params.id)

const loading = ref(false)
const homework = ref<Homework | null>(null)
const activeTab = ref('submissions')
const submissions = ref<Submission[]>([])

const submitVisible = ref(false)
const submitLoading = ref(false)
const submitForm = reactive<SubmissionRequest>({
  content: '',
  files: [],
})

const gradeLoading = ref(false)
const gradeForm = reactive<GradeRequest>({
  score: '',
  comment: '',
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

const scoreTypeMap: Record<string, string> = {
  PERCENTAGE: '百分制',
  STAR: '五星制',
  RANK: '等级制',
}

async function fetchHomework() {
  loading.value = true
  try {
    const res = await getHomework(homeworkId)
    homework.value = res.data
  } catch (error) {
    // handled
  } finally {
    loading.value = false
  }
}

async function fetchSubmissions() {
  try {
    const res = await getSubmissions(homeworkId)
    submissions.value = res.data
  } catch (error) {
    // handled
  }
}

function showEditForm() {
  // TODO: 编辑作业
}

function showSubmitForm() {
  submitForm.content = ''
  submitForm.files = []
  submitVisible.value = true
}

async function handleSubmit() {
  submitLoading.value = true
  try {
    await submitHomework(homeworkId, submitForm)
    ElMessage.success('作业已提交')
    submitVisible.value = false
    fetchHomework()
    fetchSubmissions()
  } catch (error) {
    // handled
  } finally {
    submitLoading.value = false
  }
}

async function handleGrade() {
  if (!gradeForm.score) {
    ElMessage.warning('请输入分数')
    return
  }

  gradeLoading.value = true
  try {
    await gradeHomework(homeworkId, gradeForm)
    ElMessage.success('批改完成')
    fetchHomework()
  } catch (error) {
    // handled
  } finally {
    gradeLoading.value = false
  }
}

onMounted(() => {
  fetchHomework()
  fetchSubmissions()
})
</script>

<style scoped>
.page-title {
  font-size: 18px;
  font-weight: 600;
}
</style>
