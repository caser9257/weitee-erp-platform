package cn.weitee.erp.module.infra.service.file;

import cn.weitee.erp.module.infra.dal.dataobject.file.FileBizRelDO;

import java.util.List;

/**
 * 文件-业务关联 Service 接口
 *
 * @author ruoyi-vue-pro
 */
public interface FileBizRelService {

    /**
     * 创建文件-业务关联
     */
    Long createFileBizRel(Long fileId, String bizType, Long bizId, String bizNo);

    /**
     * 删除文件-业务关联
     */
    void deleteFileBizRel(Long fileId, String bizType, Long bizId);

    /**
     * 获取业务单据关联的文件列表
     */
    List<FileBizRelDO> getFileBizRelList(String bizType, Long bizId);

    /**
     * 获取文件关联的业务单据列表
     */
    List<FileBizRelDO> getFileBizRelListByFileId(Long fileId);

    /**
     * 删除业务单据的所有文件关联
     */
    void deleteAllByBiz(String bizType, Long bizId);

}
