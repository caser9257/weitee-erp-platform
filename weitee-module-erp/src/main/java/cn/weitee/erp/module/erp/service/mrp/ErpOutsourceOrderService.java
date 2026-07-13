package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.outsource.*;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.*;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;

public interface ErpOutsourceOrderService {

    Long createOutsourceOrder(@Valid ErpOutsourceOrderSaveReqVO createReqVO);

    void updateOutsourceOrder(@Valid ErpOutsourceOrderSaveReqVO updateReqVO);

    void closeOutsourceOrder(@Valid ErpOutsourceOrderCloseReqVO reqVO);

    ErpOutsourceOrderDO getOutsourceOrder(Long id);

    PageResult<ErpOutsourceOrderDO> getOutsourceOrderPage(ErpOutsourceOrderPageReqVO pageReqVO);

    Long createOutsourceIssue(@Valid ErpOutsourceIssueCreateReqVO reqVO);

    ErpOutsourceIssueDO getOutsourceIssue(Long id);

    PageResult<ErpOutsourceIssueDO> getOutsourceIssuePage(ErpOutsourceIssuePageReqVO pageReqVO);

    List<ErpOutsourceIssueItemDO> getOutsourceIssueItemListByIssueId(Long issueId);

    List<ErpOutsourceIssueItemDO> getOutsourceIssueItemListByIssueIds(Collection<Long> issueIds);

    List<ErpOutsourceIssueBatchDO> getOutsourceIssueBatchListByIssueItemIds(Collection<Long> issueItemIds);

    Long createOutsourceReturn(@Valid ErpOutsourceReturnCreateReqVO reqVO);

    ErpOutsourceReturnDO getOutsourceReturn(Long id);

    PageResult<ErpOutsourceReturnDO> getOutsourceReturnPage(ErpOutsourceReturnPageReqVO pageReqVO);

    List<ErpOutsourceReturnItemDO> getOutsourceReturnItemListByReturnId(Long returnId);

    List<ErpOutsourceReturnItemDO> getOutsourceReturnItemListByReturnIds(Collection<Long> returnIds);

    List<ErpOutsourceReturnBatchDO> getOutsourceReturnBatchListByReturnItemIds(Collection<Long> returnItemIds);

    Long createOutsourceInbound(@Valid ErpOutsourceInboundCreateReqVO reqVO);

    ErpOutsourceInboundDO getOutsourceInbound(Long id);

    PageResult<ErpOutsourceInboundDO> getOutsourceInboundPage(ErpOutsourceInboundPageReqVO pageReqVO);

    Long createOutsourceFee(@Valid ErpOutsourceFeeSaveReqVO reqVO);

    /**
     * 作废委外加工费
     *
     * @param id     加工费ID
     * @param reason 作废原因
     */
    void voidOutsourceFee(Long id, String reason);

    ErpOutsourceFeeDO getOutsourceFee(Long id);

    PageResult<ErpOutsourceFeeDO> getOutsourceFeePage(ErpOutsourceFeePageReqVO pageReqVO);

    void createOutsourceLossEntry(@Valid ErpOutsourceLossEntryCreateReqVO reqVO);

    ErpOutsourceCostDetailRespVO getOutsourceCostDetail(Long orderId);

    ErpOutsourceReconciliationRespVO getOutsourceReconciliationDetail(Long orderId);

    ErpOutsourceLossDetailRespVO getOutsourceLossDetail(Long orderId);

}
