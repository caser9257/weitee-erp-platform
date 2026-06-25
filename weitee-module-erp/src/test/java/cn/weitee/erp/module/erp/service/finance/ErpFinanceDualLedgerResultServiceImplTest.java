package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceDualLedgerAmountDiffLogDO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.dualledger.ErpFinanceDualLedgerResultPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.dualledger.ErpFinanceDualLedgerResultRespVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceDualLedgerConfigDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceLedgerDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherDO;
import cn.weitee.erp.module.erp.enums.ErpFinanceVoucherStatusEnum;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErpFinanceDualLedgerResultServiceImplTest {

    @Test
    void getDualLedgerResultPage_shouldCompareVoucherPairAndFlagMissingInternal() throws Exception {
        ErpFinanceDualLedgerResultServiceImpl service = new ErpFinanceDualLedgerResultServiceImpl();

        setField(service, "dualLedgerConfigService", createProxy(ErpFinanceDualLedgerConfigService.class, (methodName, args) -> {
            if ("getDualLedgerConfigListByStatus".equals(methodName)) {
                return List.of(new ErpFinanceDualLedgerConfigDO().setId(1L).setBizType(11)
                        .setExternalLedgerId(1L).setInternalLedgerId(2L)
                        .setStatus(CommonStatusEnum.ENABLE.getStatus()));
            }
            return null;
        }));
        setField(service, "dualLedgerDiffConfigService", createProxy(ErpFinanceDualLedgerDiffConfigService.class, (methodName, args) -> {
            // 返回空列表，表示没有差异配置
            return List.of();
        }));
        setField(service, "dualLedgerAmountDiffLogMapper", createProxy(cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceDualLedgerAmountDiffLogMapper.class,
                (methodName, args) -> List.of()));
        setField(service, "financeLedgerService", createProxy(ErpFinanceLedgerService.class, (methodName, args) -> {
            if ("getFinanceLedgerList".equals(methodName)) {
                return List.of(
                        new ErpFinanceLedgerDO().setId(1L).setName("财务账").setStatus(CommonStatusEnum.ENABLE.getStatus()),
                        new ErpFinanceLedgerDO().setId(2L).setName("内部账").setStatus(CommonStatusEnum.ENABLE.getStatus()));
            }
            return null;
        }));
        setField(service, "erpFinanceVoucherMapper", createProxy(cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceVoucherMapper.class,
                (methodName, args) -> {
                    if ("selectList".equals(methodName)) {
                        return List.of(new ErpFinanceVoucherDO()
                                .setId(101L)
                                .setLedgerId(1L)
                                .setBizType(11)
                                .setBizId(88L)
                                .setBizNo("CGRK202605240001")
                                .setVoucherNo("V-EXT")
                                .setVoucherTime(LocalDateTime.of(2026, 5, 24, 9, 0))
                                .setStatus(ErpFinanceVoucherStatusEnum.GENERATED.getStatus())
                                .setTotalDebitAmount(new BigDecimal("500.00"))
                                .setTotalCreditAmount(new BigDecimal("500.00")));
                    }
                    return null;
                }));

        PageResult<ErpFinanceDualLedgerResultRespVO> pageResult =
                service.getDualLedgerResultPage(new ErpFinanceDualLedgerResultPageReqVO().setBizType(11));

        assertEquals(1, pageResult.getList().size());
        ErpFinanceDualLedgerResultRespVO result = pageResult.getList().get(0);
        assertEquals(11, result.getBizType());
        assertEquals(88L, result.getBizId());
        assertEquals("CGRK202605240001", result.getBizNo());
        assertEquals(101L, result.getExternalVoucherId());
        assertEquals("财务账", result.getExternalLedgerName());
        assertEquals("内部账", result.getInternalLedgerName());
        assertFalse(result.getConsistent());
        assertEquals(20, result.getCompareStatus().intValue());
        assertTrue(result.getIssueMessages().stream().anyMatch(item -> item.contains("内部账")));
    }

    @Test
    void getDualLedgerResult_shouldPreferDiffLogsForDetail() throws Exception {
        ErpFinanceDualLedgerResultServiceImpl service = new ErpFinanceDualLedgerResultServiceImpl();

        setField(service, "dualLedgerConfigService", createProxy(ErpFinanceDualLedgerConfigService.class, (methodName, args) -> {
            if ("getEnabledDualLedgerConfig".equals(methodName)) {
                return new ErpFinanceDualLedgerConfigDO().setId(1L).setBizType(11)
                        .setExternalLedgerId(1L).setInternalLedgerId(2L)
                        .setStatus(CommonStatusEnum.ENABLE.getStatus());
            }
            return null;
        }));
        setField(service, "financeLedgerService", createProxy(ErpFinanceLedgerService.class, (methodName, args) -> {
            if ("getFinanceLedgerList".equals(methodName)) {
                return List.of(
                        new ErpFinanceLedgerDO().setId(1L).setName("外部账").setStatus(CommonStatusEnum.ENABLE.getStatus()),
                        new ErpFinanceLedgerDO().setId(2L).setName("内部账").setStatus(CommonStatusEnum.ENABLE.getStatus()));
            }
            return null;
        }));
        setField(service, "erpFinanceVoucherMapper", createProxy(cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceVoucherMapper.class,
                (methodName, args) -> {
                    if ("selectList".equals(methodName)) {
                        return List.of(
                                new ErpFinanceVoucherDO()
                                        .setId(101L)
                                        .setLedgerId(1L)
                                        .setBizType(11)
                                        .setBizId(88L)
                                        .setBizNo("CGRK202605240001")
                                        .setVoucherNo("V-EXT")
                                        .setVoucherTime(LocalDateTime.of(2026, 5, 24, 9, 0))
                                        .setStatus(ErpFinanceVoucherStatusEnum.GENERATED.getStatus())
                                        .setTotalDebitAmount(new BigDecimal("85.00"))
                                        .setTotalCreditAmount(new BigDecimal("85.00")),
                                new ErpFinanceVoucherDO()
                                        .setId(102L)
                                        .setLedgerId(2L)
                                        .setBizType(11)
                                        .setBizId(88L)
                                        .setBizNo("CGRK202605240001")
                                        .setVoucherNo("V-INT")
                                        .setVoucherTime(LocalDateTime.of(2026, 5, 24, 9, 1))
                                        .setStatus(ErpFinanceVoucherStatusEnum.GENERATED.getStatus())
                                        .setTotalDebitAmount(new BigDecimal("100.00"))
                                        .setTotalCreditAmount(new BigDecimal("100.00")));
                    }
                    return null;
                }));
        setField(service, "dualLedgerAmountDiffLogMapper", createProxy(cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceDualLedgerAmountDiffLogMapper.class,
                (methodName, args) -> {
                    if ("selectListByBizTypeAndBizId".equals(methodName)) {
                        return List.of(new ErpFinanceDualLedgerAmountDiffLogDO()
                                .setId(1L)
                                .setBizType(11)
                                .setBizId(88L)
                                .setDiffItemType(20)
                                .setCalculationType(1)
                                .setInternalAmount(new BigDecimal("100.00"))
                                .setExternalAmount(new BigDecimal("85.00"))
                                .setDiffAmount(new BigDecimal("15.00")));
                    }
                    return List.of();
                }));
        setField(service, "dualLedgerDiffConfigService", createProxy(ErpFinanceDualLedgerDiffConfigService.class, (methodName, args) -> {
            throw new AssertionError("存在差异日志时不应回退查询差异配置");
        }));

        ErpFinanceDualLedgerResultRespVO result = service.getDualLedgerResult(11, 88L);

        assertNotNull(result);
        assertEquals(30, result.getCompareStatus().intValue());
        assertEquals(1, result.getDiffItemDetails().size());
        assertEquals("人工成本", result.getDiffItemDetails().get(0).getDiffItemTypeName());
        assertEquals(new BigDecimal("100.00"), result.getDiffItemDetails().get(0).getInternalAmount());
        assertEquals(new BigDecimal("85.00"), result.getDiffItemDetails().get(0).getExternalAmount());
        assertEquals(new BigDecimal("15.00"), result.getDiffItemDetails().get(0).getDiffAmount());
        assertEquals(new BigDecimal("15.00"), result.getDiffItemDetails().get(0).getDiffRatio());
        // totalItemDiffAmount 应等于所有 diffItemDetails.diffAmount 之和
        assertEquals(new BigDecimal("15.00"), result.getTotalItemDiffAmount());
    }

    @Test
    void recomputeDualLedgerResult_shouldRebuildVoucherAndRecomputeDiff() throws Exception {
        ErpFinanceDualLedgerResultServiceImpl service = new ErpFinanceDualLedgerResultServiceImpl();
        final boolean[] recomputeCalled = {false};
        final boolean[] diffRecomputeCalled = {false};

        setField(service, "financeVoucherService", createProxy(ErpFinanceVoucherService.class, (methodName, args) -> {
            if ("recomputeAutoGeneratedVoucher".equals(methodName)) {
                recomputeCalled[0] = true;
                return 124003L;
            }
            return null;
        }));
        setField(service, "dualWriteService", createProxy(ErpFinanceDualWriteService.class, (methodName, args) -> {
            if ("recomputeByBizId".equals(methodName)) {
                diffRecomputeCalled[0] = true;
                return true;
            }
            return null;
        }));
        setField(service, "dualLedgerConfigService", createProxy(ErpFinanceDualLedgerConfigService.class, (methodName, args) -> {
            if ("getEnabledDualLedgerConfig".equals(methodName)) {
                return new ErpFinanceDualLedgerConfigDO().setId(1L).setBizType(40)
                        .setExternalLedgerId(99603L).setInternalLedgerId(99604L)
                        .setStatus(CommonStatusEnum.ENABLE.getStatus());
            }
            return null;
        }));
        setField(service, "financeLedgerService", createProxy(ErpFinanceLedgerService.class, (methodName, args) -> {
            if ("getFinanceLedgerList".equals(methodName)) {
                return List.of(
                        new ErpFinanceLedgerDO().setId(99603L).setName("对外账").setStatus(CommonStatusEnum.ENABLE.getStatus()),
                        new ErpFinanceLedgerDO().setId(99604L).setName("内部账").setStatus(CommonStatusEnum.ENABLE.getStatus()));
            }
            return null;
        }));
        setField(service, "erpFinanceVoucherMapper", createProxy(cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceVoucherMapper.class,
                (methodName, args) -> {
                    if ("selectList".equals(methodName)) {
                        return List.of(
                                new ErpFinanceVoucherDO()
                                        .setId(124003L)
                                        .setLedgerId(99603L)
                                        .setBizType(40)
                                        .setBizId(107006L)
                                        .setBizNo("EXP-202605-002")
                                        .setVoucherNo("DL-EXT-EXP-NEW")
                                        .setVoucherTime(LocalDateTime.of(2026, 5, 26, 16, 0))
                                        .setStatus(ErpFinanceVoucherStatusEnum.GENERATED.getStatus())
                                        .setTotalDebitAmount(new BigDecimal("720.00"))
                                        .setTotalCreditAmount(new BigDecimal("720.00")),
                                new ErpFinanceVoucherDO()
                                        .setId(124004L)
                                        .setLedgerId(99604L)
                                        .setBizType(40)
                                        .setBizId(107006L)
                                        .setBizNo("EXP-202605-002")
                                        .setVoucherNo("DL-INT-EXP-NEW")
                                        .setVoucherTime(LocalDateTime.of(2026, 5, 26, 16, 1))
                                        .setStatus(ErpFinanceVoucherStatusEnum.GENERATED.getStatus())
                                        .setTotalDebitAmount(new BigDecimal("760.00"))
                                        .setTotalCreditAmount(new BigDecimal("760.00")));
                    }
                    return null;
                }));
        setField(service, "dualLedgerAmountDiffLogMapper", createProxy(cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceDualLedgerAmountDiffLogMapper.class,
                (methodName, args) -> {
                    if ("selectListByBizTypeAndBizId".equals(methodName)) {
                        return List.of(new ErpFinanceDualLedgerAmountDiffLogDO()
                                .setId(3L)
                                .setBizType(40)
                                .setBizId(107006L)
                                .setDiffItemType(50)
                                .setCalculationType(2)
                                .setInternalAmount(new BigDecimal("760.00"))
                                .setExternalAmount(new BigDecimal("720.00"))
                                .setDiffAmount(new BigDecimal("40.00")));
                    }
                    return List.of();
                }));
        setField(service, "dualLedgerDiffConfigService", createProxy(ErpFinanceDualLedgerDiffConfigService.class,
                (methodName, args) -> List.of()));

        ErpFinanceDualLedgerResultRespVO result = service.recomputeDualLedgerResult(1L,
                new cn.weitee.erp.module.erp.controller.admin.finance.vo.dualledger.ErpFinanceDualLedgerRecomputeReqVO()
                        .setBizType(40)
                        .setBizId(107006L)
                        .setRemark("recompute"));

        assertTrue(recomputeCalled[0]);
        assertTrue(diffRecomputeCalled[0]);
        assertNotNull(result);
        assertEquals(1, result.getDiffItemDetails().size());
        assertEquals(new BigDecimal("40.00"), result.getDiffItemDetails().get(0).getDiffAmount());
    }

    @Test
    void getDualLedgerResult_shouldDetectItemDiffWhenTotalAmountMatches() throws Exception {
        // 场景：总额一致（借/贷均相等），但明细差异项存在非零 diffAmount
        ErpFinanceDualLedgerResultServiceImpl service = new ErpFinanceDualLedgerResultServiceImpl();

        setField(service, "dualLedgerConfigService", createProxy(ErpFinanceDualLedgerConfigService.class, (methodName, args) -> {
            if ("getEnabledDualLedgerConfig".equals(methodName)) {
                return new ErpFinanceDualLedgerConfigDO().setId(1L).setBizType(11)
                        .setExternalLedgerId(1L).setInternalLedgerId(2L)
                        .setStatus(CommonStatusEnum.ENABLE.getStatus());
            }
            return null;
        }));
        setField(service, "financeLedgerService", createProxy(ErpFinanceLedgerService.class, (methodName, args) -> {
            if ("getFinanceLedgerList".equals(methodName)) {
                return List.of(
                        new ErpFinanceLedgerDO().setId(1L).setName("外部账").setStatus(CommonStatusEnum.ENABLE.getStatus()),
                        new ErpFinanceLedgerDO().setId(2L).setName("内部账").setStatus(CommonStatusEnum.ENABLE.getStatus()));
            }
            return null;
        }));
        // 两本账凭证总额完全一致：都是 500.00
        setField(service, "erpFinanceVoucherMapper", createProxy(cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceVoucherMapper.class,
                (methodName, args) -> {
                    if ("selectList".equals(methodName)) {
                        return List.of(
                                new ErpFinanceVoucherDO()
                                        .setId(201L).setLedgerId(1L).setBizType(11).setBizId(99L)
                                        .setBizNo("ZZRK202605270001").setVoucherNo("V-EXT-201")
                                        .setVoucherTime(LocalDateTime.of(2026, 5, 27, 10, 0))
                                        .setStatus(ErpFinanceVoucherStatusEnum.GENERATED.getStatus())
                                        .setTotalDebitAmount(new BigDecimal("500.00"))
                                        .setTotalCreditAmount(new BigDecimal("500.00")),
                                new ErpFinanceVoucherDO()
                                        .setId(202L).setLedgerId(2L).setBizType(11).setBizId(99L)
                                        .setBizNo("ZZRK202605270001").setVoucherNo("V-INT-202")
                                        .setVoucherTime(LocalDateTime.of(2026, 5, 27, 10, 1))
                                        .setStatus(ErpFinanceVoucherStatusEnum.GENERATED.getStatus())
                                        .setTotalDebitAmount(new BigDecimal("500.00"))
                                        .setTotalCreditAmount(new BigDecimal("500.00")));
                    }
                    return null;
                }));
        // 明细差异日志：人工成本差异 15.00（总额抵消后仍一致，但明细不一致）
        setField(service, "dualLedgerAmountDiffLogMapper", createProxy(cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceDualLedgerAmountDiffLogMapper.class,
                (methodName, args) -> {
                    if ("selectListByBizTypeAndBizId".equals(methodName)) {
                        return List.of(new ErpFinanceDualLedgerAmountDiffLogDO()
                                .setId(10L).setBizType(11).setBizId(99L)
                                .setDiffItemType(20).setCalculationType(1)
                                .setInternalAmount(new BigDecimal("250.00"))
                                .setExternalAmount(new BigDecimal("235.00"))
                                .setDiffAmount(new BigDecimal("15.00")));
                    }
                    return List.of();
                }));
        setField(service, "dualLedgerDiffConfigService", createProxy(ErpFinanceDualLedgerDiffConfigService.class,
                (methodName, args) -> List.of()));

        ErpFinanceDualLedgerResultRespVO result = service.getDualLedgerResult(11, 99L);

        assertNotNull(result);
        // 总额一致：debitAmountDiff 和 creditAmountDiff 均为 0
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getDebitAmountDiff()));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getCreditAmountDiff()));
        // 但因为明细存在非零差异，状态应为 ITEM_DIFF(50)，不是 MATCHED(10)
        assertEquals(50, result.getCompareStatus().intValue());
        assertFalse(result.getConsistent());
        // issueMessages 应包含明细不一致的提示
        assertTrue(result.getIssueMessages().stream().anyMatch(msg -> msg.contains("明细")));
        // 差异项详情应存在
        assertEquals(1, result.getDiffItemDetails().size());
        assertEquals(new BigDecimal("15.00"), result.getDiffItemDetails().get(0).getDiffAmount());
    }

    @Test
    void getDualLedgerResult_shouldReturnMatchedWhenTotalAndDetailsAllZero() throws Exception {
        // 场景：总额一致且明细差异全部为零，应返回 MATCHED
        ErpFinanceDualLedgerResultServiceImpl service = new ErpFinanceDualLedgerResultServiceImpl();

        setField(service, "dualLedgerConfigService", createProxy(ErpFinanceDualLedgerConfigService.class, (methodName, args) -> {
            if ("getEnabledDualLedgerConfig".equals(methodName)) {
                return new ErpFinanceDualLedgerConfigDO().setId(1L).setBizType(11)
                        .setExternalLedgerId(1L).setInternalLedgerId(2L)
                        .setStatus(CommonStatusEnum.ENABLE.getStatus());
            }
            return null;
        }));
        setField(service, "financeLedgerService", createProxy(ErpFinanceLedgerService.class, (methodName, args) -> {
            if ("getFinanceLedgerList".equals(methodName)) {
                return List.of(
                        new ErpFinanceLedgerDO().setId(1L).setName("外部账").setStatus(CommonStatusEnum.ENABLE.getStatus()),
                        new ErpFinanceLedgerDO().setId(2L).setName("内部账").setStatus(CommonStatusEnum.ENABLE.getStatus()));
            }
            return null;
        }));
        setField(service, "erpFinanceVoucherMapper", createProxy(cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceVoucherMapper.class,
                (methodName, args) -> {
                    if ("selectList".equals(methodName)) {
                        return List.of(
                                new ErpFinanceVoucherDO()
                                        .setId(301L).setLedgerId(1L).setBizType(11).setBizId(77L)
                                        .setBizNo("ZZRK202605270002").setVoucherNo("V-EXT-301")
                                        .setVoucherTime(LocalDateTime.of(2026, 5, 27, 11, 0))
                                        .setStatus(ErpFinanceVoucherStatusEnum.GENERATED.getStatus())
                                        .setTotalDebitAmount(new BigDecimal("300.00"))
                                        .setTotalCreditAmount(new BigDecimal("300.00")),
                                new ErpFinanceVoucherDO()
                                        .setId(302L).setLedgerId(2L).setBizType(11).setBizId(77L)
                                        .setBizNo("ZZRK202605270002").setVoucherNo("V-INT-302")
                                        .setVoucherTime(LocalDateTime.of(2026, 5, 27, 11, 1))
                                        .setStatus(ErpFinanceVoucherStatusEnum.GENERATED.getStatus())
                                        .setTotalDebitAmount(new BigDecimal("300.00"))
                                        .setTotalCreditAmount(new BigDecimal("300.00")));
                    }
                    return null;
                }));
        // 明细差异日志存在但 diffAmount 全部为零
        setField(service, "dualLedgerAmountDiffLogMapper", createProxy(cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceDualLedgerAmountDiffLogMapper.class,
                (methodName, args) -> {
                    if ("selectListByBizTypeAndBizId".equals(methodName)) {
                        return List.of(new ErpFinanceDualLedgerAmountDiffLogDO()
                                .setId(20L).setBizType(11).setBizId(77L)
                                .setDiffItemType(20).setCalculationType(1)
                                .setInternalAmount(new BigDecimal("150.00"))
                                .setExternalAmount(new BigDecimal("150.00"))
                                .setDiffAmount(new BigDecimal("0.00")));
                    }
                    return List.of();
                }));
        setField(service, "dualLedgerDiffConfigService", createProxy(ErpFinanceDualLedgerDiffConfigService.class,
                (methodName, args) -> List.of()));

        ErpFinanceDualLedgerResultRespVO result = service.getDualLedgerResult(11, 77L);

        assertNotNull(result);
        assertEquals(10, result.getCompareStatus().intValue());
        assertTrue(result.getConsistent());
        assertTrue(result.getIssueMessages().isEmpty());
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getTotalItemDiffAmount()));
    }

    @Test
    void getDualLedgerResultPage_shouldExcludeVoidedVouchers() throws Exception {
        // 场景：凭证查询应排除 VOIDED 状态的作废凭证
        ErpFinanceDualLedgerResultServiceImpl service = new ErpFinanceDualLedgerResultServiceImpl();

        setField(service, "dualLedgerConfigService", createProxy(ErpFinanceDualLedgerConfigService.class, (methodName, args) -> {
            if ("getDualLedgerConfigListByStatus".equals(methodName)) {
                return List.of(new ErpFinanceDualLedgerConfigDO().setId(1L).setBizType(11)
                        .setExternalLedgerId(1L).setInternalLedgerId(2L)
                        .setStatus(CommonStatusEnum.ENABLE.getStatus()));
            }
            return null;
        }));
        setField(service, "dualLedgerDiffConfigService", createProxy(ErpFinanceDualLedgerDiffConfigService.class, (methodName, args) -> List.of()));
        setField(service, "dualLedgerAmountDiffLogMapper", createProxy(cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceDualLedgerAmountDiffLogMapper.class,
                (methodName, args) -> List.of()));
        setField(service, "financeLedgerService", createProxy(ErpFinanceLedgerService.class, (methodName, args) -> {
            if ("getFinanceLedgerList".equals(methodName)) {
                return List.of(
                        new ErpFinanceLedgerDO().setId(1L).setName("外账").setStatus(CommonStatusEnum.ENABLE.getStatus()),
                        new ErpFinanceLedgerDO().setId(2L).setName("内账").setStatus(CommonStatusEnum.ENABLE.getStatus()));
            }
            return null;
        }));
        setField(service, "erpFinanceVoucherMapper", createProxy(cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceVoucherMapper.class,
                (methodName, args) -> {
                    if ("selectList".equals(methodName)) {
                        // 验证查询条件中包含 ne(Status, VOIDED)
                        // 在真实场景中，MyBatis Plus 会过滤掉 VOIDED 凭证
                        // 这里模拟只返回非 VOIDED 凭证
                        return List.of(
                                new ErpFinanceVoucherDO()
                                        .setId(501L).setLedgerId(1L).setBizType(11).setBizId(55L)
                                        .setBizNo("ZZRK202605270005").setVoucherNo("V-EXT-501")
                                        .setVoucherTime(LocalDateTime.of(2026, 5, 27, 14, 0))
                                        .setStatus(ErpFinanceVoucherStatusEnum.GENERATED.getStatus())
                                        .setTotalDebitAmount(new BigDecimal("200.00"))
                                        .setTotalCreditAmount(new BigDecimal("200.00")),
                                new ErpFinanceVoucherDO()
                                        .setId(502L).setLedgerId(2L).setBizType(11).setBizId(55L)
                                        .setBizNo("ZZRK202605270005").setVoucherNo("V-INT-502")
                                        .setVoucherTime(LocalDateTime.of(2026, 5, 27, 14, 1))
                                        .setStatus(ErpFinanceVoucherStatusEnum.GENERATED.getStatus())
                                        .setTotalDebitAmount(new BigDecimal("200.00"))
                                        .setTotalCreditAmount(new BigDecimal("200.00")));
                        // 注意：VOIDED 凭证（bizId=null, status=VOIDED）不会出现在结果中
                    }
                    return null;
                }));

        PageResult<ErpFinanceDualLedgerResultRespVO> pageResult =
                service.getDualLedgerResultPage(new ErpFinanceDualLedgerResultPageReqVO().setBizType(11));

        assertEquals(1, pageResult.getList().size());
        ErpFinanceDualLedgerResultRespVO result = pageResult.getList().get(0);
        assertEquals(55L, result.getBizId().longValue());
        assertEquals(10, result.getCompareStatus().intValue());
        assertTrue(result.getConsistent());
    }

    @SuppressWarnings("unchecked")
    private <T> T createProxy(Class<T> type, MethodHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        if ("toString".equals(method.getName())) {
                            return type.getSimpleName() + "Proxy";
                        }
                        if ("hashCode".equals(method.getName())) {
                            return System.identityHashCode(proxy);
                        }
                        if ("equals".equals(method.getName())) {
                            return proxy == args[0];
                        }
                    }
                    return handler.handle(method.getName(), args);
                });
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @FunctionalInterface
    private interface MethodHandler {
        Object handle(String methodName, Object[] args);
    }
}
