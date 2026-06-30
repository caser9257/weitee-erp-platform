export type WorkplaceTotal = {
  project: number
  access: number
  todo: number
}

export type MetricVariant = 'normal' | 'warning' | 'success' | 'alert'
export type TrendType = 'positive' | 'negative'

export type MetricCard = {
  title: string
  value: string
  trend?: string
  trendType: TrendType
  subtext?: string
  noteTone?: string
  variant: MetricVariant
  icon: string
}

export type TodoLevel = 'urgent' | 'normal'

export type TodoItem = {
  title: string
  initiator: string
  duration: string
  icon: string
  level: TodoLevel
}

export type ProjectStageType = 'cyan' | 'amber' | 'emerald' | 'slate'

export type Project = {
  name: string
  icon: string
  stage: string
  stageType: ProjectStageType
  time: Date | number | string
  color: string
}

export type Notice = {
  title: string
  date: Date | number | string
}

export type Shortcut = {
  name: string
  icon: string
  url: string
  color: string
}
