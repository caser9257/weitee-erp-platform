package cn.weitee.erp.module.erp.dal.mysql.mrp;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionIssueItemDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface ErpProductionIssueItemMapper extends BaseMapperX<ErpProductionIssueItemDO> {

    default List<ErpProductionIssueItemDO> selectListByProductionMaterialIds(Collection<Long> productionMaterialIds) {
        if (CollUtil.isEmpty(productionMaterialIds)) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapper<ErpProductionIssueItemDO>()
                .in(ErpProductionIssueItemDO::getProductionMaterialId, productionMaterialIds));
    }

    default List<ErpProductionIssueItemDO> selectListByIssueId(Long issueId) {
        return selectList(ErpProductionIssueItemDO::getIssueId, issueId);
    }

    default List<ErpProductionIssueItemDO> selectListByIssueIds(Collection<Long> issueIds) {
        if (CollUtil.isEmpty(issueIds)) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapper<ErpProductionIssueItemDO>()
                .in(ErpProductionIssueItemDO::getIssueId, issueIds)
                .orderByAsc(ErpProductionIssueItemDO::getId));
    }

}
