package cn.iocoder.yudao.module.erp.service.sale;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.MarketLedgerPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.MarketLedgerStatsVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.MarketLedgerVO;

/**
 * 市场执行台账服务接口
 *
 * @author system
 */
public interface ErpMarketExecutionLedgerService {

    /**
     * 分页查询市场执行台账
     *
     * @param reqVO 查询参数
     * @return 台账列表
     */
    PageResult<MarketLedgerVO> getLedgerPage(MarketLedgerPageReqVO reqVO);

    /**
     * 获取市场执行台账统计
     *
     * @return 统计数据
     */
    MarketLedgerStatsVO getLedgerStats();

    /**
     * 获取项目级聚合视图
     *
     * @param projectId 项目编号
     * @return 聚合视图
     */
    MarketLedgerVO getProjectSummary(Long projectId);

}
