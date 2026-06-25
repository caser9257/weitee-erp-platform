package cn.weitee.erp.module.erp.dal.mysql.sale;

import cn.weitee.erp.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.order.ErpSaleOrderPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErpSaleOrderMapperTest {

    @BeforeAll
    static void setUpTableInfo() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                ErpSaleOrderDO.class);
    }

    @Test
    void buildPageQuery_shouldIncludeSaleUserAndDeliveryReadyStatusFilters() {
        ErpSaleOrderPageReqVO reqVO = new ErpSaleOrderPageReqVO();
        reqVO.setSaleUserId(31L);
        reqVO.setDeliveryReadyStatus("READY_TO_SHIP");

        MPJLambdaWrapperX<ErpSaleOrderDO> query = ErpSaleOrderMapper.buildPageQuery(reqVO);
        String sqlSegment = query.getSqlSegment();

        assertTrue(sqlSegment.contains("sale_user_id"));
        assertTrue(sqlSegment.contains("delivery_ready_status"));
        assertTrue(query.getParamNameValuePairs().containsValue(31L));
        assertTrue(query.getParamNameValuePairs().containsValue("READY_TO_SHIP"));
    }

    @Test
    void buildPageQuery_shouldSkipSaleUserAndDeliveryReadyStatusFiltersWhenMissing() {
        ErpSaleOrderPageReqVO reqVO = new ErpSaleOrderPageReqVO();

        MPJLambdaWrapperX<ErpSaleOrderDO> query = ErpSaleOrderMapper.buildPageQuery(reqVO);
        String sqlSegment = query.getSqlSegment();

        assertFalse(sqlSegment.contains("sale_user_id"));
        assertFalse(sqlSegment.contains("delivery_ready_status"));
        assertFalse(query.getParamNameValuePairs().containsValue("READY_TO_SHIP"));
    }

}
