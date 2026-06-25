package cn.weitee.erp.module.ai.service.knowledge;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.ai.controller.admin.knowledge.vo.knowledge.AiKnowledgePageReqVO;
import cn.weitee.erp.module.ai.controller.admin.knowledge.vo.knowledge.AiKnowledgeSaveReqVO;
import cn.weitee.erp.module.ai.dal.dataobject.knowledge.AiKnowledgeDO;

import java.util.List;

/**
 * AI 知识�?基础信息 Service 接口
 *
 * @author xiaoxin
 */
public interface AiKnowledgeService {

    /**
     * 创建知识�?
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createKnowledge(AiKnowledgeSaveReqVO createReqVO);

    /**
     * 更新知识�?
     *
     * @param updateReqVO 更新信息
     */
    void updateKnowledge(AiKnowledgeSaveReqVO updateReqVO);

    /**
     * 删除知识�?
     *
     * @param id 知识库编�?
     */
    void deleteKnowledge(Long id);

    /**
     * 获得知识�?
     *
     * @param id 编号
     * @return 知识�?
     */
    AiKnowledgeDO getKnowledge(Long id);

    /**
     * 校验知识库是否存�?
     *
     * @param id 记录编号
     */
    AiKnowledgeDO validateKnowledgeExists(Long id);

    /**
     * 获得知识库分�?
     *
     * @param pageReqVO 分页查询
     * @return 知识库分�?
     */
    PageResult<AiKnowledgeDO> getKnowledgePage(AiKnowledgePageReqVO pageReqVO);

    /**
     * 获得指定状态的知识库列�?
     *
     * @param status 状�?
     * @return 知识库列�?
     */
    List<AiKnowledgeDO> getKnowledgeSimpleListByStatus(Integer status);

}
