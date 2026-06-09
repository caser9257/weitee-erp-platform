package cn.iocoder.yudao.module.erp.dal.mysql.finance;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpInvoiceItemDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * ERP 销项发票明细 Mapper
 *
 * @author system
 */
@Mapper
public interface ErpInvoiceItemMapper extends BaseMapperX<ErpInvoiceItemDO> {

    default List<ErpInvoiceItemDO> selectListByInvoiceId(Long invoiceId) {
        return selectList(ErpInvoiceItemDO::getInvoiceId, invoiceId);
    }

}
