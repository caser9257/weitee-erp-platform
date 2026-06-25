package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.module.erp.controller.admin.finance.vo.voucher.ErpFinanceVoucherIntegrityCheckRespVO;

import java.time.LocalDate;

/**
 * 凭证完整性检查 Service 接口
 *
 * 用于检查业务单据与财务凭证之间的完整性，发现缺失或错误的凭证
 */
public interface ErpFinanceVoucherIntegrityService {

    /**
     * 检查凭证完整性
     *
     * @param bizType   业务类型
     * @param ledgerId  账簿编号（可选）
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 检查结果
     */
    ErpFinanceVoucherIntegrityCheckRespVO checkIntegrity(Integer bizType, Long ledgerId,
                                                          LocalDate startDate, LocalDate endDate);

    /**
     * 批量重算指定业务类型的凭证
     *
     * @param bizType   业务类型
     * @param ledgerId  账簿编号（可选）
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param userId    操作人
     * @param remark    重算说明
     * @return 成功重算的凭证数
     */
    int batchRecomputeVouchers(Integer bizType, Long ledgerId,
                               LocalDate startDate, LocalDate endDate,
                               Long userId, String remark);

}
