package cn.weitee.erp.module.erp.dal.mysql.mrp;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionReportItemDO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ErpProductionReportItemMapper extends BaseMapperX<ErpProductionReportItemDO> {

    default List<ErpProductionReportItemDO> selectListByReportId(Long reportId) {
        return selectList(Wrappers.<ErpProductionReportItemDO>lambdaQuery()
                .eq(ErpProductionReportItemDO::getReportId, reportId));
    }
}
