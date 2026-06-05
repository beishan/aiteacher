<template>
  <div class="classroom-list">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>虚拟班级</span>
          <el-button type="success" @click="showAddForm">
            <el-icon><Plus /></el-icon>创建班级
          </el-button>
        </div>
      </template>

      <el-row :gutter="16">
        <el-col :span="8" v-for="cls in classrooms" :key="cls.id">
          <el-card shadow="hover" class="class-card">
            <div class="class-header">
              <h3>{{ cls.name }}</h3>
              <el-tag :type="cls.status === 'ACTIVE' ? 'success' : 'info'" size="small">
                {{ cls.status === 'ACTIVE' ? '开课中' : '已结束' }}
              </el-tag>
            </div>
            <div class="class-info">
              <p v-if="cls.subject"><el-icon><Reading /></el-icon> {{ cls.subject }}</p>
              <p><el-icon><User /></el-icon> {{ cls.memberCount }} 名学生</p>
              <p v-if="cls.description" class="desc">{{ cls.description }}</p>
            </div>
            <div class="class-members" v-if="cls.members.length > 0">
              <el-tag
                v-for="member in cls.members.slice(0, 5)"
                :key="member.id"
                size="small"
                type="info"
                style="margin-right: 4px; margin-bottom: 4px"
              >
                {{ member.studentName }}
              </el-tag>
              <el-tag v-if="cls.members.length > 5" size="small" type="info">
                +{{ cls.members.length - 5 }}
              </el-tag>
            </div>
            <div class="class-actions">
              <el-button type="primary" link @click="showEditForm(cls)">编辑</el-button>
              <el-button type="primary" link @click="manageMembers(cls)">成员管理</el-button>
              <el-popconfirm title="确定删除该班级吗？" @confirm="handleDelete(cls.id)">
                <template #reference>
                  <el-button type="danger" link>删除</el-button>
                </template>
              </el-popconfirm>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-empty v-if="classrooms.length === 0" description="暂无班级" />
    </el-card>

    <!-- 班级表单弹窗 -->
    <ClassroomForm
      :visible="formVisible"
      :classroom="currentClassroom"
      @close="formVisible = false"
      @submit="handleFormSubmit"
    />

    <!-- 成员管理弹窗 -->
    <el-dialog v-model="memberVisible" title="成员管理" width="600px">
      <div style="margin-bottom: 16px">
        <el-select
          v-model="newMemberId"
          filterable
          remote
          :remote-method="searchStudents"
          :loading="studentLoading"
          placeholder="搜索学生添加成员"
          style="width: 300px; margin-right: 8px"
        >
          <el-option
            v-for="s in studentOptions"
            :key="s.id"
            :label="s.name"
            :value="s.id"
          />
        </el-select>
        <el-button type="primary" :disabled="!newMemberId" @click="handleAddMember">添加</el-button>
      </div>

      <el-table :data="members" stripe>
        <el-table-column prop="studentName" label="学生姓名" />
        <el-table-column prop="grade" label="年级" />
        <el-table-column prop="joinedAt" label="加入时间" width="180">
          <template #default="{ row }">
            {{ new Date(row.joinedAt).toLocaleString('zh-CN') }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-popconfirm title="确定移除该成员吗？" @confirm="handleRemoveMember(row.studentId)">
              <template #reference>
                <el-button type="danger" link>移除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Plus, Reading, User } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getStudents } from '@/api/student'
import type { Student } from '@/api/student'
import {
  getClassrooms,
  createClassroom,
  updateClassroom,
  deleteClassroom,
  getClassMembers,
  addClassMember,
  removeClassMember,
} from '@/api/classroom'
import type { VirtualClass, ClassMember, ClassroomRequest } from '@/api/classroom'
import ClassroomForm from './components/ClassroomForm.vue'

const classrooms = ref<VirtualClass[]>([])
const formVisible = ref(false)
const currentClassroom = ref<VirtualClass | null>(null)
const memberVisible = ref(false)
const currentClassId = ref<number>(0)
const members = ref<ClassMember[]>([])
const newMemberId = ref<number | undefined>()
const studentOptions = ref<Student[]>([])
const studentLoading = ref(false)

async function fetchClassrooms() {
  try {
    const res = await getClassrooms()
    classrooms.value = res.data
  } catch (error) {
    // handled
  }
}

function showAddForm() {
  currentClassroom.value = null
  formVisible.value = true
}

function showEditForm(cls: VirtualClass) {
  currentClassroom.value = cls
  formVisible.value = true
}

async function handleFormSubmit(data: ClassroomRequest) {
  try {
    if (currentClassroom.value) {
      await updateClassroom(currentClassroom.value.id, data)
      ElMessage.success('班级已更新')
    } else {
      await createClassroom(data)
      ElMessage.success('班级已创建')
    }
    formVisible.value = false
    fetchClassrooms()
  } catch (error) {
    // handled
  }
}

async function handleDelete(id: number) {
  try {
    await deleteClassroom(id)
    ElMessage.success('班级已删除')
    fetchClassrooms()
  } catch (error) {
    // handled
  }
}

async function manageMembers(cls: VirtualClass) {
  currentClassId.value = cls.id
  newMemberId.value = undefined
  memberVisible.value = true
  await fetchMembers()
}

async function fetchMembers() {
  try {
    const res = await getClassMembers(currentClassId.value)
    members.value = res.data
  } catch (error) {
    // handled
  }
}

async function searchStudents(query: string) {
  if (!query) return
  studentLoading.value = true
  try {
    const res = await getStudents({ name: query, size: 20 })
    studentOptions.value = res.data.records
  } catch (error) {
    // handled
  } finally {
    studentLoading.value = false
  }
}

async function handleAddMember() {
  if (!newMemberId.value) return
  try {
    await addClassMember(currentClassId.value, newMemberId.value)
    ElMessage.success('成员已添加')
    newMemberId.value = undefined
    fetchMembers()
    fetchClassrooms()
  } catch (error) {
    // handled
  }
}

async function handleRemoveMember(studentId: number) {
  try {
    await removeClassMember(currentClassId.value, studentId)
    ElMessage.success('成员已移除')
    fetchMembers()
    fetchClassrooms()
  } catch (error) {
    // handled
  }
}

onMounted(() => {
  fetchClassrooms()
})
</script>

<style scoped>
.class-card {
  margin-bottom: 16px;
}

.class-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.class-header h3 {
  margin: 0;
  font-size: 16px;
}

.class-info p {
  margin: 4px 0;
  color: #606266;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.class-info .desc {
  color: #909399;
  font-size: 12px;
  margin-top: 8px;
}

.class-members {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

.class-actions {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
  display: flex;
  gap: 8px;
}
</style>
