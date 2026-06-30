package cn.weitee.erp.module.erp.controller.admin.sale;

import cn.weitee.erp.module.erp.controller.admin.sale.vo.order.ErpSaleOrderSaveReqVO;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErpSaleOrderSaveReqVOValidationTest {

    private final Validator validator;

    ErpSaleOrderSaveReqVOValidationTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    @Test
    void validate_shouldRejectBlankBusinessTypeSettlementTypeAndEmptyItems() {
        ErpSaleOrderSaveReqVO reqVO = new ErpSaleOrderSaveReqVO();
        reqVO.setCustomerId(1L);
        reqVO.setOrderTime(LocalDateTime.now());
        reqVO.setBusinessType(" ");
        reqVO.setSettlementType("");
        reqVO.setItems(java.util.List.of());

        Set<String> messages = validator.validate(reqVO).stream()
                .map(violation -> violation.getMessage())
                .collect(Collectors.toSet());

        assertEquals(Set.of("业务类型不能为空", "结算类型不能为空", "订单清单不能为空"), messages);
    }
}
