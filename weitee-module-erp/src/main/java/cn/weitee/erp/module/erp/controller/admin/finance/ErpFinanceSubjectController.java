package cn.weitee.erp.module.erp.controller.admin.finance;

import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.subject.ErpFinanceSubjectPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.subject.ErpFinanceSubjectRespVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.subject.ErpFinanceSubjectSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceLedgerDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceSubjectDO;
import cn.weitee.erp.module.erp.enums.ErpFinanceSubjectTypeEnum;
import cn.weitee.erp.module.erp.enums.ErpFinanceVoucherEntryDirectionEnum;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceLedgerService;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceSubjectService;
import cn.weitee.erp.module.erp.service.finance.FinanceDataPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;
import static cn.weitee.erp.framework.common.exception.enums.GlobalErrorCodeConstants.FORBIDDEN;
import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertList;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - ERP 财务科目")
@RestController
@RequestMapping("/erp/finance-subject")
@Validated
public class ErpFinanceSubjectController {

    @Resource
    private ErpFinanceSubjectService financeSubjectService;
    @Resource
    private ErpFinanceLedgerService financeLedgerService;
    @Resource
    private FinanceDataPermissionService financeDataPermissionService;

    @PostMapping("/create")
    @Operation(summary = "创建财务科目")
    @PreAuthorize("@ss.hasPermission('erp:finance-subject:create')")
    public CommonResult<Long> createFinanceSubject(@Valid @RequestBody ErpFinanceSubjectSaveReqVO createReqVO) {
        validateLedgerAccess(createReqVO.getLedgerId());
        validateSubjectAccess(createReqVO.getLedgerId(), createReqVO.getSubjectCode());
        return success(financeSubjectService.createFinanceSubject(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新财务科目")
    @PreAuthorize("@ss.hasPermission('erp:finance-subject:update')")
    public CommonResult<Boolean> updateFinanceSubject(@Valid @RequestBody ErpFinanceSubjectSaveReqVO updateReqVO) {
        validateLedgerAccess(updateReqVO.getLedgerId());
        validateExistingSubjectAccess(updateReqVO.getId());
        validateSubjectAccess(updateReqVO.getLedgerId(), updateReqVO.getSubjectCode());
        financeSubjectService.updateFinanceSubject(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除财务科目")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:finance-subject:delete')")
    public CommonResult<Boolean> deleteFinanceSubject(@RequestParam("id") Long id) {
        validateExistingSubjectAccess(id);
        financeSubjectService.deleteFinanceSubject(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得财务科目")
    @PreAuthorize("@ss.hasPermission('erp:finance-subject:query')")
    public CommonResult<ErpFinanceSubjectRespVO> getFinanceSubject(@RequestParam("id") Long id) {
        ErpFinanceSubjectDO subject = financeSubjectService.getFinanceSubject(id);
        if (subject == null) {
            return success(null);
        }
        validateLedgerAccess(subject.getLedgerId());
        validateSubjectAccess(subject.getLedgerId(), subject.getSubjectCode());
        Map<Long, ErpFinanceLedgerDO> ledgerMap = financeLedgerService.getFinanceLedgerMap(Collections.singleton(subject.getLedgerId()));
        return success(buildSubjectResp(subject, ledgerMap));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得启用财务科目精简列表")
    public CommonResult<List<ErpFinanceSubjectRespVO>> getFinanceSubjectSimpleList(@RequestParam("ledgerId") Long ledgerId) {
        validateLedgerAccess(ledgerId);
        List<ErpFinanceSubjectDO> list = financeSubjectService.getFinanceSubjectListByLedgerId(ledgerId)
                .stream().filter(item -> CommonStatusEnum.ENABLE.getStatus().equals(item.getStatus()))
                .filter(item -> financeDataPermissionService.canAccessSubject(ledgerId, item.getSubjectCode())).toList();
        return success(convertList(list, item -> new ErpFinanceSubjectRespVO()
                .setId(item.getId())
                .setLedgerId(item.getLedgerId())
                .setParentId(item.getParentId())
                .setSubjectCode(item.getSubjectCode())
                .setSubjectName(item.getSubjectName())
                .setSubjectType(item.getSubjectType())
                .setSubjectTypeName(resolveSubjectTypeName(item.getSubjectType()))
                .setBalanceDirection(item.getBalanceDirection())
                .setBalanceDirectionName(resolveDirectionName(item.getBalanceDirection()))
                .setLeaf(item.getLeaf())
                .setStatus(item.getStatus())));
    }

    @GetMapping("/page")
    @Operation(summary = "获得财务科目分页")
    @PreAuthorize("@ss.hasPermission('erp:finance-subject:query')")
    public CommonResult<PageResult<ErpFinanceSubjectRespVO>> getFinanceSubjectPage(@Valid ErpFinanceSubjectPageReqVO pageReqVO) {
        PageResult<ErpFinanceSubjectDO> pageResult = financeSubjectService.getFinanceSubjectPage(pageReqVO);
        if (pageResult.getList().isEmpty()) {
            return success(PageResult.empty(pageResult.getTotal()));
        }
        Map<Long, ErpFinanceLedgerDO> ledgerMap = financeLedgerService.getFinanceLedgerMap(
                convertSet(pageResult.getList(), ErpFinanceSubjectDO::getLedgerId));
        return success(new PageResult<>(convertList(pageResult.getList(), item -> buildSubjectResp(item, ledgerMap)),
                pageResult.getTotal()));
    }

    private ErpFinanceSubjectRespVO buildSubjectResp(ErpFinanceSubjectDO subject, Map<Long, ErpFinanceLedgerDO> ledgerMap) {
        ErpFinanceSubjectRespVO respVO = BeanUtils.toBean(subject, ErpFinanceSubjectRespVO.class);
        ErpFinanceLedgerDO ledger = ledgerMap.get(subject.getLedgerId());
        respVO.setLedgerName(ledger == null ? null : ledger.getName());
        respVO.setSubjectTypeName(resolveSubjectTypeName(subject.getSubjectType()));
        respVO.setBalanceDirectionName(resolveDirectionName(subject.getBalanceDirection()));
        return respVO;
    }

    private String resolveSubjectTypeName(Integer subjectType) {
        for (ErpFinanceSubjectTypeEnum value : ErpFinanceSubjectTypeEnum.values()) {
            if (value.getType().equals(subjectType)) {
                return value.getName();
            }
        }
        return null;
    }

    private String resolveDirectionName(Integer direction) {
        for (ErpFinanceVoucherEntryDirectionEnum value : ErpFinanceVoucherEntryDirectionEnum.values()) {
            if (value.getType().equals(direction)) {
                return value.getName();
            }
        }
        return null;
    }

    private void validateExistingSubjectAccess(Long id) {
        if (id == null) {
            return;
        }
        ErpFinanceSubjectDO subject = financeSubjectService.getFinanceSubject(id);
        if (subject != null) {
            validateLedgerAccess(subject.getLedgerId());
            validateSubjectAccess(subject.getLedgerId(), subject.getSubjectCode());
        }
    }

    private void validateLedgerAccess(Long ledgerId) {
        if (!financeDataPermissionService.canAccessLedger(ledgerId)) {
            throw exception(FORBIDDEN);
        }
    }

    private void validateSubjectAccess(Long ledgerId, String subjectCode) {
        if (!financeDataPermissionService.canAccessSubject(ledgerId, subjectCode)) {
            throw exception(FORBIDDEN);
        }
    }
}
