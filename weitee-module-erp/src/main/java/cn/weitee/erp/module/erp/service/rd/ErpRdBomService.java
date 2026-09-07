package cn.weitee.erp.module.erp.service.rd;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomApprovalViewRespVO;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomSaveReqVO;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomIntegrityIssueRespVO;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomVersionDiffRespVO;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomBaselineDiffVO;
import cn.weitee.erp.module.erp.dal.dataobject.rd.ErpRdBomDO;
import cn.weitee.erp.module.erp.dal.dataobject.rd.ErpRdBomItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.rd.ErpRdBomItemSubstituteDO;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ErpRdBomService {

    Long createRdBom(@Valid ErpRdBomSaveReqVO createReqVO);

    /**
     * 导入专用创建：跳过整单完整性断言（导入链路已在落库前自行完成行级校验剔行），
     * 其余校验与 {@link #createRdBom} 一致。
     *
     * @param createReqVO 创建请求
     * @return 研发 BOM 编号
     */
    Long createRdBomForImport(@Valid ErpRdBomSaveReqVO createReqVO);

    /**
     * 行级完整性校验（导入专用）：返回问题清单但不抛异常，供导入链路将 ERROR 行剔入 failDetails。
     *
     * @param items 明细列表（SaveReqVO.Item 形态）
     * @return 完整性问题清单（含 WARN/ERROR），无问题返回空列表
     */
    List<ErpRdBomIntegrityIssueRespVO> validateRdBomItemsIntegrity(List<ErpRdBomSaveReqVO.Item> items);

    void updateRdBom(@Valid ErpRdBomSaveReqVO updateReqVO);

    void deleteRdBom(Long id);

    ErpRdBomDO getRdBom(Long id);

    /**
     * 按产品、BOM 编码和版本查找未删除的同身份 BOM；空版本按草稿版本匹配。
     */
    ErpRdBomDO getRdBomByIdentity(Long productId, String bomCode, String version);

    PageResult<ErpRdBomDO> getRdBomPage(ErpRdBomPageReqVO pageReqVO);

    List<ErpRdBomItemDO> getRdBomItemList(Long bomId);

    /**
     * 批量查询多张 BOM 的明细（分页列表装配用，消除 N+1）
     *
     * @param bomIds 研发 BOM 编号集合
     * @return 明细列表
     */
    List<ErpRdBomItemDO> getRdBomItemListByBomIds(Collection<Long> bomIds);

    List<ErpRdBomItemSubstituteDO> getRdBomItemSubstituteList(Collection<Long> bomItemIds);

    void publishRdBom(Long id);

    /**
     * 发起升版式变更：仅限已审批通过的 BOM。
     * 复制源 BOM 的明细与替代料，生成一条新版本（DRAFT）的研发 BOM，源版本保持不变。
     * 新版本须经再次提交审批生效。
     *
     * @param id 源研发 BOM 编号（须为 APPROVE 状态）
     * @return 新版本研发 BOM 编号
     */
    Long startChangeRdBom(Long id);

    /**
     * 按成品编号获取其最新版本的研发 BOM（用于结构树根节点回退）
     *
     * @param productId 成品编号
     * @return 最新研发 BOM；不存在返回 null
     */
    ErpRdBomDO getLatestRdBomByProductId(Long productId);

    /**
     * 批量按成品编号获取各自最新版本的研发 BOM（结构树递归，避免 N+1）
     *
     * @param productIds 成品编号集合
     * @return key=成品编号，value=该成品最新研发 BOM
     */
    Map<Long, ErpRdBomDO> getLatestRdBomMapByProductIds(Collection<Long> productIds);

    /**
     * BPM 回调：更新研发 BOM 审批状态（通过/驳回）
     */
    void updateRdBomStatusByBpm(Long id, String processInstanceId, Integer status, String reason);

    /**
     * BPM 回调：撤回审批，回退为草稿
     */
    void rollbackRdBomStatusToDraftByBpm(Long id, String processInstanceId, String reason);

    /**
     * 反向追溯：按物料查直接/递归的上级研发 BOM（Where-Used）
     *
     * @param materialId 物料编号
     * @return 直接父 BOM 列表（按层级展开时递归向上，level 标记深度）
     */
    List<cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomWhereUsedRespVO> getWhereUsed(Long materialId);

    /**
     * 校验研发 BOM 完整性（漏件 / 悬浮件 / 用量异常）
     *
     * @param id 研发 BOM 编号
     * @return 问题清单；空列表代表校验通过
     */
    List<ErpRdBomIntegrityIssueRespVO> validateRdBomIntegrity(Long id);

    /**
     * 版本明细对比：按物料对齐两版明细，输出新增/删除/修改（字段级）/未变四类条目
     *
     * @param sourceId 旧版本 BOM 编号
     * @param targetId 新版本 BOM 编号
     * @return 对比结果
     */
    ErpRdBomVersionDiffRespVO getRdBomVersionDiff(Long sourceId, Long targetId);

    /**
     * 作废研发 BOM：仅限已审批通过的 BOM。作废为终态，版本保留但退出"最新版"选择。
     *
     * @param id     研发 BOM 编号（须为 APPROVE 状态）
     * @param reason 作废原因（可空）
     */
    void voidRdBom(Long id, String reason);

    /**
     * 取消作废：与作废对称的逆向流转（VOID → APPROVE），版本重新参与"最新版"选择
     *
     * @param id 研发 BOM 编号（须为 VOID 状态）
     */
    void unvoidRdBom(Long id);

    /**
     * 版本沿革链：沿 sourceBomId 双向遍历，返回同一成品下与本 BOM 关联的全部版本（按主版本升序）
     *
     * @param id 任一版本的 BOM 编号
     * @return 版本链列表（含自身）
     */
    List<ErpRdBomDO> getRdBomVersionChain(Long id);

    /**
     * 审批视图聚合：BOM 详细内容 + 与基准版本的明细差异 + 变更记录。
     * 对比基准解析规则：sourceBomId 优先；为空时回退同成品最近一个 APPROVE 版本；均无则视为首次提交。
     *
     * @param id 研发 BOM 编号
     * @return 审批视图数据
     */
    ErpRdBomApprovalViewRespVO getRdBomApprovalView(Long id);

    /**
     * 导入增量差异警示：本次导入的物料集合相较该成品最新版 BOM 缺失了哪些物料。
     * 仅提示不阻断（改版减料属正常工程行为，缺失清单供人工确认是否漏行）。
     *
     * @param productId          成品编号；为 null 时返回 null（无法确定对比基准）
     * @param importMaterialIds  本次导入文件解析出的明细物料编号集合
     * @return 差异结果；该成品无任何非作废 BOM 时返回 null（首次导入）
     */
    ErpRdBomBaselineDiffVO diffImportAgainstLatest(Long productId, Collection<Long> importMaterialIds);

}
