package cn.weitee.erp.module.erp.service.stock;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.out.ErpStockOutPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.out.ErpStockOutSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockOutDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockOutItemDO;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * ERP 其它出库单 Service 接口
 *
 * @author WeTai
 */
public interface ErpStockOutService {

    /**
     * 创建其它出库单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createStockOut(@Valid ErpStockOutSaveReqVO createReqVO);

    /**
     * 更新其它出库单
     *
     * @param updateReqVO 更新信息
     */
    void updateStockOut(@Valid ErpStockOutSaveReqVO updateReqVO);

    /**
     * 更新其它出库单的状态
     *
     * @param id 编号
     * @param status 状态
     */
    void updateStockOutStatus(Long id, Integer status);

    /**
     * 旧手工状态接口专用入口：供应链 BPM 接入后禁止再手工变更状态
     *
     * @param id 编号
     * @param status 目标状态
     */
    void updateStockOutStatusManually(Long id, Integer status);

    /**
     * 更新其它出库单的状态（BPM 审批回调）
     *
     * @param id 编号
     * @param processInstanceId 流程实例 ID
     * @param status 状态
     * @param reason 原因
     */
    void updateStockOutStatusByBpm(Long id, String processInstanceId, Integer status, String reason);

    /**
     * BPM 驳回 / 撤回后回退到草稿态
     *
     * @param id 编号
     * @param processInstanceId 流程实例 ID
     * @param reason 原因
     */
    void rollbackStockOutStatusToDraftByBpm(Long id, String processInstanceId, String reason);

    /**
     * 删除其它出库单
     *
     * @param ids 编号数组
     */
    void deleteStockOut(List<Long> ids);

    /**
     * 获得其它出库单
     *
     * @param id 编号
     * @return 其它出库单
     */
    ErpStockOutDO getStockOut(Long id);

    /**
     * 获得其它出库单分页
     *
     * @param pageReqVO 分页查询
     * @return 其它出库单分页
     */
    PageResult<ErpStockOutDO> getStockOutPage(ErpStockOutPageReqVO pageReqVO);

    // ==================== 出库项 ====================

    /**
     * 获得其它出库单项列表
     *
     * @param outId 出库编号
     * @return 其它出库单项列表
     */
    List<ErpStockOutItemDO> getStockOutItemListByOutId(Long outId);

    /**
     * 获得其它出库单项 List
     *
     * @param outIds 出库编号数组
     * @return 其它出库单项 List
     */
    List<ErpStockOutItemDO> getStockOutItemListByOutIds(Collection<Long> outIds);

}
