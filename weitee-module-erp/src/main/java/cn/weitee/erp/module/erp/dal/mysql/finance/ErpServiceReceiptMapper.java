package cn.weitee.erp.module.erp.dal.mysql.finance;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.receipt.ErpServiceReceiptPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpServiceReceiptDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 服务接收单 Mapper
 *
 * @author ruoyi-vue-pro
 */
@Mapper
public interface ErpServiceReceiptMapper extends BaseMapperX<ErpServiceReceiptDO> {

    default PageResult<ErpServiceReceiptDO> selectPage(ErpServiceReceiptPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpServiceReceiptDO>()
                .likeIfPresent(ErpServiceReceiptDO::getNo, reqVO.getNo())
                .eqIfPresent(ErpServiceReceiptDO::getLeaseContractId, reqVO.getLeaseContractId())
                .eqIfPresent(ErpServiceReceiptDO::getSupplierId, reqVO.getSupplierId())
                .eqIfPresent(ErpServiceReceiptDO::getPeriod, reqVO.getPeriod())
                .eqIfPresent(ErpServiceReceiptDO::getStatus, reqVO.getStatus())
                .orderByDesc(ErpServiceReceiptDO::getId));
    }

}
