package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.projectdualcost.ErpFinanceDualProjectCostPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.projectdualcost.ErpFinanceDualProjectCostRespVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.projectdualcost.ErpFinanceDualProjectCostRebuildReqVO;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 项目双账成本 Service 接口
 */
public interface ErpFinanceDualProjectCostService {

    /**
     * 分页查询项目双账成本结果
     */
    PageResult<ErpFinanceDualProjectCostRespVO> getProjectDualCostPage(ErpFinanceDualProjectCostPageReqVO pageReqVO);

    /**
     * 查询单个项目双账成本结果
     */
    ErpFinanceDualProjectCostRespVO getProjectDualCost(Long id);

    /**
     * 查询项目双账成本明细
     */
    List<ErpFinanceDualProjectCostRespVO> getProjectDualCostItems(Long resultId);

    /**
     * 项目级重跑
     */
    void rebuildProjectDualCost(Long userId, ErpFinanceDualProjectCostRebuildReqVO reqVO);

    /**
     * 按期间批量重跑所有项目的双账成本
     */
    int rebuildBatchByPeriod(Long userId, String period, String remark);

    void exportExternalProjectCost(ErpFinanceDualProjectCostPageReqVO pageReqVO, HttpServletResponse response) throws IOException;

    /**
     * 导出内部账项目成本
     */
    void exportInternalProjectCost(ErpFinanceDualProjectCostPageReqVO pageReqVO, HttpServletResponse response) throws IOException;
}
