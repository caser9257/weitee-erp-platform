package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.productdualcost.ErpFinanceDualProductCostPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.productdualcost.ErpFinanceDualProductCostRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.productdualcost.ErpFinanceDualProductCostRebuildReqVO;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 产品双账成本 Service 接口
 */
public interface ErpFinanceDualProductCostService {

    PageResult<ErpFinanceDualProductCostRespVO> getProductDualCostPage(ErpFinanceDualProductCostPageReqVO pageReqVO);

    ErpFinanceDualProductCostRespVO getProductDualCost(Long id);

    List<ErpFinanceDualProductCostRespVO> getProductDualCostItems(Long resultId);

    void rebuildProductDualCost(Long userId, ErpFinanceDualProductCostRebuildReqVO reqVO);

    /**
     * 按期间批量重跑所有产品的双账成本
     */
    int rebuildBatchByPeriod(Long userId, String period, String remark);

    void exportExternalProductCost(ErpFinanceDualProductCostPageReqVO pageReqVO, HttpServletResponse response) throws IOException;

    void exportInternalProductCost(ErpFinanceDualProductCostPageReqVO pageReqVO, HttpServletResponse response) throws IOException;
}
