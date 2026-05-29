  <template>
  <BusinessScaffoldPage
    title="MES PDA / 扫码报工与工时采集"
    description="面向生产现场的报工终端骨架，后续补扫码识别、报工数量、工时采集和异常提报。"
    view-path="src/views/mes/pda-execute/report.vue"
    :api-paths="['src/api/mes/pda-execute/index.ts']"
    :reuse-paths="['src/views/mes/work-order/index.vue', 'src/api/mes/work-order/index.ts']"
  />
</template>

<script setup lang="ts">
import BusinessScaffoldPage from '@/components/BusinessScaffoldPage/index.vue'

defineOptions({ name: 'MesPdaReport' })
</script>
