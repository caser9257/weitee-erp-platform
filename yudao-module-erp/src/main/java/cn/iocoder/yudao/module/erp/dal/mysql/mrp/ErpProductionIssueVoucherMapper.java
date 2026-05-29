package cn.iocoder.yudao.module.erp.dal.mysql.mrp;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.issuevoucher.ErpProductionIssueVoucherPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionIssueVoucherDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ErpProductionIssueVoucherMapper extends BaseMapperX<ErpProductionIssueVoucherDO> {

    default PageResult<ErpProductionIssueVoucherDO> selectPage(ErpProductionIssueVoucherPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpProductionIssueVoucherDO>()
                .likeIfPresent(ErpProductionIssueVoucherDO::getVoucherNo, reqVO.getVoucherNo())
                .likeIfPresent(ErpProductionIssueVoucherDO::getIssueNo, reqVO.getIssueNo())
                .eqIfPresent(ErpProductionIssueVoucherDO::getProductionOrderId, reqVO.getProductionOrderId())
                .eqIfPresent(ErpProductionIssueVoucherDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ErpProductionIssueVoucherDO::getVoucherTime, reqVO.getVoucherTime())
                .orderByDesc(ErpProductionIssueVoucherDO::getId));
    }

    default ErpProductionIssueVoucherDO selectByIssueId(Long issueId) {
        return selectOne(ErpProductionIssueVoucherDO::getIssueId, issueId);
    }

}
