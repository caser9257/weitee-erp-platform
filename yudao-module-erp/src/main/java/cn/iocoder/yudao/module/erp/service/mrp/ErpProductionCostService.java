package cn.iocoder.yudao.module.erp.service.mrp;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostDetailRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostEntryPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostSummaryRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostProjectSummaryRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostProductSummaryRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostEntrySaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostProductSummaryReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostTrendReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostTrendRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionCostEntryDO;

import jakarta.validation.Valid;
import java.util.List;

public interface ErpProductionCostService {

    Long createProductionCostEntry(@Valid ErpProductionCostEntrySaveReqVO createReqVO);

    void updateProductionCostEntry(@Valid ErpProductionCostEntrySaveReqVO updateReqVO);

    void deleteProductionCostEntry(Long id);

    ErpProductionCostEntryDO getProductionCostEntry(Long id);

    PageResult<ErpProductionCostEntryDO> getProductionCostEntryPage(ErpProductionCostEntryPageReqVO pageReqVO);

    PageResult<ErpProductionCostSummaryRespVO> getProductionCostSummaryPage(ErpProductionCostEntryPageReqVO pageReqVO);

    List<ErpProductionCostEntryDO> getProductionCostEntryListByProductionOrderId(Long productionOrderId);

    ErpProductionCostDetailRespVO getCostDetail(Long productionOrderId);

    List<ErpProductionCostProjectSummaryRespVO> getProjectSummary(String accountingMonth);

    /**
     * 获取产品维度成本汇总
     *
     * @param accountingMonth 归集月份（可选，为空则汇总所有月份）
     * @return 产品成本汇总列表
     */
    List<ErpProductionCostProductSummaryRespVO> getProductSummary(String accountingMonth);

    /**
     * 获取产品维度成本汇总（支持多条件筛选）
     *
     * @param reqVO 查询条件
     * @return 产品成本汇总列表
     */
    List<ErpProductionCostProductSummaryRespVO> getProductSummary(ErpProductionCostProductSummaryReqVO reqVO);

    /**
     * 从盘点盘亏创建生产成本条目
     *
     * @param costEntry 成本条目
     * @return 条目编号
     */
    Long createProductionCostEntryFromCheck(ErpProductionCostEntryDO costEntry);

    /**
     * 获取产品成本趋势分析数据
     *
     * @param reqVO 查询条件
     * @return 趋势分析数据
     */
    ErpProductionCostTrendRespVO getCostTrend(ErpProductionCostTrendReqVO reqVO);

}
