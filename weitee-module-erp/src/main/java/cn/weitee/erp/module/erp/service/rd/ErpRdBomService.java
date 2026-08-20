package cn.weitee.erp.module.erp.service.rd;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomSaveReqVO;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomIntegrityIssueRespVO;
import cn.weitee.erp.module.erp.dal.dataobject.rd.ErpRdBomDO;
import cn.weitee.erp.module.erp.dal.dataobject.rd.ErpRdBomItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.rd.ErpRdBomItemSubstituteDO;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ErpRdBomService {

    Long createRdBom(@Valid ErpRdBomSaveReqVO createReqVO);

    void updateRdBom(@Valid ErpRdBomSaveReqVO updateReqVO);

    void deleteRdBom(Long id);

    ErpRdBomDO getRdBom(Long id);

    PageResult<ErpRdBomDO> getRdBomPage(ErpRdBomPageReqVO pageReqVO);

    List<ErpRdBomItemDO> getRdBomItemList(Long bomId);

    List<ErpRdBomItemSubstituteDO> getRdBomItemSubstituteList(Collection<Long> bomItemIds);

    void publishRdBom(Long id);

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

}
