package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.lease.ErpLeaseContractPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.lease.ErpLeaseContractSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpLeaseContractDO;

import java.util.List;

/**
 * 租赁合同 Service 接口
 *
 * @author ruoyi-vue-pro
 */
public interface ErpLeaseContractService {

    Long createLeaseContract(ErpLeaseContractSaveReqVO reqVO);

    void updateLeaseContract(ErpLeaseContractSaveReqVO reqVO);

    void deleteLeaseContract(Long id);

    ErpLeaseContractDO getLeaseContract(Long id);

    PageResult<ErpLeaseContractDO> getLeaseContractPage(ErpLeaseContractPageReqVO reqVO);

    List<ErpLeaseContractDO> getLeaseContractList();

}
