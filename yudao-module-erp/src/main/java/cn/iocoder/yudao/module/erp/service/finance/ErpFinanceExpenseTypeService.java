package cn.iocoder.yudao.module.erp.service.finance;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.expense.ExpenseTypeVO;
import cn.iocoder.yudao.module.erp.enums.ErpFinanceExpenseTypeEnum;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.dal.dataobject.dict.DictDataDO;
import cn.iocoder.yudao.module.system.service.dict.DictDataService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstantsExpense.*;

/**
 * 费用类型服务（混合模式）
 * 支持核心枚举类型和字典扩展类型
 */
@Service
@Slf4j
public class ErpFinanceExpenseTypeService {

    @Resource
    private DictDataService dictDataService;
    @Resource
    private DeptApi deptApi;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 核心类型集合（硬编码，保证类型安全）
     */
    private static final Set<Integer> CORE_TYPES = Set.of(
        ErpFinanceExpenseTypeEnum.RESEARCH.getType(),
        ErpFinanceExpenseTypeEnum.TRAVEL.getType(),
        ErpFinanceExpenseTypeEnum.MATERIAL.getType(),
        ErpFinanceExpenseTypeEnum.TEST.getType(),
        ErpFinanceExpenseTypeEnum.ENTERTAINMENT.getType(),
        ErpFinanceExpenseTypeEnum.SERVICE.getType(),
        ErpFinanceExpenseTypeEnum.LABOR.getType(),
        ErpFinanceExpenseTypeEnum.PETTY_PURCHASE.getType(),
        ErpFinanceExpenseTypeEnum.OTHER.getType()
    );

    /**
     * 字典类型常量
     */
    public static final String DICT_TYPE = "erp_expense_type";

    /**
     * 获取费用类型列表（核心类型 + 扩展类型）
     */
    public List<ExpenseTypeVO> getExpenseTypeList() {
        List<ExpenseTypeVO> result = new ArrayList<>();

        // 1. 从字典获取所有类型
        List<DictDataDO> dictList = dictDataService.getDictDataList(null, DICT_TYPE);

        // 2. 转换为VO
        for (DictDataDO dict : dictList) {
            JsonNode attrs = parseBizAttributes(dict.getBizAttributes());
            result.add(ExpenseTypeVO.builder()
                .value(Integer.parseInt(dict.getValue()))
                .label(dict.getLabel())
                .core(attrs.path("core").asBoolean(false))
                .projectRequired(attrs.path("projectRequired").asBoolean(false))
                .costCenterRequired(attrs.path("costCenterRequired").asBoolean(false))
                .leaseContractRequired(attrs.path("leaseContractRequired").asBoolean(false))
                .assetCandidateFlag(attrs.path("assetCandidateFlag").asBoolean(false))
                .category(attrs.path("category").asText(null))
                .autoGenerateVoucher(attrs.path("autoGenerateVoucher").asBoolean(false))
                .voucherBizType(attrs.path("voucherBizType").isInt() ? attrs.path("voucherBizType").asInt() : null)
                .build());
        }

        // 3. 按排序字段排序
        result.sort(Comparator.comparingInt(ExpenseTypeVO::getValue));

        return result;
    }

    /**
     * 获取核心类型列表
     */
    public List<ExpenseTypeVO> getCoreTypeList() {
        return getExpenseTypeList().stream()
            .filter(ExpenseTypeVO::isCore)
            .collect(Collectors.toList());
    }

    /**
     * 获取扩展类型列表
     */
    public List<ExpenseTypeVO> getExtendedTypeList() {
        return getExpenseTypeList().stream()
            .filter(item -> !item.isCore())
            .collect(Collectors.toList());
    }

    /**
     * 校验费用类型
     */
    public void validateExpenseType(Integer expenseType, Integer projectId, Long costCenterId, String leaseContractNo) {
        // 1. 获取类型配置
        ExpenseTypeVO typeConfig = getExpenseTypeConfig(expenseType);
        if (typeConfig == null) {
            throw exception(EXPENSE_TYPE_NOT_EXISTS);
        }

        // 2. 校验项目必填
        if (typeConfig.isProjectRequired() && projectId == null) {
            throw exception(EXPENSE_PROJECT_REQUIRED);
        }

        // 3. 校验成本中心必填
        if (typeConfig.isCostCenterRequired()) {
            if (costCenterId == null) {
                throw exception(EXPENSE_COST_CENTER_REQUIRED);
            }
            // 校验部门的 cost_type 已配置
            cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO dept = deptApi.getDept(costCenterId);
            if (dept == null) {
                throw exception(EXPENSE_COST_CENTER_REQUIRED);
            }
        }

        // 4. 校验租赁合同必填
        if (typeConfig.isLeaseContractRequired() && StrUtil.isBlank(leaseContractNo)) {
            throw exception(EXPENSE_LEASE_CONTRACT_REQUIRED);
        }
    }

    /**
     * 获取费用类型配置
     */
    public ExpenseTypeVO getExpenseTypeConfig(Integer expenseType) {
        DictDataDO dict = dictDataService.getDictData(DICT_TYPE, String.valueOf(expenseType));
        if (dict == null) {
            return null;
        }

        JsonNode attrs = parseBizAttributes(dict.getBizAttributes());
        return ExpenseTypeVO.builder()
            .value(Integer.parseInt(dict.getValue()))
            .label(dict.getLabel())
            .core(attrs.path("core").asBoolean(false))
            .projectRequired(attrs.path("projectRequired").asBoolean(false))
            .costCenterRequired(attrs.path("costCenterRequired").asBoolean(false))
            .leaseContractRequired(attrs.path("leaseContractRequired").asBoolean(false))
            .assetCandidateFlag(attrs.path("assetCandidateFlag").asBoolean(false))
            .category(attrs.path("category").asText(null))
            .autoGenerateVoucher(attrs.path("autoGenerateVoucher").asBoolean(false))
            .voucherBizType(attrs.path("voucherBizType").isInt() ? attrs.path("voucherBizType").asInt() : null)
            .build();
    }

    /**
     * 解析业务属性JSON
     */
    private JsonNode parseBizAttributes(String bizAttributes) {
        if (StrUtil.isBlank(bizAttributes)) {
            return objectMapper.createObjectNode();
        }
        try {
            return objectMapper.readTree(bizAttributes);
        } catch (Exception e) {
            log.warn("解析费用类型业务属性失败: {}", bizAttributes, e);
            return objectMapper.createObjectNode();
        }
    }

    /**
     * 判断是否为核心类型
     */
    public boolean isCoreType(Integer expenseType) {
        return CORE_TYPES.contains(expenseType);
    }

    /**
     * 判断是否需要生成凭证
     */
    public boolean isAutoGenerateVoucher(Integer expenseType) {
        ExpenseTypeVO config = getExpenseTypeConfig(expenseType);
        return config != null && config.isAutoGenerateVoucher();
    }

    /**
     * 获取凭证业务类型
     */
    public Integer getVoucherBizType(Integer expenseType) {
        ExpenseTypeVO config = getExpenseTypeConfig(expenseType);
        if (config == null) {
            return null;
        }
        return config.getVoucherBizType();
    }

    /**
     * 判断是否需要成本中心
     */
    public boolean isCostCenterRequired(Integer expenseType) {
        ExpenseTypeVO config = getExpenseTypeConfig(expenseType);
        return config != null && config.isCostCenterRequired();
    }

    /**
     * 判断是否需要租赁合同
     */
    public boolean isLeaseContractRequired(Integer expenseType) {
        ExpenseTypeVO config = getExpenseTypeConfig(expenseType);
        return config != null && config.isLeaseContractRequired();
    }

    /**
     * 判断是否标记为资产候选
     */
    public boolean isAssetCandidateFlag(Integer expenseType) {
        ExpenseTypeVO config = getExpenseTypeConfig(expenseType);
        return config != null && config.isAssetCandidateFlag();
    }
}
