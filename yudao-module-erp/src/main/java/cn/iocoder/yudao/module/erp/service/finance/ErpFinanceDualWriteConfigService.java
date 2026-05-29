package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceDualWriteConfigDO;

/**
 * ERP 双写配置服务接口
 */
public interface ErpFinanceDualWriteConfigService {

    /**
     * 获取指定账簿的双写配置
     *
     * @param ledgerId 账簿ID
     * @return 双写配置
     */
    ErpFinanceDualWriteConfigDO getConfigByLedgerId(Long ledgerId);

    /**
     * 创建双写配置
     *
     * @param config 配置信息
     * @return 配置ID
     */
    Long createConfig(ErpFinanceDualWriteConfigDO config);

    /**
     * 更新双写配置
     *
     * @param config 配置信息
     */
    void updateConfig(ErpFinanceDualWriteConfigDO config);

    /**
     * 删除双写配置
     *
     * @param id 配置ID
     */
    void deleteConfig(Long id);

    /**
     * 获取双写配置详情
     *
     * @param id 配置ID
     * @return 配置详情
     */
    ErpFinanceDualWriteConfigDO getConfig(Long id);
}
