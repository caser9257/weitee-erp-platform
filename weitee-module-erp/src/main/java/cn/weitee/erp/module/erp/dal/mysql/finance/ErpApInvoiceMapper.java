package cn.weitee.erp.module.erp.dal.mysql.finance;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.apinvoice.ErpApInvoicePageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpApInvoiceDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ErpApInvoiceMapper extends BaseMapperX<ErpApInvoiceDO> {

    default PageResult<ErpApInvoiceDO> selectPage(ErpApInvoicePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpApInvoiceDO>()
                .likeIfPresent(ErpApInvoiceDO::getInvoiceNo, reqVO.getInvoiceNo())
                .eqIfPresent(ErpApInvoiceDO::getSupplierId, reqVO.getSupplierId())
                .eqIfPresent(ErpApInvoiceDO::getInvoiceType, reqVO.getInvoiceType())
                .eqIfPresent(ErpApInvoiceDO::getMatchStatus, reqVO.getMatchStatus())
                .betweenIfPresent(ErpApInvoiceDO::getInvoiceDate, reqVO.getInvoiceDate())
                .likeIfPresent(ErpApInvoiceDO::getRemark, reqVO.getRemark())
                .orderByDesc(ErpApInvoiceDO::getId));
    }

    default ErpApInvoiceDO selectBySupplierIdAndInvoiceNo(Long supplierId, String invoiceNo) {
        return selectOne(new LambdaQueryWrapperX<ErpApInvoiceDO>()
                .eq(ErpApInvoiceDO::getSupplierId, supplierId)
                .eq(ErpApInvoiceDO::getInvoiceNo, invoiceNo)
                .last("LIMIT 1"));
    }

    default ErpApInvoiceDO selectByInvoiceNo(String invoiceNo) {
        return selectOne(new LambdaQueryWrapperX<ErpApInvoiceDO>()
                .eq(ErpApInvoiceDO::getInvoiceNo, invoiceNo)
                .last("LIMIT 1"));
    }

}
