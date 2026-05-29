package cn.iocoder.yudao.module.erp.dal.mysql.mrp;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionIssueVoucherItemDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface ErpProductionIssueVoucherItemMapper extends BaseMapperX<ErpProductionIssueVoucherItemDO> {

    default List<ErpProductionIssueVoucherItemDO> selectListByVoucherId(Long voucherId) {
        return selectList(ErpProductionIssueVoucherItemDO::getVoucherId, voucherId);
    }

    default List<ErpProductionIssueVoucherItemDO> selectListByVoucherIds(Collection<Long> voucherIds) {
        if (CollUtil.isEmpty(voucherIds)) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapper<ErpProductionIssueVoucherItemDO>()
                .in(ErpProductionIssueVoucherItemDO::getVoucherId, voucherIds)
                .orderByAsc(ErpProductionIssueVoucherItemDO::getId));
    }

}
