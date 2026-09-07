package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.voucherfailure.ErpFinanceVoucherFailurePageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherFailureDO;

/**
 * ERP 凭证生成失败记录 Service 接口
 *
 * 失败记录的唯一写入路径：recordFailure（业务审核事务内落库）；
 * 终态（SUCCESS/CONFIRMED）唯一写入路径：retry / confirm（CAS）
 *
 * @author WeTai
 */
public interface ErpFinanceVoucherFailureService {

    /**
     * 记录一次凭证生成失败（幂等：同一业务单据至多一条待重试记录）
     *
     * 由 {@link ErpFinanceBizHookService} 在凭证生成失败的 catch 中调用，
     * 加入调用方（业务审核）事务：业务回滚则失败记录一并回滚
     *
     * @param bizType      业务类型
     * @param bizId        业务单据编号
     * @param errorMessage 失败原因
     * @param errorStack   异常堆栈（截断）
     */
    void recordFailure(Integer bizType, Long bizId, String errorMessage, String errorStack);

    /**
     * 重试生成凭证
     *
     * 仅待重试记录可重试；凭证生成在独立事务执行（幂等），
     * 成功则 CAS 置为 SUCCESS，失败则 retry_count+1 并保持 PENDING
     *
     * @param id 失败记录编号
     * @return 生成的凭证编号
     */
    Long retryVoucherFailure(Long id);

    /**
     * 人工确认关闭失败记录（如业务单据已反审核、无需补凭证）
     *
     * @param id     失败记录编号
     * @param reason 关闭原因（必填）
     */
    void confirmVoucherFailure(Long id, String reason);

    /**
     * 分页查询失败记录
     */
    PageResult<ErpFinanceVoucherFailureDO> getVoucherFailurePage(ErpFinanceVoucherFailurePageReqVO pageReqVO);

    /**
     * 获取失败记录
     */
    ErpFinanceVoucherFailureDO getVoucherFailure(Long id);

}
