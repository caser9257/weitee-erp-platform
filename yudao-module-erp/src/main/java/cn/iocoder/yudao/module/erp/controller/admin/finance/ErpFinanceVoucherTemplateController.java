package cn.iocoder.yudao.module.erp.controller.admin.finance;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.voucher.ErpFinanceVoucherTemplatePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.voucher.ErpFinanceVoucherTemplateRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.voucher.ErpFinanceVoucherTemplateSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceLedgerDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceVoucherTemplateDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceVoucherTemplateItemDO;
import cn.iocoder.yudao.module.erp.enums.ErpFinanceVoucherAmountSourceEnum;
import cn.iocoder.yudao.module.erp.enums.ErpFinanceVoucherEntryDirectionEnum;
import cn.iocoder.yudao.module.erp.enums.ErpResearchExpenseCategoryEnum;
import cn.iocoder.yudao.module.erp.enums.common.ErpBizTypeEnum;
import cn.iocoder.yudao.module.erp.service.finance.ErpFinanceLedgerService;
import cn.iocoder.yudao.module.erp.service.finance.ErpFinanceVoucherTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - 财务凭证模板")
@RestController
@RequestMapping("/erp/finance-voucher-template")
@Validated
public class ErpFinanceVoucherTemplateController {

    @Resource
    private ErpFinanceVoucherTemplateService voucherTemplateService;
    @Resource
    private ErpFinanceLedgerService financeLedgerService;

    @PostMapping("/create")
    @Operation(summary = "创建财务凭证模板")
    @PreAuthorize("@ss.hasPermission('erp:finance-voucher-template:create')")
    public CommonResult<Long> createVoucherTemplate(@Valid @RequestBody ErpFinanceVoucherTemplateSaveReqVO createReqVO) {
        return success(voucherTemplateService.createVoucherTemplate(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新财务凭证模板")
    @PreAuthorize("@ss.hasPermission('erp:finance-voucher-template:update')")
    public CommonResult<Boolean> updateVoucherTemplate(@Valid @RequestBody ErpFinanceVoucherTemplateSaveReqVO updateReqVO) {
        voucherTemplateService.updateVoucherTemplate(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除财务凭证模板")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:finance-voucher-template:delete')")
    public CommonResult<Boolean> deleteVoucherTemplate(@RequestParam("id") Long id) {
        voucherTemplateService.deleteVoucherTemplate(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得财务凭证模板")
    @PreAuthorize("@ss.hasPermission('erp:finance-voucher-template:query')")
    public CommonResult<ErpFinanceVoucherTemplateRespVO> getVoucherTemplate(@RequestParam("id") Long id) {
        ErpFinanceVoucherTemplateDO template = voucherTemplateService.getVoucherTemplate(id);
        if (template == null) {
            return success(null);
        }
        List<ErpFinanceVoucherTemplateItemDO> items = voucherTemplateService.getVoucherTemplateItemListByTemplateId(id);
        Map<Long, ErpFinanceLedgerDO> ledgerMap = financeLedgerService.getFinanceLedgerMap(Collections.singleton(template.getLedgerId()));
        return success(buildTemplateResp(template, items, ledgerMap));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得财务凭证模板精简列表")
    public CommonResult<List<ErpFinanceVoucherTemplateRespVO>> getVoucherTemplateSimpleList(@RequestParam("ledgerId") Long ledgerId,
                                                                                           @RequestParam("bizType") Integer bizType) {
        List<ErpFinanceVoucherTemplateDO> list = voucherTemplateService.getVoucherTemplateListByLedgerAndBizType(ledgerId, bizType)
                .stream().filter(item -> CommonStatusEnum.ENABLE.getStatus().equals(item.getStatus())).toList();
        return success(convertList(list, item -> new ErpFinanceVoucherTemplateRespVO()
                .setId(item.getId())
                .setLedgerId(item.getLedgerId())
                .setBizType(item.getBizType())
                .setName(item.getName())
                .setAutoGenerate(item.getAutoGenerate())));
    }

    @GetMapping("/page")
    @Operation(summary = "获得财务凭证模板分页")
    @PreAuthorize("@ss.hasPermission('erp:finance-voucher-template:query')")
    public CommonResult<PageResult<ErpFinanceVoucherTemplateRespVO>> getVoucherTemplatePage(@Valid ErpFinanceVoucherTemplatePageReqVO pageReqVO) {
        PageResult<ErpFinanceVoucherTemplateDO> pageResult = voucherTemplateService.getVoucherTemplatePage(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }
        Map<Long, List<ErpFinanceVoucherTemplateItemDO>> itemMap = convertMultiMap(
                voucherTemplateService.getVoucherTemplateItemListByTemplateIds(convertSet(pageResult.getList(), ErpFinanceVoucherTemplateDO::getId)),
                ErpFinanceVoucherTemplateItemDO::getTemplateId);
        Map<Long, ErpFinanceLedgerDO> ledgerMap = financeLedgerService.getFinanceLedgerMap(convertSet(pageResult.getList(), ErpFinanceVoucherTemplateDO::getLedgerId));
        List<ErpFinanceVoucherTemplateRespVO> respList = convertList(pageResult.getList(),
                template -> buildTemplateResp(template, itemMap.get(template.getId()), ledgerMap));
        return success(new PageResult<>(respList, pageResult.getTotal()));
    }

    private ErpFinanceVoucherTemplateRespVO buildTemplateResp(ErpFinanceVoucherTemplateDO template,
                                                              List<ErpFinanceVoucherTemplateItemDO> items,
                                                              Map<Long, ErpFinanceLedgerDO> ledgerMap) {
        ErpFinanceVoucherTemplateRespVO respVO = BeanUtils.toBean(template, ErpFinanceVoucherTemplateRespVO.class);
        ErpFinanceLedgerDO ledger = ledgerMap.get(template.getLedgerId());
        respVO.setLedgerName(ledger == null ? null : ledger.getName());
        respVO.setBizTypeName(resolveBizTypeName(template.getBizType()));
        respVO.setResearchCategoryName(resolveResearchCategoryName(template.getResearchCategory()));
        respVO.setItems(BeanUtils.toBean(items, ErpFinanceVoucherTemplateRespVO.Item.class, item -> {
            item.setEntryDirectionName(resolveDirectionName(item.getEntryDirection()));
            item.setAmountSourceName(resolveAmountSourceName(item.getAmountSource()));
        }));
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

    private String resolveResearchCategoryName(Integer researchCategory) {
        if (researchCategory == null) {
            return null;
        }
        ErpResearchExpenseCategoryEnum categoryEnum = ErpResearchExpenseCategoryEnum.fromType(researchCategory);
        return categoryEnum == null ? null : categoryEnum.getName();
    }

    private String resolveDirectionName(Integer direction) {
        for (ErpFinanceVoucherEntryDirectionEnum value : ErpFinanceVoucherEntryDirectionEnum.values()) {
            if (value.getType().equals(direction)) {
                return value.getName();
            }
        }
        return null;
    }

    private String resolveAmountSourceName(Integer amountSource) {
        for (ErpFinanceVoucherAmountSourceEnum value : ErpFinanceVoucherAmountSourceEnum.values()) {
            if (value.getType().equals(amountSource)) {
                return value.getName();
            }
        }
        return null;
    }
}
