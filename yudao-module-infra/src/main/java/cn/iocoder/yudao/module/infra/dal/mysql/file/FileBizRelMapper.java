package cn.iocoder.yudao.module.infra.dal.mysql.file;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileBizRelDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 文件-业务关联 Mapper
 *
 * @author ruoyi-vue-pro
 */
@Mapper
public interface FileBizRelMapper extends BaseMapperX<FileBizRelDO> {

    default List<FileBizRelDO> selectListByBiz(String bizType, Long bizId) {
        return selectList(new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<FileBizRelDO>()
                .eq(FileBizRelDO::getBizType, bizType)
                .eq(FileBizRelDO::getBizId, bizId));
    }

    default List<FileBizRelDO> selectListByFileId(Long fileId) {
        return selectList(FileBizRelDO::getFileId, fileId);
    }

    default int deleteByBiz(String bizType, Long bizId) {
        return delete(new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<FileBizRelDO>()
                .eq(FileBizRelDO::getBizType, bizType)
                .eq(FileBizRelDO::getBizId, bizId));
    }

}
