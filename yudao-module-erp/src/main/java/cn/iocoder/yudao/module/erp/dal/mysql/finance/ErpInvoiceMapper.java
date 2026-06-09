package cn.iocoder.yudao.module.erp.dal.mysql.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.invoice.ErpInvoicePageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpInvoiceDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * ERP 销项发票 Mapper
 *
 * @author system
 */
@Mapper
public interface ErpInvoiceMapper extends BaseMapperX<ErpInvoiceDO> {

    default PageResult<ErpInvoiceDO> selectPage(ErpInvoicePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpInvoiceDO>()
                .likeIfPresent(ErpInvoiceDO::getNo, reqVO.getNo())
                .eqIfPresent(ErpInvoiceDO::getCustomerId, reqVO.getCustomerId())
                .eqIfPresent(ErpInvoiceDO::getOrderId, reqVO.getOrderId())
                .eqIfPresent(ErpInvoiceDO::getStatus, reqVO.getStatus())
                .orderByDesc(ErpInvoiceDO::getId));
    }

}
