<template>
  <el-dialog
    :model-value="visible"
    :title="isEdit ? '编辑学生' : '新增学生'"
    width="720px"
    @close="$emit('close')"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      label-position="right"
    >
      <div class="form-section">
        <div class="form-section-title">
          <el-icon><User /></el-icon>基本信息
        </div>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" placeholder="请输入学生姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="form.gender" placeholder="请选择性别" style="width: 100%">
                <el-option label="男" value="MALE" />
                <el-option label="女" value="FEMALE" />
                <el-option label="其他" value="OTHER" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="出生日期" prop="birthDate">
              <el-date-picker
                v-model="form.birthDate"
                type="date"
                placeholder="请选择出生日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
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
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="就读学校" prop="school">
              <el-input v-model="form.school" placeholder="请输入学校名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入学日期" prop="enrollmentDate">
              <el-date-picker
                v-model="form.enrollmentDate"
                type="date"
                placeholder="请选择入学日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="学习科目" prop="subjects">
          <el-select
            v-model="form.subjects"
            multiple
            placeholder="请选择学习科目"
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

      <div class="form-section">
        <div class="form-section-title">
          <el-icon><Phone /></el-icon>联系方式
        </div>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学生来源" prop="source">
              <el-select v-model="form.source" placeholder="请选择来源" style="width: 100%">
                <el-option label="家长推荐" value="REFERRAL" />
                <el-option label="群推荐" value="GROUP" />
                <el-option label="平台引流" value="PLATFORM" />
                <el-option label="其他" value="OTHER" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学生电话" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入学生电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="家长姓名" prop="parentName">
              <el-input v-model="form.parentName" placeholder="请输入家长姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="家长电话" prop="parentPhone">
              <el-input v-model="form.parentPhone" placeholder="请输入家长电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="家长关系" prop="parentRelation">
          <el-radio-group v-model="form.parentRelation">
            <el-radio value="FATHER">父亲</el-radio>
            <el-radio value="MOTHER">母亲</el-radio>
            <el-radio value="GUARDIAN">其他监护人</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="家庭住址" prop="address">
          <el-input v-model="form.address" placeholder="请输入家庭住址" />
        </el-form-item>
      </div>

      <div class="form-section">
        <div class="form-section-title">
          <el-icon><More /></el-icon>其他信息
        </div>
        <el-form-item label="标签" prop="tags">
          <el-select
            v-model="form.tags"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="请选择或输入标签"
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
            :rows="3"
            placeholder="请输入备注信息"
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
import { User, Phone, More } from '@element-plus/icons-vue'
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
.form-section {
  padding: 16px 20px;
  margin-bottom: 16px;
  background: #fafbfc;
  border-radius: 8px;
  border-left: 3px solid #409eff;
}

.form-section:last-child {
  margin-bottom: 0;
}

.form-section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
}

.form-section-title .el-icon {
  color: #409eff;
}

.form-section :deep(.el-form-item) {
  margin-bottom: 18px;
}

.form-section :deep(.el-form-item:last-child) {
  margin-bottom: 0;
}
</style>
