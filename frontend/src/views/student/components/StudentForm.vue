<template>
  <el-dialog
    :model-value="visible"
    :title="isEdit ? '编辑学生' : '新增学生'"
    width="720px"
    class="student-dialog"
    @close="$emit('close')"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      label-position="right"
      class="student-form"
    >
      <!-- 基本信息 -->
      <div class="section">
        <div class="section-title">基本信息</div>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="form.gender">
                <el-radio-button value="MALE">男</el-radio-button>
                <el-radio-button value="FEMALE">女</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="年级" prop="grade">
              <el-select v-model="form.grade" placeholder="请选择年级" style="width: 100%">
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
          </el-col>
          <el-col :span="12">
            <el-form-item label="学校" prop="school">
              <el-input v-model="form.school" placeholder="请输入学校" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="入学日期" prop="enrollmentDate">
              <el-date-picker
                v-model="form.enrollmentDate"
                type="date"
                placeholder="请选择日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出生日期" prop="birthDate">
              <el-date-picker
                v-model="form.birthDate"
                type="date"
                placeholder="请选择日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="科目" prop="subjects">
          <el-select
            v-model="form.subjects"
            multiple
            placeholder="请选择科目"
            style="width: 100%"
          >
            <el-option label="语文" value="语文" />
            <el-option label="数学" value="数学" />
            <el-option label="英语" value="英语" />
            <el-option label="物理" value="物理" />
            <el-option label="化学" value="化学" />
            <el-option label="生物" value="生物" />
            <el-option label="历史" value="历史" />
            <el-option label="政治" value="政治" />
            <el-option label="地理" value="地理" />
          </el-select>
        </el-form-item>
      </div>

      <!-- 联系方式 -->
      <div class="section">
        <div class="section-title">联系方式</div>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="家长姓名" prop="parentName">
              <el-input v-model="form.parentName" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="家长电话" prop="parentPhone">
              <el-input v-model="form.parentPhone" placeholder="请输入" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="关系" prop="parentRelation">
              <el-select v-model="form.parentRelation" placeholder="请选择" style="width: 100%">
                <el-option label="父亲" value="FATHER" />
                <el-option label="母亲" value="MOTHER" />
                <el-option label="其他监护人" value="GUARDIAN" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学生电话" prop="phone">
              <el-input v-model="form.phone" placeholder="选填" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="来源" prop="source">
          <el-radio-group v-model="form.source">
            <el-radio-button value="REFERRAL">家长推荐</el-radio-button>
            <el-radio-button value="GROUP">群推荐</el-radio-button>
            <el-radio-button value="PLATFORM">平台引流</el-radio-button>
            <el-radio-button value="OTHER">其他</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </div>

      <!-- 备注 -->
      <div class="section last">
        <div class="section-title">备注</div>
        <el-form-item label="标签" prop="tags">
          <el-select
            v-model="form.tags"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="选择或自定义标签"
            style="width: 100%"
          >
            <el-option label="基础薄弱" value="基础薄弱" />
            <el-option label="学习积极" value="学习积极" />
            <el-option label="需要关注" value="需要关注" />
            <el-option label="进步明显" value="进步明显" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="2"
            placeholder="其他需要记录的信息"
          />
        </el-form-item>
      </div>
    </el-form>

    <template #footer>
      <el-button @click="$emit('close')">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">
        {{ isEdit ? '保存' : '创建' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import type { Student, StudentRequest } from '@/api/student'

const props = defineProps<{
  visible: boolean
  student?: Student | null
}>()

const emit = defineEmits<{
  close: []
  submit: [data: StudentRequest]
}>()

const formRef = ref<FormInstance>()
const loading = ref(false)
const isEdit = ref(false)

const form = reactive<StudentRequest>({
  name: '',
  gender: undefined,
  birthDate: undefined,
  grade: '',
  school: undefined,
  subjects: [],
  source: undefined,
  address: undefined,
  phone: undefined,
  parentName: undefined,
  parentPhone: '',
  parentRelation: undefined,
  remark: undefined,
  enrollmentDate: '',
  tags: [],
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入学生姓名', trigger: 'blur' }],
  grade: [{ required: true, message: '请选择年级', trigger: 'change' }],
  parentPhone: [{ required: true, message: '请输入家长电话', trigger: 'blur' }],
  enrollmentDate: [{ required: true, message: '请选择入学日期', trigger: 'change' }],
}

watch(() => props.visible, (val) => {
  if (val && props.student) {
    isEdit.value = true
    Object.assign(form, {
      name: props.student.name,
      gender: props.student.gender,
      birthDate: props.student.birthDate,
      grade: props.student.grade,
      school: props.student.school,
      subjects: props.student.subjects || [],
      source: props.student.source,
      address: props.student.address,
      phone: props.student.phone,
      parentName: props.student.parentName,
      parentPhone: props.student.parentPhone,
      parentRelation: props.student.parentRelation,
      remark: props.student.remark,
      enrollmentDate: props.student.enrollmentDate,
      tags: props.student.tags || [],
    })
  } else if (val) {
    isEdit.value = false
    Object.assign(form, {
      name: '',
      gender: undefined,
      birthDate: undefined,
      grade: '',
      school: undefined,
      subjects: [],
      source: undefined,
      address: undefined,
      phone: undefined,
      parentName: undefined,
      parentPhone: '',
      parentRelation: undefined,
      remark: undefined,
      enrollmentDate: '',
      tags: [],
    })
  }
})

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    emit('submit', { ...form })
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.student-form {
  padding: 0 8px;
}

.section {
  padding-bottom: 32px;
  margin-bottom: 32px;
  border-bottom: 1px solid #f0f0f0;
}

.section.last {
  padding-bottom: 0;
  margin-bottom: 0;
  border-bottom: none;
}

.section-title {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 22px;
}

.section :deep(.el-form-item) {
  margin-bottom: 28px;
}

.section :deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

.section :deep(.el-row) {
  margin-bottom: 12px;
}

.section :deep(.el-row:last-of-type) {
  margin-bottom: 0;
}
</style>
