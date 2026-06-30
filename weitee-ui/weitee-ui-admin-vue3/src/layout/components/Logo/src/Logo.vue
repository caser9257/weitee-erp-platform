<script lang="ts" setup>
import { computed, onMounted, ref, unref, watch } from 'vue'
import { useAppStore } from '@/store/modules/app'
import { useDesign } from '@/hooks/web/useDesign'

defineOptions({ name: 'Logo' })

const { getPrefixCls } = useDesign()

const prefixCls = getPrefixCls('logo')

const appStore = useAppStore()

const show = ref(true)

const title = computed(() => appStore.getTitle)

const layout = computed(() => appStore.getLayout)

const collapse = computed(() => appStore.getCollapse)

onMounted(() => {
  if (unref(collapse)) show.value = false
})

watch(
  () => collapse.value,
  (collapse: boolean) => {
    if (unref(layout) === 'topLeft' || unref(layout) === 'cutMenu') {
      show.value = true
      return
    }
    if (!collapse) {
      setTimeout(() => {
        show.value = !collapse
      }, 400)
    } else {
      show.value = !collapse
    }
  }
)

watch(
  () => layout.value,
  (layout) => {
    if (layout === 'top' || layout === 'cutMenu') {
      show.value = true
    } else {
      if (unref(collapse)) {
        show.value = false
      } else {
        show.value = true
      }
    }
  }
)
</script>

<template>
  <div>
    <router-link
      :class="[
        prefixCls,
        layout !== 'classic' ? `${prefixCls}__Top` : '',
        'flex !h-[var(--logo-height)] items-center cursor-pointer pl-14px pr-16px relative decoration-none overflow-hidden'
      ]"
      to="/"
    >
      <div
        class="flex h-[44px] w-[44px] items-center justify-center rounded-14px border border-[rgb(125_211_252_/_0.24)] bg-[linear-gradient(145deg,_rgb(30_64_175_/_0.92),_rgb(8_145_178_/_0.78))] shadow-[0_14px_28px_rgb(15_23_42_/_0.24)]"
      >
        <Icon icon="ep:cpu" color="#F8FBFF" :size="22" />
      </div>
      <div
        v-if="show"
        :class="[
          'ml-14px flex min-w-0 flex-col',
          {
            'text-[var(--logo-title-text-color)]': layout === 'classic',
            'text-[var(--top-header-text-color)]':
              layout === 'topLeft' || layout === 'top' || layout === 'cutMenu'
          }
        ]"
      >
        <span class="truncate text-16px font-700 tracking-[0.03em]">{{ title }}</span>
        <span class="truncate text-11px text-[rgb(191_219_254_/_0.74)]">项目主链协同平台</span>
      </div>
    </router-link>
  </div>
</template>
