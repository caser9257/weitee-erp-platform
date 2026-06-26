package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.receipt.ErpServiceReceiptPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.receipt.ErpServiceReceiptSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpServiceReceiptDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpServiceReceiptMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.List;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.*;

/**
 * 服务接收�?Service 实现
 *
 * @author weitee
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
        // 只有草稿状态才能编�?
        if (receipt.getStatus() != 0) {
            throw exception(SERVICE_RECEIPT_STATUS_INVALID);
        }
        BeanUtils.copyProperties(reqVO, receipt);
        serviceReceiptMapper.updateById(receipt);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteServiceReceipt(Long id) {
        ErpServiceReceiptDO receipt = serviceReceiptMapper.selectById(id);
        if (receipt == null) {
            throw exception(SERVICE_RECEIPT_NOT_EXISTS);
        }
        // 只有草稿状态才能删�?
        if (receipt.getStatus() != 0) {
            throw exception(SERVICE_RECEIPT_STATUS_INVALID);
        }
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
        // 校验状态：只有草稿(0)状态才能确�?
        if (receipt.getStatus() != 0) {
            throw exception(SERVICE_RECEIPT_STATUS_INVALID);
        }
        receipt.setStatus(10); // 已确�?
        serviceReceiptMapper.updateById(receipt);
        log.info("[confirmServiceReceipt] 服务接收单已确认：{}", id);
    }

}
