package cn.iocoder.yudao.module.erp.service.purchase;

import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpQcDefectReasonDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpQcDefectReasonMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * IQC 不良原因主数据 Service 实现
 */
@Service
@Validated
public class ErpQcDefectReasonServiceImpl implements ErpQcDefectReasonService {

    @Resource
    private ErpQcDefectReasonMapper erpQcDefectReasonMapper;

    @Override
    public List<ErpQcDefectReasonDO> getDefectReasonListByStatus(Integer status) {
        return erpQcDefectReasonMapper.selectListByStatus(status);
    }

}
