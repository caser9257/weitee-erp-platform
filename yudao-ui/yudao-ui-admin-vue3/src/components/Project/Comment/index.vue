<template>
  <div class="project-comment">
    <!-- 评论输入框 -->
    <div class="comment-input">
      <el-input
        v-model="newComment"
        type="textarea"
        :rows="2"
        placeholder="输入评论..."
        @keydown.ctrl.enter="handleSubmit"
      />
      <div class="comment-actions">
        <el-button type="primary" size="small" :loading="submitting" @click="handleSubmit">
          发送
        </el-button>
      </div>
    </div>

    <!-- 评论列表 -->
    <div v-if="commentList.length > 0" class="comment-list">
      <div v-for="comment in commentList" :key="comment.id" class="comment-item">
        <el-avatar :size="36" :src="comment.userAvatar" />
        <div class="comment-body">
          <div class="comment-header">
            <span class="user-name">{{ comment.userName || '用户' + comment.userId }}</span>
            <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
          </div>
          <div class="comment-content">{{ comment.content }}</div>
          <div class="comment-footer">
            <span class="action-btn" @click="handleLike(comment)">
              <Icon icon="ep:thumb-up" :class="{ 'liked': comment.likedByMe }" />
              {{ comment.likeCount || 0 }}
            </span>
            <span class="action-btn" @click="handleReply(comment)">
              <Icon icon="ep:chat-dot-round" />
              回复
            </span>
          </div>

          <!-- 回复列表 -->
          <div v-if="comment.replies && comment.replies.length > 0" class="reply-list">
            <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
              <el-avatar :size="24" :src="reply.userAvatar" />
              <div class="reply-body">
                <span class="user-name">{{ reply.userName || '用户' + reply.userId }}</span>
                <span v-if="reply.replyUserName" class="reply-to"> 回复 {{ reply.replyUserName }}</span>
                <span class="reply-content">{{ reply.content }}</span>
              </div>
            </div>
          </div>

          <!-- 回复输入框 -->
          <div v-if="replyTarget?.id === comment.id" class="reply-input">
            <el-input
              v-model="replyContent"
              size="small"
              :placeholder="'回复 ' + (replyTarget.userName || '用户') + '...'"
              @keydown.ctrl.enter="handleReplySubmit"
            />
            <el-button size="small" type="primary" @click="handleReplySubmit">回复</el-button>
            <el-button size="small" @click="replyTarget = null">取消</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty v-else description="暂无评论" :image-size="80" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/modules/user'
import request from '@/utils/axios'
import dayjs from 'dayjs'

interface Comment {
  id: number
  projectId: number
  taskId: number
  parentId?: number
  content: string
  userId: number
  userName?: string
  userAvatar?: string
  replyUserId?: number
  replyUserName?: string
  likeCount: number
  likedByMe: boolean
  replies?: Comment[]
  createTime: string
}

const props = defineProps<{
  projectId: number
  taskId: number
}>()

const userStore = useUserStore()
const commentList = ref<Comment[]>([])
const newComment = ref('')
const replyContent = ref('')
const replyTarget = ref<Comment | null>(null)
const submitting = ref(false)

const formatTime = (time: string) => {
  return dayjs(time).format('MM-DD HH:mm')
}

const fetchComments = async () => {
  try {
    const res = await request.get({
      url: '/project/comment/page',
      params: {
        projectId: props.projectId,
        taskId: props.taskId,
        pageNo: 1,
        pageSize: 50
      }
    })
    commentList.value = res.list || []
  } catch (error) {
    console.error('获取评论失败', error)
  }
}

const handleSubmit = async () => {
  if (!newComment.value.trim()) return
  submitting.value = true
  try {
    await request.post({
      url: '/project/comment/create',
      data: {
        projectId: props.projectId,
        taskId: props.taskId,
        content: newComment.value
      }
    })
    newComment.value = ''
    ElMessage.success('评论成功')
    await fetchComments()
  } catch (error) {
    ElMessage.error('评论失败')
  } finally {
    submitting.value = false
  }
}

const handleReply = (comment: Comment) => {
  replyTarget.value = comment
  replyContent.value = ''
}

const handleReplySubmit = async () => {
  if (!replyContent.value.trim() || !replyTarget.value) return
  try {
    await request.post({
      url: '/project/comment/create',
      data: {
        projectId: props.projectId,
        taskId: props.taskId,
        parentId: replyTarget.value.id,
        content: replyContent.value,
        replyUserId: replyTarget.value.userId,
        replyCommentId: replyTarget.value.id
      }
    })
    replyContent.value = ''
    replyTarget.value = null
    ElMessage.success('回复成功')
    await fetchComments()
  } catch (error) {
    ElMessage.error('回复失败')
  }
}

const handleLike = async (comment: Comment) => {
  try {
    await request.post({ url: '/project/comment/like', params: { id: comment.id } })
    await fetchComments()
  } catch (error) {
    console.error('点赞失败', error)
  }
}

watch(() => props.taskId, () => {
  if (props.taskId) fetchComments()
})

onMounted(() => {
  if (props.taskId) fetchComments()
})
</script>

<style scoped lang="scss">
.project-comment {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.comment-input {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.comment-actions {
  display: flex;
  justify-content: flex-end;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.comment-item {
  display: flex;
  gap: 12px;
}

.comment-body {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.user-name {
  font-weight: 500;
  font-size: 14px;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-content {
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 8px;
}

.comment-footer {
  display: flex;
  gap: 16px;
}

.action-btn {
  font-size: 12px;
  color: #999;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;

  &:hover {
    color: #409eff;
  }

  .liked {
    color: #409eff;
  }
}

.reply-list {
  margin-top: 12px;
  padding-left: 12px;
  border-left: 2px solid #eee;
}

.reply-item {
  display: flex;
  gap: 8px;
  padding: 8px 0;
}

.reply-body {
  font-size: 13px;
}

.reply-to {
  color: #409eff;
}

.reply-content {
  margin-left: 4px;
}

.reply-input {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}
</style>
