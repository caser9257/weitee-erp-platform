<!-- ExpenseTypeSelect.vue - 费用类型选择组件 -->
<template>
  <el-select
    v-model="modelValue"
    :placeholder="placeholder"
    :disabled="disabled"
    :clearable="clearable"
    @change="handleChange"
  >
    <!-- 系统类型 -->
    <el-option-group label="系统类型">
      <el-option
        v-for="item in coreTypes"
        :key="item.value"
        :label="item.label"
        :value="item.value"
      >
        <div class="expense-type-option">
          <span class="type-label">{{ item.label }}</span>
          <el-tag v-if="item.projectRequired" size="small" type="info">需项目</el-tag>
        </div>
      </el-option>
    </el-option-group>

    <!-- 行政费用 -->
    <el-option-group label="行政费用" v-if="adminTypes.length">
      <el-option
        v-for="item in adminTypes"
        :key="item.value"
        :label="item.label"
        :value="item.value"
      >
        <div class="expense-type-option">
          <span class="type-label">{{ item.label }}</span>
          <el-tag v-if="item.costCenterRequired" size="small" type="warning">需成本中心</el-tag>
        </div>
      </el-option>
    </el-option-group>

    <!-- 租赁费用 -->
    <el-option-group label="租赁费用" v-if="leaseTypes.length">
      <el-option
        v-for="item in leaseTypes"
        :key="item.value"
        :label="item.label"
        :value="item.value"
      >
        <div class="expense-type-option">
          <span class="type-label">{{ item.label }}</span>
          <el-tag v-if="item.leaseContractRequired" size="small" type="success">需租赁合同</el-tag>
        </div>
      </el-option>
    </el-option-group>
  </el-select>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { FinanceExpenseApi } from '@/api/erp/finance/expense';
import { ExpenseCategory } from '@/api/erp/finance/expense/type';

const props = defineProps({
  modelValue: {
    type: [Number, String],
    default: undefined
  },
  placeholder: {
    type: String,
    default: '请选择费用类型'
  },
  disabled: {
    type: Boolean,
    default: false
  },
  clearable: {
    type: Boolean,
    default: true
  },
  // 是否只显示核心类型
  onlyCore: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['update:modelValue', 'change']);

// 费用类型列表
const typeList = ref([]);

// 核心类型
const coreTypes = computed(() => {
  return typeList.value.filter(item => item.core);
});

// 行政费用类型
const adminTypes = computed(() => {
  if (props.onlyCore) return [];
  return typeList.value.filter(item => !item.core && item.category === ExpenseCategory.ADMIN);
});

// 租赁费用类型
const leaseTypes = computed(() => {
  if (props.onlyCore) return [];
  return typeList.value.filter(item => !item.core && item.category === ExpenseCategory.LEASE);
});

// 获取当前选中的类型配置
const selectedTypeConfig = computed(() => {
  return typeList.value.find(item => item.value === props.modelValue);
});

// 加载费用类型列表
onMounted(async () => {
  try {
    typeList.value = await FinanceExpenseApi.getFinanceExpenseTypeList();
  } catch (error) {
    console.error('获取费用类型列表失败', error);
  }
});

// 值变化
const handleChange = (value) => {
  emit('update:modelValue', value);
  emit('change', value, selectedTypeConfig.value);
};

// 暴露配置信息
defineExpose({
  selectedTypeConfig,
  typeList
});
</script>

<style scoped>
.expense-type-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.type-label {
  flex: 1;
}
</style>
