<template>
  <div class="student-detail" v-loading="loading">
    <!-- 返回按钮 -->
    <el-page-header @back="$router.push('/students')" style="margin-bottom: 20px">
      <template #content>
        <span class="page-title">{{ student?.name || '学生详情' }}</span>
      </template>
      <template #extra>
        <el-button type="primary" @click="showEditForm">编辑</el-button>
      </template>
    </el-page-header>

    <!-- 学生基本信息 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>基本信息</span>
          <el-tag :type="statusTagType[student?.status || '']" size="small">
            {{ statusMap[student?.status || ''] }}
          </el-tag>
        </div>
      </template>

      <el-descriptions :column="3" border>
        <el-descriptions-item label="姓名">{{ student?.name }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ genderMap[student?.gender || ''] || '-' }}</el-descriptions-item>
        <el-descriptions-item label="年龄">{{ student?.age || '-' }}岁</el-descriptions-item>
        <el-descriptions-item label="年级">{{ gradeMap[student?.grade || ''] || student?.grade }}</el-descriptions-item>
        <el-descriptions-item label="学校">{{ student?.school || '-' }}</el-descriptions-item>
        <el-descriptions-item label="入学日期">{{ student?.enrollmentDate }}</el-descriptions-item>
        <el-descriptions-item label="学习科目">
          <el-tag v-for="s in student?.subjects" :key="s" size="small" style="margin-right: 4px">
            {{ s }}
          </el-tag>
          <span v-if="!student?.subjects?.length">-</span>
        </el-descriptions-item>
        <el-descriptions-item label="学生来源">{{ sourceMap[student?.source || ''] || '-' }}</el-descriptions-item>
        <el-descriptions-item label="学生电话">{{ student?.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="家长姓名">{{ student?.parentName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="家长电话">{{ student?.parentPhone }}</el-descriptions-item>
        <el-descriptions-item label="家长关系">{{ relationMap[student?.parentRelation || ''] || '-' }}</el-descriptions-item>
        <el-descriptions-item label="家庭住址" :span="2">{{ student?.address || '-' }}</el-descriptions-item>
        <el-descriptions-item label="标签">
          <el-tag v-for="t in student?.tags" :key="t" type="info" size="small" style="margin-right: 4px">
            {{ t }}
          </el-tag>
          <span v-if="!student?.tags?.length">-</span>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="3">{{ student?.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 课时费和费用记录 -->
    <el-card style="margin-top: 16px">
      <el-tabs v-model="activeTab">
        <!-- 课程列表 -->
        <el-tab-pane label="课程列表" name="courses">
          <div style="margin-bottom: 16px">
            <el-button type="primary" @click="courseFormVisible = true">
              <el-icon><Plus /></el-icon>新增课程
            </el-button>
          </div>
          <el-table :data="courses" stripe>
            <el-table-column prop="subject" label="科目" width="80" />
            <el-table-column prop="title" label="课程标题" min-width="140" show-overflow-tooltip />
            <el-table-column prop="startTime" label="上课时间" width="160">
              <template #default="{ row }">
                {{ row.startTime?.replace('T', ' ') }}
              </template>
            </el-table-column>
            <el-table-column prop="endTime" label="下课时间" width="160">
              <template #default="{ row }">
                {{ row.endTime?.replace('T', ' ') }}
              </template>
            </el-table-column>
            <el-table-column prop="courseType" label="类型" width="80">
              <template #default="{ row }">
                <el-tag :type="courseTypeTag[row.courseType]" size="small">
                  {{ courseTypeMap[row.courseType] || row.courseType }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="courseStatusTag[row.status]" size="small">
                  {{ courseStatusMap[row.status] || row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="location" label="地点" min-width="100" show-overflow-tooltip />
          </el-table>

          <el-pagination
            v-if="courseTotal > 0"
            style="margin-top: 16px; justify-content: flex-end"
            v-model:current-page="coursePage"
            :page-size="coursePageSize"
            :total="courseTotal"
            layout="total, prev, pager, next"
            @current-change="fetchCourses"
          />
          <el-empty v-if="!courses.length" description="暂无课程记录" />
        </el-tab-pane>

        <!-- 课时费管理 -->
        <el-tab-pane label="课时费管理" name="fees">
          <div style="margin-bottom: 16px">
            <el-button type="success" @click="showFeeForm()">
              <el-icon><Plus /></el-icon>新增课时费
            </el-button>
          </div>

          <el-table :data="fees" stripe>
            <el-table-column prop="feeType" label="计费类型" width="100">
              <template #default="{ row }">
                {{ feeTypeMap[row.feeType] }}
              </template>
            </el-table-column>
            <el-table-column prop="subject" label="科目" width="80" />
            <el-table-column label="定价" min-width="160" show-overflow-tooltip>
              <template #default="{ row }">
                <template v-if="row.priceTiers?.length">
                  <el-tag v-for="t in row.priceTiers" :key="t.hours" size="small" style="margin-right: 4px">
                    {{ t.hours }}h ¥{{ t.price }}
                  </el-tag>
                </template>
                <span v-else-if="row.unitPrice">¥{{ row.unitPrice }}/次</span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="prepaidCount" label="预缴课时" width="90" />
            <el-table-column prop="remainingCount" label="剩余课时" width="90">
              <template #default="{ row }">
                <span :class="{ 'text-danger': row.remainingCount !== null && row.remainingCount <= 2 }">
                  {{ row.remainingCount ?? '-' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="periodStart" label="周期开始" width="110" />
            <el-table-column prop="periodEnd" label="周期结束" width="110" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">
                  {{ row.status === 'ACTIVE' ? '生效' : row.status === 'EXPIRED' ? '过期' : '取消' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
            <el-table-column label="操作" width="80">
              <template #default="{ row }">
                <el-button type="primary" link @click="showFeeForm(row)">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 费用记录 -->
        <el-tab-pane label="费用记录" name="records">
          <div style="margin-bottom: 16px">
            <el-button type="success" @click="showRecordForm()">
              <el-icon><Plus /></el-icon>新增记录
            </el-button>
          </div>

          <el-table :data="records" stripe>
            <el-table-column prop="paymentDate" label="日期" width="110" />
            <el-table-column prop="paymentType" label="类型" width="80">
              <template #default="{ row }">
                <el-tag :type="row.paymentType === 'INCOME' ? 'success' : 'danger'" size="small">
                  {{ row.paymentType === 'INCOME' ? '收入' : '退款' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="amount" label="金额" width="100">
              <template #default="{ row }">
                <span :class="row.paymentType === 'INCOME' ? 'text-success' : 'text-danger'">
                  {{ row.paymentType === 'INCOME' ? '+' : '-' }}¥{{ row.amount }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="paymentMethod" label="方式" width="80">
              <template #default="{ row }">
                {{ paymentMethodMap[row.paymentMethod] || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="note" label="备注" min-width="150" show-overflow-tooltip />
          </el-table>

          <el-pagination
            v-if="recordTotal > 0"
            style="margin-top: 16px; justify-content: flex-end"
            v-model:current-page="recordPage"
            :page-size="20"
            :total="recordTotal"
            layout="total, prev, pager, next"
            @current-change="fetchRecords"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 课时费表单弹窗 -->
    <el-dialog
      v-model="feeFormVisible"
      :title="currentFee ? '编辑课时费' : '新增课时费'"
      width="560px"
    >
      <el-form ref="feeFormRef" :model="feeForm" :rules="feeRules" label-width="100px">
        <el-form-item label="计费类型" prop="feeType">
          <el-select v-model="feeForm.feeType" placeholder="请选择计费类型" style="width: 100%">
            <el-option label="按次计费" value="PER_TIME" />
            <el-option label="按次预缴（包课）" value="PREPAID" />
            <el-option label="按时间段缴费" value="PERIOD" />
          </el-select>
        </el-form-item>
        <el-form-item label="科目" prop="subject">
          <el-select v-model="feeForm.subject" placeholder="请选择科目" style="width: 100%">
            <el-option label="语文" value="语文" />
            <el-option label="数学" value="数学" />
            <el-option label="英语" value="英语" />
            <el-option label="物理" value="物理" />
            <el-option label="化学" value="化学" />
          </el-select>
        </el-form-item>
        <el-form-item label="课时单价" prop="unitPrice">
          <el-input-number v-model="feeForm.unitPrice" :min="0" :precision="2" style="width: 100%" placeholder="无阶梯价格时使用" />
        </el-form-item>

        <el-form-item label="阶梯定价">
          <div style="width: 100%">
            <div v-for="(tier, index) in feeForm.priceTiers" :key="index" style="display: flex; align-items: center; gap: 8px; margin-bottom: 8px">
              <el-input-number v-model="tier.hours" :min="0.5" :step="0.5" :precision="1" placeholder="时长" style="flex: 1" />
              <span>小时</span>
              <el-input-number v-model="tier.price" :min="0" :precision="2" placeholder="价格" style="flex: 1" />
              <span>元</span>
              <el-button type="danger" :icon="Delete" circle size="small" @click="removePriceTier(index)" />
            </div>
            <el-button type="primary" link @click="addPriceTier">
              <el-icon><Plus /></el-icon>添加阶梯
            </el-button>
            <div v-if="feeForm.priceTiers?.length" style="color: #909399; font-size: 12px; margin-top: 4px">
              结算时按课程实际时长匹配最接近的阶梯价格
            </div>
          </div>
        </el-form-item>
        <el-form-item v-if="feeForm.feeType === 'PREPAID'" label="预缴课时" prop="prepaidCount">
          <el-input-number v-model="feeForm.prepaidCount" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="feeForm.feeType === 'PERIOD'" label="周期开始" prop="periodStart">
          <el-date-picker v-model="feeForm.periodStart" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="feeForm.feeType === 'PERIOD'" label="周期结束" prop="periodEnd">
          <el-date-picker v-model="feeForm.periodEnd" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="feeForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="feeFormVisible = false">取消</el-button>
        <el-button type="primary" :loading="feeSubmitting" @click="handleFeeSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 费用记录表单弹窗 -->
    <el-dialog v-model="recordFormVisible" title="新增费用记录" width="500px">
      <el-form ref="recordFormRef" :model="recordForm" :rules="recordRules" label-width="100px">
        <el-form-item label="收支类型" prop="paymentType">
          <el-radio-group v-model="recordForm.paymentType">
            <el-radio value="INCOME">收入</el-radio>
            <el-radio value="REFUND">退款</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="金额" prop="amount">
          <el-input-number v-model="recordForm.amount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="日期" prop="paymentDate">
          <el-date-picker v-model="recordForm.paymentDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="支付方式" prop="paymentMethod">
          <el-select v-model="recordForm.paymentMethod" placeholder="请选择" style="width: 100%">
            <el-option label="现金" value="CASH" />
            <el-option label="微信" value="WECHAT" />
            <el-option label="支付宝" value="ALIPAY" />
            <el-option label="银行转账" value="BANK" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="recordForm.note" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="recordFormVisible = false">取消</el-button>
        <el-button type="primary" :loading="recordSubmitting" @click="handleRecordSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 编辑学生弹窗 -->
    <StudentForm
      :visible="editFormVisible"
      :student="student"
      @close="editFormVisible = false"
      @submit="handleEditSubmit"
    />

    <!-- 新增课程弹窗 -->
    <CourseForm
      :visible="courseFormVisible"
      :default-student="student ? { id: studentId, name: student.name } : null"
      @close="courseFormVisible = false"
      @submit="handleCourseSubmit"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Plus, Delete } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getStudent, updateStudent,
  getStudentFees, createStudentFee, updateStudentFee,
  getFeeRecords, createFeeRecord,
} from '@/api/student'
import type { Student, StudentFee, FeeRecord, StudentRequest, StudentFeeRequest, FeeRecordRequest } from '@/api/student'
import { getCourses, createCourse } from '@/api/course'
import type { Course, CourseRequest } from '@/api/course'
import StudentForm from './components/StudentForm.vue'
import CourseForm from '@/views/schedule/components/CourseForm.vue'

const route = useRoute()
const studentId = Number(route.params.id)

const loading = ref(false)
const student = ref<Student | null>(null)
const activeTab = ref('courses')

// 课时费
const fees = ref<StudentFee[]>([])
const feeFormVisible = ref(false)
const feeSubmitting = ref(false)
const currentFee = ref<StudentFee | null>(null)
const feeFormRef = ref<FormInstance>()
const feeForm = reactive<StudentFeeRequest>({
  feeType: '',
  unitPrice: undefined,
  prepaidCount: undefined,
  periodStart: undefined,
  periodEnd: undefined,
  subject: undefined,
  remark: undefined,
  priceTiers: [],
})

// 费用记录
const records = ref<FeeRecord[]>([])
const recordTotal = ref(0)
const recordPage = ref(1)
const recordFormVisible = ref(false)
const recordSubmitting = ref(false)
const recordFormRef = ref<FormInstance>()
const recordForm = reactive<FeeRecordRequest>({
  amount: 0,
  paymentType: 'INCOME',
  paymentDate: '',
  paymentMethod: undefined,
  note: undefined,
})

// 课程列表
const courses = ref<Course[]>([])
const courseTotal = ref(0)
const coursePage = ref(1)
const coursePageSize = 10

// 编辑学生
const editFormVisible = ref(false)

// 新增课程
const courseFormVisible = ref(false)

const genderMap: Record<string, string> = { MALE: '男', FEMALE: '女', OTHER: '其他' }
const gradeMap: Record<string, string> = {
  PRIMARY_1: '小学1年级', PRIMARY_2: '小学2年级', PRIMARY_3: '小学3年级',
  PRIMARY_4: '小学4年级', PRIMARY_5: '小学5年级', PRIMARY_6: '小学6年级',
  JUNIOR_1: '初一', JUNIOR_2: '初二', JUNIOR_3: '初三',
  SENIOR_1: '高一', SENIOR_2: '高二', SENIOR_3: '高三',
}
const statusMap: Record<string, string> = { ACTIVE: '在读', PAUSED: '暂停', GRADUATED: '毕业', LOST: '流失' }
const statusTagType: Record<string, string> = { ACTIVE: 'success', PAUSED: 'warning', GRADUATED: 'info', LOST: 'danger' }
const sourceMap: Record<string, string> = { REFERRAL: '家长推荐', GROUP: '群推荐', PLATFORM: '平台引流', OTHER: '其他' }
const relationMap: Record<string, string> = { FATHER: '父亲', MOTHER: '母亲', GUARDIAN: '其他监护人' }
const feeTypeMap: Record<string, string> = { PER_TIME: '按次计费', PREPAID: '预缴包课', PERIOD: '按周期' }
const paymentMethodMap: Record<string, string> = { CASH: '现金', WECHAT: '微信', ALIPAY: '支付宝', BANK: '银行转账' }
const courseTypeMap: Record<string, string> = { ONE_ON_ONE: '一对一', ONLINE: '在线', SMALL_CLASS: '小班' }
const courseTypeTag: Record<string, string> = { ONE_ON_ONE: '', ONLINE: 'warning', SMALL_CLASS: 'success' }
const courseStatusMap: Record<string, string> = { SCHEDULED: '已排课', COMPLETED: '已完成', CANCELLED: '已取消' }
const courseStatusTag: Record<string, string> = { SCHEDULED: 'primary', COMPLETED: 'success', CANCELLED: 'info' }

const feeRules: FormRules = {
  feeType: [{ required: true, message: '请选择计费类型', trigger: 'change' }],
}

const recordRules: FormRules = {
  paymentType: [{ required: true, message: '请选择收支类型', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  paymentDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
}

async function fetchStudent() {
  loading.value = true
  try {
    const res = await getStudent(studentId)
    student.value = res.data
  } catch (error) {
    // handled
  } finally {
    loading.value = false
  }
}

async function fetchFees() {
  try {
    const res = await getStudentFees(studentId)
    fees.value = res.data
  } catch (error) {
    // handled
  }
}

async function fetchRecords() {
  try {
    const res = await getFeeRecords(studentId, recordPage.value)
    records.value = res.data.records
    recordTotal.value = res.data.total
  } catch (error) {
    // handled
  }
}

async function fetchCourses() {
  try {
    const res = await getCourses({
      studentId,
      page: coursePage.value,
      size: coursePageSize,
    })
    courses.value = res.data.records
    courseTotal.value = res.data.total
  } catch (error) {
    // handled
  }
}

function showEditForm() {
  editFormVisible.value = true
}

async function handleEditSubmit(data: StudentRequest) {
  try {
    await updateStudent(studentId, data)
    ElMessage.success('学生信息已更新')
    editFormVisible.value = false
    fetchStudent()
  } catch (error) {
    // handled
  }
}

async function handleCourseSubmit(data: CourseRequest) {
  try {
    data.studentId = studentId
    const res = await createCourse(data)
    ElMessage.success(`已创建 ${res.data.length} 节课程`)
    courseFormVisible.value = false
    fetchCourses()
  } catch (error) {
    // handled
  }
}

function showFeeForm(fee?: StudentFee) {
  currentFee.value = fee || null
  if (fee) {
    Object.assign(feeForm, {
      feeType: fee.feeType,
      unitPrice: fee.unitPrice,
      prepaidCount: fee.prepaidCount,
      periodStart: fee.periodStart,
      periodEnd: fee.periodEnd,
      subject: fee.subject,
      remark: fee.remark,
      priceTiers: fee.priceTiers ? [...fee.priceTiers] : [],
    })
  } else {
    Object.assign(feeForm, {
      feeType: '',
      unitPrice: undefined,
      prepaidCount: undefined,
      periodStart: undefined,
      periodEnd: undefined,
      subject: undefined,
      remark: undefined,
      priceTiers: [],
    })
  }
  feeFormVisible.value = true
}

function addPriceTier() {
  feeForm.priceTiers!.push({ hours: 1, price: 0 })
}

function removePriceTier(index: number) {
  feeForm.priceTiers!.splice(index, 1)
}

async function handleFeeSubmit() {
  const valid = await feeFormRef.value?.validate().catch(() => false)
  if (!valid) return

  feeSubmitting.value = true
  try {
    if (currentFee.value) {
      await updateStudentFee(studentId, currentFee.value.id, feeForm)
      ElMessage.success('课时费已更新')
    } else {
      await createStudentFee(studentId, feeForm)
      ElMessage.success('课时费已创建')
    }
    feeFormVisible.value = false
    fetchFees()
  } catch (error) {
    // handled
  } finally {
    feeSubmitting.value = false
  }
}

function showRecordForm() {
  Object.assign(recordForm, {
    amount: 0,
    paymentType: 'INCOME',
    paymentDate: '',
    paymentMethod: undefined,
    note: undefined,
  })
  recordFormVisible.value = true
}

async function handleRecordSubmit() {
  const valid = await recordFormRef.value?.validate().catch(() => false)
  if (!valid) return

  recordSubmitting.value = true
  try {
    await createFeeRecord(studentId, recordForm)
    ElMessage.success('费用记录已创建')
    recordFormVisible.value = false
    fetchRecords()
    fetchFees() // 刷新课时费（剩余课时可能变化）
  } catch (error) {
    // handled
  } finally {
    recordSubmitting.value = false
  }
}

onMounted(() => {
  fetchStudent()
  fetchFees()
  fetchRecords()
  fetchCourses()
})
</script>

<style scoped>
.page-title {
  font-size: 18px;
  font-weight: 600;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.text-danger {
  color: #f56c6c;
  font-weight: 600;
}

.text-success {
  color: #67c23a;
}
</style>
