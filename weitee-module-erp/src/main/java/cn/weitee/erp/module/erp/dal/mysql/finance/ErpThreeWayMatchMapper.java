package cn.weitee.erp.module.erp.dal.mysql.finance;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.match.ErpThreeWayMatchPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpThreeWayMatchDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 三单匹配 Mapper
 *
 * @author ruoyi-vue-pro
 */
@Mapper
public interface ErpThreeWayMatchMapper extends BaseMapperX<ErpThreeWayMatchDO> {

    default PageResult<ErpThreeWayMatchDO> selectPage(ErpThreeWayMatchPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpThreeWayMatchDO>()
                .likeIfPresent(ErpThreeWayMatchDO::getLeaseContractNo, reqVO.getLeaseContractNo())
                .likeIfPresent(ErpThreeWayMatchDO::getServiceReceiptNo, reqVO.getServiceReceiptNo())
                .likeIfPresent(ErpThreeWayMatchDO::getInvoiceNo, reqVO.getInvoiceNo())
                .eqIfPresent(ErpThreeWayMatchDO::getMatchResult, reqVO.getMatchResult())
                .eqIfPresent(ErpThreeWayMatchDO::getStatus, reqVO.getStatus())
                .orderByDesc(ErpThreeWayMatchDO::getId));
    }

}
