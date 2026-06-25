package cn.weitee.erp.module.erp.dal.mysql.mrp;

import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.suggest.ErpPurchaseSuggestPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpPurchaseSuggestDO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErpPurchaseSuggestMapperTest {

    @Test
    void buildPageQuery_shouldIncludeSourceOrderIdCondition() {
        ErpPurchaseSuggestPageReqVO reqVO = new ErpPurchaseSuggestPageReqVO();
        reqVO.setSourceOrderId(22L);

        LambdaQueryWrapperX<ErpPurchaseSuggestDO> query = ErpPurchaseSuggestMapper.buildPageQuery(reqVO);

        assertTrue(query.getExpression().getNormal().size() > 0);
    }

    @Test
    void buildPageQuery_shouldSkipSourceOrderIdConditionWhenMissing() {
        ErpPurchaseSuggestPageReqVO reqVO = new ErpPurchaseSuggestPageReqVO();

        LambdaQueryWrapperX<ErpPurchaseSuggestDO> query = ErpPurchaseSuggestMapper.buildPageQuery(reqVO);

        assertFalse(query.getExpression().getNormal().size() > 0);
    }

}
