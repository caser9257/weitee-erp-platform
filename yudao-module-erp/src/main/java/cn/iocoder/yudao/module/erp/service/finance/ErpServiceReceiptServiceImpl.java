package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.receipt.ErpServiceReceiptPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.receipt.ErpServiceReceiptSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpServiceReceiptDO;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpServiceReceiptMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.*;

/**
 * 服务接收单 Service 实现
 *
 * @author ruoyi-vue-pro
 */
@Service
@Validated
@Slf4j
public class ErpServiceReceiptServiceImpl implements ErpServiceReceiptService {

    @Resource
    private ErpServiceReceiptMapper serviceReceiptMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createServiceReceipt(ErpServiceReceiptSaveReqVO reqVO) {
        ErpServiceReceiptDO receipt = BeanUtils.toBean(reqVO, ErpServiceReceiptDO.class);
        receipt.setStatus(0); // 草稿
        serviceReceiptMapper.insert(receipt);
        return receipt.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateServiceReceipt(ErpServiceReceiptSaveReqVO reqVO) {
        ErpServiceReceiptDO receipt = serviceReceiptMapper.selectById(reqVO.getId());
        if (receipt == null) {
            throw exception(SERVICE_RECEIPT_NOT_EXISTS);
        }
        BeanUtils.copyProperties(reqVO, receipt);
        serviceReceiptMapper.updateById(receipt);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteServiceReceipt(Long id) {
        serviceReceiptMapper.deleteById(id);
    }

    @Override
    public ErpServiceReceiptDO getServiceReceipt(Long id) {
        return serviceReceiptMapper.selectById(id);
    }

    @Override
    public PageResult<ErpServiceReceiptDO> getServiceReceiptPage(ErpServiceReceiptPageReqVO reqVO) {
        return serviceReceiptMapper.selectPage(reqVO);
    }

    @Override
    public List<ErpServiceReceiptDO> getServiceReceiptList() {
        return serviceReceiptMapper.selectList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmServiceReceipt(Long id) {
        ErpServiceReceiptDO receipt = serviceReceiptMapper.selectById(id);
        if (receipt == null) {
            throw exception(SERVICE_RECEIPT_NOT_EXISTS);
        }
        // 校验状态：只有草稿(0)状态才能确认
        if (receipt.getStatus() != 0) {
            throw exception(SERVICE_RECEIPT_STATUS_INVALID);
        }
        receipt.setStatus(10); // 已确认
        serviceReceiptMapper.updateById(receipt);
        log.info("[confirmServiceReceipt] 服务接收单已确认：{}", id);
    }

}
