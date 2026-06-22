package cn.iocoder.yudao.module.erp.dal.mysql.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apinvoice.ErpApInvoicePageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpApInvoiceDO;
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
