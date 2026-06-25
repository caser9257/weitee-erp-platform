package cn.weitee.erp.module.erp.controller.admin.finance;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.dualledger.ErpFinanceDualLedgerDiffConfigPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.dualledger.ErpFinanceDualLedgerDiffConfigRespVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.dualledger.ErpFinanceDualLedgerDiffConfigSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceDualLedgerDiffConfigDO;
import cn.weitee.erp.module.erp.enums.ErpFinanceDualLedgerDiffItemTypeEnum;
import cn.weitee.erp.module.erp.enums.ErpFinanceDualLedgerDiffSourceTypeEnum;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceDualLedgerDiffConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - ERP 双账套差异项口径配置")
@RestController
@RequestMapping("/erp/finance-dual-ledger-diff-config")
@Validated
public class ErpFinanceDualLedgerDiffConfigController {

    @Resource
    private ErpFinanceDualLedgerDiffConfigService dualLedgerDiffConfigService;

    @PostMapping("/create")
    @Operation(summary = "创建双账套差异项口径配置")
    @PreAuthorize("@ss.hasPermission('erp:finance-dual-ledger-diff-config:create')")
    public CommonResult<Long> createDualLedgerDiffConfig(@Valid @RequestBody ErpFinanceDualLedgerDiffConfigSaveReqVO createReqVO) {
        return success(dualLedgerDiffConfigService.createDualLedgerDiffConfig(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新双账套差异项口径配置")
    @PreAuthorize("@ss.hasPermission('erp:finance-dual-ledger-diff-config:update')")
    public CommonResult<Boolean> updateDualLedgerDiffConfig(@Valid @RequestBody ErpFinanceDualLedgerDiffConfigSaveReqVO updateReqVO) {
        dualLedgerDiffConfigService.updateDualLedgerDiffConfig(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除双账套差异项口径配置")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:finance-dual-ledger-diff-config:delete')")
    public CommonResult<Boolean> deleteDualLedgerDiffConfig(@RequestParam("id") Long id) {
        dualLedgerDiffConfigService.deleteDualLedgerDiffConfig(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得双账套差异项口径配置")
    @PreAuthorize("@ss.hasPermission('erp:finance-dual-ledger-diff-config:query')")
    public CommonResult<ErpFinanceDualLedgerDiffConfigRespVO> getDualLedgerDiffConfig(@RequestParam("id") Long id) {
        ErpFinanceDualLedgerDiffConfigDO config = dualLedgerDiffConfigService.getDualLedgerDiffConfig(id);
        return success(config == null ? null : buildResp(config));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得启用中的双账套差异项口径简洁列表")
    public CommonResult<List<ErpFinanceDualLedgerDiffConfigRespVO>> getDualLedgerDiffConfigSimpleList(
            @RequestParam(value = "bizType", required = false) Integer bizType) {
        List<ErpFinanceDualLedgerDiffConfigDO> list = dualLedgerDiffConfigService
                .getDualLedgerDiffConfigList(bizType, CommonStatusEnum.ENABLE.getStatus());
        return success(convertList(list, this::buildResp));
    }

    @GetMapping("/page")
    @Operation(summary = "获得双账套差异项口径配置分页")
    @PreAuthorize("@ss.hasPermission('erp:finance-dual-ledger-diff-config:query')")
    public CommonResult<PageResult<ErpFinanceDualLedgerDiffConfigRespVO>> getDualLedgerDiffConfigPage(
            @Valid ErpFinanceDualLedgerDiffConfigPageReqVO pageReqVO) {
        PageResult<ErpFinanceDualLedgerDiffConfigDO> pageResult =
                dualLedgerDiffConfigService.getDualLedgerDiffConfigPage(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }
        List<ErpFinanceDualLedgerDiffConfigRespVO> respList = convertList(pageResult.getList(), this::buildResp);
        return success(new PageResult<>(respList, pageResult.getTotal()));
    }

    private ErpFinanceDualLedgerDiffConfigRespVO buildResp(ErpFinanceDualLedgerDiffConfigDO config) {
        ErpFinanceDualLedgerDiffConfigRespVO respVO = BeanUtils.toBean(config, ErpFinanceDualLedgerDiffConfigRespVO.class);
        respVO.setBizTypeName(resolveBizTypeName(config.getBizType()));
        respVO.setDiffItemTypeName(ErpFinanceDualLedgerDiffItemTypeEnum.resolveName(config.getDiffItemType()));
        respVO.setExternalSourceTypeName(ErpFinanceDualLedgerDiffSourceTypeEnum.resolveName(config.getExternalSourceType()));
        respVO.setExternalSourceValueName(resolveSourceValueName(config.getExternalSourceType(), config.getExternalSourceValue()));
        respVO.setInternalSourceTypeName(ErpFinanceDualLedgerDiffSourceTypeEnum.resolveName(config.getInternalSourceType()));
        respVO.setInternalSourceValueName(resolveSourceValueName(config.getInternalSourceType(), config.getInternalSourceValue()));
        return respVO;
    }

    private String resolveBizTypeName(Integer bizType) {
        for (ErpBizTypeEnum value : ErpBizTypeEnum.values()) {
            if (value.getType().equals(bizType)) {
                return value.getName();
            }
        }
        return null;
    }

    private String resolveSourceValueName(Integer sourceType, Integer sourceValue) {
        if (sourceValue == null) {
            return null;
        }
        if (ErpFinanceDualLedgerDiffSourceTypeEnum.COST_ITEM.getType().equals(sourceType)) {
            return ErpFinanceDualLedgerDiffItemTypeEnum.resolveName(sourceValue);
        }
        return null;
    }

}
