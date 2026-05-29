<template>
  <div class="project-member">
    <div class="member-list">
      <div v-for="member in members" :key="member.userId" class="member-item">
        <div class="member-info">
          <el-avatar :size="32">{{ member.userName?.charAt(0) }}</el-avatar>
          <span class="member-name">{{ member.userName }}</span>
          <el-tag v-if="member.owner" type="warning" size="small">负责人</el-tag>
        </div>
        <el-button
          v-if="!member.owner"
          type="danger"
          link
          @click="handleRemove(member.userId)"
        >
          移除
        </el-button>
      </div>
    </div>

    <div class="add-member">
      <el-input
        v-model="newMemberId"
        placeholder="输入用户ID"
        style="width: 200px; margin-right: 8px"
      />
      <el-button type="primary" @click="handleAdd">添加成员</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ProjectManageApi } from '@/api/project/project'

const props = defineProps<{ projectId: number }>()
const emit = defineEmits(['close'])

const message = useMessage()

const members = ref<any[]>([])
const newMemberId = ref('')

const loadMembers = async () => {
  try {
    members.value = await ProjectManageApi.getMembers(props.projectId)
  } catch (error) {
    console.error('加载成员失败', error)
  }
}

const handleAdd = async () => {
  if (!newMemberId.value) {
    message.warning('请输入用户ID')
    return
  }
  try {
    await ProjectManageApi.addMember(props.projectId, Number(newMemberId.value))
    message.success('添加成功')
    newMemberId.value = ''
    loadMembers()
  } catch (error) {
    console.error('添加成员失败', error)
  }
}

const handleRemove = async (userId: number) => {
  try {
    await message.confirm('确认移除该成员？')
    await ProjectManageApi.removeMember(props.projectId, userId)
    message.success('移除成功')
    loadMembers()
  } catch (error) {
    console.error('移除成员失败', error)
  }
}

onMounted(() => {
  loadMembers()
})
</script>

<style scoped lang="scss">
.project-member {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.member-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.member-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px;
  border: 1px solid #eee;
  border-radius: 6px;
}

.member-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.member-name {
  font-size: 14px;
}

.add-member {
  display: flex;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #eee;
}
</style>
