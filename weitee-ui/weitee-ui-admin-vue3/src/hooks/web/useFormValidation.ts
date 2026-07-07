import type { FormInstance } from 'element-plus'

/**
 * 表单校验组合式函数
 *
 * 对 Element Plus 的 formRef.value.validate() 进行封装：
 * 1. 校验失败时自动聚焦第一个错误字段
 * 2. 校验失败时弹出醒目提示（可通过参数控制）
 *
 * @example
 * const formRef = ref<FormInstance>()
 * const { validateForm } = useFormValidation(formRef)
 *
 * const submitForm = async () => {
 *   if (!await validateForm()) return
 *   // ... 提交逻辑
 * }
 */
export function useFormValidation(formRef: Ref<FormInstance | undefined>, options?: { showMessage?: boolean }) {
  const { showMessage = true } = options || {}

  const validateForm = async (): Promise<boolean> => {
    try {
      const valid = await formRef.value?.validate()
      if (!valid) {
        focusFirstError()
        if (showMessage) {
          useMessage().warning('请完善必填信息后再提交')
        }
        return false
      }
      return true
    } catch {
      focusFirstError()
      if (showMessage) {
        useMessage().warning('请完善必填信息后再提交')
      }
      return false
    }
  }

  function focusFirstError() {
    nextTick(() => {
      const root = formRef.value?.$el as HTMLElement | undefined
      const container = root || document
      const input = container.querySelector<HTMLElement>(
        '.el-form-item.is-error input:not([type="hidden"]):not([disabled]), ' +
        '.el-form-item.is-error textarea:not([disabled]), ' +
        '.el-form-item.is-error [tabindex]:not([tabindex="-1"])'
      )
      input?.focus()
    })
  }

  return { validateForm }
}