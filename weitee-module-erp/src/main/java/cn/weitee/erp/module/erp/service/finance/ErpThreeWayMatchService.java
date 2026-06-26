package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.match.ErpThreeWayMatchPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpThreeWayMatchDO;

import java.util.List;

/**
 * 三单匹配 Service 接口
 *
 * @author weitee
 */
public interface ErpThreeWayMatchService {

    /**
     * 执行三单匹配
     *
     * @param leaseContractId 租赁合同ID
     * @param serviceReceiptId 服务接收单ID
     * @param invoiceNo 发票�?
     * @return 匹配结果ID
     */
    Long match(Long leaseContractId, Long serviceReceiptId, String invoiceNo);

    /**
     * 获取匹配记录分页
     */
    PageResult<ErpThreeWayMatchDO> getMatchPage(ErpThreeWayMatchPageReqVO reqVO);

    /**
     * 获取匹配记录列表
     */
    List<ErpThreeWayMatchDO> getMatchList();

    /**
     * 获取匹配记录
     */
    ErpThreeWayMatchDO getMatch(Long id);

    /**
     * 确认匹配并生成应付台�?
     */
    void confirmMatch(Long id);

}
