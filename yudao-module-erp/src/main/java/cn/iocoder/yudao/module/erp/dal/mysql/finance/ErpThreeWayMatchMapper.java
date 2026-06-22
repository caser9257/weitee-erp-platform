package cn.iocoder.yudao.module.erp.dal.mysql.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.match.ErpThreeWayMatchPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpThreeWayMatchDO;
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
