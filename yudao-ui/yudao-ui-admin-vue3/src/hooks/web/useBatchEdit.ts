import { computed, ref } from 'vue'

export interface BatchEditOpenPayload<Row = unknown> {
  ids: number[]
  rows?: Row[]
}

export const useBatchEdit = <Row = unknown>() => {
  const visible = ref(false)
  const submitting = ref(false)
  const selectedIds = ref<number[]>([])
  const selectedRows = ref<Row[]>([])

  const selectedCount = computed(() => selectedIds.value.length)
  const canSubmit = computed(() => selectedIds.value.length > 0 && !submitting.value)

  const open = (payload: BatchEditOpenPayload<Row>) => {
    selectedIds.value = Array.from(
      new Set(payload.ids.filter((item) => item !== undefined && item !== null))
    )
    selectedRows.value = Array.isArray(payload.rows) ? payload.rows.slice() : []
    submitting.value = false
    visible.value = true
  }

  const close = () => {
    if (submitting.value) {
      return
    }
    visible.value = false
    selectedIds.value = []
    selectedRows.value = []
  }

  const reset = () => {
    selectedIds.value = []
    selectedRows.value = []
    submitting.value = false
    visible.value = false
  }

  return {
    visible,
    submitting,
    selectedIds,
    selectedRows,
    selectedCount,
    canSubmit,
    open,
    close,
    reset
  }
}
