package cn.weitee.erp.module.erp.controller.admin.finance;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.dualledger.ErpFinanceDualLedgerConfigPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.dualledger.ErpFinanceDualLedgerConfigRespVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.dualledger.ErpFinanceDualLedgerConfigSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceDualLedgerConfigDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceLedgerDO;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceDualLedgerConfigService;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceLedgerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertList;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertMap;

@Tag(name = "管理后台 - ERP 双账套账簿映射")
@RestController
@RequestMapping("/erp/finance-dual-ledger-config")
@Validated
public class ErpFinanceDualLedgerConfigController {

    @Resource
    private ErpFinanceDualLedgerConfigService dualLedgerConfigService;
    @Resource
    private ErpFinanceLedgerService financeLedgerService;

    @PostMapping("/create")
    @Operation(summary = "创建双账套账簿映射")
    @PreAuthorize("@ss.hasPermission('erp:finance-dual-ledger-config:create')")
    public CommonResult<Long> createDualLedgerConfig(@Valid @RequestBody ErpFinanceDualLedgerConfigSaveReqVO createReqVO) {
        return success(dualLedgerConfigService.createDualLedgerConfig(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新双账套账簿映射")
    @PreAuthorize("@ss.hasPermission('erp:finance-dual-ledger-config:update')")
    public CommonResult<Boolean> updateDualLedgerConfig(@Valid @RequestBody ErpFinanceDualLedgerConfigSaveReqVO updateReqVO) {
        dualLedgerConfigService.updateDualLedgerConfig(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除双账套账簿映射")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:finance-dual-ledger-config:delete')")
    public CommonResult<Boolean> deleteDualLedgerConfig(@RequestParam("id") Long id) {
        dualLedgerConfigService.deleteDualLedgerConfig(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得双账套账簿映射")
    @PreAuthorize("@ss.hasPermission('erp:finance-dual-ledger-config:query')")
    public CommonResult<ErpFinanceDualLedgerConfigRespVO> getDualLedgerConfig(@RequestParam("id") Long id) {
        ErpFinanceDualLedgerConfigDO config = dualLedgerConfigService.getDualLedgerConfig(id);
        if (config == null) {
            return success(null);
        }
        return success(buildResp(config, loadLedgerMap(config)));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得启用双账套账簿映射精简列表")
    @PreAuthorize("@ss.hasPermission('erp:finance-dual-ledger-config:query')")
    public CommonResult<List<ErpFinanceDualLedgerConfigRespVO>> getDualLedgerConfigSimpleList() {
        List<ErpFinanceDualLedgerConfigDO> list = dualLedgerConfigService
                .getDualLedgerConfigListByStatus(CommonStatusEnum.ENABLE.getStatus());
        Map<Long, ErpFinanceLedgerDO> ledgerMap = loadLedgerMap(list);
        return success(convertList(list, config -> buildResp(config, ledgerMap)));
    }

    @GetMapping("/page")
    @Operation(summary = "获得双账套账簿映射分页")
    @PreAuthorize("@ss.hasPermission('erp:finance-dual-ledger-config:query')")
    public CommonResult<PageResult<ErpFinanceDualLedgerConfigRespVO>> getDualLedgerConfigPage(
            @Valid ErpFinanceDualLedgerConfigPageReqVO pageReqVO) {
        PageResult<ErpFinanceDualLedgerConfigDO> pageResult = dualLedgerConfigService.getDualLedgerConfigPage(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }
        Map<Long, ErpFinanceLedgerDO> ledgerMap = loadLedgerMap(pageResult.getList());
        List<ErpFinanceDualLedgerConfigRespVO> respList = convertList(pageResult.getList(),
                config -> buildResp(config, ledgerMap));
        return success(new PageResult<>(respList, pageResult.getTotal()));
    }

    private ErpFinanceDualLedgerConfigRespVO buildResp(ErpFinanceDualLedgerConfigDO config,
                                                       Map<Long, ErpFinanceLedgerDO> ledgerMap) {
        ErpFinanceDualLedgerConfigRespVO respVO = BeanUtils.toBean(config, ErpFinanceDualLedgerConfigRespVO.class);
        respVO.setBizTypeName(resolveBizTypeName(config.getBizType()));
        ErpFinanceLedgerDO externalLedger = ledgerMap.get(config.getExternalLedgerId());
        respVO.setExternalLedgerName(externalLedger == null ? null : externalLedger.getName());
        ErpFinanceLedgerDO internalLedger = ledgerMap.get(config.getInternalLedgerId());
        respVO.setInternalLedgerName(internalLedger == null ? null : internalLedger.getName());
        return respVO;
    }

    private Map<Long, ErpFinanceLedgerDO> loadLedgerMap(ErpFinanceDualLedgerConfigDO config) {
        if (config == null) {
            return Collections.emptyMap();
        }
        return loadLedgerMap(Collections.singletonList(config));
    }

    private Map<Long, ErpFinanceLedgerDO> loadLedgerMap(Collection<ErpFinanceDualLedgerConfigDO> configs) {
        if (configs == null || configs.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<Long> ledgerIds = new LinkedHashSet<>();
        configs.forEach(item -> {
            ledgerIds.add(item.getExternalLedgerId());
            ledgerIds.add(item.getInternalLedgerId());
        });
        return convertMap(financeLedgerService.getFinanceLedgerList(ledgerIds), ErpFinanceLedgerDO::getId);
    }

    private String resolveBizTypeName(Integer bizType) {
        for (ErpBizTypeEnum value : ErpBizTypeEnum.values()) {
            if (value.getType().equals(bizType)) {
                return value.getName();
            }
        }
        return null;
    }
}
