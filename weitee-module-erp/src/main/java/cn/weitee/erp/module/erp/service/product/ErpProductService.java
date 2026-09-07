package cn.weitee.erp.module.erp.service.product;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductSimpleRespVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ProductSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * ERP 产品 Service 接口
 *
 * @author WeTai
 */
public interface ErpProductService {

    /**
     * 创建产品
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProduct(@Valid ProductSaveReqVO createReqVO);

    /**
     * 更新产品
     *
     * @param updateReqVO 更新信息
     */
    void updateProduct(@Valid ProductSaveReqVO updateReqVO);

    /**
     * 删除产品
     *
     * @param id 编号
     */
    void deleteProduct(Long id);

    /**
     * 校验产品们的有效性
     *
     * @param ids 编号数组
     * @return 产品列表
     */
    List<ErpProductDO> validProductList(Collection<Long> ids);

    /**
     * P5 口径：校验物料集合全部处于已审核状态（BOM 发布前强制）。
     * 聚合收集全部未审核物料并一次性报出（名称+编码+状态），避免逐个整改。
     *
     * @param productIds 物料编号集合（顶层成品 + 明细物料 + 替代料）
     */
    void validateProductsApprovedForBom(Collection<Long> productIds);

    /**
     * BPM 回调：更新物料审核状态
     */
    void updateProductAuditStatusByBpm(Long id, String processInstanceId, Integer status, String reason);

    /**
     * 修改审批结束：主表 CAS 恢复为已审批（APPROVE）。
     * 通过/驳回/撤回三条路径统一走此方法恢复主表状态（终态单一路径）。
     *
     * @param id                物料编号
     * @param processInstanceId 本次修改审批流程实例编号
     */
    void restoreProductAfterUpdateApproval(Long id, String processInstanceId);

    /**
     * 物料修改审批撤回后恢复主表：审批状态回退至已审批，并清空 processInstanceId（流程已终止）
     *
     * @param id 物料主键
     */
    void restoreProductAfterCancel(Long id);

    /**
     * 批量修改审批结束：主表 CAS 批量恢复为已审批（APPROVE），单条 UPDATE IN 完成，防 N+1。
     * 通过/驳回两条路径统一走此方法恢复主表状态（终态单一路径）。
     *
     * @param ids               物料编号集合
     * @param processInstanceId 本次批量修改审批流程实例编号
     */
    void restoreProductsAfterUpdateApproval(java.util.Collection<Long> ids, String processInstanceId);

    /**
     * 批量修改审批撤回后批量恢复主表：审批状态回退至已审批，并清空 processInstanceId
     *
     * @param ids 物料编号集合
     */
    void restoreProductsAfterCancel(java.util.Collection<Long> ids);

    /**
     * 判断物料是否处于审批中（新建或修改审批）
     */
    boolean isProductInApproval(Long id);

    /**
     * BPM 回调：撤回审核，回退为草稿
     */
    void rollbackProductAuditStatusToDraftByBpm(Long id, String processInstanceId, String reason);

    /**
     * 获得产品
     *
     * @param id 编号
     * @return 产品
     */
    ErpProductDO getProduct(Long id);

    /**
     * 两段式变更阶段一回调：CR_PENDING → EDITING（通过）/ APPROVE（驳回、撤回）。
     * 终态单一路径：由 ProductChangeRequestResultHandler 调用。
     *
     * @param id                物料编号
     * @param processInstanceId 本次流程实例编号
     * @param approved          true=阶段一通过，false=驳回/撤回
     * @param reason            原因
     */
    void completeChangeRequest(Long id, String processInstanceId, boolean approved, String reason);

    /**
     * 两段式变更阶段二回调：CONFIRM_PENDING → APPROVE（通过，暂存落主表）/ EDITING（驳回、撤回）。
     * 终态单一路径：由 ProductChangeConfirmResultHandler 调用。
     *
     * @param id                物料编号
     * @param processInstanceId 本次流程实例编号
     * @param approved          true=阶段二通过，false=驳回/撤回
     * @param reason            原因
     */
    void completeChangeConfirm(Long id, String processInstanceId, boolean approved, String reason);

    /**
     * 废除一段式回调：OBSOLETE_CR_PENDING → OBSOLETED（通过，销号留痕+编码释放）/ APPROVE（驳回、撤回）。
     * 终态单一路径：由 ProductObsoleteRequestResultHandler 调用。
     *
     * @param id                物料编号
     * @param processInstanceId 本次流程实例编号
     * @param approved          true=审批通过，false=驳回/撤回
     * @param reason            原因
     */
    void completeObsoleteRequest(Long id, String processInstanceId, boolean approved, String reason);

    /**
     * 启停一段式回调：STOP_PENDING → APPROVE（通过，目标 status 落主表）/ APPROVE（驳回、撤回，状态不变）。
     * 目标 status 取自暂存快照（提交启停审批时写入），先业务后终态。
     * 终态单一路径：由 ProductStatusChangeResultHandler 调用。
     *
     * @param id                物料编号
     * @param processInstanceId 本次流程实例编号
     * @param approved          true=审批通过，false=驳回/撤回
     * @param reason            原因
     */
    void completeStatusChange(Long id, String processInstanceId, boolean approved, String reason);

    /**
     * 获得指定状态的产品 VO 列表
     *
     * @param status 状态
     * @return 产品 VO 列表
     */
    List<ErpProductRespVO> getProductVOListByStatus(Integer status);

    List<ErpProductRespVO> getProductVOListByStatus(Integer status, String name);

    /**
     * 获得产品 VO 列表
     *
     * @param ids 编号数组
     * @return 产品 VO 列表
     */
    List<ErpProductRespVO> getProductVOList(Collection<Long> ids);

    /**
     * 获得产品 VO Map
     *
     * @param ids 编号数组
     * @return 产品 VO Map
     */
    default Map<Long, ErpProductRespVO> getProductVOMap(Collection<Long> ids) {
        return convertMap(getProductVOList(ids), ErpProductRespVO::getId);
    }

    /**
     * 获得产品 VO 分页
     *
     * @param pageReqVO 分页查询
     * @return 产品分页
     */
    PageResult<ErpProductRespVO> getProductVOPage(ErpProductPageReqVO pageReqVO);

    /**
     * 将产品 DO 列表构建为产品 VO 列表（补充分类/单位名称、Cadence 扩展、BOM 引用标记）。
     *
     * @param list 产品 DO 列表
     * @return 产品 VO 列表
     */
    List<ErpProductRespVO> buildProductVOList(List<ErpProductDO> list);

    /**
     * 基于产品分类编号，获得产品数量
     *
     * @param categoryId 产品分类编号
     * @return 产品数量
     */
    Long getProductCountByCategoryId(Long categoryId);

    /**
     * 基于产品单位编号，获得产品数量
     *
     * @param unitId 产品单位编号
     * @return 产品数量
     */
    Long getProductCountByUnitId(Long unitId);

    /**
     * 获得已审核物料精简列表（供 BOM 引用），字段为白名单裁剪，仅含下拉与单位反查所需
     *
     * @return 精简物料列表
     */
    List<ErpProductSimpleRespVO> getApprovedProductSimpleList(String keyword);

}