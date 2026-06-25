package cn.weitee.erp.module.erp.dal.mysql.purchase;

import cn.weitee.erp.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErpPurchaseOrderMapperTest {

    @BeforeAll
    static void setUpTableInfo() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                ErpPurchaseOrderDO.class);
    }

    @Test
    void buildPageQuery_shouldOrderByCreateTimeDescThenIdDesc() {
        ErpPurchaseOrderPageReqVO reqVO = new ErpPurchaseOrderPageReqVO();

        MPJLambdaWrapperX<ErpPurchaseOrderDO> query = ErpPurchaseOrderMapper.buildPageQuery(reqVO);

        assertNotNull(query);
    }

    @Test
    void buildPageQuery_shouldIncludeSourceOrderExistsFilter() {
        ErpPurchaseOrderPageReqVO reqVO = new ErpPurchaseOrderPageReqVO();
        reqVO.setSourceOrderId(52L);

        MPJLambdaWrapperX<ErpPurchaseOrderDO> query = ErpPurchaseOrderMapper.buildPageQuery(reqVO);
        String sqlSegment = query.getSqlSegment();

        assertTrue(sqlSegment.contains("erp_purchase_suggest"));
        assertTrue(sqlSegment.contains("convert_purchase_order_id"));
        assertTrue(sqlSegment.contains("source_order_id"));
        assertTrue(query.getParamNameValuePairs().containsValue(52L));
    }

    @Test
    void buildPageQuery_shouldSkipSourceOrderExistsFilterWhenMissing() {
        ErpPurchaseOrderPageReqVO reqVO = new ErpPurchaseOrderPageReqVO();

        MPJLambdaWrapperX<ErpPurchaseOrderDO> query = ErpPurchaseOrderMapper.buildPageQuery(reqVO);

        assertFalse(query.getSqlSegment().contains("erp_purchase_suggest"));
        assertFalse(query.getSqlSegment().contains("source_order_id"));
    }

    @Test
    void buildPageQuery_shouldExcludeOrdersWithPendingPurchaseInWhenInEnable() {
        ErpPurchaseOrderPageReqVO reqVO = new ErpPurchaseOrderPageReqVO();
        reqVO.setInEnable(true);

        MPJLambdaWrapperX<ErpPurchaseOrderDO> query = ErpPurchaseOrderMapper.buildPageQuery(reqVO);
        String sqlSegment = query.getSqlSegment();

        assertTrue(sqlSegment.contains("erp_purchase_in"));
        assertTrue(sqlSegment.contains("order_id = t.id"));
        assertTrue(sqlSegment.contains("qa_status"));
        assertTrue(sqlSegment.contains("NOT EXISTS"));
    }

}
