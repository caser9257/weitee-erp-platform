package cn.iocoder.yudao.module.bpm.service.approval;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.delegation.BpmApprovalDelegationPageReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.delegation.BpmApprovalDelegationRespVO;
import cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.delegation.BpmApprovalDelegationSaveReqVO;

import java.util.List;

/**
 * 审批委托 Service 接口
 */
public interface BpmApprovalDelegationService {

    /**
     * 创建委托
     *
     * @param reqVO 委托信息
     * @return 委托 ID
     */
    Long createDelegation(BpmApprovalDelegationSaveReqVO reqVO);

    /**
     * 更新委托
     *
     * @param reqVO 委托信息
     */
    void updateDelegation(BpmApprovalDelegationSaveReqVO reqVO);

    /**
     * 删除委托
     *
     * @param id 委托 ID
     */
    void deleteDelegation(Long id);

    /**
     * 获取委托详情
     *
     * @param id 委托 ID
     * @return 委托详情
     */
    BpmApprovalDelegationRespVO getDelegation(Long id);

    /**
     * 获取委托分页列表
     *
     * @param reqVO 分页参数
     * @return 委托列表
     */
    PageResult<BpmApprovalDelegationRespVO> getDelegationPage(BpmApprovalDelegationPageReqVO reqVO);

    /**
     * 获取用户的有效委托列表
     *
     * @param userId 用户 ID
     * @return 委托列表
     */
    List<BpmApprovalDelegationRespVO> getValidDelegations(Long userId);

    /**
     * 获取用户的代理人
     *
     * @param userId    用户 ID
     * @param sceneCode 场景编码
     * @return 代理人 ID，如果没有委托则返回 null
     */
    Long getDelegateUserId(Long userId, String sceneCode);

}
