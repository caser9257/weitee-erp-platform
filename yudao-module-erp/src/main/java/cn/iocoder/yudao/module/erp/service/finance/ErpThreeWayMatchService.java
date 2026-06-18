package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpThreeWayMatchDO;

import java.util.List;

/**
 * 三单匹配 Service 接口
 *
 * @author ruoyi-vue-pro
 */
public interface ErpThreeWayMatchService {

    /**
     * 执行三单匹配
     *
     * @param leaseContractId 租赁合同ID
     * @param serviceReceiptId 服务接收单ID
     * @param invoiceNo 发票号
     * @param invoiceAmount 发票金额
     * @return 匹配结果ID
     */
    Long match(Long leaseContractId, Long serviceReceiptId, String invoiceNo, java.math.BigDecimal invoiceAmount);

    /**
     * 获取匹配记录列表
     */
    List<ErpThreeWayMatchDO> getMatchList();

    /**
     * 获取匹配记录
     */
    ErpThreeWayMatchDO getMatch(Long id);

    /**
     * 确认匹配并生成应付台账
     */
    void confirmMatch(Long id);

}
