<script lang="tsx">
import { PropType } from 'vue'
import { ElMenu, ElScrollbar } from 'element-plus'
import { useAppStore } from '@/store/modules/app'
import { usePermissionStore } from '@/store/modules/permission'
import { useRenderMenuItem } from './components/useRenderMenuItem'
import GroupedFlyoutMenu from './components/GroupedFlyoutMenu'
import { isUrl } from '@/utils/is'
import { useDesign } from '@/hooks/web/useDesign'
import { LayoutType } from '@/types/layout'

const { getPrefixCls } = useDesign()

const prefixCls = getPrefixCls('menu')

export default defineComponent({
  // eslint-disable-next-line vue/no-reserved-component-names
  name: 'Menu',
  props: {
    menuSelect: {
      type: Function as PropType<(index: string) => void>,
      default: undefined
    }
  },
  setup(props) {
    const appStore = useAppStore()

    const layout = computed(() => appStore.getLayout)

    const { push, currentRoute } = useRouter()

    const permissionStore = usePermissionStore()

    const menuMode = computed((): 'vertical' | 'horizontal' => {
      // 竖
      const vertical: LayoutType[] = ['classic', 'topLeft', 'cutMenu']

      if (vertical.includes(unref(layout))) {
        return 'vertical'
      } else {
        return 'horizontal'
      }
    })

    const routers = computed(() =>
      unref(layout) === 'cutMenu' ? permissionStore.getMenuTabRouters : permissionStore.getRouters
    )

    const collapse = computed(() => appStore.getCollapse)

    const uniqueOpened = computed(() => appStore.getUniqueOpened)

    const mobile = computed(() => appStore.getMobile)

    const shouldUseGroupedFlyout = computed(() => {
      const isSupportedVerticalLayout = ['classic', 'topLeft', 'cutMenu'].includes(unref(layout))
      const canKeepRailExpanded = unref(layout) === 'cutMenu' || !unref(collapse)
      return (
        isSupportedVerticalLayout &&
        !unref(mobile) &&
        canKeepRailExpanded &&
        unref(routers).some((route) => route.meta?.menuGroupKey)
      )
    })

    const activeMenu = computed(() => {
      const { meta, path } = unref(currentRoute)
      // if set path, the sidebar will highlight the path you set
      if (meta.activeMenu) {
        return meta.activeMenu as string
      }
      return path
    })

    const defaultOpeneds = computed(() => {
      const { path: currentPath, meta } = unref(currentRoute)
      if (!currentPath || currentPath === '/') {
        return []
      }

      const segments = currentPath.split('/').filter(Boolean)
      const openedPaths = segments.map((_, index) => `/${segments.slice(0, index + 1).join('/')}`)
      const menuGroupKey = meta.menuGroupKey as string | undefined
      if (menuGroupKey) {
        openedPaths.push(menuGroupKey)
      }

      return Array.from(new Set(openedPaths))
    })

    const menuRenderKey = computed(() => `${unref(activeMenu)}|${defaultOpeneds.value.join(',')}`)

    const menuSelect = (index: string) => {
      if (props.menuSelect) {
        props.menuSelect(index)
      }
      // 自定义事件
      if (isUrl(index)) {
        window.open(index)
      } else {
        push(index)
      }
    }

    const renderMenuWrap = () => {
      if (unref(layout) === 'top' || unref(shouldUseGroupedFlyout)) {
        return renderMenu()
      } else {
        return <ElScrollbar class={`${prefixCls}__scrollbar`}>{renderMenu()}</ElScrollbar>
      }
    }

    const renderMenu = () => {
      const routeList = unref(routers)
      if (unref(shouldUseGroupedFlyout)) {
        return (
          <GroupedFlyoutMenu
            routers={routeList}
            basePath="/"
            activeMenu={unref(activeMenu)}
            menuSelect={menuSelect}
          ></GroupedFlyoutMenu>
        )
      }

      return (
        <ElMenu
          key={unref(menuRenderKey)}
          class={`${prefixCls}__content`}
          defaultActive={unref(activeMenu)}
          defaultOpeneds={defaultOpeneds.value}
          mode={unref(menuMode)}
          collapse={
            unref(layout) === 'top' || unref(layout) === 'cutMenu' ? false : unref(collapse)
          }
          uniqueOpened={unref(layout) === 'top' ? false : unref(uniqueOpened)}
          backgroundColor="var(--left-menu-bg-color)"
          textColor="var(--left-menu-text-color)"
          activeTextColor="var(--left-menu-text-active-color)"
          popperClass={
            unref(menuMode) === 'vertical'
              ? `${prefixCls}-popper--vertical`
              : `${prefixCls}-popper--horizontal`
          }
          onSelect={menuSelect}
        >
          {{
            default: () => {
              const { renderMenuItem } = useRenderMenuItem({
                mode: unref(menuMode),
                activeMenu: unref(activeMenu),
                menuSelect,
                enableGroupedFlyout: !unref(mobile)
              })
              return renderMenuItem(routeList)
            }
          }}
        </ElMenu>
      )
    }

    return () => (
      <div
        id={prefixCls}
        class={[
          `${prefixCls} ${prefixCls}__${unref(menuMode)}`,
          'h-[100%] flex-col bg-[var(--left-menu-bg-color)]',
          {
            'overflow-hidden': !unref(shouldUseGroupedFlyout),
            'w-[var(--left-menu-min-width)]': unref(collapse) && unref(layout) !== 'cutMenu',
            'w-[var(--left-menu-max-width)]': !unref(collapse) && unref(layout) !== 'cutMenu',
            'v-menu--grouped-flyout': unref(shouldUseGroupedFlyout),
            [`v-menu--grouped-flyout-${unref(layout)}`]: unref(shouldUseGroupedFlyout)
          }
        ]}
        style={unref(shouldUseGroupedFlyout) ? "overflow: visible !important;" : "transition: width var(--transition-time-02);"}
      >
        {renderMenuWrap()}
      </div>
    )
  }
})
</script>

<style lang="scss" scoped>
$prefix-cls: #{$namespace}-menu;

.#{$prefix-cls} {
  position: relative;
  transition: width var(--transition-time-02);
  background:
    radial-gradient(circle at top left, rgb(56 189 248 / 0.12), transparent 24%),
    linear-gradient(180deg, #13243a 0%, #0c1829 58%, #09111d 100%);
  border-right: 1px solid rgb(148 163 184 / 0.12);
  box-shadow: inset -1px 0 0 rgb(255 255 255 / 0.03);

  :deep(.#{$elNamespace}-scrollbar__wrap) {
    overflow-x: hidden;
  }

  &.v-menu--grouped-flyout {
    overflow: visible;
    z-index: 80;
    isolation: isolate;

    :deep(.#{$elNamespace}-scrollbar),
    :deep(.#{$elNamespace}-scrollbar__wrap),
    :deep(.#{$elNamespace}-scrollbar__view) {
      overflow: visible !important;
    }
  }

  :deep(.#{$elNamespace}-menu) {
    width: 100% !important;
    border-right: none;
    background: transparent !important;
  }

  :deep(.#{$prefix-cls}__content) {
    padding: 14px 12px 20px;

    .#{$elNamespace}-sub-menu__title,
    .#{$elNamespace}-menu-item {
      position: relative;
      margin: 0 0 8px;
      border-radius: 16px;
      padding-right: 14px !important;
      transition:
        background-color var(--transition-time-02),
        color var(--transition-time-02),
        box-shadow var(--transition-time-02),
        transform var(--transition-time-02);

      &:hover {
        color: #f8fbff !important;
        background-color: rgb(30 64 175 / 0.28) !important;
        transform: translateX(2px);
      }
    }

    .v-menu-submenu--root > .#{$elNamespace}-sub-menu__title,
    .v-menu-item--root {
      min-height: 52px;
      padding-left: 16px !important;
      font-size: 14px;
      font-weight: 600;
      color: rgb(226 232 240 / 0.9) !important;
      background: linear-gradient(180deg, rgb(255 255 255 / 0.05), rgb(255 255 255 / 0.02))
        !important;
      border: 1px solid rgb(148 163 184 / 0.1);
      box-shadow: inset 0 1px 0 rgb(255 255 255 / 0.03);
    }

    .v-menu-submenu--root.is-opened > .#{$elNamespace}-sub-menu__title {
      color: #f8fbff !important;
      background:
        linear-gradient(135deg, rgb(30 64 175 / 0.78), rgb(8 145 178 / 0.62)) !important;
      border: 1px solid rgb(125 211 252 / 0.38);
      box-shadow:
        0 10px 24px rgb(15 23 42 / 0.2),
        inset 0 1px 0 rgb(255 255 255 / 0.08);
    }

    .v-menu-submenu--child,
    .#{$elNamespace}-menu > .#{$elNamespace}-sub-menu .#{$elNamespace}-sub-menu {
      margin-left: 10px;
    }

    .v-menu-submenu--child > .#{$elNamespace}-sub-menu__title,
    .v-menu-item--child,
    .#{$elNamespace}-menu .#{$elNamespace}-menu-item {
      min-height: 40px;
      padding-left: 22px !important;
      font-size: 13px;
      color: rgb(203 213 225 / 0.78) !important;
      background: rgb(9 17 29 / 0.34) !important;
    }

    .#{$elNamespace}-menu {
      padding-top: 2px;
      background: transparent !important;
    }

    .v-menu-submenu--child.#{$elNamespace}-sub-menu.is-active > .#{$elNamespace}-sub-menu__title,
    .#{$elNamespace}-menu-item.is-active {
      color: #f8fbff !important;
      background:
        linear-gradient(135deg, rgb(30 64 175 / 0.78), rgb(8 145 178 / 0.62)) !important;
      border: 1px solid rgb(125 211 252 / 0.38);
      box-shadow:
        0 10px 24px rgb(15 23 42 / 0.2),
        inset 0 1px 0 rgb(255 255 255 / 0.08);

      &::before {
        position: absolute;
        top: 10px;
        bottom: 10px;
        left: 0;
        width: 3px;
        content: '';
        border-radius: 999px;
        background: #7dd3fc;
      }
    }

    .#{$elNamespace}-sub-menu__title [class*='iconify'],
    .#{$elNamespace}-menu-item [class*='iconify'] {
      margin-right: 12px;
      font-size: 17px;
    }

    .v-menu-submenu--root > .#{$elNamespace}-sub-menu__title [class*='iconify'],
    .v-menu-item--root [class*='iconify'] {
      color: rgb(125 211 252 / 0.95);
    }

    .v-menu-submenu--child > .#{$elNamespace}-sub-menu__title [class*='iconify'],
    .v-menu-item--child [class*='iconify'] {
      margin-right: 10px;
      font-size: 15px;
      color: rgb(148 163 184 / 0.9);
    }

    .#{$prefix-cls}__title {
      line-height: 1.2;
    }
  }

  :deep(.#{$elNamespace}-menu--collapse) {
    width: var(--left-menu-min-width);
    padding: 14px 10px 20px;

    & > .#{$elNamespace}-menu-item,
    & > .#{$elNamespace}-sub-menu > .#{$elNamespace}-sub-menu__title {
      justify-content: center;
      padding-right: 0 !important;
      padding-left: 0 !important;
    }

    & > .#{$elNamespace}-menu-item.is-active,
    & > .is-active > .#{$elNamespace}-sub-menu__title {
      position: relative;
      background:
        linear-gradient(135deg, rgb(30 64 175 / 0.82), rgb(8 145 178 / 0.66)) !important;
    }
  }

  :deep(.horizontal-collapse-transition) {
    .#{$prefix-cls}__title {
      display: none;
    }
  }

  &__vertical {
    :deep(.#{$elNamespace}-menu--vertical) {
      &:not(.#{$elNamespace}-menu--collapse) .#{$elNamespace}-sub-menu__title,
      .#{$elNamespace}-menu-item {
        padding-right: 0;
      }
    }
  }

  &__horizontal {
    height: calc(var(--top-tool-height)) !important;

    :deep(.#{$elNamespace}-menu--horizontal) {
      height: calc(var(--top-tool-height));
      border-bottom: none;
      // 重新设置底部高亮颜色
      & > .#{$elNamespace}-sub-menu.is-active {
        .#{$elNamespace}-sub-menu__title {
          border-bottom-color: var(--el-color-primary) !important;
        }
      }

      .#{$elNamespace}-menu-item.is-active {
        position: relative;

        &::after {
          display: none !important;
        }
      }

      .#{$prefix-cls}__title {
        /* stylelint-disable-next-line */
        max-height: calc(var(--top-tool-height) - 2px) !important;
        /* stylelint-disable-next-line */
        line-height: calc(var(--top-tool-height) - 2px);
      }
    }
  }

  :deep(.v-grouped-flyout) {
    position: relative;
    display: block;
    width: 100%;
    height: 100%;
    overflow: visible;
    box-sizing: border-box;
    padding: 0;
    z-index: 2400;
  }

  :deep(.v-grouped-flyout__rail) {
    display: flex;
    width: 100%;
    min-width: 0;
    flex-direction: column;
    gap: 0;
  }

  :deep(.v-grouped-flyout__rail-menu) {
    width: 100% !important;
    border-right: none !important;
    background: transparent !important;
    padding-top: 2px;
  }

  :deep(.v-grouped-flyout__rail-menu > .#{$elNamespace}-menu-item.v-grouped-flyout__rail-item) {
    position: relative;
    min-height: 40px !important;
    margin: 0 0 8px !important;
    border-radius: 16px !important;
    padding-top: 0 !important;
    padding-right: 14px !important;
    padding-bottom: 0 !important;
    padding-left: 22px !important;
    font-size: 13px !important;
    color: rgb(203 213 225 / 0.78) !important;
    background: rgb(9 17 29 / 0.34) !important;
    transition:
      background-color var(--transition-time-02),
      color var(--transition-time-02),
      box-shadow var(--transition-time-02),
      transform var(--transition-time-02);

    &:hover,
    &:focus-visible,
    &.is-preview {
      color: #f8fbff !important;
      background-color: rgb(30 64 175 / 0.28) !important;
      outline: none;
      transform: translateX(2px);
    }
  }

  :deep(.v-grouped-flyout__rail-menu > .#{$elNamespace}-menu-item.v-grouped-flyout__rail-item.is-active) {
    color: #f8fbff !important;
    background: linear-gradient(135deg, rgb(30 64 175 / 0.78), rgb(8 145 178 / 0.62)) !important;
    border: 1px solid rgb(125 211 252 / 0.38) !important;
    box-shadow:
      0 10px 24px rgb(15 23 42 / 0.2),
      inset 0 1px 0 rgb(255 255 255 / 0.08) !important;

    &::before {
      position: absolute;
      top: 10px;
      bottom: 10px;
      left: 0;
      width: 3px;
      content: '';
      border-radius: 999px;
      background: #7dd3fc;
    }
  }

  :deep(.v-grouped-flyout__rail-menu > .#{$elNamespace}-menu-item.v-grouped-flyout__rail-item [class*='iconify']) {
    margin-right: 10px !important;
    font-size: 15px !important;
    color: rgb(148 163 184 / 0.9) !important;
  }

  :deep(.v-grouped-flyout__rail-menu > .#{$elNamespace}-menu-item.v-grouped-flyout__rail-item.is-active [class*='iconify']) {
    color: rgb(125 211 252 / 0.95) !important;
  }

  :deep(.v-grouped-flyout__rail-menu > .#{$elNamespace}-menu-item.v-grouped-flyout__rail-item .#{$prefix-cls}__title) {
    line-height: 1.2 !important;
  }

  :deep(.v-menu-grouped-flyout-embedded) {
    position: relative;
    display: block;
    width: 100%;
    min-width: 0;
    list-style: none;
    margin: 0;
    padding: 0;
    z-index: 2380;
    overflow: visible;
  }

  :deep(.v-menu-submenu--root:not(.is-opened) .v-menu-grouped-flyout-embedded) {
    display: none;
  }
}
</style>

<style lang="scss">
$prefix-cls: #{$namespace}-menu-popper;

.#{$prefix-cls}--vertical,
.#{$prefix-cls}--horizontal {
  z-index: 2500;

  .is-active {
    & > .el-sub-menu__title {
      color: #f8fbff !important;
    }
  }

  .el-sub-menu__title,
  .el-menu-item {
    border-radius: 12px;

    &:hover {
      color: #f8fbff !important;
      background-color: rgb(30 64 175 / 0.28) !important;
    }
  }

  .el-menu-item.is-active {
    position: relative;
    color: #f8fbff !important;
    background:
      linear-gradient(135deg, rgb(30 64 175 / 0.78), rgb(8 145 178 / 0.62)) !important;

    &:hover {
      background:
        linear-gradient(135deg, rgb(30 64 175 / 0.78), rgb(8 145 178 / 0.62)) !important;
    }
  }
}

.v-grouped-flyout__submenu-popper {
  z-index: 2600 !important;
}

.v-grouped-flyout__panel {
  position: fixed;
  z-index: 3200;
  display: flex;
  min-height: 240px;
  min-width: 420px;
  flex-direction: column;
  overflow: hidden;
  box-sizing: border-box;
  border: 1px solid rgb(148 163 184 / 0.18);
  border-radius: 24px;
  background:
    linear-gradient(180deg, rgb(15 23 42 / 0.985), rgb(8 15 28 / 0.97)),
    radial-gradient(circle at top left, rgb(56 189 248 / 0.16), transparent 32%),
    radial-gradient(circle at bottom right, rgb(37 99 235 / 0.12), transparent 28%);
  box-shadow:
    0 28px 56px rgb(15 23 42 / 0.38),
    0 10px 24px rgb(8 15 28 / 0.22),
    inset 0 1px 0 rgb(255 255 255 / 0.05);
  backdrop-filter: blur(12px);

  &::before {
    position: absolute;
    inset: 0;
    content: '';
    border-radius: inherit;
    background:
      linear-gradient(180deg, rgb(255 255 255 / 0.05), transparent 18%, transparent 82%, rgb(255 255 255 / 0.02));
    pointer-events: none;
  }

  &::after {
    position: absolute;
    top: 0;
    right: 18px;
    left: 18px;
    height: 3px;
    content: '';
    border-radius: 999px;
    background: linear-gradient(90deg, rgb(125 211 252 / 0.18), rgb(56 189 248 / 0.82), rgb(37 99 235 / 0.2));
    pointer-events: none;
  }
}

.v-grouped-flyout__panel-header {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 18px 14px;
  border-bottom: 1px solid rgb(148 163 184 / 0.08);
  background:
    linear-gradient(180deg, rgb(255 255 255 / 0.04), rgb(255 255 255 / 0.015)),
    radial-gradient(circle at top left, rgb(56 189 248 / 0.08), transparent 40%);
}

.v-grouped-flyout__panel-header-main {
  display: inline-flex;
  min-width: 0;
  align-items: center;
  gap: 10px;
  padding: 9px 12px;
  border: 1px solid rgb(125 211 252 / 0.14);
  border-radius: 16px;
  background: linear-gradient(180deg, rgb(255 255 255 / 0.06), rgb(255 255 255 / 0.02));
  box-shadow:
    inset 0 1px 0 rgb(255 255 255 / 0.06),
    0 8px 18px rgb(8 15 28 / 0.16);
}

.v-grouped-flyout__panel-icon {
  display: inline-flex;
  width: 30px;
  height: 30px;
  flex: 0 0 30px;
  align-items: center;
  justify-content: center;
  border: 1px solid rgb(125 211 252 / 0.18);
  border-radius: 11px;
  background:
    linear-gradient(135deg, rgb(30 64 175 / 0.34), rgb(8 145 178 / 0.2)),
    rgb(15 23 42 / 0.42);
  color: rgb(125 211 252 / 0.95);
  box-shadow: inset 0 1px 0 rgb(255 255 255 / 0.06);

  [class*='iconify'] {
    font-size: 15px;
  }
}

.v-grouped-flyout__panel-title {
  overflow: hidden;
  color: #f8fbff;
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 0.02em;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.v-grouped-flyout__panel-body {
  flex: 1;
  min-height: 0;
}

.v-grouped-flyout__panel-body .el-scrollbar__wrap {
  overflow-x: hidden;
  overflow-y: auto;
}

.v-grouped-flyout__panel-body .el-scrollbar__view {
  min-height: 100%;
}

.v-grouped-flyout__panel-body .el-scrollbar__bar.is-vertical {
  top: 10px;
  right: 8px;
  bottom: 10px;
  width: 8px;
  opacity: 1;
}

.v-grouped-flyout__panel-body .el-scrollbar__bar.is-vertical .el-scrollbar__thumb {
  background: rgb(125 211 252 / 0.48);
  border-radius: 999px;
}

.v-grouped-flyout__menu {
  padding: 14px 14px 18px !important;
  background: transparent !important;
  border-right: none !important;
}

.v-grouped-flyout__menu .el-menu-item,
.v-grouped-flyout__submenu > .el-sub-menu__title {
  position: relative;
  min-height: 42px;
  margin: 0 0 10px;
  border: 1px solid transparent;
  border-radius: 16px;
  background:
    linear-gradient(180deg, rgb(255 255 255 / 0.035), rgb(255 255 255 / 0.015)),
    rgb(9 17 29 / 0.36) !important;
  box-shadow:
    inset 0 1px 0 rgb(255 255 255 / 0.04),
    inset 0 -1px 0 rgb(15 23 42 / 0.18);
  transition:
    background-color var(--transition-time-02),
    border-color var(--transition-time-02),
    box-shadow var(--transition-time-02),
    transform var(--transition-time-02),
    color var(--transition-time-02);
}

.v-grouped-flyout__menu .el-menu-item:hover,
.v-grouped-flyout__submenu > .el-sub-menu__title:hover {
  color: #f8fbff !important;
  border-color: rgb(96 165 250 / 0.16);
  background:
    linear-gradient(180deg, rgb(255 255 255 / 0.055), rgb(255 255 255 / 0.02)),
    rgb(30 64 175 / 0.22) !important;
  box-shadow:
    inset 0 1px 0 rgb(255 255 255 / 0.06),
    0 10px 22px rgb(8 15 28 / 0.18);
  transform: translateX(2px);
}

.v-grouped-flyout__menu .el-menu-item [class*='iconify'],
.v-grouped-flyout__submenu > .el-sub-menu__title [class*='iconify'] {
  margin-right: 10px;
  color: rgb(148 163 184 / 0.92);
  font-size: 15px;
}

.v-grouped-flyout__menu .el-menu-item .v-menu__title,
.v-grouped-flyout__submenu > .el-sub-menu__title .v-menu__title {
  font-weight: 600;
}

.v-grouped-flyout__menu .el-menu-item.is-active,
.v-grouped-flyout__menu .el-sub-menu.is-active > .el-sub-menu__title {
  position: relative;
  color: #f8fbff !important;
  border: 1px solid rgb(125 211 252 / 0.42) !important;
  background:
    linear-gradient(135deg, rgb(30 64 175 / 0.9), rgb(8 145 178 / 0.76)),
    linear-gradient(180deg, rgb(255 255 255 / 0.08), rgb(255 255 255 / 0.02)) !important;
  box-shadow:
    0 14px 30px rgb(15 23 42 / 0.26),
    0 0 0 1px rgb(125 211 252 / 0.08),
    inset 0 1px 0 rgb(255 255 255 / 0.12) !important;
}

.v-grouped-flyout__menu .el-menu-item.is-active [class*='iconify'],
.v-grouped-flyout__menu .el-sub-menu.is-active > .el-sub-menu__title [class*='iconify'] {
  color: rgb(186 230 253 / 0.98) !important;
}

.v-grouped-flyout__menu .el-menu-item.is-active::before,
.v-grouped-flyout__menu .el-sub-menu.is-active > .el-sub-menu__title::before {
  position: absolute;
  top: 9px;
  bottom: 9px;
  left: 0;
  width: 4px;
  content: '';
  border-radius: 999px;
  background: linear-gradient(180deg, rgb(186 230 253 / 0.96), rgb(125 211 252 / 0.8));
  box-shadow: 0 0 10px rgb(125 211 252 / 0.32);
}

.v-grouped-flyout__menu .el-sub-menu .el-menu {
  padding-top: 2px;
}

.v-grouped-flyout__menu .el-sub-menu .el-menu-item {
  min-height: 40px;
  border-radius: 14px;
}

.v-grouped-flyout__submenu-popper .el-menu {
  border: 1px solid rgb(148 163 184 / 0.16);
  background:
    linear-gradient(180deg, rgb(15 23 42 / 0.98), rgb(9 17 29 / 0.96)),
    radial-gradient(circle at top left, rgb(56 189 248 / 0.14), transparent 34%);
  box-shadow:
    0 24px 48px rgb(15 23 42 / 0.34),
    inset 0 1px 0 rgb(255 255 255 / 0.04);
}
</style>
