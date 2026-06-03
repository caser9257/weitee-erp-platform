package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.dualwrite.ErpFinanceDualWriteLogPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceDualWriteLogDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceVoucherDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceVoucherEntryDO;

import java.util.List;

/**
 * ERP 双写服务接口
 */
public interface ErpFinanceDualWriteService {

    /**
     * 双写凭证
     * 当对外账簿生成凭证时，自动在映射的内部账簿中生成对应凭证
     *
     * @param sourceVoucher 源凭证
     * @param entries       源凭证分录
     * @param sourceLedgerId 源账簿ID
     */
    void dualWriteVoucher(ErpFinanceVoucherDO sourceVoucher, List<ErpFinanceVoucherEntryDO> entries, Long sourceLedgerId);

    /**
     * 按源凭证同步执行一次双写
     * 用于业务单据自动生成凭证后，立即把目标账簿金额差异重算落库
     *
     * @param sourceVoucherId 源凭证 ID
     */
    void syncDualWriteBySourceVoucherId(Long sourceVoucherId);

    /**
     * 重试失败的双写任务
     *
     * @param logId 双写日志ID
     */
    void retryDualWrite(Long logId);

    /**
     * 获取双写日志分页
     *
     * @param pageReqVO 分页查询参数
     * @return 双写日志分页结果
     */
    PageResult<ErpFinanceDualWriteLogDO> getDualWriteLogPage(ErpFinanceDualWriteLogPageReqVO pageReqVO);

    /**
     * 获取双写日志详情
     *
     * @param id 日志ID
     * @return 双写日志详情
     */
    ErpFinanceDualWriteLogDO getDualWriteLog(Long id);

    /**
     * 按业务类型批量重算双账套凭证金额差异
     *
     * @param bizType 业务类型
     * @return 重算的凭证数量
     */
    int recomputeDualLedgerVouchers(Integer bizType);

    /**
     * 按业务单据重算双账套凭证金额差异
     *
     * @param bizType 业务类型
     * @param bizId   业务单据ID
     * @return 重算是否成功
     */
    boolean recomputeByBizId(Integer bizType, Long bizId);

    /**
     * 按业务单据重算双账套凭证金额差异（strict 模式，失败直接抛异常）
     * 用于编排型入口，确保调用方能感知失败
     *
     * @param bizType 业务类型
     * @param bizId   业务单据ID
     */
    void recomputeByBizIdStrict(Integer bizType, Long bizId);
}
