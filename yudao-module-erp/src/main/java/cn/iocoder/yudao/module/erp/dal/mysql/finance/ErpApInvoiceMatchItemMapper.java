package cn.iocoder.yudao.module.erp.dal.mysql.finance;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apinvoice.ErpApInvoicePendingItemPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apinvoice.ErpApInvoicePendingItemRespVO;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpApInvoiceMatchItemDO;
import cn.iocoder.yudao.module.erp.enums.ErpApInvoiceMatchItemStatusEnum;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface ErpApInvoiceMatchItemMapper extends BaseMapperX<ErpApInvoiceMatchItemDO> {

    default List<ErpApInvoiceMatchItemDO> selectListByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return selectBatchIds(ids);
    }

    default List<ErpApInvoiceMatchItemDO> selectActiveListByInvoiceId(Long invoiceId) {
        return selectList(new LambdaQueryWrapperX<ErpApInvoiceMatchItemDO>()
                .eq(ErpApInvoiceMatchItemDO::getInvoiceId, invoiceId)
                .eq(ErpApInvoiceMatchItemDO::getStatus, ErpApInvoiceMatchItemStatusEnum.ACTIVE.getStatus())
                .orderByAsc(ErpApInvoiceMatchItemDO::getId));
    }

    default List<ErpApInvoiceMatchItemDO> selectActiveListByInvoiceIds(Collection<Long> invoiceIds) {
        if (CollUtil.isEmpty(invoiceIds)) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<ErpApInvoiceMatchItemDO>()
                .in(ErpApInvoiceMatchItemDO::getInvoiceId, invoiceIds)
                .eq(ErpApInvoiceMatchItemDO::getStatus, ErpApInvoiceMatchItemStatusEnum.ACTIVE.getStatus())
                .orderByAsc(ErpApInvoiceMatchItemDO::getId));
    }

    default List<ErpApInvoiceMatchItemDO> selectActiveListByStatementIds(Collection<Long> statementIds) {
        if (CollUtil.isEmpty(statementIds)) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<ErpApInvoiceMatchItemDO>()
                .in(ErpApInvoiceMatchItemDO::getApStatementId, statementIds)
                .eq(ErpApInvoiceMatchItemDO::getStatus, ErpApInvoiceMatchItemStatusEnum.ACTIVE.getStatus())
                .orderByAsc(ErpApInvoiceMatchItemDO::getId));
    }

    default List<ErpApInvoiceMatchItemDO> selectActiveListByPurchaseInItemIds(Collection<Long> purchaseInItemIds) {
        if (CollUtil.isEmpty(purchaseInItemIds)) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<ErpApInvoiceMatchItemDO>()
                .in(ErpApInvoiceMatchItemDO::getSourcePurchaseInItemId, purchaseInItemIds)
                .eq(ErpApInvoiceMatchItemDO::getStatus, ErpApInvoiceMatchItemStatusEnum.ACTIVE.getStatus())
                .orderByAsc(ErpApInvoiceMatchItemDO::getId));
    }

    Page<ErpApInvoicePendingItemRespVO> selectPendingItemPage(Page<ErpApInvoicePendingItemRespVO> page,
                                                              @Param("reqVO") ErpApInvoicePendingItemPageReqVO reqVO,
                                                              @Param("supplierId") Long supplierId,
                                                              @Param("activeStatus") Integer activeStatus,
                                                              @Param("purchaseInBizType") Integer purchaseInBizType,
                                                              @Param("closedStatus") Integer closedStatus);

}
