package cn.weitee.erp.module.erp.dal.mysql.finance;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.lease.ErpLeaseContractPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpLeaseContractDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 租赁合同 Mapper
 *
 * @author weitee
 */
@Mapper
public interface ErpLeaseContractMapper extends BaseMapperX<ErpLeaseContractDO> {

    default PageResult<ErpLeaseContractDO> selectPage(ErpLeaseContractPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpLeaseContractDO>()
                .likeIfPresent(ErpLeaseContractDO::getNo, reqVO.getNo())
                .likeIfPresent(ErpLeaseContractDO::getName, reqVO.getName())
                .eqIfPresent(ErpLeaseContractDO::getSupplierId, reqVO.getSupplierId())
                .eqIfPresent(ErpLeaseContractDO::getStatus, reqVO.getStatus())
                .orderByDesc(ErpLeaseContractDO::getId));
    }

}
