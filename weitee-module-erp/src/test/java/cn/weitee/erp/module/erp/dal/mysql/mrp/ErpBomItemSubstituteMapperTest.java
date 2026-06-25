package cn.weitee.erp.module.erp.dal.mysql.mrp;

import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.substitute.ErpBomItemSubstitutePageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomItemSubstituteDO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ErpBomItemSubstituteMapperTest {

    @Test
    void buildPageQuery_shouldIncludeAllFilters() {
        ErpBomItemSubstitutePageReqVO reqVO = new ErpBomItemSubstitutePageReqVO();
        reqVO.setBomItemIds(List.of(1L, 2L));
        reqVO.setSubstituteMaterialId(3L);
        reqVO.setEnableAutoRecommend(Boolean.TRUE);

        LambdaQueryWrapperX<ErpBomItemSubstituteDO> query = ErpBomItemSubstituteMapper.buildPageQuery(reqVO);

        assertThat(query.getExpression().getNormal()).hasSize(11);
    }

    @Test
    void buildPageQuery_shouldSkipMissingFilters() {
        ErpBomItemSubstitutePageReqVO reqVO = new ErpBomItemSubstitutePageReqVO();

        LambdaQueryWrapperX<ErpBomItemSubstituteDO> query = ErpBomItemSubstituteMapper.buildPageQuery(reqVO);

        assertThat(query.getExpression().getNormal()).isEmpty();
    }

}
