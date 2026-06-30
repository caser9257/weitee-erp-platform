import type { App } from 'vue'
import { useUserStore } from '@/store/modules/user'

const { t } = useI18n() // 国际化
const userStore = useUserStore()
const super_admin = 'super_admin'
const all_permission = '*:*:*'

/** 判断权限的指令 directive */
export function hasPermi(app: App<Element>) {
  app.directive('hasPermi', (el, binding) => {
    const { value } = binding

    if (value && value instanceof Array && value.length > 0) {
      const hasPermissions = hasPermission(value)

      if (!hasPermissions) {
        el.parentNode && el.parentNode.removeChild(el)
      }
    } else {
      throw new Error(t('permission.hasPermission'))
    }
  })
}

/** 判断权限的方法 function */
export const hasPermission = (permission: string[]) => {
  const roles = userStore.roles || []
  return (
    roles.includes(super_admin) ||
    userStore.permissions.has(all_permission) ||
    permission.some((permission) => userStore.permissions.has(permission))
  )
}
