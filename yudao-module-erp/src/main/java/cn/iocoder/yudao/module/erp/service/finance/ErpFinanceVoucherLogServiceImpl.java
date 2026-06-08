package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceVoucherLogDO;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceVoucherLogMapper;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

@Service
public class ErpFinanceVoucherLogServiceImpl implements ErpFinanceVoucherLogService {

    @Resource
    private ErpFinanceVoucherLogMapper voucherLogMapper;

    @Override
    public void createVoucherLog(ErpFinanceVoucherLogDO log) {
        voucherLogMapper.insert(log);
    }

    @Override
    public List<ErpFinanceVoucherLogDO> getVoucherLogsByVoucherId(Long voucherId) {
        return voucherLogMapper.selectList(
                new LambdaQueryWrapperX<ErpFinanceVoucherLogDO>()
                        .eq(ErpFinanceVoucherLogDO::getVoucherId, voucherId)
                        .orderByDesc(ErpFinanceVoucherLogDO::getCreateTime));
    }
}
