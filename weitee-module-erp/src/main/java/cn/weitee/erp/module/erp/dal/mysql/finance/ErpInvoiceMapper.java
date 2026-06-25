package cn.weitee.erp.module.erp.dal.mysql.finance;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.invoice.ErpInvoicePageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpInvoiceDO;
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
