package cn.weitee.erp.module.system.dal.mysql.dept;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.system.dal.dataobject.dept.UserPostDO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface UserPostMapper extends BaseMapperX<UserPostDO> {

    default List<UserPostDO> selectListByUserId(Long userId) {
        return selectList(UserPostDO::getUserId, userId);
    }

    default List<UserPostDO> selectListByUserIds(Collection<Long> userIds) {
        return selectList(new LambdaQueryWrapperX<UserPostDO>().inIfPresent(UserPostDO::getUserId, userIds));
    }

    default void deleteByUserIdAndPostId(Long userId, Collection<Long> postIds) {
        delete(new LambdaQueryWrapperX<UserPostDO>()
                .eq(UserPostDO::getUserId, userId)
                .in(UserPostDO::getPostId, postIds));
    }

    default List<UserPostDO> selectListByPostIds(Collection<Long> postIds) {
        return selectList(UserPostDO::getPostId, postIds);
    }

    default List<UserPostDO> selectListByPostId(Long postId) {
        return selectList(UserPostDO::getPostId, postId);
    }

    default void deleteByPostId(Long postId) {
        delete(Wrappers.lambdaUpdate(UserPostDO.class).eq(UserPostDO::getPostId, postId));
    }

    default void deleteByUserId(Long userId) {
        delete(Wrappers.lambdaUpdate(UserPostDO.class).eq(UserPostDO::getUserId, userId));
    }
}
