package cn.weitee.erp.module.project.service.file;

import cn.weitee.erp.module.project.controller.admin.vo.file.ProjectFileSaveReqVO;
import cn.weitee.erp.module.project.dal.dataobject.file.ProjectFileDO;

import jakarta.validation.Valid;
import java.util.List;

public interface ProjectFileService {
    Long uploadFile(@Valid ProjectFileSaveReqVO createReqVO);
    void deleteFile(Long id);
    ProjectFileDO getFile(Long id);
    List<ProjectFileDO> getFileList();
}
