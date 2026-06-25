package cn.iocoder.yudao.module.infra.dal.mysql.file;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.file.FilePageReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 文件操作 Mapper
 *
 * @author WeTai
 */
@Mapper
public interface FileMapper extends BaseMapperX<FileDO> {

    /**
     * 分页查询正常文件（不在回收站）
     */
    default PageResult<FileDO> selectPage(FilePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<FileDO>()
                .isNull(FileDO::getDeleteTime)  // 只查询未删除的文件
                .likeIfPresent(FileDO::getName, reqVO.getName())
                .likeIfPresent(FileDO::getPath, reqVO.getPath())
                .likeIfPresent(FileDO::getType, reqVO.getType())
                .eqIfPresent(FileDO::getFolderId, reqVO.getFolderId())
                .betweenIfPresent(FileDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(FileDO::getId));
    }

    /**
     * 分页查询回收站文件（已删除的文件）
     */
    default PageResult<FileDO> selectRecyclePage(FilePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<FileDO>()
                .isNotNull(FileDO::getDeleteTime)  // 只查询已删除的文件
                .likeIfPresent(FileDO::getName, reqVO.getName())
                .likeIfPresent(FileDO::getPath, reqVO.getPath())
                .likeIfPresent(FileDO::getType, reqVO.getType())
                .betweenIfPresent(FileDO::getDeleteTime, reqVO.getCreateTime())  // 复用创建时间作为删除时间筛选
                .orderByDesc(FileDO::getDeleteTime));
    }

    /**
     * 获取所有回收站文件
     */
    default List<FileDO> selectRecycleFiles() {
        return selectList(new LambdaQueryWrapperX<FileDO>()
                .isNotNull(FileDO::getDeleteTime)
                .orderByDesc(FileDO::getDeleteTime));
    }

}
