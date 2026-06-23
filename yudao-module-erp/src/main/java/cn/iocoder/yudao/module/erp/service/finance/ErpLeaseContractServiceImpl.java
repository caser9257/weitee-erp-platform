package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.lease.ErpLeaseContractPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.lease.ErpLeaseContractSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpLeaseContractApprovalDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpLeaseContractDO;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpLeaseContractApprovalMapper;
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

    @Resource
    private ErpLeaseContractApprovalMapper leaseContractApprovalMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createLeaseContract(ErpLeaseContractSaveReqVO reqVO) {
        ErpLeaseContractDO contract = BeanUtils.toBean(reqVO, ErpLeaseContractDO.class);
        contract.setStatus(0); // 草稿
        leaseContractMapper.insert(contract);
        log.info("[createLeaseContract] 创建租赁合同，id={}, no={}", contract.getId(), contract.getNo());
        return contract.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLeaseContract(ErpLeaseContractSaveReqVO reqVO) {
        ErpLeaseContractDO contract = leaseContractMapper.selectById(reqVO.getId());
        if (contract == null) {
            throw exception(LEASE_CONTRACT_NOT_EXISTS);
        }
        // 只有草稿状态才能编辑
        if (contract.getStatus() != 0) {
            throw exception(LEASE_CONTRACT_STATUS_INVALID);
        }
        BeanUtils.copyProperties(reqVO, contract);
        leaseContractMapper.updateById(contract);
        log.info("[updateLeaseContract] 更新租赁合同，id={}, no={}", contract.getId(), contract.getNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLeaseContract(Long id) {
        ErpLeaseContractDO contract = leaseContractMapper.selectById(id);
        if (contract == null) {
            throw exception(LEASE_CONTRACT_NOT_EXISTS);
        }
        // 只有草稿状态才能删除
        if (contract.getStatus() != 0) {
            throw exception(LEASE_CONTRACT_STATUS_INVALID);
        }
        leaseContractMapper.deleteById(id);
        log.info("[deleteLeaseContract] 删除租赁合同，id={}, no={}", id, contract.getNo());
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitApproval(Long id) {
        // 1. 校验合同存在
        ErpLeaseContractDO contract = leaseContractMapper.selectById(id);
        if (contract == null) {
            throw exception(LEASE_CONTRACT_NOT_EXISTS);
        }
        // 2. 校验状态：只有草稿(0)才能提交审批
        if (contract.getStatus() != 0) {
            throw exception(LEASE_CONTRACT_STATUS_INVALID);
        }
        // 3. 更新状态为审批中(5)
        Integer oldStatus = contract.getStatus();
        contract.setStatus(5);
        leaseContractMapper.updateById(contract);

        // 4. 写入审批记录
        saveApprovalRecord(id, contract.getNo(), "SUBMIT", oldStatus, 5, null);
        log.info("[submitApproval] 租赁合同已提交审批：{}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id, String remark) {
        // 1. 校验合同存在
        ErpLeaseContractDO contract = leaseContractMapper.selectById(id);
        if (contract == null) {
            throw exception(LEASE_CONTRACT_NOT_EXISTS);
        }
        // 2. 校验状态：只有审批中(5)才能审批通过
        if (contract.getStatus() != 5) {
            throw exception(LEASE_CONTRACT_STATUS_INVALID);
        }
        // 3. 更新状态为生效(10)
        Integer oldStatus = contract.getStatus();
        contract.setStatus(10);
        leaseContractMapper.updateById(contract);

        // 4. 写入审批记录
        saveApprovalRecord(id, contract.getNo(), "APPROVE", oldStatus, 10, remark);
        log.info("[approve] 租赁合同已审批通过：{}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long id, String remark) {
        // 1. 校验合同存在
        ErpLeaseContractDO contract = leaseContractMapper.selectById(id);
        if (contract == null) {
            throw exception(LEASE_CONTRACT_NOT_EXISTS);
        }
        // 2. 校验状态：只有审批中(5)才能驳回
        if (contract.getStatus() != 5) {
            throw exception(LEASE_CONTRACT_STATUS_INVALID);
        }
        // 3. 更新状态为草稿(0)
        Integer oldStatus = contract.getStatus();
        contract.setStatus(0);
        leaseContractMapper.updateById(contract);

        // 4. 写入审批记录
        saveApprovalRecord(id, contract.getNo(), "REJECT", oldStatus, 0, remark);
        log.info("[reject] 租赁合同已驳回：{}", id);
    }

    /**
     * 保存审批记录
     */
    private void saveApprovalRecord(Long contractId, String contractNo, String action,
                                     Integer statusBefore, Integer statusAfter, String remark) {
        ErpLeaseContractApprovalDO approval = ErpLeaseContractApprovalDO.builder()
                .leaseContractId(contractId)
                .leaseContractNo(contractNo)
                .action(action)
                .statusBefore(statusBefore)
                .statusAfter(statusAfter)
                .remark(remark)
                .build();
        leaseContractApprovalMapper.insert(approval);
    }

}
