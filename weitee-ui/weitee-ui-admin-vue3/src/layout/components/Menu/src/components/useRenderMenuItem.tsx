import { ElSubMenu, ElMenuItem } from 'element-plus'
import { hasOneShowingChild } from '../helper'
import { isUrl } from '@/utils/is'
import { useRenderMenuTitle } from './useRenderMenuTitle'
import { pathResolve } from '@/utils/routerHelper'
import GroupedFlyoutMenu from './GroupedFlyoutMenu'
import { hasGroupedMenuEntries } from '../groupedMenu'

const { renderMenuTitle } = useRenderMenuTitle()

export const useRenderMenuItem = ({
  mode,
  activeMenu,
  menuSelect,
  enableGroupedFlyout = false
}: {
  mode?: 'vertical' | 'horizontal'
  activeMenu?: string
  menuSelect?: (index: string) => void
  enableGroupedFlyout?: boolean
}) => {
    const renderRouteItem = (v: AppRouteRecordRaw, parentPath = '/', depth = 0) => {
      const meta = v.meta ?? {}
      const { oneShowingChild, onlyOneChild } = hasOneShowingChild(v.children, v)
      const fullPath = isUrl(v.path) ? v.path : pathResolve(parentPath, v.path)
      const menuItemClass = [
        'v-menu-item',
        depth === 0 ? 'v-menu-item--root' : 'v-menu-item--child',
        oneShowingChild ? 'v-menu-item--single' : ''
      ]
      const subMenuClass = [
        'v-menu-submenu',
        depth === 0 ? 'v-menu-submenu--root' : 'v-menu-submenu--child'
      ]

      if (
        oneShowingChild &&
        (!onlyOneChild?.children || onlyOneChild?.noShowingChildren) &&
        !meta?.alwaysShow
      ) {
        return (
          <ElMenuItem
            class={menuItemClass}
            index={onlyOneChild ? pathResolve(fullPath, onlyOneChild.path) : fullPath}
          >
            {{
              default: () => renderMenuTitle(onlyOneChild ? onlyOneChild?.meta : meta)
            }}
          </ElMenuItem>
        )
      }

      return (
        <ElSubMenu class={subMenuClass} index={fullPath}>
          {{
            title: () => renderMenuTitle(meta),
            default: () => renderMenuItem(v.children!, fullPath, depth + 1)
          }}
        </ElSubMenu>
      )
    }

    const renderGroupedRoutes = (
      routers: AppRouteRecordRaw[],
      parentPath = '/',
      depth = 0
    ) => {
      const groupedRouteMap = new Map<
        string,
        {
          title: string
          icon?: string
          order: number
          routes: AppRouteRecordRaw[]
        }
      >()
      const ungroupedRoutes: AppRouteRecordRaw[] = []

      for (const route of routers) {
        const groupKey = route.meta?.menuGroupKey as string | undefined
        if (!groupKey) {
          ungroupedRoutes.push(route)
          continue
        }

        if (!groupedRouteMap.has(groupKey)) {
          groupedRouteMap.set(groupKey, {
            title: (route.meta?.menuGroupTitle as string) || '',
            icon: route.meta?.menuGroupIcon as string | undefined,
            order: Number(route.meta?.menuGroupOrder ?? Number.MAX_SAFE_INTEGER),
            routes: []
          })
        }

        groupedRouteMap.get(groupKey)!.routes.push(route)
      }

      const groupedEntries = Array.from(groupedRouteMap.entries()).sort((left, right) => {
        const leftOrder = left[1].order
        const rightOrder = right[1].order
        if (leftOrder !== rightOrder) {
          return leftOrder - rightOrder
        }
        return left[0].localeCompare(right[0])
      })

      return [
        ...groupedEntries.map(([groupKey, group]) => (
          <ElSubMenu
            class={['v-menu-submenu', depth === 0 ? 'v-menu-submenu--root' : 'v-menu-submenu--child']}
            index={groupKey}
          >
            {{
              title: () =>
                renderMenuTitle({
                  title: group.title,
                  icon: group.icon
                } as any),
              default: () =>
                group.routes
                  .sort(
                    (left, right) =>
                      Number(left.meta?.menuOrder ?? Number.MAX_SAFE_INTEGER) -
                      Number(right.meta?.menuOrder ?? Number.MAX_SAFE_INTEGER)
                  )
                  .map((route) => renderRouteItem(route, parentPath, depth + 1))
            }}
          </ElSubMenu>
        )),
        ...ungroupedRoutes.map((route) => renderRouteItem(route, parentPath, depth))
      ]
    }

    const renderMenuItem = (routers: AppRouteRecordRaw[], parentPath = '/', depth = 0) => {
      const visibleRouters = routers.filter((v) => !v.meta?.hidden)
      const shouldGroupRoutes = hasGroupedMenuEntries(visibleRouters)
      const shouldUseEmbeddedGroupedFlyout =
        enableGroupedFlyout && mode === 'vertical' && depth > 0 && shouldGroupRoutes

      if (shouldUseEmbeddedGroupedFlyout) {
        return [
          <li class="v-menu-grouped-flyout-embedded">
            <GroupedFlyoutMenu
              routers={visibleRouters}
              basePath={parentPath}
              activeMenu={activeMenu || ''}
              menuSelect={menuSelect}
            ></GroupedFlyoutMenu>
          </li>
        ]
      }

      if (shouldGroupRoutes) {
        return renderGroupedRoutes(visibleRouters, parentPath, depth)
      }

      return visibleRouters.map((v) => {
        return renderRouteItem(v, parentPath, depth)
      })
    }

    return {
      renderMenuItem
    }
  }
