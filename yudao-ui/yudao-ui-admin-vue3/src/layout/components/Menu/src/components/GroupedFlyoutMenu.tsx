import { ElMenu, ElMenuItem, ElScrollbar, ElSubMenu } from 'element-plus'
import { CSSProperties, PropType, Teleport } from 'vue'
import { useRouter } from 'vue-router'
import { Icon } from '@/components/Icon'
import { useRenderMenuTitle } from './useRenderMenuTitle'
import { hasOneShowingChild } from '../helper'
import { isUrl } from '@/utils/is'
import { pathResolve } from '@/utils/routerHelper'
import { buildGroupedMenuEntries, resolveGroupedMenuActiveKey } from '../groupedMenu'

const { renderMenuTitle } = useRenderMenuTitle()

export default defineComponent({
  name: 'GroupedFlyoutMenu',
  props: {
    routers: {
      type: Array as PropType<AppRouteRecordRaw[]>,
      required: true
    },
    basePath: {
      type: String,
      default: '/'
    },
    activeMenu: {
      type: String,
      required: true
    },
    menuSelect: {
      type: Function as PropType<(index: string) => void>,
      default: undefined
    }
  },
  setup(props) {
    const { currentRoute, push } = useRouter()
    const railRef = ref<HTMLElement>()
    const panelRef = ref<HTMLElement>()
    const panelScrollbarRef = ref<any>()
    const parentSubMenuOpened = ref(true)
    let parentSubMenuObserver: MutationObserver | null = null
    let panelResizeObserver: ResizeObserver | null = null

    const groupedMenuModel = computed(() => buildGroupedMenuEntries(props.routers, props.basePath))

    const hoveredGroupKey = ref('')
    const lockedGroupKey = ref('')

    const activeGroupKey = computed(() =>
      resolveGroupedMenuActiveKey({
        groups: groupedMenuModel.value.groups,
        activeMenu: props.activeMenu,
        currentPath: currentRoute.value.path,
        routeMetaGroupKey: currentRoute.value.meta.menuGroupKey as string | undefined
      })
    )

    watch(
      () => [currentRoute.value.fullPath, props.activeMenu],
      () => {
        if (!activeGroupKey.value) {
          hoveredGroupKey.value = ''
          lockedGroupKey.value = ''
        }
      },
      { immediate: true }
    )

    const selectedGroupKey = computed(() => {
      return lockedGroupKey.value || activeGroupKey.value || ''
    })

    const displayedGroupKey = computed(() => {
      if (!parentSubMenuOpened.value) {
        return ''
      }
      return lockedGroupKey.value || hoveredGroupKey.value || ''
    })

    const displayedGroup = computed(() =>
      groupedMenuModel.value.groups.find((group) => group.key === displayedGroupKey.value)
    )

    const directRouteIndexes = computed(() =>
      groupedMenuModel.value.directRoutes.map((route) => resolveRouteIndex(route))
    )

    const railActiveIndex = computed(() => {
      const activeDirectRouteIndex = directRouteIndexes.value.find(
        (index) => index === props.activeMenu || index === currentRoute.value.path
      )
      return activeDirectRouteIndex || selectedGroupKey.value || ''
    })

    const floatingPanelStyle = ref<CSSProperties>({
      top: '0px',
      left: '0px',
      width: '420px',
      height: '360px',
      maxHeight: 'calc(100vh - 24px)',
      minHeight: '240px'
    })

    const updateFloatingPanelPosition = () => {
      const railElement = railRef.value
      if (!railElement) {
        return
      }

      const railRect = railElement.getBoundingClientRect()
      const activeRailItem =
        (railElement.querySelector('.v-grouped-flyout__rail-item.is-active') as HTMLElement | null) ||
        (railElement.querySelector('.v-grouped-flyout__rail-item.is-preview') as HTMLElement | null)
      const anchorRect = activeRailItem?.getBoundingClientRect() || railRect
      const menuContainerRect =
        railElement.closest('.v-menu')?.getBoundingClientRect() || railRect
      const viewportPadding = 12
      const panelElement = panelRef.value
      const panelHeaderElement = panelElement?.querySelector(
        '.v-grouped-flyout__panel-header'
      ) as HTMLElement | null
      const panelMenuElement = panelElement?.querySelector(
        '.v-grouped-flyout__menu'
      ) as HTMLElement | null
      const firstMenuEntryElement = panelMenuElement?.querySelector(
        '.el-menu-item, .el-sub-menu__title'
      ) as HTMLElement | null
      const headerHeight = panelHeaderElement?.offsetHeight || 72
      const menuStyle = panelMenuElement ? window.getComputedStyle(panelMenuElement) : null
      const menuPaddingTop = menuStyle ? parseFloat(menuStyle.paddingTop || '14') : 14
      const menuPaddingBottom = menuStyle ? parseFloat(menuStyle.paddingBottom || '18') : 18
      const menuEntryStyle = firstMenuEntryElement
        ? window.getComputedStyle(firstMenuEntryElement)
        : null
      const menuEntryHeight = firstMenuEntryElement?.offsetHeight || 42
      const menuEntryGap = menuEntryStyle ? parseFloat(menuEntryStyle.marginBottom || '10') : 10
      const visibleRowCount = 4
      const menuViewportHeight =
        menuPaddingTop +
        menuPaddingBottom +
        visibleRowCount * menuEntryHeight +
        Math.max(0, visibleRowCount - 1) * menuEntryGap
      const naturalPanelHeight = headerHeight + menuViewportHeight
      const desiredTop = anchorRect.top - 8
      const viewportAvailableHeight = Math.max(
        220,
        window.innerHeight - viewportPadding * 2
      )
      const preferredPanelWidth = 420
      const panelWidth = Math.min(
        preferredPanelWidth,
        Math.max(320, window.innerWidth - menuContainerRect.right - 24)
      )
      const panelLeft = Math.max(
        viewportPadding,
        Math.min(menuContainerRect.right + 12, window.innerWidth - panelWidth - viewportPadding)
      )
      const panelMaxHeight = viewportAvailableHeight
      const panelMinHeight = Math.min(240, panelMaxHeight)
      const panelHeight = Math.max(
        panelMinHeight,
        Math.min(panelMaxHeight, naturalPanelHeight)
      )
      const panelTop = Math.max(
        viewportPadding,
        Math.min(desiredTop, window.innerHeight - panelHeight - viewportPadding)
      )

      floatingPanelStyle.value = {
        top: `${panelTop}px`,
        left: `${panelLeft}px`,
        width: `${panelWidth}px`,
        height: `${panelHeight}px`,
        maxHeight: `${panelMaxHeight}px`,
        minHeight: `${panelMinHeight}px`
      }
    }

    const handleGlobalScroll = (event: Event) => {
      const target = event.target as Node | null
      if (target && panelRef.value?.contains(target)) {
        return
      }
      updateFloatingPanelPosition()
    }

    const clearFloatingState = () => {
      hoveredGroupKey.value = ''
      lockedGroupKey.value = ''
    }

    const syncParentSubMenuOpened = () => {
      const parentSubMenuElement = railRef.value?.closest('.el-sub-menu') as HTMLElement | null
      parentSubMenuOpened.value = parentSubMenuElement
        ? parentSubMenuElement.classList.contains('is-opened')
        : true

      if (!parentSubMenuOpened.value) {
        hoveredGroupKey.value = ''
        lockedGroupKey.value = ''
      }
    }

    const handleGlobalPointerDown = (event: Event) => {
      const target = event.target as Node | null
      if (!target) {
        return
      }

      if (railRef.value?.contains(target) || panelRef.value?.contains(target)) {
        return
      }

      clearFloatingState()
    }

    onMounted(() => {
      nextTick(updateFloatingPanelPosition)
      nextTick(syncParentSubMenuOpened)
      window.addEventListener('resize', updateFloatingPanelPosition)
      window.addEventListener('scroll', handleGlobalScroll, true)
      document.addEventListener('pointerdown', handleGlobalPointerDown, true)

      const parentSubMenuElement = railRef.value?.closest('.el-sub-menu') as HTMLElement | null
      if (parentSubMenuElement) {
        parentSubMenuObserver = new MutationObserver(() => {
          syncParentSubMenuOpened()
        })
        parentSubMenuObserver.observe(parentSubMenuElement, {
          attributes: true,
          attributeFilter: ['class']
        })
      }

      panelResizeObserver = new ResizeObserver(() => {
        updateFloatingPanelPosition()
      })
      if (panelRef.value) {
        panelResizeObserver.observe(panelRef.value)
      }
    })

    onBeforeUnmount(() => {
      window.removeEventListener('resize', updateFloatingPanelPosition)
      window.removeEventListener('scroll', handleGlobalScroll, true)
      document.removeEventListener('pointerdown', handleGlobalPointerDown, true)
      parentSubMenuObserver?.disconnect()
      parentSubMenuObserver = null
      panelResizeObserver?.disconnect()
      panelResizeObserver = null
    })

    watch(
      () => [displayedGroupKey.value, currentRoute.value.fullPath],
      () => {
        nextTick(() => {
          panelScrollbarRef.value?.setScrollTop?.(0)
          updateFloatingPanelPosition()
        })
      },
      { immediate: true }
    )

    const handleSelect = (index: string) => {
      hoveredGroupKey.value = ''
      lockedGroupKey.value = ''

      if (props.menuSelect) {
        props.menuSelect(index)
        return
      }

      if (isUrl(index)) {
        window.open(index)
      } else {
        push(index)
      }
    }

    const handleGroupMouseenter = (groupKey: string) => {
      if (!parentSubMenuOpened.value) {
        return
      }
      if (lockedGroupKey.value) {
        return
      }
      hoveredGroupKey.value = groupKey
    }

    const handleGroupClick = (groupKey: string) => {
      lockedGroupKey.value = lockedGroupKey.value === groupKey ? '' : groupKey
      hoveredGroupKey.value = ''
    }

    const handleMouseleave = () => {
      hoveredGroupKey.value = ''
    }

    const resolveRouteIndex = (route: AppRouteRecordRaw) => {
      return isUrl(route.path) ? route.path : pathResolve(props.basePath, route.path)
    }

    const handleRailSelect = (index: string) => {
      if (directRouteIndexes.value.includes(index)) {
        hoveredGroupKey.value = ''
        lockedGroupKey.value = ''
        handleSelect(index)
        return
      }

      handleGroupClick(index)
    }

    const renderRouteItem = (route: AppRouteRecordRaw, parentPath = '/', depth = 0) => {
      const meta = route.meta ?? {}
      const { oneShowingChild, onlyOneChild } = hasOneShowingChild(route.children, route)
      const fullPath = isUrl(route.path) ? route.path : pathResolve(parentPath, route.path)
      const menuItemClass = [
        'v-menu-item',
        'v-grouped-flyout__menu-item',
        depth === 0 ? 'v-menu-item--root' : 'v-menu-item--child',
        oneShowingChild ? 'v-menu-item--single' : ''
      ]
      const subMenuClass = [
        'v-menu-submenu',
        'v-grouped-flyout__submenu',
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
        <ElSubMenu
          class={subMenuClass}
          index={fullPath}
          popperClass="v-grouped-flyout__submenu-popper"
          teleported={true}
        >
          {{
            title: () => renderMenuTitle(meta),
            default: () => route.children?.map((child) => renderRouteItem(child, fullPath, depth + 1))
          }}
        </ElSubMenu>
      )
    }

    return () => (
      <div class="v-grouped-flyout" onMouseleave={handleMouseleave}>
        <div ref={railRef} class="v-grouped-flyout__rail">
          <ElMenu
            key={`${railActiveIndex.value}|${displayedGroupKey.value}`}
            class="v-grouped-flyout__rail-menu"
            defaultActive={railActiveIndex.value}
            mode="vertical"
            collapse={false}
            uniqueOpened={false}
            backgroundColor="transparent"
            textColor="var(--left-menu-text-color)"
            activeTextColor="var(--left-menu-text-active-color)"
            onSelect={handleRailSelect}
          >
            {{
              default: () => [
                ...groupedMenuModel.value.groups.map((group) => (
                  <ElMenuItem
                    index={group.key}
                    class={[
                      'v-grouped-flyout__rail-item',
                      'v-menu-item',
                      'v-menu-item--child',
                      {
                        'is-preview':
                          hoveredGroupKey.value === group.key && railActiveIndex.value !== group.key
                      }
                    ]}
                    onMouseenter={() => handleGroupMouseenter(group.key)}
                    onFocus={() => handleGroupMouseenter(group.key)}
                  >
                    {{
                      default: () =>
                        renderMenuTitle({
                          title: group.title,
                          icon: group.icon
                        } as any)
                    }}
                  </ElMenuItem>
                )),
                ...groupedMenuModel.value.directRoutes.map((route) => (
                  <ElMenuItem
                    index={resolveRouteIndex(route)}
                    class={['v-grouped-flyout__rail-item', 'v-menu-item', 'v-menu-item--child']}
                  >
                    {{
                      default: () => renderMenuTitle(route.meta ?? {})
                    }}
                  </ElMenuItem>
                ))
              ]
            }}
          </ElMenu>
        </div>

        {displayedGroup.value && displayedGroupKey.value ? (
          <Teleport to="body">
            <div
              ref={panelRef}
              class="v-grouped-flyout__panel v-grouped-flyout__panel--floating"
              style={floatingPanelStyle.value}
            >
              <div class="v-grouped-flyout__panel-header">
                <div class="v-grouped-flyout__panel-header-main">
                  {displayedGroup.value.icon ? (
                    <span class="v-grouped-flyout__panel-icon">
                      <Icon icon={displayedGroup.value.icon}></Icon>
                    </span>
                  ) : undefined}
                  <span class="v-grouped-flyout__panel-title">{displayedGroup.value.title}</span>
                </div>
              </div>
              <ElScrollbar
                ref={panelScrollbarRef}
                class="v-grouped-flyout__panel-body"
                always
              >
                <ElMenu
                  key={`${props.activeMenu}|${displayedGroup.value.key}`}
                  class="v-grouped-flyout__menu"
                  defaultActive={props.activeMenu}
                  defaultOpeneds={[displayedGroup.value.key]}
                  mode="vertical"
                  collapse={false}
                  uniqueOpened={false}
                  backgroundColor="transparent"
                  textColor="var(--left-menu-text-color)"
                  activeTextColor="var(--left-menu-text-active-color)"
                  onSelect={handleSelect}
                >
                  {{
                    default: () =>
                      displayedGroup.value?.routes.map((route) =>
                        renderRouteItem(route, props.basePath, 0)
                      )
                  }}
                </ElMenu>
              </ElScrollbar>
            </div>
          </Teleport>
        ) : undefined}
      </div>
    )
  }
})
