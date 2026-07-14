package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.asset.ErpFinanceAssetSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceAssetCandidateDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceAssetDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceAssetCandidateMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceAssetDepreciationMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceAssetMapper;
import cn.weitee.erp.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.weitee.erp.module.erp.enums.ErpFinanceAssetCandidateStatusEnum;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceAsset.ASSET_CANDIDATE_CONFIRM_FAIL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErpFinanceAssetServiceImplTest {

    @Mock
    private ErpFinanceAssetMapper financeAssetMapper;
    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private ErpFinanceAssetCandidateMapper financeAssetCandidateMapper;
    @Mock
    private ErpFinanceAssetDepreciationMapper financeAssetDepreciationMapper;
    @Mock
    private ErpNoRedisDAO noRedisDAO;
    @Mock
    private ErpFinanceExpenseService financeExpenseService;
    @InjectMocks
    private ErpFinanceAssetServiceImpl service;

    @Test
    void createFinanceAsset_shouldConfirmPendingCandidateBeforeInsert() {
        ErpFinanceAssetSaveReqVO reqVO = createReqVO(10L);
        when(financeAssetCandidateMapper.update(any(ErpFinanceAssetCandidateDO.class),
                any(Wrapper.class))).thenReturn(1);
        when(noRedisDAO.generate("GDZC")).thenReturn("GDZC20260714000001");
        when(financeAssetMapper.selectByNo("GDZC20260714000001")).thenReturn(null);
        doAnswer(invocation -> {
            invocation.<ErpFinanceAssetDO>getArgument(0).setId(100L);
            return 1;
        }).when(financeAssetMapper).insert(any(ErpFinanceAssetDO.class));

        Long assetId = service.createFinanceAsset(reqVO);

        assertThat(assetId).isEqualTo(100L);
        InOrder inOrder = inOrder(financeAssetCandidateMapper, financeAssetMapper);
        inOrder.verify(financeAssetCandidateMapper).update(
                any(ErpFinanceAssetCandidateDO.class), any(Wrapper.class));
        inOrder.verify(financeAssetMapper).insert(any(ErpFinanceAssetDO.class));
        verify(financeAssetCandidateMapper).update(
                org.mockito.ArgumentMatchers.argThat(candidate ->
                        ErpFinanceAssetCandidateStatusEnum.CONFIRMED.getStatus().equals(candidate.getStatus())
                                && "候选确认".equals(candidate.getRemark())),
                any(Wrapper.class));
    }

    @Test
    void createFinanceAsset_shouldFailWithoutInsert_whenCandidateIsNotPending() {
        ErpFinanceAssetSaveReqVO reqVO = createReqVO(10L);

        assertThatThrownBy(() -> service.createFinanceAsset(reqVO))
                .isInstanceOf(ServiceException.class)
                .hasMessage(ASSET_CANDIDATE_CONFIRM_FAIL.getMsg());
        verify(financeAssetMapper, never()).insert(any(ErpFinanceAssetDO.class));
    }

    @Test
    void createFinanceAsset_shouldNotTouchCandidate_whenCandidateIdIsNull() {
        ErpFinanceAssetSaveReqVO reqVO = createReqVO(null);
        when(noRedisDAO.generate("GDZC")).thenReturn("GDZC20260714000002");
        when(financeAssetMapper.selectByNo("GDZC20260714000002")).thenReturn(null);
        doAnswer(invocation -> {
            invocation.<ErpFinanceAssetDO>getArgument(0).setId(101L);
            return 1;
        }).when(financeAssetMapper).insert(any(ErpFinanceAssetDO.class));

        Long assetId = service.createFinanceAsset(reqVO);

        assertThat(assetId).isEqualTo(101L);
        verifyNoInteractions(financeAssetCandidateMapper);
    }

    private ErpFinanceAssetSaveReqVO createReqVO(Long candidateId) {
        return new ErpFinanceAssetSaveReqVO()
                .setCandidateId(candidateId)
                .setOriginalAmount(new BigDecimal("1000.00"))
                .setSalvageRate(new BigDecimal("0.05"))
                .setRemark("候选确认");
    }
}
