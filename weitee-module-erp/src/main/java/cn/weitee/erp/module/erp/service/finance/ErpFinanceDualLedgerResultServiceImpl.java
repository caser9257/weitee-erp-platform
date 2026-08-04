package cn.weitee.erp.module.erp.service.finance;

import cn.hutool.core.util.ObjectUtil;
import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.dualledger.ErpFinanceDualLedgerRecomputeReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.dualledger.ErpFinanceDualLedgerResultPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.dualledger.ErpFinanceDualLedgerResultRespVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceDualLedgerConfigDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceDualLedgerAmountDiffLogDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceDualLedgerDiffConfigDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceLedgerDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherEntryDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceDualLedgerAmountDiffLogMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceVoucherMapper;
import cn.weitee.erp.module.erp.enums.ErpFinanceDualLedgerCompareStatusEnum;
import cn.weitee.erp.module.erp.enums.ErpFinanceDualLedgerDiffItemTypeEnum;
import cn.weitee.erp.module.erp.enums.ErpFinanceDiffCalculationTypeEnum;
import cn.weitee.erp.module.erp.enums.ErpFinanceVoucherStatusEnum;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
import cn.idev.excel.ExcelWriter;
import cn.idev.excel.FastExcel;
import cn.idev.excel.write.metadata.WriteSheet;
import cn.idev.excel.write.metadata.WriteTable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertMap;

@Service
@Validated
public class ErpFinanceDualLedgerResultServiceImpl implements ErpFinanceDualLedgerResultService {

    @Resource
    private ErpFinanceDualLedgerConfigService dualLedgerConfigService;
    @Resource
    private ErpFinanceLedgerService financeLedgerService;
    @Resource
    private ErpFinanceVoucherMapper erpFinanceVoucherMapper;
    @Resource
    private ErpFinanceVoucherService financeVoucherService;
    @Resource
    private ErpFinanceDualWriteService dualWriteService;
    @Resource
    private ErpFinanceDualLedgerDiffConfigService dualLedgerDiffConfigService;
    @Resource
    private ErpFinanceDualLedgerAmountDiffLogMapper dualLedgerAmountDiffLogMapper;

    @Override
    public PageResult<ErpFinanceDualLedgerResultRespVO> getDualLedgerResultPage(ErpFinanceDualLedgerResultPageReqVO pageReqVO) {
        List<ErpFinanceDualLedgerConfigDO> configList = dualLedgerConfigService
                .getDualLedgerConfigListByStatus(CommonStatusEnum.ENABLE.getStatus());
        if (configList == null || configList.isEmpty()) {
            return PageResult.empty(0L);
        }
        if (pageReqVO.getBizType() != null) {
            configList = configList.stream()
                    .filter(item -> ObjectUtil.equal(item.getBizType(), pageReqVO.getBizType()))
                    .collect(Collectors.toList());
        }
        if (configList.isEmpty()) {
            return PageResult.empty(0L);
        }
        Map<Integer, ErpFinanceDualLedgerConfigDO> configMap = configList.stream()
                .collect(Collectors.toMap(ErpFinanceDualLedgerConfigDO::getBizType, item -> item, (left, right) -> left,
                        LinkedHashMap::new));
        Map<Long, ErpFinanceLedgerDO> ledgerMap = loadLedgerMap(configList);
        List<ErpFinanceVoucherDO> voucherList = loadVoucherList(configMap.values(), pageReqVO);
        List<ErpFinanceDualLedgerResultRespVO> resultList = buildResultList(configMap, ledgerMap, voucherList, pageReqVO);
        long total = resultList.size();
        int fromIndex = Math.min((pageReqVO.getPageNo() - 1) * pageReqVO.getPageSize(), resultList.size());
        int toIndex = Math.min(fromIndex + pageReqVO.getPageSize(), resultList.size());
        return new PageResult<>(resultList.subList(fromIndex, toIndex), total);
    }

    @Override
    public ErpFinanceDualLedgerResultRespVO getDualLedgerResult(Integer bizType, Long bizId) {
        ErpFinanceDualLedgerConfigDO config = dualLedgerConfigService.getEnabledDualLedgerConfig(bizType);
        if (config == null) {
            return null;
        }
        Map<Long, ErpFinanceLedgerDO> ledgerMap = loadLedgerMap(Collections.singletonList(config));
        List<ErpFinanceVoucherDO> voucherList = loadVoucherList(Collections.singletonList(config),
                new ErpFinanceDualLedgerResultPageReqVO().setBizType(bizType).setBizId(bizId));
        return buildResult(config, ledgerMap, voucherList, bizType, bizId);
    }

    @Override
    public ErpFinanceDualLedgerResultRespVO recomputeDualLedgerResult(Long userId, ErpFinanceDualLedgerRecomputeReqVO reqVO) {
        financeVoucherService.recomputeAutoGeneratedVoucher(reqVO.getBizType(), reqVO.getBizId(), userId, reqVO.getRemark());
        dualWriteService.recomputeByBizId(reqVO.getBizType(), reqVO.getBizId());
        return getDualLedgerResult(reqVO.getBizType(), reqVO.getBizId());
    }

    @Override
    public void exportSingleLedger(Integer bizType, Long bizId, String ledgerSide,
                                    HttpServletResponse response) throws IOException {
        // 1. 校验参数
        if (bizType == null || bizId == null || ObjectUtil.hasEmpty(ledgerSide)) {
            throw new IllegalArgumentException("导出参数不完整");
        }
        if (!"external".equals(ledgerSide) && !"internal".equals(ledgerSide)) {
            throw new IllegalArgumentException("账套侧参数必须为 external 或 internal");
        }

        // 2. 获取双账套配置
        ErpFinanceDualLedgerConfigDO config = dualLedgerConfigService.getEnabledDualLedgerConfig(bizType);
        if (config == null) {
            throw new IllegalStateException("未找到该业务类型的双账套配置");
        }

        // 3. 确定目标账簿ID
        Long targetLedgerId = "external".equals(ledgerSide)
                ? config.getExternalLedgerId() : config.getInternalLedgerId();
        if (targetLedgerId == null) {
            throw new IllegalStateException("目标账簿未配置");
        }

        // 4. 查找目标凭证
        ErpFinanceVoucherDO voucher = financeVoucherService.getVoucherByLedgerAndBiz(
                targetLedgerId, bizType, bizId);
        if (voucher == null) {
            throw new IllegalStateException("当前账簿暂无可导出凭证");
        }

        // 5. 获取凭证分录
        List<ErpFinanceVoucherEntryDO> entries = financeVoucherService
                .getVoucherEntryListByVoucherId(voucher.getId());
        if (entries == null || entries.isEmpty()) {
            throw new IllegalStateException("凭证分录数据为空");
        }

        // 6. 获取账簿名称
        Map<Long, ErpFinanceLedgerDO> ledgerMap = financeLedgerService
                .getFinanceLedgerMap(Collections.singleton(targetLedgerId));
        ErpFinanceLedgerDO ledger = ledgerMap.get(targetLedgerId);
        String ledgerName = ledger != null ? ledger.getName() : "";

        // 7. 准备上下文数据
        String bizTypeName = resolveBizTypeName(bizType);
        String bizNo = voucher.getBizNo() != null ? voucher.getBizNo() : "";
        String voucherNo = voucher.getVoucherNo() != null ? voucher.getVoucherNo() : "";
        String voucherTime = voucher.getVoucherTime() != null
                ? voucher.getVoucherTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "";
        String voucherStatusName = resolveVoucherStatusName(voucher.getStatus());
        String exportTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 8. 构建分录导出数据
        List<VoucherEntryExportVO> exportList = new ArrayList<>(entries.size());
        for (ErpFinanceVoucherEntryDO entry : entries) {
            VoucherEntryExportVO vo = new VoucherEntryExportVO();
            vo.setVoucherNo(voucherNo);
            vo.setBizTypeName(bizTypeName);
            vo.setBizNo(bizNo);
            vo.setVoucherTime(voucherTime);
            vo.setEntryNo(entry.getEntryNo());
            vo.setSummary(entry.getSummary() != null ? entry.getSummary() : "");
            vo.setSubjectCode(entry.getSubjectCode() != null ? entry.getSubjectCode() : "");
            vo.setSubjectName(entry.getSubjectName() != null ? entry.getSubjectName() : "");
            vo.setDebitAmount(entry.getDebitAmount());
            vo.setCreditAmount(entry.getCreditAmount());
            exportList.add(vo);
        }

        // 9. 生成文件名（净化非法字符，使用中性标题不含账簿名）
        String fileName = bizNo.isEmpty()
                ? "凭证明细导出.xlsx"
                : bizNo + "-凭证明细导出.xlsx";

        // 10. 设置响应头，必须在 getOutputStream() 之前
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);

        // 11. 使用 FastExcel 多表方式导出（上下文区 + 分录明细）
        try (ExcelWriter writer = FastExcel.write(response.getOutputStream()).build()) {
            WriteSheet sheet = FastExcel.writerSheet("凭证明细").build();

            // 11.1 上下文区（单列合并显示）
            WriteTable contextTable = FastExcel.writerTable().build();
            List<List<Object>> contextData = new ArrayList<>();
            contextData.add(Collections.singletonList("业务类型：" + bizTypeName));
            contextData.add(Collections.singletonList("业务单号：" + bizNo));
            contextData.add(Collections.singletonList("凭证号：" + voucherNo));
            contextData.add(Collections.singletonList("凭证时间：" + voucherTime));
            contextData.add(Collections.singletonList("凭证状态：" + voucherStatusName));
            contextData.add(Collections.singletonList("导出时间：" + exportTime));
            writer.write(contextData, sheet, contextTable);

            // 11.2 空行分隔
            WriteTable emptyTable = FastExcel.writerTable().build();
            writer.write(Collections.singletonList(Collections.singletonList("")), sheet, emptyTable);

            // 11.3 分录明细表
            WriteTable detailTable = FastExcel.writerTable().build();
            List<List<Object>> detailData = new ArrayList<>();
            for (VoucherEntryExportVO vo : exportList) {
                List<Object> row = new ArrayList<>();
                row.add(vo.getVoucherNo());
                row.add(vo.getBizTypeName());
                row.add(vo.getBizNo());
                row.add(vo.getVoucherTime());
                row.add(vo.getEntryNo());
                row.add(vo.getSummary());
                row.add(vo.getSubjectCode());
                row.add(vo.getSubjectName());
                row.add(vo.getDebitAmount());
                row.add(vo.getCreditAmount());
                detailData.add(row);
            }
            writer.write(detailData, sheet, detailTable);
        }
    }

    /**
     * 净化文件名中的非法字符
     */
    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "未知账簿";
        }
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    /**
     * 解析凭证状态名称
     */
    private String resolveVoucherStatusName(Integer status) {
        if (status == null) {
            return "";
        }
        for (ErpFinanceVoucherStatusEnum value : ErpFinanceVoucherStatusEnum.values()) {
            if (ObjectUtil.equal(value.getStatus(), status)) {
                return value.getName();
            }
        }
        return "";
    }

    private List<ErpFinanceVoucherDO> loadVoucherList(Collection<ErpFinanceDualLedgerConfigDO> configList,
                                                      ErpFinanceDualLedgerResultPageReqVO pageReqVO) {
        Set<Long> ledgerIds = new LinkedHashSet<>();
        Set<Integer> bizTypes = new LinkedHashSet<>();
        for (ErpFinanceDualLedgerConfigDO config : configList) {
            ledgerIds.add(config.getExternalLedgerId());
            ledgerIds.add(config.getInternalLedgerId());
            bizTypes.addAll(resolveVoucherBizTypes(config.getBizType()));
        }
        List<ErpFinanceVoucherDO> voucherList = erpFinanceVoucherMapper.selectList(new LambdaQueryWrapperX<ErpFinanceVoucherDO>()
                .in(ErpFinanceVoucherDO::getLedgerId, ledgerIds)
                .in(ErpFinanceVoucherDO::getBizType, bizTypes)
                .eqIfPresent(ErpFinanceVoucherDO::getBizId, pageReqVO.getBizId())
                .likeIfPresent(ErpFinanceVoucherDO::getBizNo, pageReqVO.getBizNo())
                .orderByDesc(ErpFinanceVoucherDO::getVoucherTime)
                .orderByDesc(ErpFinanceVoucherDO::getId));
        // 排除已作废的凭证（rollbackAutoGeneratedVoucher 会将旧凭证置为 VOIDED 但不删除）
        return voucherList.stream()
                .filter(v -> !ErpFinanceVoucherStatusEnum.VOIDED.getStatus().equals(v.getStatus()))
                .collect(Collectors.toList());
    }

    private List<ErpFinanceDualLedgerResultRespVO> buildResultList(Map<Integer, ErpFinanceDualLedgerConfigDO> configMap,
                                                                   Map<Long, ErpFinanceLedgerDO> ledgerMap,
                                                                   List<ErpFinanceVoucherDO> voucherList,
                                                                   ErpFinanceDualLedgerResultPageReqVO pageReqVO) {
        Map<String, List<ErpFinanceVoucherDO>> groupedVoucherMap = voucherList.stream()
                .filter(Objects::nonNull)
                .filter(item -> configMap.containsKey(resolveSourceBizType(item.getBizType())))
                .collect(Collectors.groupingBy(item -> buildGroupKey(resolveSourceBizType(item.getBizType()), item.getBizId()),
                        LinkedHashMap::new, Collectors.toList()));
        List<ErpFinanceDualLedgerResultRespVO> resultList = new ArrayList<>();
        for (Map.Entry<String, List<ErpFinanceVoucherDO>> entry : groupedVoucherMap.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }
            Integer bizType = resolveSourceBizType(entry.getValue().get(0).getBizType());
            Long bizId = entry.getValue().get(0).getBizId();
            ErpFinanceDualLedgerResultRespVO result = buildResult(configMap.get(bizType), ledgerMap, entry.getValue(), bizType, bizId);
            if (result == null) {
                continue;
            }
            if (pageReqVO.getConsistent() != null && !ObjectUtil.equal(pageReqVO.getConsistent(), result.getConsistent())) {
                continue;
            }
            if (pageReqVO.getCompareStatus() != null && !ObjectUtil.equal(pageReqVO.getCompareStatus(), result.getCompareStatus())) {
                continue;
            }
            resultList.add(result);
        }
        resultList.sort(Comparator.comparing(ErpFinanceDualLedgerResultRespVO::getVoucherTime,
                        Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(ErpFinanceDualLedgerResultRespVO::getBizId, Comparator.nullsLast(Comparator.reverseOrder())));
        return resultList;
    }

    private ErpFinanceDualLedgerResultRespVO buildResult(ErpFinanceDualLedgerConfigDO config,
                                                         Map<Long, ErpFinanceLedgerDO> ledgerMap,
                                                         List<ErpFinanceVoucherDO> voucherList,
                                                         Integer bizType,
                                                         Long bizId) {
        if (config == null) {
            return null;
        }
        ErpFinanceVoucherDO externalVoucher = findVoucher(voucherList, config.getExternalLedgerId());
        ErpFinanceVoucherDO internalVoucher = findVoucher(voucherList, config.getInternalLedgerId());
        ErpFinanceDualLedgerResultRespVO respVO = new ErpFinanceDualLedgerResultRespVO();
        respVO.setBizType(bizType);
        respVO.setBizTypeName(resolveBizTypeName(bizType));
        respVO.setBizId(bizId);
        respVO.setBizNo(externalVoucher != null ? externalVoucher.getBizNo()
                : internalVoucher == null ? null : internalVoucher.getBizNo());
        respVO.setExternalLedgerId(config.getExternalLedgerId());
        respVO.setExternalLedgerName(resolveLedgerName(ledgerMap, config.getExternalLedgerId()));
        respVO.setInternalLedgerId(config.getInternalLedgerId());
        respVO.setInternalLedgerName(resolveLedgerName(ledgerMap, config.getInternalLedgerId()));
        fillVoucherInfo(respVO, externalVoucher, true);
        fillVoucherInfo(respVO, internalVoucher, false);
        respVO.setVoucherTime(maxTime(externalVoucher == null ? null : externalVoucher.getVoucherTime(),
                internalVoucher == null ? null : internalVoucher.getVoucherTime()));
        respVO.setDebitAmountDiff(defaultAmount(respVO.getExternalDebitAmount()).subtract(defaultAmount(respVO.getInternalDebitAmount())));
        respVO.setCreditAmountDiff(defaultAmount(respVO.getExternalCreditAmount()).subtract(defaultAmount(respVO.getInternalCreditAmount())));
        // 计算差异项详情
        respVO.setDiffItemDetails(buildDiffItemDetails(bizType, respVO));
        // 计算差异项汇总金额（与状态判定口径一致）
        respVO.setTotalItemDiffAmount(calcTotalItemDiffAmount(respVO.getDiffItemDetails()));
        List<String> issues = buildIssues(respVO);
        Integer compareStatus = resolveCompareStatus(respVO, issues);
        respVO.setIssueMessages(issues);
        respVO.setCompareStatus(compareStatus);
        respVO.setCompareStatusName(ErpFinanceDualLedgerCompareStatusEnum.resolveName(compareStatus));
        respVO.setConsistent(ErpFinanceDualLedgerCompareStatusEnum.isConsistent(compareStatus));
        return respVO;
    }

    private List<String> buildIssues(ErpFinanceDualLedgerResultRespVO respVO) {
        List<String> issues = new ArrayList<>();
        if (respVO.getExternalVoucherId() == null) {
            issues.add("外账缺少凭证");
        }
        if (respVO.getInternalVoucherId() == null) {
            issues.add("内部账缺少凭证");
        }
        if (!issues.isEmpty()) {
            return issues;
        }
        if (!ObjectUtil.equal(respVO.getExternalVoucherStatus(), respVO.getInternalVoucherStatus())) {
            issues.add("两本账凭证状态不一致");
        }
        if (defaultAmount(respVO.getDebitAmountDiff()).compareTo(BigDecimal.ZERO) != 0
                || defaultAmount(respVO.getCreditAmountDiff()).compareTo(BigDecimal.ZERO) != 0) {
            issues.add("两本账凭证金额不一致");
        }
        // 检查明细差异项：只要存在任一非零差异项，即视为不一致
        if (hasNonZeroDiffItemDetails(respVO)) {
            issues.add("差异项明细存在不一致");
        }
        return issues;
    }

    private Integer resolveCompareStatus(ErpFinanceDualLedgerResultRespVO respVO, List<String> issues) {
        if (respVO.getExternalVoucherId() == null || respVO.getInternalVoucherId() == null) {
            return ErpFinanceDualLedgerCompareStatusEnum.MISSING.getStatus();
        }
        if (!ObjectUtil.equal(respVO.getExternalVoucherStatus(), respVO.getInternalVoucherStatus())) {
            return ErpFinanceDualLedgerCompareStatusEnum.STATUS_DIFF.getStatus();
        }
        if (defaultAmount(respVO.getDebitAmountDiff()).compareTo(BigDecimal.ZERO) != 0
                || defaultAmount(respVO.getCreditAmountDiff()).compareTo(BigDecimal.ZERO) != 0) {
            return ErpFinanceDualLedgerCompareStatusEnum.AMOUNT_DIFF.getStatus();
        }
        // 明细差异项：只要存在任一非零差异项，即视为不一致
        if (hasNonZeroDiffItemDetails(respVO)) {
            return ErpFinanceDualLedgerCompareStatusEnum.ITEM_DIFF.getStatus();
        }
        return ErpFinanceDualLedgerCompareStatusEnum.MATCHED.getStatus();
    }

    /**
     * 判断差异项详情中是否存在任一非零差异金额
     */
    private boolean hasNonZeroDiffItemDetails(ErpFinanceDualLedgerResultRespVO respVO) {
        List<ErpFinanceDualLedgerResultRespVO.DiffItemDetail> details = respVO.getDiffItemDetails();
        if (details == null || details.isEmpty()) {
            return false;
        }
        return details.stream()
                .anyMatch(detail -> defaultAmount(detail.getDiffAmount()).compareTo(BigDecimal.ZERO) != 0);
    }

    /**
     * 汇总所有差异项的差异金额，用于前端"差额"列展示（与状态判定口径一致）
     */
    private BigDecimal calcTotalItemDiffAmount(List<ErpFinanceDualLedgerResultRespVO.DiffItemDetail> details) {
        if (details == null || details.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return details.stream()
                .map(detail -> defaultAmount(detail.getDiffAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void fillVoucherInfo(ErpFinanceDualLedgerResultRespVO respVO, ErpFinanceVoucherDO voucher, boolean external) {
        if (voucher == null) {
            return;
        }
        if (external) {
            respVO.setExternalVoucherId(voucher.getId());
            respVO.setExternalVoucherNo(voucher.getVoucherNo());
            respVO.setExternalVoucherStatus(voucher.getStatus());
            respVO.setExternalDebitAmount(voucher.getTotalDebitAmount());
            respVO.setExternalCreditAmount(voucher.getTotalCreditAmount());
            return;
        }
        respVO.setInternalVoucherId(voucher.getId());
        respVO.setInternalVoucherNo(voucher.getVoucherNo());
        respVO.setInternalVoucherStatus(voucher.getStatus());
        respVO.setInternalDebitAmount(voucher.getTotalDebitAmount());
        respVO.setInternalCreditAmount(voucher.getTotalCreditAmount());
    }

    private ErpFinanceVoucherDO findVoucher(List<ErpFinanceVoucherDO> voucherList, Long ledgerId) {
        if (voucherList == null || voucherList.isEmpty()) {
            return null;
        }
        return voucherList.stream()
                .filter(item -> ObjectUtil.equal(item.getLedgerId(), ledgerId))
                .max(Comparator.comparing(ErpFinanceVoucherDO::getId))
                .orElse(null);
    }

    private Map<Long, ErpFinanceLedgerDO> loadLedgerMap(Collection<ErpFinanceDualLedgerConfigDO> configList) {
        Set<Long> ledgerIds = new LinkedHashSet<>();
        for (ErpFinanceDualLedgerConfigDO config : configList) {
            ledgerIds.add(config.getExternalLedgerId());
            ledgerIds.add(config.getInternalLedgerId());
        }
        return convertMap(financeLedgerService.getFinanceLedgerList(ledgerIds), ErpFinanceLedgerDO::getId);
    }

    private Set<Integer> resolveVoucherBizTypes(Integer sourceBizType) {
        if (ObjectUtil.equal(sourceBizType, ErpBizTypeEnum.FINANCE_EXPENSE.getType())) {
            return Set.of(ErpBizTypeEnum.FINANCE_EXPENSE.getType(),
                    ErpBizTypeEnum.FINANCE_EXPENSE_EXPENSE.getType(),
                    ErpBizTypeEnum.FINANCE_EXPENSE_CAPITALIZE.getType());
        }
        return Set.of(sourceBizType);
    }

    private Integer resolveSourceBizType(Integer actualBizType) {
        if (ObjectUtil.equal(actualBizType, ErpBizTypeEnum.FINANCE_EXPENSE_EXPENSE.getType())
                || ObjectUtil.equal(actualBizType, ErpBizTypeEnum.FINANCE_EXPENSE_CAPITALIZE.getType())) {
            return ErpBizTypeEnum.FINANCE_EXPENSE.getType();
        }
        return actualBizType;
    }

    private String buildGroupKey(Integer bizType, Long bizId) {
        return bizType + "_" + ObjectUtil.defaultIfNull(bizId, 0L);
    }

    private String resolveLedgerName(Map<Long, ErpFinanceLedgerDO> ledgerMap, Long ledgerId) {
        ErpFinanceLedgerDO ledger = ledgerMap.get(ledgerId);
        return ledger == null ? null : ledger.getName();
    }

    private String resolveBizTypeName(Integer bizType) {
        for (ErpBizTypeEnum value : ErpBizTypeEnum.values()) {
            if (ObjectUtil.equal(value.getType(), bizType)) {
                return value.getName();
            }
        }
        return null;
    }

    private LocalDateTime maxTime(LocalDateTime left, LocalDateTime right) {
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        return left.isAfter(right) ? left : right;
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return ObjectUtil.defaultIfNull(amount, BigDecimal.ZERO);
    }

    /**
     * 构建差异项详情列表
     */
    private List<ErpFinanceDualLedgerResultRespVO.DiffItemDetail> buildDiffItemDetails(
            Integer bizType, ErpFinanceDualLedgerResultRespVO respVO) {
        List<ErpFinanceDualLedgerAmountDiffLogDO> diffLogs = dualLedgerAmountDiffLogMapper
                .selectListByBizTypeAndBizId(bizType, respVO.getBizId());
        if (diffLogs != null && !diffLogs.isEmpty()) {
            return diffLogs.stream().map(this::buildDiffItemDetailFromLog).collect(Collectors.toList());
        }
        // 查询该业务类型的差异配置
        List<ErpFinanceDualLedgerDiffConfigDO> diffConfigs = dualLedgerDiffConfigService
                .getDualLedgerDiffConfigList(bizType, CommonStatusEnum.ENABLE.getStatus());
        if (diffConfigs == null || diffConfigs.isEmpty()) {
            return Collections.emptyList();
        }

        List<ErpFinanceDualLedgerResultRespVO.DiffItemDetail> details = new ArrayList<>();
        for (ErpFinanceDualLedgerDiffConfigDO diffConfig : diffConfigs) {
            ErpFinanceDualLedgerResultRespVO.DiffItemDetail detail = new ErpFinanceDualLedgerResultRespVO.DiffItemDetail();
            detail.setDiffItemType(diffConfig.getDiffItemType());
            detail.setDiffItemTypeName(ErpFinanceDualLedgerDiffItemTypeEnum.resolveName(diffConfig.getDiffItemType()));
            detail.setCalculationType(diffConfig.getCalculationType());
            detail.setCalculationTypeName(ErpFinanceDiffCalculationTypeEnum.resolveName(diffConfig.getCalculationType()));

            // 计算差异金额（简化处理，实际应该根据凭证分录计算）
            BigDecimal internalAmount = defaultAmount(respVO.getInternalDebitAmount());
            BigDecimal externalAmount = defaultAmount(respVO.getExternalDebitAmount());
            detail.setInternalAmount(internalAmount);
            detail.setExternalAmount(externalAmount);
            detail.setDiffAmount(internalAmount.subtract(externalAmount));

            // 计算差异比例
            if (internalAmount.compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal diffRatio = detail.getDiffAmount().multiply(new BigDecimal("100"))
                        .divide(internalAmount, 2, java.math.RoundingMode.HALF_UP);
                detail.setDiffRatio(diffRatio);
            } else {
                detail.setDiffRatio(BigDecimal.ZERO);
            }

            details.add(detail);
        }
        return details;
    }

    private ErpFinanceDualLedgerResultRespVO.DiffItemDetail buildDiffItemDetailFromLog(
            ErpFinanceDualLedgerAmountDiffLogDO log) {
        ErpFinanceDualLedgerResultRespVO.DiffItemDetail detail = new ErpFinanceDualLedgerResultRespVO.DiffItemDetail();
        detail.setDiffItemType(log.getDiffItemType());
        detail.setDiffItemTypeName(ErpFinanceDualLedgerDiffItemTypeEnum.resolveName(log.getDiffItemType()));
        detail.setCalculationType(log.getCalculationType());
        detail.setCalculationTypeName(ErpFinanceDiffCalculationTypeEnum.resolveName(log.getCalculationType()));
        detail.setInternalAmount(defaultAmount(log.getInternalAmount()));
        detail.setExternalAmount(defaultAmount(log.getExternalAmount()));
        detail.setDiffAmount(defaultAmount(log.getDiffAmount()));
        if (defaultAmount(log.getInternalAmount()).compareTo(BigDecimal.ZERO) != 0) {
            detail.setDiffRatio(defaultAmount(log.getDiffAmount()).multiply(new BigDecimal("100"))
                    .divide(defaultAmount(log.getInternalAmount()), 2, java.math.RoundingMode.HALF_UP));
        } else {
            detail.setDiffRatio(BigDecimal.ZERO);
        }
        return detail;
    }

    /**
     * 凭证分录导出 VO（仅用于内部数据传递，不使用 @ExcelProperty）
     */
    @lombok.Data
    public static class VoucherEntryExportVO {

        private String voucherNo;

        private String bizTypeName;

        private String bizNo;

        private String voucherTime;

        private Integer entryNo;

        private String summary;

        private String subjectCode;

        private String subjectName;

        private BigDecimal debitAmount;

        private BigDecimal creditAmount;
    }
}
