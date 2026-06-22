package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.lease.ErpLeaseContractPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.lease.ErpLeaseContractSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpLeaseContractDO;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpLeaseContractMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.*;

/**
 * 租赁合同 Service 实现
 *
 * @author ruoyi-vue-pro
 */
@Service
@Validated
@Slf4j
public class ErpLeaseContractServiceImpl implements ErpLeaseContractService {

    @Resource
    private ErpLeaseContractMapper leaseContractMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createLeaseContract(ErpLeaseContractSaveReqVO reqVO) {
        ErpLeaseContractDO contract = BeanUtils.toBean(reqVO, ErpLeaseContractDO.class);
        contract.setStatus(0); // 草稿
        leaseContractMapper.insert(contract);
        return contract.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLeaseContract(ErpLeaseContractSaveReqVO reqVO) {
        ErpLeaseContractDO contract = leaseContractMapper.selectById(reqVO.getId());
        if (contract == null) {
            throw exception(LEASE_CONTRACT_NOT_EXISTS);
        }
        BeanUtils.copyProperties(reqVO, contract);
        leaseContractMapper.updateById(contract);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLeaseContract(Long id) {
        leaseContractMapper.deleteById(id);
    }

    @Override
    public ErpLeaseContractDO getLeaseContract(Long id) {
        return leaseContractMapper.selectById(id);
    }

    @Override
    public PageResult<ErpLeaseContractDO> getLeaseContractPage(ErpLeaseContractPageReqVO reqVO) {
        return leaseContractMapper.selectPage(reqVO);
    }

    @Override
    public List<ErpLeaseContractDO> getLeaseContractList() {
        return leaseContractMapper.selectList();
    }

}
