package cn.iocoder.yudao.module.bpm.dal.mysql.approval;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.bpm.dal.dataobject.approval.BpmApprovalDelegationDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.List;

/**
 * 审批委托 Mapper
 */
@Mapper
public interface BpmApprovalDelegationMapper extends BaseMapperX<BpmApprovalDelegationDO> {

    /**
     * 查询用户的有效委托列表
     *
     * @param userId 用户 ID
     * @param now    当前时间
     * @return 委托列表
     */
    default List<BpmApprovalDelegationDO> selectListByUserId(Long userId, Date now) {
        return selectList(new LambdaQueryWrapperX<BpmApprovalDelegationDO>()
                .eq(BpmApprovalDelegationDO::getUserId, userId)
                .eq(BpmApprovalDelegationDO::getStatus, 0)
                .le(BpmApprovalDelegationDO::getStartTime, now)
                .ge(BpmApprovalDelegationDO::getEndTime, now)
                .orderByDesc(BpmApprovalDelegationDO::getCreateTime));
    }

    /**
     * 查询用户在指定场景的有效委托
     *
     * @param userId    用户 ID
     * @param sceneCode 场景编码
     * @param now       当前时间
     * @return 委托列表
     */
    default List<BpmApprovalDelegationDO> selectListByUserIdAndSceneCode(Long userId, String sceneCode, Date now) {
        return selectList(new LambdaQueryWrapperX<BpmApprovalDelegationDO>()
                .eq(BpmApprovalDelegationDO::getUserId, userId)
                .eq(BpmApprovalDelegationDO::getStatus, 0)
                .le(BpmApprovalDelegationDO::getStartTime, now)
                .ge(BpmApprovalDelegationDO::getEndTime, now)
                .and(w -> w.isNull(BpmApprovalDelegationDO::getSceneCode)
                        .or()
                        .eq(BpmApprovalDelegationDO::getSceneCode, sceneCode))
                .orderByDesc(BpmApprovalDelegationDO::getCreateTime));
    }

}
