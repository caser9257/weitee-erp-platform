package cn.weitee.erp.module.ai.dal.mysql.knowledge;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.ai.controller.admin.knowledge.vo.knowledge.AiKnowledgePageReqVO;
import cn.weitee.erp.module.ai.dal.dataobject.knowledge.AiKnowledgeDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * AI 知识�?Mapper
 *
 * @author xiaoxin
 */
@Mapper
public interface AiKnowledgeMapper extends BaseMapperX<AiKnowledgeDO> {

    default PageResult<AiKnowledgeDO> selectPage(AiKnowledgePageReqVO pageReqVO) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<AiKnowledgeDO>()
                .likeIfPresent(AiKnowledgeDO::getName, pageReqVO.getName())
                .eqIfPresent(AiKnowledgeDO::getStatus, pageReqVO.getStatus())
                .betweenIfPresent(AiKnowledgeDO::getCreateTime, pageReqVO.getCreateTime())
                .orderByDesc(AiKnowledgeDO::getId));
    }

    default List<AiKnowledgeDO> selectListByStatus(Integer status) {
        return selectList(AiKnowledgeDO::getStatus, status);
    }

}
