package cn.weitee.erp.module.erp.service.stock;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.in.ErpStockInPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.in.ErpStockInSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockInDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockInItemDO;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * ERP 其它入库单 Service 接口
 *
 * @author WeTai
 */
public interface ErpStockInService {

    /**
     * 创建其它入库单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createStockIn(@Valid ErpStockInSaveReqVO createReqVO);

    /**
     * 更新其它入库单
     *
     * @param updateReqVO 更新信息
     */
    void updateStockIn(@Valid ErpStockInSaveReqVO updateReqVO);

    /**
     * 更新其它入库单的状态
     *
     * @param id 编号
     * @param status 状态
     */
    void updateStockInStatus(Long id, Integer status);

    /**
     * 旧手工状态接口专用入口：供应链 BPM 接入后禁止再手工变更状态
     *
     * @param id 编号
     * @param status 目标状态
     */
    void updateStockInStatusManually(Long id, Integer status);

    /**
     * BPM 审批通过/驳回后更新其它入库单状态
     *
     * @param id 编号
     * @param processInstanceId 流程实例 ID
     * @param status 状态
     * @param reason 原因
     */
    void updateStockInStatusByBpm(Long id, String processInstanceId, Integer status, String reason);

    /**
     * BPM 驳回 / 撤回后回退到草稿态
     *
     * @param id 编号
     * @param processInstanceId 流程实例 ID
     * @param reason 原因
     */
    void rollbackStockInStatusToDraftByBpm(Long id, String processInstanceId, String reason);

    /**
     * 删除其它入库单
     *
     * @param ids 编号数组
     */
    void deleteStockIn(List<Long> ids);

    /**
     * 获得其它入库单
     *
     * @param id 编号
     * @return 其它入库单
     */
    ErpStockInDO getStockIn(Long id);

    /**
     * 获得其它入库单分页
     *
     * @param pageReqVO 分页查询
     * @return 其它入库单分页
     */
    PageResult<ErpStockInDO> getStockInPage(ErpStockInPageReqVO pageReqVO);

    // ==================== 入库项 ====================

    /**
     * 获得其它入库单项列表
     *
     * @param inId 入库编号
     * @return 其它入库单项列表
     */
    List<ErpStockInItemDO> getStockInItemListByInId(Long inId);

    /**
     * 获得其它入库单项 List
     *
     * @param inIds 入库编号数组
     * @return 其它入库单项 List
     */
    List<ErpStockInItemDO> getStockInItemListByInIds(Collection<Long> inIds);

}
