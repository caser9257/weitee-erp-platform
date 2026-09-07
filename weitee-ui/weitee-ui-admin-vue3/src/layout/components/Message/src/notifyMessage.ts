import type { RouteLocationRaw } from 'vue-router'
import type { NotifyMessageVO } from '@/api/system/notify/message'

const BPM_PROCESS_INSTANCE_DETAIL_PATH = '/bpm/process-instance/detail'
const BPM_PROCESS_INSTANCE_APPROVE_CODE = 'bpm_process_instance_approve'
const BPM_PROCESS_INSTANCE_REJECT_CODE = 'bpm_process_instance_reject'
const BPM_TASK_ASSIGNED_CODE = 'bpm_task_assigned'
const BPM_TASK_TIMEOUT_CODE = 'bpm_task_timeout'
const IMPORT_RESULT_RD_BOM_CODE = 'erp_import_result_rd_bom'

// 项目管理通知模板编码
const PROJECT_COMMENT_MENTION_CODE = 'project_comment_mention'
const PROJECT_COMMENT_REPLY_CODE = 'project_comment_reply'
const PROJECT_TASK_ASSIGNED_CODE = 'project_task_assigned'
const PROJECT_TASK_COMPLETED_CODE = 'project_task_completed'

export interface NotifyMessageSummaryItem {
  label: string
  value: string
}

export interface NotifyMessageAction {
  label: string
  route?: RouteLocationRaw
  href?: string
}

export interface NotifyMessagePreview {
  categoryLabel: string
  categoryType: 'danger' | 'warning' | 'success' | 'info'
  icon: string
  title: string
  body: string
  summaryItems: NotifyMessageSummaryItem[]
  action?: NotifyMessageAction
}

const getTemplateParams = (templateParams: NotifyMessageVO['templateParams']) => {
  if (!templateParams || typeof templateParams !== 'object' || Array.isArray(templateParams)) {
    return undefined
  }
  return templateParams as Record<string, unknown>
}

export const getNotifyMessageParam = (message: NotifyMessageVO, key: string) => {
  const value = getTemplateParams(message.templateParams)?.[key]
  if (value === undefined || value === null) {
    return undefined
  }
  const text = String(value).trim()
  return text || undefined
}

const getNotifyMessageUrlFromContent = (content?: string) => {
  const match = content?.match(/https?:\/\/\S+/i)
  return match?.[0]
}

export const stripNotifyMessageLinks = (content?: string) => {
  return (content || '')
    .replace(/(?:\u5904\u7406|\u8be6\u60c5)?\u94fe\u63a5[:\uff1a]\s*https?:\/\/\S+/gi, '')
    .replace(/https?:\/\/\S+/gi, '')
    .replace(/\s+/g, ' ')
    .trim()
}

const pushSummaryItem = (
  summaryItems: NotifyMessageSummaryItem[],
  label: string,
  value?: string
) => {
  if (!value) {
    return
  }
  summaryItems.push({ label, value })
}

const getImportCount = (message: NotifyMessageVO, key: string) => {
  const value = getNotifyMessageParam(message, key)
  return value && /^\d+$/.test(value) ? value : undefined
}

const isDuplicateMaterialWarning = (message: NotifyMessageVO) => {
  const failSample = getNotifyMessageParam(message, 'failSample') || ''
  const contentText = stripNotifyMessageLinks(message.templateContent)
  return failSample.includes('产品编码重复') || contentText.includes('产品编码重复')
}

/*
const getActionLabel = (message: NotifyMessageVO, pathname?: string) => {
  if (message.templateCode === BPM_TASK_ASSIGNED_CODE) {
    return '去处理'
  }
  if (pathname === BPM_PROCESS_INSTANCE_DETAIL_PATH || message.templateCode.startsWith('bpm_')) {
    return '查看流程'
  }
  return '查看详情'
}

*/
const getActionLabel = (message: NotifyMessageVO, pathname?: string) => {
  if (message.templateCode === BPM_TASK_ASSIGNED_CODE) {
    return '\u53bb\u5904\u7406'
  }
  if (pathname === BPM_PROCESS_INSTANCE_DETAIL_PATH || message.templateCode.startsWith('bpm_')) {
    return '\u67e5\u770b\u6d41\u7a0b'
  }
  return '\u67e5\u770b\u8be6\u60c5'
}

const getQueryRecord = (url: URL) => {
  const query: Record<string, string> = {}
  url.searchParams.forEach((value, key) => {
    if (!(key in query)) {
      query[key] = value
    }
  })
  return query
}

const resolveNotifyAction = (message: NotifyMessageVO): NotifyMessageAction | undefined => {
  const detailUrl =
    getNotifyMessageParam(message, 'detailUrl') ||
    getNotifyMessageUrlFromContent(message.templateContent)

  // 项目通知处理
  if (message.templateCode?.startsWith('project_')) {
    const projectId = getNotifyMessageParam(message, 'projectId')
    const taskId = getNotifyMessageParam(message, 'taskId')
    if (projectId) {
      return {
        label: '查看详情',
        route: {
          path: `/project/project/detail/${projectId}`,
          query: taskId ? { taskId } : undefined
        }
      }
    }
  }

  if (!detailUrl) {
    return undefined
  }

  let url: URL
  try {
    url = new URL(detailUrl, window.location.origin)
  } catch {
    return {
      label: getActionLabel(message),
      href: detailUrl
    }
  }

  const label = getActionLabel(message, url.pathname)
  const query = getQueryRecord(url)
  if (url.pathname === BPM_PROCESS_INSTANCE_DETAIL_PATH) {
    return {
      label,
      route: {
        name: 'BpmProcessInstanceDetail',
        query
      }
    }
  }

  if (url.origin === window.location.origin || !/^https?:\/\//i.test(detailUrl)) {
    return {
      label,
      route: {
        path: url.pathname,
        query
      }
    }
  }

  return {
    label,
    href: detailUrl
  }
}

/*
export const getNotifyMessagePreview = (message: NotifyMessageVO): NotifyMessagePreview => {
  const processInstanceName = getNotifyMessageParam(message, 'processInstanceName')
  const taskName = getNotifyMessageParam(message, 'taskName')
  const startUserNickname = getNotifyMessageParam(message, 'startUserNickname')
  const reason = getNotifyMessageParam(message, 'reason')
  const action = resolveNotifyAction(message)
  const contentText = stripNotifyMessageLinks(message.templateContent)
  const summaryItems: NotifyMessageSummaryItem[] = []

  switch (message.templateCode) {
    case BPM_TASK_ASSIGNED_CODE:
      pushSummaryItem(summaryItems, '流程', processInstanceName)
      pushSummaryItem(summaryItems, '当前节点', taskName)
      pushSummaryItem(summaryItems, '发起人', startUserNickname)
      return {
        categoryLabel: '审批待办',
        categoryType: 'danger',
        icon: 'ep:document-checked',
        title: '新的审批待办',
        body: summaryItems.length === 0 ? contentText || '请点击下方按钮进入处理。' : '',
        summaryItems,
        action
      }
    case BPM_TASK_TIMEOUT_CODE:
      pushSummaryItem(summaryItems, '流程', processInstanceName)
      pushSummaryItem(summaryItems, '当前节点', taskName)
      return {
        categoryLabel: '超时提醒',
        categoryType: 'warning',
        icon: 'ep:alarm-clock',
        title: '审批处理超时提醒',
        body: summaryItems.length === 0 ? contentText || '有流程超时，请尽快查看。' : '',
        summaryItems,
        action
      }
    case BPM_PROCESS_INSTANCE_APPROVE_CODE:
      pushSummaryItem(summaryItems, '流程', processInstanceName)
      return {
        categoryLabel: '流程通知',
        categoryType: 'success',
        icon: 'ep:circle-check',
        title: '审批结果通知',
        body: summaryItems.length === 0 ? contentText || '审批已通过。' : '',
        summaryItems,
        action
      }
    case BPM_PROCESS_INSTANCE_REJECT_CODE:
      pushSummaryItem(summaryItems, '流程', processInstanceName)
      pushSummaryItem(summaryItems, '原因', reason)
      return {
        categoryLabel: '流程通知',
        categoryType: 'warning',
        icon: 'ep:warning-filled',
        title: '审批结果通知',
        body: summaryItems.length === 0 ? contentText || '审批未通过。' : '',
        summaryItems,
        action
      }
    default:
      return {
        categoryLabel: '站内信',
        categoryType: 'info',
        icon: 'ep:bell',
        title: message.templateNickname || '站内信通知',
        body: contentText || (action ? '请点击下方按钮查看详情。' : '暂无预览内容，请进入消息列表查看。'),
        summaryItems,
        action
      }
  }
}
*/

export const getNotifyMessagePreview = (message: NotifyMessageVO): NotifyMessagePreview => {
  const processInstanceName = getNotifyMessageParam(message, 'processInstanceName')
  const taskName = getNotifyMessageParam(message, 'taskName')
  const startUserNickname = getNotifyMessageParam(message, 'startUserNickname')
  const reason = getNotifyMessageParam(message, 'reason')
  const action = resolveNotifyAction(message)
  const contentText = stripNotifyMessageLinks(message.templateContent)
  const summaryItems: NotifyMessageSummaryItem[] = []

  switch (message.templateCode) {
    case IMPORT_RESULT_RD_BOM_CODE: {
      const totalCount = getImportCount(message, 'totalCount')
      const successCount = getImportCount(message, 'successCount')
      const failCount = getImportCount(message, 'failCount')
      pushSummaryItem(summaryItems, '总行数', totalCount)
      pushSummaryItem(summaryItems, '成功', successCount)
      pushSummaryItem(summaryItems, '失败', failCount)
      return {
        categoryLabel: '导入结果',
        categoryType: failCount && failCount !== '0' ? 'warning' : 'success',
        icon: 'ep:document-checked',
        title: '研发 BOM 导入完成',
        body: isDuplicateMaterialWarning(message)
          ? '存在重复产品编码，请核对清单后再提交。'
          : '详细校验信息请点击“立即查看”查看。',
        summaryItems,
        action
      }
    }
    case BPM_TASK_ASSIGNED_CODE:
      pushSummaryItem(summaryItems, '\u6d41\u7a0b', processInstanceName)
      pushSummaryItem(summaryItems, '\u5f53\u524d\u8282\u70b9', taskName)
      pushSummaryItem(summaryItems, '\u53d1\u8d77\u4eba', startUserNickname)
      return {
        categoryLabel: '\u5ba1\u6279\u5f85\u529e',
        categoryType: 'danger',
        icon: 'ep:document-checked',
        title: '\u65b0\u7684\u5ba1\u6279\u5f85\u529e',
        body:
          summaryItems.length === 0
            ? contentText || '\u8bf7\u70b9\u51fb\u4e0b\u65b9\u6309\u94ae\u8fdb\u5165\u5904\u7406\u3002'
            : '',
        summaryItems,
        action
      }
    case BPM_TASK_TIMEOUT_CODE:
      pushSummaryItem(summaryItems, '\u6d41\u7a0b', processInstanceName)
      pushSummaryItem(summaryItems, '\u5f53\u524d\u8282\u70b9', taskName)
      return {
        categoryLabel: '\u8d85\u65f6\u63d0\u9192',
        categoryType: 'warning',
        icon: 'ep:alarm-clock',
        title: '\u5ba1\u6279\u5904\u7406\u8d85\u65f6\u63d0\u9192',
        body:
          summaryItems.length === 0
            ? contentText || '\u6709\u6d41\u7a0b\u8d85\u65f6\uff0c\u8bf7\u5c3d\u5feb\u67e5\u770b\u3002'
            : '',
        summaryItems,
        action
      }
    case BPM_PROCESS_INSTANCE_APPROVE_CODE:
      pushSummaryItem(summaryItems, '\u6d41\u7a0b', processInstanceName)
      return {
        categoryLabel: '\u6d41\u7a0b\u901a\u77e5',
        categoryType: 'success',
        icon: 'ep:circle-check',
        title: '\u5ba1\u6279\u7ed3\u679c\u901a\u77e5',
        body: summaryItems.length === 0 ? contentText || '\u5ba1\u6279\u5df2\u901a\u8fc7\u3002' : '',
        summaryItems,
        action
      }
    case BPM_PROCESS_INSTANCE_REJECT_CODE:
      pushSummaryItem(summaryItems, '\u6d41\u7a0b', processInstanceName)
      pushSummaryItem(summaryItems, '\u539f\u56e0', reason)
      return {
        categoryLabel: '\u6d41\u7a0b\u901a\u77e5',
        categoryType: 'warning',
        icon: 'ep:warning-filled',
        title: '\u5ba1\u6279\u7ed3\u679c\u901a\u77e5',
        body: summaryItems.length === 0 ? contentText || '\u5ba1\u6279\u672a\u901a\u8fc7\u3002' : '',
        summaryItems,
        action
      }
    case PROJECT_COMMENT_MENTION_CODE:
    case PROJECT_COMMENT_REPLY_CODE:
    case PROJECT_TASK_ASSIGNED_CODE:
    case PROJECT_TASK_COMPLETED_CODE:
      return {
        categoryLabel: '\u9879\u76ee\u901a\u77e5',
        categoryType: 'info',
        icon: 'ep:chat-dot-round',
        title: message.templateNickname || '\u9879\u76ee\u901a\u77e5',
        body: contentText || '\u6709\u65b0\u7684\u9879\u76ee\u6d88\u606f\uff0c\u8bf7\u67e5\u770b\u3002',
        summaryItems,
        action
      }
    default:
      return {
        categoryLabel: '\u7ad9\u5185\u4fe1',
        categoryType: 'info',
        icon: 'ep:bell',
        title: message.templateNickname || '\u7ad9\u5185\u4fe1\u901a\u77e5',
        body:
          contentText ||
          (action
            ? '\u8bf7\u70b9\u51fb\u4e0b\u65b9\u6309\u94ae\u67e5\u770b\u8be6\u60c5\u3002'
            : '\u6682\u65e0\u9884\u89c8\u5185\u5bb9\uff0c\u8bf7\u8fdb\u5165\u6d88\u606f\u5217\u8868\u67e5\u770b\u3002'),
        summaryItems,
        action
      }
  }
}
