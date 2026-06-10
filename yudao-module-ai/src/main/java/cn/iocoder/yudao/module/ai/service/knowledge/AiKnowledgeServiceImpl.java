package cn.iocoder.yudao.module.ai.service.knowledge;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.knowledge.AiKnowledgePageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.knowledge.vo.knowledge.AiKnowledgeSaveReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.knowledge.AiKnowledgeDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.mysql.knowledge.AiKnowledgeMapper;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.KNOWLEDGE_NOT_EXISTS;

/**
 * AI 知识�?基础信息 Service 实现�?
 *
 * @author xiaoxin
 */
@Service
@Slf4j
public class AiKnowledgeServiceImpl implements AiKnowledgeService {

    @Resource
    private AiKnowledgeMapper aiKnowledgeMapper;

    @Resource
    private AiModelService modelService;
    @Resource
    private AiKnowledgeSegmentService knowledgeSegmentService;
    @Resource
    private AiKnowledgeDocumentService knowledgeDocumentService;

    @Override
    public Long createKnowledge(AiKnowledgeSaveReqVO createReqVO) {
        // 1. 校验模型配置
        AiModelDO model = modelService.validateModel(createReqVO.getEmbeddingModelId());

        // 2. 插入知识�?
        AiKnowledgeDO knowledge = BeanUtils.toBean(createReqVO, AiKnowledgeDO.class)
                .setEmbeddingModel(model.getModel());
        aiKnowledgeMapper.insert(knowledge);
        return knowledge.getId();
    }

    @Override
    public void updateKnowledge(AiKnowledgeSaveReqVO updateReqVO) {
        // 1.1 校验知识库存�?
        AiKnowledgeDO oldKnowledge = validateKnowledgeExists(updateReqVO.getId());
        // 1.2 校验模型配置
        AiModelDO model = modelService.validateModel(updateReqVO.getEmbeddingModelId());

        // 2. 更新知识�?
        AiKnowledgeDO updateObj = BeanUtils.toBean(updateReqVO, AiKnowledgeDO.class)
                .setEmbeddingModel(model.getModel());
        aiKnowledgeMapper.updateById(updateObj);

        // 3. 如果模型变化，需�?reindex 所有的文档
        if (ObjUtil.notEqual(oldKnowledge.getEmbeddingModelId(), updateReqVO.getEmbeddingModelId())) {
            knowledgeSegmentService.reindexByKnowledgeIdAsync(updateReqVO.getId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteKnowledge(Long id) {
        // 1. 校验存在
        validateKnowledgeExists(id);

        // 2. 删除知识库下的所有文档及段落
        knowledgeDocumentService.deleteKnowledgeDocumentByKnowledgeId(id);

        // 3. 删除知识�?
        // 特殊：知识库需要最后删除，不然相关的配置会找不�?
        aiKnowledgeMapper.deleteById(id);
    }

    @Override
    public AiKnowledgeDO getKnowledge(Long id) {
        return aiKnowledgeMapper.selectById(id);
    }

    @Override
    public AiKnowledgeDO validateKnowledgeExists(Long id) {
        AiKnowledgeDO knowledge = aiKnowledgeMapper.selectById(id);
        if (knowledge == null) {
            throw exception(KNOWLEDGE_NOT_EXISTS);
        }
        return knowledge;
    }

    @Override
    public PageResult<AiKnowledgeDO> getKnowledgePage(AiKnowledgePageReqVO pageReqVO) {
        return aiKnowledgeMapper.selectPage(pageReqVO);
    }

    @Override
    public List<AiKnowledgeDO> getKnowledgeSimpleListByStatus(Integer status) {
        return aiKnowledgeMapper.selectListByStatus(status);
    }

}
