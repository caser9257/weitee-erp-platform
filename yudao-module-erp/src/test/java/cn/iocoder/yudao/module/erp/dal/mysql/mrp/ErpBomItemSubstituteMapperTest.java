package cn.iocoder.yudao.module.erp.dal.mysql.mrp;

import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.substitute.ErpBomItemSubstitutePageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpBomItemSubstituteDO;
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
