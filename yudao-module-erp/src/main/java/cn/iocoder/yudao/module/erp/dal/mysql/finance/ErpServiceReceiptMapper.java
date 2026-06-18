package cn.iocoder.yudao.module.erp.dal.mysql.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.receipt.ErpServiceReceiptPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpServiceReceiptDO;
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
