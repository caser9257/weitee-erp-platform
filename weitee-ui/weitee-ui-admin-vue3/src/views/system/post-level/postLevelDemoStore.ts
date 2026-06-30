import { reactive } from 'vue'
import type {
  PostLevelAssignUserItemVO,
  PostLevelAssignedUserVO,
  PostLevelChangeLogVO,
  PostLevelDashboardVO,
  PostLevelDetailVO,
  PostLevelTreeNodeVO
} from '@/api/system/postLevel'

export interface DemoDeptOption {
  id: number
  name: string
  parentId?: number
  children?: DemoDeptOption[]
  sort?: number
  status?: number
}

export interface DemoPostTemplate {
  id: number
  deptId: number
  deptName?: string
  code: string
  name: string
  level: string
  staffQuota: number
  keyPosition: boolean
  allowPartTime: boolean
  sort: number
  status: number
  jobDescription: string
  remark: string
  assignedUsers: PostLevelAssignedUserVO[]
  changeLogs: PostLevelChangeLogVO[]
}

export interface DemoPostPageQuery {
  pageNo: number
  pageSize: number
  name?: string
  level?: string
  deptId?: number
  keyPosition?: boolean
}

export interface DemoOrgTreeNode {
  id: string
  type: 'root' | 'dept' | 'post'
  deptId?: number
  postId?: number
  parentId?: number
  label: string
  fullPath?: string
  sort?: number
  depth?: number
  childDeptCount?: number
  directPostCount?: number
  postCount?: number
  assignedUserCount?: number
  staffQuota?: number
  status?: number
  level?: string
  children?: DemoOrgTreeNode[]
}

interface DemoDeptFlatItem {
  id: number
  name: string
  parentId?: number
  pathNames: string[]
  sort?: number
  status?: number
}

interface DemoPostSeedAssignment {
  userId: number
  primary?: boolean
  remark?: string
}

interface DemoPostSeed {
  deptId: number
  name: string
  level: string
  staffQuota: number
  keyPosition?: boolean
  allowPartTime?: boolean
  remark?: string
  jobDescription?: string
  assignedUsers?: DemoPostSeedAssignment[]
}

const COMMON_ENABLE = 0
const ROOT_DEPT_ID = 1
const ROOT_LABEL = '公司'
const DEFAULT_DEPT_ID = 110

const departments: DemoDeptOption[] = [
  {
    id: ROOT_DEPT_ID,
    name: '公司',
    children: [
      {
        id: 100,
        name: '综合职能中心',
        parentId: ROOT_DEPT_ID,
        children: [
          { id: 110, name: '综合部', parentId: 100 },
          { id: 120, name: '财务部', parentId: 100 },
          { id: 130, name: '人力资源部', parentId: 100 },
          { id: 140, name: '供应链部', parentId: 100 },
          { id: 150, name: '工艺部', parentId: 100 },
          { id: 160, name: '市场营销部', parentId: 100 },
          { id: 170, name: '质量部', parentId: 100 },
          { id: 180, name: '专家办', parentId: 100 }
        ]
      },
      {
        id: 200,
        name: '研发中心',
        parentId: ROOT_DEPT_ID,
        children: [
          {
            id: 210,
            name: '系统部',
            parentId: 200,
            children: [
              { id: 211, name: '调测组', parentId: 210 },
              { id: 212, name: '测试组', parentId: 210 },
              { id: 213, name: '射频组', parentId: 210 },
              { id: 214, name: '结构组', parentId: 210 }
            ]
          },
          {
            id: 220,
            name: '软件部',
            parentId: 200,
            children: [
              { id: 221, name: 'C++研发组', parentId: 220 },
              { id: 222, name: '嵌入式组', parentId: 220 },
              { id: 223, name: '信息化组', parentId: 220 }
            ]
          },
          {
            id: 230,
            name: '硬件部',
            parentId: 200,
            children: [
              { id: 231, name: 'FPGA组', parentId: 230 },
              { id: 232, name: 'PCB组', parentId: 230 },
              { id: 233, name: '器件组', parentId: 230 },
              { id: 234, name: '研发助理组', parentId: 230 },
              { id: 235, name: '硬件设计组', parentId: 230 }
            ]
          }
        ]
      },
      {
        id: 300,
        name: '制造中心',
        parentId: ROOT_DEPT_ID,
        children: [
          { id: 310, name: '生产管理', parentId: 300 },
          { id: 320, name: '后勤保障', parentId: 300 },
          { id: 330, name: 'SMT组', parentId: 300 },
          { id: 340, name: '电装组', parentId: 300 },
          { id: 350, name: '粘接1组', parentId: 300 },
          { id: 360, name: '粘接2组', parentId: 300 },
          { id: 370, name: '钎焊组', parentId: 300 },
          { id: 380, name: '键合组', parentId: 300 },
          { id: 390, name: '激光封焊组', parentId: 300 }
        ]
      }
    ]
  }
]

const flatDepartments: DemoDeptFlatItem[] = []
const deptChildrenMap = new Map<number, number[]>()

const buildDeptIndex = (nodes: DemoDeptOption[], parentId?: number, parentPath: string[] = []) => {
  nodes.forEach((node) => {
    const pathNames = [...parentPath, node.name]
    flatDepartments.push({
      id: node.id,
      name: node.name,
      parentId,
      pathNames,
      sort: node.sort,
      status: node.status
    })
    if (parentId !== undefined) {
      const siblings = deptChildrenMap.get(parentId) || []
      siblings.push(node.id)
      deptChildrenMap.set(parentId, siblings)
    }
    if (node.children?.length) {
      buildDeptIndex(node.children, node.id, pathNames)
    }
  })
}

buildDeptIndex(departments)

const deptMap = new Map(flatDepartments.map((item) => [item.id, item]))

const candidateUsers: PostLevelAssignedUserVO[] = [
  { userId: 101, username: 'zhangli', nickname: '张丽', mobile: '13800000001', status: COMMON_ENABLE, primary: true },
  { userId: 102, username: 'wanghao', nickname: '王浩', mobile: '13800000002', status: COMMON_ENABLE, primary: true },
  { userId: 103, username: 'chenyi', nickname: '陈毅', mobile: '13800000003', status: COMMON_ENABLE, primary: true },
  { userId: 104, username: 'liyun', nickname: '李云', mobile: '13800000004', status: COMMON_ENABLE, primary: true },
  { userId: 105, username: 'sunjie', nickname: '孙洁', mobile: '13800000005', status: COMMON_ENABLE, primary: true },
  { userId: 106, username: 'zhouqi', nickname: '周琪', mobile: '13800000006', status: COMMON_ENABLE, primary: true }
]

const createAssigned = (userId: number, primary: boolean, startDate: string, remark?: string): PostLevelAssignedUserVO => {
  const base = candidateUsers.find((item) => item.userId === userId)
  return {
    userId,
    username: base?.username || `user_${userId}`,
    nickname: base?.nickname || `人员${userId}`,
    mobile: base?.mobile || '',
    status: COMMON_ENABLE,
    primary,
    startDate,
    remark
  }
}

const createLog = (userId: number, action: string, actionTime: string, remark?: string): PostLevelChangeLogVO => {
  const base = candidateUsers.find((item) => item.userId === userId)
  return {
    userId,
    nickname: base?.nickname || `人员${userId}`,
    action,
    actionTime,
    remark
  }
}

const buildStartDate = (postIndex: number, assignIndex: number) => {
  const month = ((postIndex + assignIndex) % 9) + 1
  const day = ((postIndex * 3 + assignIndex * 2) % 20) + 1
  return `2026-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')} 09:00:00`
}

const createDefaultDescription = (name: string, deptId: number) => {
  const deptName = deptMap.get(deptId)?.name || '当前部门'
  return `负责 ${deptName} 中与【${name}】相关的日常工作、协同推进与结果交付。`
}

const buildAssignedUsers = (seed: DemoPostSeed, postIndex: number) =>
  (seed.assignedUsers || []).map((item, assignIndex) =>
    createAssigned(item.userId, item.primary ?? true, buildStartDate(postIndex, assignIndex), item.remark)
  )

const buildChangeLogs = (assignedUsers: PostLevelAssignedUserVO[]) =>
  assignedUsers.map((item) =>
    createLog(item.userId, item.primary ? '主岗任职' : '兼岗任职', item.startDate || '2026-04-16 10:00:00', item.remark)
  )

const postSeeds: DemoPostSeed[] = [
  { deptId: 110, name: '项目助理', level: '初级', staffQuota: 1, allowPartTime: true },
  { deptId: 120, name: '会计', level: '中级', staffQuota: 1, keyPosition: true, assignedUsers: [{ userId: 101 }] },
  { deptId: 120, name: '出纳', level: '初级', staffQuota: 1 },
  { deptId: 130, name: 'IT', level: '中级', staffQuota: 1, keyPosition: true, assignedUsers: [{ userId: 104 }] },
  { deptId: 130, name: '行政', level: '初级', staffQuota: 1, allowPartTime: true },
  { deptId: 130, name: '培训', level: '初级', staffQuota: 1, allowPartTime: true },
  { deptId: 130, name: '招聘', level: '中级', staffQuota: 1, keyPosition: true, assignedUsers: [{ userId: 105 }] },
  { deptId: 130, name: '保密专员', level: '中级', staffQuota: 1 },
  { deptId: 140, name: '采购工程师', level: '中级', staffQuota: 2, keyPosition: true, assignedUsers: [{ userId: 103 }] },
  { deptId: 140, name: '库房专员', level: '初级', staffQuota: 1, assignedUsers: [{ userId: 106 }] },
  { deptId: 150, name: '工艺工程师', level: '中级', staffQuota: 2, keyPosition: true, assignedUsers: [{ userId: 102 }] },
  { deptId: 160, name: '客户经理', level: '中级', staffQuota: 2, assignedUsers: [{ userId: 101 }] },
  { deptId: 160, name: '销售经理', level: '中级', staffQuota: 2, keyPosition: true, assignedUsers: [{ userId: 102 }] },
  { deptId: 160, name: '销售助理', level: '初级', staffQuota: 1, allowPartTime: true },
  { deptId: 160, name: '销售总监', level: '高级', staffQuota: 1, keyPosition: true, assignedUsers: [{ userId: 103 }] },
  { deptId: 160, name: '体系工程师', level: '中级', staffQuota: 1, assignedUsers: [{ userId: 104 }] },
  { deptId: 160, name: '质量助理', level: '初级', staffQuota: 1, allowPartTime: true },
  { deptId: 170, name: '检验员', level: '初级', staffQuota: 4, assignedUsers: [{ userId: 105 }, { userId: 106 }] },
  { deptId: 180, name: '售前技术支持', level: '中级', staffQuota: 2, keyPosition: true, assignedUsers: [{ userId: 101 }] },
  { deptId: 211, name: '测试技服员', level: '初级', staffQuota: 2, assignedUsers: [{ userId: 106 }] },
  { deptId: 211, name: '微组装调测工程师', level: '中级', staffQuota: 1, keyPosition: true, assignedUsers: [{ userId: 103 }] },
  { deptId: 212, name: '产品调试工程师', level: '中级', staffQuota: 2, assignedUsers: [{ userId: 104 }] },
  { deptId: 212, name: '软件调试工程师', level: '中级', staffQuota: 2, assignedUsers: [{ userId: 105 }] },
  { deptId: 212, name: '整机测试工程师', level: '中级', staffQuota: 2, assignedUsers: [{ userId: 106 }] },
  { deptId: 213, name: '射频工程师', level: '中级', staffQuota: 1, keyPosition: true, assignedUsers: [{ userId: 102 }] },
  { deptId: 214, name: '结构工程师', level: '中级', staffQuota: 1, keyPosition: true, assignedUsers: [{ userId: 103 }] },
  { deptId: 221, name: 'C++研发工程师', level: '中级', staffQuota: 2, keyPosition: true, assignedUsers: [{ userId: 104 }] },
  { deptId: 222, name: '嵌入式工程师', level: '中级', staffQuota: 3, keyPosition: true, assignedUsers: [{ userId: 105 }] },
  { deptId: 223, name: '信息化工程师', level: '中级', staffQuota: 1, keyPosition: true, assignedUsers: [{ userId: 104 }] },
  { deptId: 231, name: 'FPGA工程师', level: '中级', staffQuota: 1, keyPosition: true, assignedUsers: [{ userId: 103 }] },
  { deptId: 232, name: 'PCB工程师', level: '中级', staffQuota: 1, keyPosition: true, assignedUsers: [{ userId: 102 }] },
  { deptId: 233, name: '器件工程师', level: '中级', staffQuota: 1, keyPosition: true },
  { deptId: 234, name: '研发助理', level: '初级', staffQuota: 1, allowPartTime: true },
  { deptId: 235, name: '硬件工程师', level: '中级', staffQuota: 2, keyPosition: true, assignedUsers: [{ userId: 101 }] },
  { deptId: 310, name: '生产计划员', level: '中级', staffQuota: 1, keyPosition: true, assignedUsers: [{ userId: 105 }] },
  { deptId: 320, name: '后勤员', level: '初级', staffQuota: 1 },
  { deptId: 320, name: '物流员', level: '初级', staffQuota: 1, assignedUsers: [{ userId: 106 }] },
  { deptId: 330, name: 'SMT', level: '初级', staffQuota: 3, assignedUsers: [{ userId: 102, remark: '生产支援' }] },
  { deptId: 340, name: '电装', level: '初级', staffQuota: 4 },
  { deptId: 350, name: '粘片操作员', level: '初级', staffQuota: 2, assignedUsers: [{ userId: 103 }] },
  { deptId: 350, name: '辅助操作员（包装/清洗）', level: '初级', staffQuota: 2 },
  { deptId: 360, name: '粘片操作员', level: '初级', staffQuota: 2 },
  { deptId: 370, name: '钎焊操作员', level: '初级', staffQuota: 2 },
  { deptId: 380, name: '键合操作员', level: '初级', staffQuota: 2 },
  { deptId: 390, name: '激光封焊', level: '初级', staffQuota: 2, assignedUsers: [{ userId: 104 }] }
]

const initialTemplates: DemoPostTemplate[] = postSeeds.map((seed, index) => {
  const assignedUsers = buildAssignedUsers(seed, index)
  return {
    id: index + 1,
    deptId: seed.deptId,
    code: `post-${seed.deptId}-${index + 1}`,
    name: seed.name,
    level: seed.level,
    staffQuota: seed.staffQuota,
    keyPosition: !!seed.keyPosition,
    allowPartTime: !!seed.allowPartTime,
    sort: (index + 1) * 10,
    status: COMMON_ENABLE,
    jobDescription: seed.jobDescription || createDefaultDescription(seed.name, seed.deptId),
    remark: seed.remark || '',
    assignedUsers,
    changeLogs: buildChangeLogs(assignedUsers)
  }
})

const state = reactive({
  departments,
  templates: initialTemplates
})

const clone = <T>(value: T): T => JSON.parse(JSON.stringify(value))

const getDescendantDeptIds = (deptId: number): number[] => {
  const children = deptChildrenMap.get(deptId) || []
  return children.reduce<number[]>((all, childId) => [...all, childId, ...getDescendantDeptIds(childId)], [])
}

const getScopedDeptIds = (deptId?: number) => {
  if (!deptId) return flatDepartments.map((item) => item.id)
  return [deptId, ...getDescendantDeptIds(deptId)]
}

const getScopedTemplates = (deptId?: number) => {
  const scopedDeptIds = new Set(getScopedDeptIds(deptId))
  return state.templates.filter((item) => scopedDeptIds.has(item.deptId))
}

const getVacancyCount = (template: DemoPostTemplate) => {
  const primaryCount = template.assignedUsers.filter((item) => item.primary).length
  return Math.max(template.staffQuota - primaryCount, 0)
}

const getRiskTags = (template: DemoPostTemplate) => {
  const tags: string[] = []
  if (getVacancyCount(template) > 0) tags.push('有编制缺口')
  if (template.keyPosition) tags.push('关键岗位')
  if (template.keyPosition && template.assignedUsers.length <= 1) tags.push('后备不足')
  if (template.assignedUsers.some((item) => !item.primary)) tags.push('存在兼岗')
  return tags
}

const getLevelOrder = () => ['高级', '中级', '初级', '未分级']

const getDeptFullName = (deptId?: number) => {
  if (!deptId) return ''
  return deptMap.get(deptId)?.pathNames.join(' / ') || ''
}

const getDeptPostSummary = (deptId?: number) => {
  const scoped = getScopedTemplates(deptId)
  return scoped.reduce(
    (summary, item) => {
      summary.postCount += 1
      summary.staffQuota += item.staffQuota
      summary.assignedUserCount += item.assignedUsers.length
      return summary
    },
    { postCount: 0, staffQuota: 0, assignedUserCount: 0 }
  )
}

export const listDemoDeptOptions = () => clone(state.departments)

export const listDemoUserOptions = () => clone(candidateUsers)

export const getDemoDeptRootId = () => ROOT_DEPT_ID

export const getDemoPostTemplate = (postId: number): DemoPostTemplate | undefined => {
  const row = state.templates.find((item) => item.id === postId)
  return row ? { ...clone(row), deptName: getDeptFullName(row.deptId) } : undefined
}

export const getDemoDashboard = (deptId?: number): PostLevelDashboardVO => {
  const scoped = getScopedTemplates(deptId)
  const levelSummaries = getLevelOrder()
    .map((level) => {
      const rows = scoped.filter((item) => item.level === level)
      if (!rows.length) return null
      return {
        level,
        postCount: rows.length,
        assignedUserCount: rows.reduce((sum, item) => sum + item.assignedUsers.length, 0),
        vacancyCount: rows.reduce((sum, item) => sum + getVacancyCount(item), 0)
      }
    })
    .filter(Boolean) as PostLevelDashboardVO['levelSummaries']

  const riskTips = scoped
    .flatMap((item) => {
      const tips: string[] = []
      if (getVacancyCount(item) > 0) tips.push(`岗位【${item.name}】存在 ${getVacancyCount(item)} 个编制缺口`)
      if (item.keyPosition && item.assignedUsers.length <= 1) tips.push(`关键岗位【${item.name}】后备力量不足`)
      if (item.assignedUsers.some((user) => !user.primary)) tips.push(`岗位【${item.name}】存在兼岗配置`)
      return tips
    })
    .slice(0, 6)

  return {
    postCount: scoped.length,
    assignedUserCount: scoped.reduce((sum, item) => sum + item.assignedUsers.length, 0),
    vacancyCount: scoped.reduce((sum, item) => sum + getVacancyCount(item), 0),
    keyPostCount: scoped.filter((item) => item.keyPosition).length,
    partTimeUserCount: scoped.reduce((sum, item) => sum + item.assignedUsers.filter((user) => !user.primary).length, 0),
    noBackupCount: scoped.filter((item) => item.keyPosition && item.assignedUsers.length <= 1).length,
    levelSummaries,
    riskTips
  }
}

export const getDemoTree = (deptId?: number): PostLevelTreeNodeVO[] => {
  const scoped = getScopedTemplates(deptId)
  return getLevelOrder()
    .map((level) => {
      const rows = scoped.filter((item) => item.level === level)
      if (!rows.length) return null
      return {
        id: `level-${deptId || 'all'}-${level}`,
        label: level,
        type: 'level' as const,
        level,
        assignedUserCount: rows.reduce((sum, item) => sum + item.assignedUsers.length, 0),
        staffQuota: rows.reduce((sum, item) => sum + item.staffQuota, 0),
        vacancyCount: rows.reduce((sum, item) => sum + getVacancyCount(item), 0),
        children: rows
          .slice()
          .sort((a, b) => a.sort - b.sort)
          .map((item) => ({
            id: `post-${item.id}`,
            postId: item.id,
            label: `${item.name}（${deptMap.get(item.deptId)?.name || '未归属部门'}）`,
            type: 'post' as const,
            level: item.level,
            deptId: item.deptId,
            assignedUserCount: item.assignedUsers.length,
            staffQuota: item.staffQuota,
            vacancyCount: getVacancyCount(item)
          }))
      }
    })
    .filter(Boolean) as PostLevelTreeNodeVO[]
}

const buildDeptPostNode = (deptId: number, depth: number, fullPath: string): DemoOrgTreeNode | null => {
  const dept = deptMap.get(deptId)
  if (!dept) return null

  const childDeptIds = deptChildrenMap.get(deptId) || []
  const childDeptNodes = childDeptIds
    .map((childId) => buildDeptPostNode(childId, depth + 1, `${fullPath} / ${deptMap.get(childId)?.name || ''}`))
    .filter(Boolean) as DemoOrgTreeNode[]

  const directPosts = getScopedTemplates(deptId)
    .filter((item) => item.deptId === deptId)
    .sort((a, b) => a.sort - b.sort)
    .map((item) => ({
      id: `post-${item.id}`,
      type: 'post' as const,
      postId: item.id,
      deptId: item.deptId,
      parentId: deptId,
      label: item.name,
      fullPath: `${fullPath} / ${item.name}`,
      level: item.level,
      staffQuota: item.staffQuota,
      assignedUserCount: item.assignedUsers.length,
      status: item.status,
      sort: item.sort,
      depth: depth + 1
    }))

  const summary = getDeptPostSummary(deptId)
  return {
    id: `dept-${dept.id}`,
    type: 'dept',
    deptId: dept.id,
    parentId: dept.parentId,
    label: dept.name,
    fullPath,
    sort: dept.sort,
    status: dept.status,
    depth,
    childDeptCount: childDeptNodes.length,
    directPostCount: directPosts.length,
    postCount: summary.postCount,
    assignedUserCount: summary.assignedUserCount,
    staffQuota: summary.staffQuota,
    children: [...childDeptNodes, ...directPosts]
  }
}

export const getDemoOrgTree = (): DemoOrgTreeNode[] => {
  const rootChildren = (deptChildrenMap.get(ROOT_DEPT_ID) || [])
    .map((deptId) => buildDeptPostNode(deptId, 2, `${ROOT_LABEL} / ${deptMap.get(deptId)?.name || ''}`))
    .filter(Boolean) as DemoOrgTreeNode[]

  const rootSummary = getDeptPostSummary(ROOT_DEPT_ID)
  return [
    {
      id: 'root',
      type: 'root',
      label: ROOT_LABEL,
      fullPath: ROOT_LABEL,
      depth: 1,
      childDeptCount: rootChildren.length,
      directPostCount: 0,
      postCount: rootSummary.postCount,
      assignedUserCount: rootSummary.assignedUserCount,
      staffQuota: rootSummary.staffQuota,
      children: rootChildren
    }
  ]
}

export const getDemoDetail = (postId: number): PostLevelDetailVO | undefined => {
  const row = state.templates.find((item) => item.id === postId)
  if (!row) return undefined
  return {
    postId: row.id,
    code: row.code,
    name: row.name,
    level: row.level,
    deptId: row.deptId,
    deptName: getDeptFullName(row.deptId),
    status: row.status,
    sort: row.sort,
    staffQuota: row.staffQuota,
    keyPosition: row.keyPosition,
    allowPartTime: row.allowPartTime,
    jobDescription: row.jobDescription,
    remark: row.remark,
    assignedUserCount: row.assignedUsers.length,
    primaryUserCount: row.assignedUsers.filter((item) => item.primary).length,
    vacancyCount: getVacancyCount(row),
    riskTags: getRiskTags(row),
    assignedUsers: clone(row.assignedUsers),
    changeLogs: clone(row.changeLogs)
  }
}

export const queryDemoPosts = (query: DemoPostPageQuery) => {
  const filtered = getScopedTemplates(query.deptId)
    .filter((item) => !query.name || item.name.includes(query.name))
    .filter((item) => !query.level || item.level === query.level)
    .filter((item) => query.keyPosition === undefined || item.keyPosition === query.keyPosition)
    .sort((a, b) => a.sort - b.sort)

  const start = (query.pageNo - 1) * query.pageSize
  const end = start + query.pageSize
  return {
    total: filtered.length,
    list: clone(filtered.slice(start, end)).map((item: DemoPostTemplate) => ({
      ...item,
      deptName: getDeptFullName(item.deptId)
    }))
  }
}

export const saveDemoPost = (payload: Partial<DemoPostTemplate> & { id?: number }) => {
  if (payload.id) {
    const index = state.templates.findIndex((item) => item.id === payload.id)
    if (index >= 0) {
      state.templates[index] = {
        ...state.templates[index],
        ...payload
      } as DemoPostTemplate
    }
    return payload.id
  }

  const nextId = Math.max(...state.templates.map((item) => item.id), 0) + 1
  state.templates.push({
    id: nextId,
    deptId: payload.deptId || DEFAULT_DEPT_ID,
    code: payload.code || `demo-post-${nextId}`,
    name: payload.name || '新岗位',
    level: payload.level || '未分级',
    staffQuota: payload.staffQuota || 1,
    keyPosition: !!payload.keyPosition,
    allowPartTime: !!payload.allowPartTime,
    sort: payload.sort || nextId * 10,
    status: payload.status ?? COMMON_ENABLE,
    jobDescription: payload.jobDescription || '',
    remark: payload.remark || '本地演示新增岗位',
    assignedUsers: [],
    changeLogs: []
  })
  return nextId
}

export const deleteDemoPost = (postId: number) => {
  const index = state.templates.findIndex((item) => item.id === postId)
  if (index >= 0) state.templates.splice(index, 1)
}

export const saveDemoAssignments = (postId: number, assignments: PostLevelAssignUserItemVO[]) => {
  const row = state.templates.find((item) => item.id === postId)
  if (!row) return
  row.assignedUsers = assignments.filter((item) => !!item.userId).map((item) => {
    const base = candidateUsers.find((user) => user.userId === item.userId)
    return {
      userId: item.userId,
      username: base?.username || `user_${item.userId}`,
      nickname: base?.nickname || `人员${item.userId}`,
      mobile: base?.mobile || '',
      status: COMMON_ENABLE,
      primary: !!item.primary,
      startDate: item.startDate,
      endDate: item.endDate,
      remark: item.remark
    }
  })
  row.changeLogs = row.assignedUsers.slice(0, 6).map((item) => ({
    userId: item.userId,
    nickname: item.nickname,
    action: item.primary ? '主岗任职' : '兼岗任职',
    actionTime: item.startDate || '2026-04-16 10:00:00',
    remark: item.remark
  }))
}

export const getDefaultDemoPostId = (deptId?: number) => getScopedTemplates(deptId)[0]?.id
