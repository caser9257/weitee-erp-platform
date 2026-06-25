package cn.weitee.erp.module.bpm.service.approval;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.framework.security.core.util.SecurityFrameworkUtils;
import cn.weitee.erp.module.bpm.controller.admin.approval.vo.scene.BpmApprovalScenePageReqVO;
import cn.weitee.erp.module.bpm.controller.admin.approval.vo.scene.BpmApprovalSceneRespVO;
import cn.weitee.erp.module.bpm.controller.admin.approval.vo.scene.BpmApprovalSceneSaveReqVO;
import cn.weitee.erp.module.bpm.dal.dataobject.approval.BpmApprovalSceneDO;
import cn.weitee.erp.module.bpm.dal.mysql.approval.BpmApprovalSceneMapper;
import cn.weitee.erp.module.bpm.dal.mysql.approval.BpmApprovalSchemeMapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.bpm.enums.ErrorCodeConstants.*;

/**
 * 审批场景 Service 实现类
 */
@Service
@Validated
public class BpmApprovalSceneServiceImpl implements BpmApprovalSceneService {

    @Resource
    private BpmApprovalSceneMapper approvalSceneMapper;

    @Resource
    private BpmApprovalSchemeMapper approvalSchemeMapper;

    @Override
    public PageResult<BpmApprovalSceneRespVO> getScenePage(BpmApprovalScenePageReqVO pageReqVO) {
        PageResult<BpmApprovalSceneDO> pageResult = approvalSceneMapper.selectPage(pageReqVO);
        return BeanUtils.toBean(pageResult, BpmApprovalSceneRespVO.class);
    }

    @Override
    public BpmApprovalSceneRespVO getScene(Long id) {
        BpmApprovalSceneDO scene = validateSceneExists(id);
        return BeanUtils.toBean(scene, BpmApprovalSceneRespVO.class);
    }

    @Override
    public BpmApprovalSceneRespVO getSceneByCode(String sceneCode) {
        BpmApprovalSceneDO scene = approvalSceneMapper.selectBySceneCode(sceneCode);
        if (scene == null) {
            throw exception(APPROVAL_SCENE_NOT_EXISTS);
        }
        return BeanUtils.toBean(scene, BpmApprovalSceneRespVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createScene(BpmApprovalSceneSaveReqVO createReqVO) {
        // 校验场景编码唯一
        validateSceneCodeUnique(null, createReqVO.getSceneCode());
        validateActiveSchemeExists(createReqVO.getActiveSchemeId());
        BpmApprovalSceneDO scene = BeanUtils.toBean(createReqVO, BpmApprovalSceneDO.class);
        // 设置归属用户ID为当前登录用户
        scene.setOwnerUserId(SecurityFrameworkUtils.getLoginUserId());
        approvalSceneMapper.insert(scene);
        return scene.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateScene(BpmApprovalSceneSaveReqVO updateReqVO) {
        BpmApprovalSceneDO scene = validateSceneExists(updateReqVO.getId());
        validateSceneOwnership(scene);
        validateSceneCodeUnique(scene.getId(), updateReqVO.getSceneCode());
        validateActiveSchemeExists(updateReqVO.getActiveSchemeId());
        BpmApprovalSceneDO updateObj = BeanUtils.toBean(updateReqVO, BpmApprovalSceneDO.class);
        approvalSceneMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteScene(Long id) {
        BpmApprovalSceneDO scene = validateSceneExists(id);
        validateSceneOwnership(scene);
        approvalSceneMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSceneStatus(Long id, Integer status) {
        BpmApprovalSceneDO scene = validateSceneExists(id);
        validateSceneOwnership(scene);
        approvalSceneMapper.updateById(new BpmApprovalSceneDO()
                .setId(id)
                .setStatus(status));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindScheme(Long id, Long activeSchemeId) {
        BpmApprovalSceneDO scene = validateSceneExists(id);
        validateSceneOwnership(scene);
        validateActiveSchemeExists(activeSchemeId);
        // 使用 UpdateWrapper 确保可以更新为 NULL（解绑场景）
        approvalSceneMapper.update(null, new LambdaUpdateWrapper<BpmApprovalSceneDO>()
                .eq(BpmApprovalSceneDO::getId, id)
                .set(BpmApprovalSceneDO::getActiveSchemeId, activeSchemeId));
    }

    private BpmApprovalSceneDO validateSceneExists(Long id) {
        BpmApprovalSceneDO scene = approvalSceneMapper.selectById(id);
        if (scene == null) {
            throw exception(APPROVAL_SCENE_NOT_EXISTS);
        }
        return scene;
    }

    /**
     * 校验当前用户是否有权限操作该场景
     * 
     * 流程配置管理员只能操作自己配置的场景
     * 
     * @param scene 审批场景
     */
    private void validateSceneOwnership(BpmApprovalSceneDO scene) {
        Long currentUserId = SecurityFrameworkUtils.getLoginUserId();
        // 如果 ownerUserId 为空或者是当前用户，则允许操作
        if (scene.getOwnerUserId() == null || scene.getOwnerUserId().equals(currentUserId)) {
            return;
        }
        // 如果用户是流程管理员角色，则允许操作
        if (isBpmAdmin(currentUserId)) {
            return;
        }
        throw exception(APPROVAL_SCENE_NO_PERMISSION);
    }

    /**
     * 判断用户是否为流程管理员
     */
    private boolean isBpmAdmin(Long userId) {
        // TODO: 调用权限服务判断用户是否为流程管理员角色
        // 这里需要根据实际情况实现
        return false;
    }

    private void validateSceneCodeUnique(Long id, String sceneCode) {
        BpmApprovalSceneDO scene = approvalSceneMapper.selectBySceneCode(sceneCode);
        if (scene != null && !scene.getId().equals(id)) {
            throw exception(APPROVAL_SCENE_CODE_DUPLICATE, sceneCode);
        }
    }

    /**
     * 校验生效方案是否存在（若指定了 activeSchemeId）
     *
     * @param activeSchemeId 生效方案编号，可为 null
     */
    private void validateActiveSchemeExists(Long activeSchemeId) {
        if (activeSchemeId == null) {
            return;
        }
        if (approvalSchemeMapper.selectById(activeSchemeId) == null) {
            throw exception(APPROVAL_SCHEME_NOT_EXISTS);
        }
    }

}
