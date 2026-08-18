package cn.weitee.erp.module.bpm.service.approval.generic;

import cn.weitee.erp.module.bpm.controller.admin.approval.vo.scene.BpmApprovalSceneRespVO;
import cn.weitee.erp.module.bpm.service.approval.BpmApprovalSceneService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

/**
 * 通用审批配置服务
 *
 * 统一按场景编码解析 generic_config，供提交服务 / 结果处理器 / 上下文提供者复用。
 */
@Service
@Slf4j
public class GenericApprovalConfigService {

    @Resource
    private BpmApprovalSceneService approvalSceneService;

    /**
     * 按场景编码解析通用审批配置
     *
     * @param sceneCode 场景编码
     * @return 配置；场景不存在 / 未启用通用接入 / 配置非法时返回 null
     */
    public GenericApprovalConfig getConfig(String sceneCode) {
        try {
            BpmApprovalSceneRespVO scene = approvalSceneService.getSceneByCode(sceneCode);
            if (scene == null) {
                return null;
            }
            return GenericApprovalConfig.parse(scene.getGenericConfig());
        } catch (Exception e) {
            log.warn("[getConfig] 通用审批配置解析失败，sceneCode={}, msg={}", sceneCode, e.getMessage());
            return null;
        }
    }

    /**
     * 判断场景是否启用通用审批接入
     */
    public boolean isGenericEnabled(String sceneCode) {
        return getConfig(sceneCode) != null;
    }

}
