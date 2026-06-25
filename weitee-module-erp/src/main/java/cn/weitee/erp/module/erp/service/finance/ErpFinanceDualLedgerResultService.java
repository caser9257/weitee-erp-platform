package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.dualledger.ErpFinanceDualLedgerRecomputeReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.dualledger.ErpFinanceDualLedgerResultPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.dualledger.ErpFinanceDualLedgerResultRespVO;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface ErpFinanceDualLedgerResultService {

    PageResult<ErpFinanceDualLedgerResultRespVO> getDualLedgerResultPage(ErpFinanceDualLedgerResultPageReqVO pageReqVO);

    ErpFinanceDualLedgerResultRespVO getDualLedgerResult(Integer bizType, Long bizId);

    ErpFinanceDualLedgerResultRespVO recomputeDualLedgerResult(Long userId, ErpFinanceDualLedgerRecomputeReqVO reqVO);

    /**
     * 单条记录导出套账凭证
     *
     * @param bizType   业务类型
     * @param bizId     业务主键
     * @param ledgerSide 导出账套侧：external 或 internal
     * @param response  HTTP 响应
     */
    void exportSingleLedger(Integer bizType, Long bizId, String ledgerSide, HttpServletResponse response) throws IOException;
}
