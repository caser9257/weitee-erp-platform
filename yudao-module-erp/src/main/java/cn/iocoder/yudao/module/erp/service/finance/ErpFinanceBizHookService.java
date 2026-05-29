package cn.iocoder.yudao.module.erp.service.finance;

import java.time.LocalDate;

public interface ErpFinanceBizHookService {

    Long handleApprovedBiz(Integer bizType, Long bizId, LocalDate bizDate);

    void handleRollbackBiz(Integer bizType, Long bizId, Long userId, String remark);

}
