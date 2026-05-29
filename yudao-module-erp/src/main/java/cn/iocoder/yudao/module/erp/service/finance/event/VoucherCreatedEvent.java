package cn.iocoder.yudao.module.erp.service.finance.event;

import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceVoucherDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceVoucherEntryDO;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * 凭证创建事件
 * 用于触发双写逻辑
 */
@Getter
public class VoucherCreatedEvent extends ApplicationEvent {

    /**
     * 源凭证
     */
    private final ErpFinanceVoucherDO voucher;

    /**
     * 凭证分录列表
     */
    private final List<ErpFinanceVoucherEntryDO> entries;

    /**
     * 源账簿ID
     */
    private final Long sourceLedgerId;

    public VoucherCreatedEvent(Object source, ErpFinanceVoucherDO voucher,
                               List<ErpFinanceVoucherEntryDO> entries, Long sourceLedgerId) {
        super(source);
        this.voucher = voucher;
        this.entries = entries;
        this.sourceLedgerId = sourceLedgerId;
    }
}
