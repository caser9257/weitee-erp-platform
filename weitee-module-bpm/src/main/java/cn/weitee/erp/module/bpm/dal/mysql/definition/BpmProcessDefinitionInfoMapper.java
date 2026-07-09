package cn.weitee.erp.module.bpm.dal.mysql.definition;

import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.bpm.dal.dataobject.definition.BpmProcessDefinitionInfoDO;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface BpmProcessDefinitionInfoMapper extends BaseMapperX<BpmProcessDefinitionInfoDO> {

    default List<BpmProcessDefinitionInfoDO> selectListByProcessDefinitionIds(Collection<String> processDefinitionIds) {
        return selectList(BpmProcessDefinitionInfoDO::getProcessDefinitionId, processDefinitionIds);
    }

    default BpmProcessDefinitionInfoDO selectByProcessDefinitionId(String processDefinitionId) {
        return selectOne(new LambdaQueryWrapperX<BpmProcessDefinitionInfoDO>()
                .eq(BpmProcessDefinitionInfoDO::getProcessDefinitionId, processDefinitionId)
                .orderByDesc(BpmProcessDefinitionInfoDO::getId)
                .last("LIMIT 1"));
    }

    default void updateByModelId(String modelId, BpmProcessDefinitionInfoDO updateObj) {
        update(updateObj,
                new LambdaQueryWrapperX<BpmProcessDefinitionInfoDO>().eq(BpmProcessDefinitionInfoDO::getModelId, modelId));
    }

}
