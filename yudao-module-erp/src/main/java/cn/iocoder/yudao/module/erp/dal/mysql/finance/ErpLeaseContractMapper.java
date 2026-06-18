package cn.iocoder.yudao.module.erp.dal.mysql.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.lease.ErpLeaseContractPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpLeaseContractDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 租赁合同 Mapper
 *
 * @author ruoyi-vue-pro
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
