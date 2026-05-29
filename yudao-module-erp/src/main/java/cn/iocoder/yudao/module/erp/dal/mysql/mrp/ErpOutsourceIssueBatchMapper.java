package cn.iocoder.yudao.module.erp.dal.mysql.mrp;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpOutsourceIssueBatchDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface ErpOutsourceIssueBatchMapper extends BaseMapperX<ErpOutsourceIssueBatchDO> {
    default List<ErpOutsourceIssueBatchDO> selectListByIssueItemIds(Collection<Long> issueItemIds) {
        if (CollUtil.isEmpty(issueItemIds)) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapper<ErpOutsourceIssueBatchDO>()
                .in(ErpOutsourceIssueBatchDO::getIssueItemId, issueItemIds)
                .orderByAsc(ErpOutsourceIssueBatchDO::getId));
    }
}
