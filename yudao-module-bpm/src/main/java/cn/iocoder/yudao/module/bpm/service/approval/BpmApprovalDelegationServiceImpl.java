package cn.iocoder.yudao.module.bpm.service.approval;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.delegation.BpmApprovalDelegationPageReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.delegation.BpmApprovalDelegationRespVO;
import cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.delegation.BpmApprovalDelegationSaveReqVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.approval.BpmApprovalDelegationDO;
import cn.iocoder.yudao.module.bpm.dal.mysql.approval.BpmApprovalDelegationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.Date;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.APPROVAL_DELEGATION_NOT_EXISTS;

/**
 * 审批委托 Service 实现类
 */
@Service
@Validated
@Slf4j
public class BpmApprovalDelegationServiceImpl implements BpmApprovalDelegationService {

    @Resource
    private BpmApprovalDelegationMapper approvalDelegationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDelegation(BpmApprovalDelegationSaveReqVO reqVO) {
        BpmApprovalDelegationDO delegation = BeanUtils.toBean(reqVO, BpmApprovalDelegationDO.class);
        delegation.setStatus(0); // 正常状态
        approvalDelegationMapper.insert(delegation);
        return delegation.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDelegation(BpmApprovalDelegationSaveReqVO reqVO) {
        validateDelegationExists(reqVO.getId());
        BpmApprovalDelegationDO delegation = BeanUtils.toBean(reqVO, BpmApprovalDelegationDO.class);
        approvalDelegationMapper.updateById(delegation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDelegation(Long id) {
        validateDelegationExists(id);
        approvalDelegationMapper.deleteById(id);
    }

    @Override
    public BpmApprovalDelegationRespVO getDelegation(Long id) {
        BpmApprovalDelegationDO delegation = approvalDelegationMapper.selectById(id);
        if (delegation == null) {
            throw exception(APPROVAL_DELEGATION_NOT_EXISTS);
        }
        return BeanUtils.toBean(delegation, BpmApprovalDelegationRespVO.class);
    }

    @Override
    public PageResult<BpmApprovalDelegationRespVO> getDelegationPage(BpmApprovalDelegationPageReqVO reqVO) {
        PageResult<BpmApprovalDelegationDO> pageResult = approvalDelegationMapper.selectPage(reqVO, new LambdaQueryWrapperX<BpmApprovalDelegationDO>()
                .eqIfPresent(BpmApprovalDelegationDO::getUserId, reqVO.getUserId())
                .eqIfPresent(BpmApprovalDelegationDO::getDelegateUserId, reqVO.getDelegateUserId())
                .eqIfPresent(BpmApprovalDelegationDO::getStatus, reqVO.getStatus())
                .orderByDesc(BpmApprovalDelegationDO::getCreateTime));
        return BeanUtils.toBean(pageResult, BpmApprovalDelegationRespVO.class);
    }

    @Override
    public List<BpmApprovalDelegationRespVO> getValidDelegations(Long userId) {
        List<BpmApprovalDelegationDO> delegations = approvalDelegationMapper.selectListByUserId(userId, new Date());
        return BeanUtils.toBean(delegations, BpmApprovalDelegationRespVO.class);
    }

    @Override
    public Long getDelegateUserId(Long userId, String sceneCode) {
        List<BpmApprovalDelegationDO> delegations = approvalDelegationMapper.selectListByUserIdAndSceneCode(userId, sceneCode, new Date());
        if (delegations.isEmpty()) {
            return null;
        }
        // 优先返回场景精确匹配的委托，其次返回通配（sceneCode 为空）的委托
        for (BpmApprovalDelegationDO delegation : delegations) {
            if (sceneCode.equals(delegation.getSceneCode())) {
                return delegation.getDelegateUserId();
            }
        }
        // 无精确匹配，返回第一条（通配委托）
        return delegations.get(0).getDelegateUserId();
    }

    private void validateDelegationExists(Long id) {
        if (approvalDelegationMapper.selectById(id) == null) {
            throw exception(APPROVAL_DELEGATION_NOT_EXISTS);
        }
    }

}
