package cn.iocoder.yudao.module.erp.dal.mysql.mrp;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpOutsourceIssueItemDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface ErpOutsourceIssueItemMapper extends BaseMapperX<ErpOutsourceIssueItemDO> {
    default List<ErpOutsourceIssueItemDO> selectListByIssueId(Long issueId) {
        return selectList(ErpOutsourceIssueItemDO::getIssueId, issueId);
    }
    default List<ErpOutsourceIssueItemDO> selectListByIssueIds(Collection<Long> issueIds) {
        if (CollUtil.isEmpty(issueIds)) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapper<ErpOutsourceIssueItemDO>()
                .in(ErpOutsourceIssueItemDO::getIssueId, issueIds)
                .orderByAsc(ErpOutsourceIssueItemDO::getId));
    }
}
