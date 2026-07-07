import { ref, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import type { App } from 'vue'

/** 当前表单提交错误提示文案（Dialog 组件注入读取） */
export const currentFormSubmitError = ref('')

/**
 * 全局表单校验增强插件
 * 效果：
 * 1. validate() 失败时自动聚焦第一个错误字段
 * 2. 弹出 warning 提示
 * 3. 通过 currentFormSubmitError 向 Dialog 传递错误横幅文案
 */
export function setupFormValidateEnhance(app: App) {
  // 获取 ElForm 组件原始定义
  const originalElForm = app.component('ElForm')
  if (!originalElForm) {
    console.warn('[formValidateEnhance] ElForm component not found, skipping')
    return
  }

  // 使用 extends 包裹 ElForm，拦截 validate 方法
  app.component('ElForm', {
    extends: originalElForm,
    mounted() {
      const self = this as any
      if (typeof self.validate !== 'function') return

      const originalValidate = self.validate.bind(self)

      // 重写 validate：失败时聚焦第一个错误字段 + 弹出提示
      self.validate = async (...args: any[]) => {
        const result = await originalValidate(...args)
        if (!result) {
          await nextTick()

          // 1. 自动聚焦到第一个错误字段
          const fields = (self as any).fields
          const firstError = fields?.find((f: any) => f.validateState === 'error')
          if (firstError) {
            try {
              firstError.focus()
            } catch {
              // focus 可能失败（如被遮挡），静默忽略
            }
          }

          // 2. 弹出 warning 提示
          ElMessage.warning('请完善必填信息后再提交')

          // 3. 向 Dialog 传递错误横幅文案
          currentFormSubmitError.value = '请完善必填信息后再提交'
        }
        return result
      }
    }
  })
}