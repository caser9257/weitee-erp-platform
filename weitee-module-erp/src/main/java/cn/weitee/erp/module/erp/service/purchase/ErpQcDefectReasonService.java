package cn.weitee.erp.module.erp.service.purchase;

import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpQcDefectReasonDO;

import java.util.List;

/**
 * IQC 不良原因主数据 Service 接口
 */
public interface ErpQcDefectReasonService {

    /**
     * 按状态查询不良原因列表
     *
     * @param status 状态
     * @return 不良原因列表
     */
    List<ErpQcDefectReasonDO> getDefectReasonListByStatus(Integer status);

}
