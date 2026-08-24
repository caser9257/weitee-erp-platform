package cn.weitee.erp.module.erp.dal.mysql.finance;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceDualProjectCostResultDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;

/**
 * 项目双账成本结果 Mapper
 */
@Mapper
public interface ErpFinanceDualProjectCostResultMapper extends BaseMapperX<ErpFinanceDualProjectCostResultDO> {

    @Delete("""
            <script>
            DELETE FROM erp_finance_dual_project_cost_result
            WHERE id IN
            <foreach collection="ids" item="id" open="(" separator="," close=")">
                #{id}
            </foreach>
            </script>
            """)
    int hardDeleteByIds(@Param("ids") Collection<Long> ids);
}
