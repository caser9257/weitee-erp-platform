package cn.iocoder.yudao.module.erp.service.stock;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.check.ErpStockCheckPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.check.ErpStockCheckSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockCheckDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockCheckItemDO;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * ERP 库存盘点单 Service 接口
 *
 * @author WeTai
 */
public interface ErpStockCheckService {

    /**
     * 创建库存盘点单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createStockCheck(@Valid ErpStockCheckSaveReqVO createReqVO);

    /**
     * 更新库存盘点单
     *
     * @param updateReqVO 更新信息
     */
    void updateStockCheck(@Valid ErpStockCheckSaveReqVO updateReqVO);

    /**
     * 更新库存盘点单的状态
     *
     * @param id 编号
     * @param status 状态
     */
    void updateStockCheckStatus(Long id, Integer status);

    /**
     * 启动盘点（DRAFT → COUNTING）
     *
     * 生成快照 + 冻结仓库
     *
     * @param id 盘点单ID
     */
    void startCounting(Long id);

    /**
     * 提交审核（COUNTING → REVIEWING）
     *
     * 计算差异金额
     *
     * @param id 盘点单ID
     */
    void submitForReview(Long id);

    /**
     * 审核通过（REVIEWING → APPROVED → CLOSED）
     *
     * 更新库存 + 生成凭证 + 解冻仓库
     *
     * @param id 盘点单ID
     */
    void approveAndClose(Long id);

    /**
     * 驳回到上一状态
     *
     * @param id 盘点单ID
     */
    void reject(Long id);

    /**
     * 获取盘点差异报告
     *
     * @param checkId 盘点单ID
     * @return 差异报告（盘盈总额、盘亏总额）
     */
    java.math.BigDecimal[] getDiffReport(Long checkId);

    /**
     * 删除库存盘点单
     *
     * @param ids 编号数组
     */
    void deleteStockCheck(List<Long> ids);

    /**
     * 获得库存盘点单
     *
     * @param id 编号
     * @return 库存盘点单
     */
    ErpStockCheckDO getStockCheck(Long id);

    /**
     * 获得库存盘点单分页
     *
     * @param pageReqVO 分页查询
     * @return 库存盘点单分页
     */
    PageResult<ErpStockCheckDO> getStockCheckPage(ErpStockCheckPageReqVO pageReqVO);

    // ==================== 盘点项 ====================

    /**
     * 获得库存盘点单项列表
     *
     * @param checkId 盘点编号
     * @return 库存盘点单项列表
     */
    List<ErpStockCheckItemDO> getStockCheckItemListByCheckId(Long checkId);

    /**
     * 获得库存盘点单项 List
     *
     * @param checkIds 盘点编号数组
     * @return 库存盘点单项 List
     */
    List<ErpStockCheckItemDO> getStockCheckItemListByCheckIds(Collection<Long> checkIds);

}