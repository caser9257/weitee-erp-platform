package cn.weitee.erp.module.erp.dal.mysql.mrp;

import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.suggest.ErpProductionSuggestPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionSuggestDO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErpProductionSuggestMapperTest {

    @Test
    void buildPageQuery_shouldIncludeSourceOrderIdCondition() {
        ErpProductionSuggestPageReqVO reqVO = new ErpProductionSuggestPageReqVO();
        reqVO.setSourceOrderId(22L);

        LambdaQueryWrapperX<ErpProductionSuggestDO> query = ErpProductionSuggestMapper.buildPageQuery(reqVO);

        assertTrue(query.getExpression().getNormal().size() > 0);
    }

    @Test
    void buildPageQuery_shouldSkipSourceOrderIdConditionWhenMissing() {
        ErpProductionSuggestPageReqVO reqVO = new ErpProductionSuggestPageReqVO();

        LambdaQueryWrapperX<ErpProductionSuggestDO> query = ErpProductionSuggestMapper.buildPageQuery(reqVO);

        assertFalse(query.getExpression().getNormal().size() > 0);
    }

}
