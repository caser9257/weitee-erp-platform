package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceDualWriteConfigDO;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceDualWriteConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstantsFinanceDualWrite.*;

/**
 * ERP 双写配置服务实现
 */
@Slf4j
@Service
public class ErpFinanceDualWriteConfigServiceImpl implements ErpFinanceDualWriteConfigService {

    @Resource
    private ErpFinanceDualWriteConfigMapper dualWriteConfigMapper;

    @Override
    public ErpFinanceDualWriteConfigDO getConfigByLedgerId(Long ledgerId) {
        return dualWriteConfigMapper.selectOne(
                new LambdaQueryWrapperX<ErpFinanceDualWriteConfigDO>()
                        .eq(ErpFinanceDualWriteConfigDO::getLedgerId, ledgerId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createConfig(ErpFinanceDualWriteConfigDO config) {
        // 检查是否已存在配置
        ErpFinanceDualWriteConfigDO existingConfig = getConfigByLedgerId(config.getLedgerId());
        if (existingConfig != null) {
            throw exception(DUAL_WRITE_CONFIG_ALREADY_EXISTS);
        }
        dualWriteConfigMapper.insert(config);
        return config.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(ErpFinanceDualWriteConfigDO config) {
        // 校验存在
        validateConfigExists(config.getId());
        dualWriteConfigMapper.updateById(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConfig(Long id) {
        // 校验存在
        validateConfigExists(id);
        dualWriteConfigMapper.deleteById(id);
    }

    @Override
    public ErpFinanceDualWriteConfigDO getConfig(Long id) {
        return dualWriteConfigMapper.selectById(id);
    }

    private void validateConfigExists(Long id) {
        if (dualWriteConfigMapper.selectById(id) == null) {
            throw exception(DUAL_WRITE_CONFIG_NOT_EXISTS);
        }
    }
}
