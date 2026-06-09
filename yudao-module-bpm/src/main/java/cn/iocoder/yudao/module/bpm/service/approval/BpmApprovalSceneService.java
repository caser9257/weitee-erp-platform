package cn.iocoder.yudao.module.bpm.service.approval;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.scene.BpmApprovalScenePageReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.scene.BpmApprovalSceneRespVO;
import cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.scene.BpmApprovalSceneSaveReqVO;

import jakarta.validation.Valid;

/**
 * 审批场景 Service 接口
 */
public interface BpmApprovalSceneService {

    /**
     * 获取审批场景分页
     */
    PageResult<BpmApprovalSceneRespVO> getScenePage(BpmApprovalScenePageReqVO pageReqVO);

    /**
     * 获取审批场景详情
     */
    BpmApprovalSceneRespVO getScene(Long id);

    /**
     * 根据场景编码获取审批场景
     */
    BpmApprovalSceneRespVO getSceneByCode(String sceneCode);

    /**
     * 创建审批场景
     */
    Long createScene(@Valid BpmApprovalSceneSaveReqVO createReqVO);

    /**
     * 更新审批场景
     */
    void updateScene(@Valid BpmApprovalSceneSaveReqVO updateReqVO);

    /**
     * 删除审批场景
     */
    void deleteScene(Long id);

    /**
     * 更新审批场景状态（启用/禁用）
     *
     * @param id     场景编号
     * @param status 目标状态：1-启用 0-禁用
     */
    void updateSceneStatus(Long id, Integer status);

    /**
     * 绑定审批方案到场景
     *
     * @param id            场景编号
     * @param activeSchemeId 生效方案编号，传 null 表示解绑
     */
    void bindScheme(Long id, Long activeSchemeId);

}
