package cn.weitee.erp.module.bpm.service.definition;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.bpm.controller.admin.definition.vo.group.BpmUserGroupPageReqVO;
import cn.weitee.erp.module.bpm.controller.admin.definition.vo.group.BpmUserGroupSaveReqVO;
import cn.weitee.erp.module.bpm.dal.dataobject.definition.BpmUserGroupDO;
import cn.weitee.erp.module.bpm.dal.mysql.definition.BpmUserGroupMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.weitee.erp.module.bpm.enums.ErrorCodeConstants.USER_GROUP_IS_DISABLE;
import static cn.weitee.erp.module.bpm.enums.ErrorCodeConstants.USER_GROUP_NOT_EXISTS;

/**
 * 用户组 Service 实现类
 *
 * @author WeTai
 */
@Service
@Validated
public class BpmUserGroupServiceImpl implements BpmUserGroupService {

    @Resource
    private BpmUserGroupMapper bpmUserGroupMapper;

    @Override
    public Long createUserGroup(BpmUserGroupSaveReqVO createReqVO) {
        BpmUserGroupDO userGroup = BeanUtils.toBean(createReqVO, BpmUserGroupDO.class);
        bpmUserGroupMapper.insert(userGroup);
        return userGroup.getId();
    }

    @Override
    public void updateUserGroup(BpmUserGroupSaveReqVO updateReqVO) {
        // 校验存在
        validateUserGroupExists(updateReqVO.getId());
        // 更新
        BpmUserGroupDO updateObj = BeanUtils.toBean(updateReqVO, BpmUserGroupDO.class);
        bpmUserGroupMapper.updateById(updateObj);
    }

    @Override
    public void deleteUserGroup(Long id) {
        // 校验存在
        this.validateUserGroupExists(id);
        // 删除
        bpmUserGroupMapper.deleteById(id);
    }

    private void validateUserGroupExists(Long id) {
        if (bpmUserGroupMapper.selectById(id) == null) {
            throw exception(USER_GROUP_NOT_EXISTS);
        }
    }

    @Override
    public BpmUserGroupDO getUserGroup(Long id) {
        return bpmUserGroupMapper.selectById(id);
    }

    @Override
    public List<BpmUserGroupDO> getUserGroupList(Collection<Long> ids) {
        return bpmUserGroupMapper.selectByIds(ids);
    }


    @Override
    public List<BpmUserGroupDO> getUserGroupListByStatus(Integer status) {
        return bpmUserGroupMapper.selectListByStatus(status);
    }

    @Override
    public PageResult<BpmUserGroupDO> getUserGroupPage(BpmUserGroupPageReqVO pageReqVO) {
        return bpmUserGroupMapper.selectPage(pageReqVO);
    }

    @Override
    public void validUserGroups(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 获得用户组信息
        List<BpmUserGroupDO> userGroups = bpmUserGroupMapper.selectByIds(ids);
        Map<Long, BpmUserGroupDO> userGroupMap = convertMap(userGroups, BpmUserGroupDO::getId);
        // 校验
        ids.forEach(id -> {
            BpmUserGroupDO userGroup = userGroupMap.get(id);
            if (userGroup == null) {
                throw exception(USER_GROUP_NOT_EXISTS);
            }
            if (!CommonStatusEnum.ENABLE.getStatus().equals(userGroup.getStatus())) {
                throw exception(USER_GROUP_IS_DISABLE, userGroup.getName());
            }
        });
    }

}
