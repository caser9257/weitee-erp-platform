package cn.iocoder.yudao.module.erp.service.finance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public interface ErpFinanceRdMonthEndService {

    MonthEndResult executeMonthEnd(String period);

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class MonthEndResult {

        private Long expenseCarryForwardVoucherId;

        private Integer capitalizeDepreciationCount;
    }
}
