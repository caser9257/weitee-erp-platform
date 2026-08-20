package cn.weitee.erp.module.bpm.framework.flowable.core.candidate.strategy.dept;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.weitee.erp.module.bpm.framework.flowable.core.candidate.BpmTaskCandidateStrategy;
import cn.weitee.erp.module.system.api.dept.DeptApi;
import cn.weitee.erp.module.system.api.dept.dto.DeptRespDTO;
import cn.weitee.erp.module.system.api.user.AdminUserApi;
import cn.weitee.erp.module.system.api.user.dto.AdminUserRespDTO;
import cn.weitee.erp.module.system.dal.dataobject.dept.DeptDO;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 部门的负责人 {@link BpmTaskCandidateStrategy} 抽象类
 *
 * @author jason
 */
@Slf4j
public abstract class AbstractBpmTaskCandidateDeptLeaderStrategy implements BpmTaskCandidateStrategy {

    @Resource
    protected DeptApi deptApi;
    @Resource
    protected AdminUserApi adminUserApi;
    @Resource
    protected cn.weitee.erp.module.system.dal.mysql.dept.DeptMapper deptMapper;

    /**
     * 获得指定层级的部门负责人，只有第 level 的负责人
     *
     * @param dept 指定部门
     * @param level 第几级
     * @return 部门负责人的编号
     */
    protected Long getAssignLevelDeptLeaderId(DeptRespDTO dept, Integer level) {
        Assert.isTrue(level > 0, "level 必须大于 0");
        if (dept == null) {
            return null;
        }
        DeptRespDTO currentDept = dept;
        for (int i = 1; i < level; i++) {
            DeptRespDTO parentDept = deptApi.getDept(currentDept.getParentId());
            if (parentDept == null) { // 找不到父级部门，到了最高级。返回最高级的部门负责人
                break;
            }
            currentDept = parentDept;
        }
        return currentDept.getLeaderUserId();
    }

    /**
     * 获得连续层级的部门负责人，包含 [1, level] 的负责人
     *
     * @param deptIds 指定部门编号数组
     * @param level 最大层级
     * @return 连续部门负责人 Id
     */
    protected Set<Long> getMultiLevelDeptLeaderIds(List<Long> deptIds, Integer level) {
        Assert.isTrue(level > 0, "level 必须大于 0");
        if (CollUtil.isEmpty(deptIds)) {
            return new HashSet<>();
        }
        Set<Long> deptLeaderIds = new LinkedHashSet<>(); // 保证有序
        for (Long deptId : deptIds) {
            DeptRespDTO dept = deptApi.getDept(deptId);
            for (int i = 0; i < level; i++) {
                if (dept.getLeaderUserId() != null) {
                    deptLeaderIds.add(dept.getLeaderUserId());
                }
                DeptRespDTO parentDept = deptApi.getDept(dept.getParentId());
                if (parentDept == null) { // 找不到父级部门. 已经到了最高层级了
                    break;
                }
                dept = parentDept;
            }
        }
        return deptLeaderIds;
    }

    /**
     * 获取发起人的部门
     *
     * @param startUserId 发起人 Id
     */
    protected DeptRespDTO getStartUserDept(Long startUserId) {
        try {
            AdminUserRespDTO startUser = adminUserApi.getUser(startUserId);
            log.info("[getStartUserDept][startUserId={}, startUser={}]", startUserId, startUser);
            if (startUser == null) {
                return null;
            }
            if (startUser.getDeptId() == null) { // 找不到部门
                log.info("[getStartUserDept][startUserId={}, deptId=null]", startUserId);
                return null;
            }
            DeptRespDTO dept = deptApi.getDept(startUser.getDeptId());
            // fallback：deptApi.getDept 可能被数据权限拦截器过滤导致返回 null
            // 直接用 deptMapper 绕过服务层查询
            if (dept == null) {
                log.info("[getStartUserDept][deptApi.getDept(null), fallback to deptMapper]");
                cn.weitee.erp.module.system.dal.dataobject.dept.DeptDO deptDO = deptMapper.selectById(startUser.getDeptId());
                if (deptDO != null) {
                    dept = BeanUtils.toBean(deptDO, DeptRespDTO.class);
                }
            }
            log.info("[getStartUserDept][startUserId={}, deptId={}, dept={}]", startUserId, startUser.getDeptId(), dept);
            return dept;
        } catch (Exception e) {
            log.error("[getStartUserDept][startUserId={}, exception={}]", startUserId, e.getMessage(), e);
            return null;
        }
    }

}
