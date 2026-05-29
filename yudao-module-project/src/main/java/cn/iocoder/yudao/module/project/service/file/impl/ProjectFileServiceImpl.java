package cn.iocoder.yudao.module.project.service.file.impl;

import cn.iocoder.yudao.module.project.controller.admin.vo.file.ProjectFileSaveReqVO;
import cn.iocoder.yudao.module.project.dal.dataobject.file.ProjectFileContentDO;
import cn.iocoder.yudao.module.project.dal.dataobject.file.ProjectFileDO;
import cn.iocoder.yudao.module.project.dal.dataobject.file.ProjectFileLinkDO;
import cn.iocoder.yudao.module.project.dal.dataobject.file.ProjectFileUserDO;
import cn.iocoder.yudao.module.project.dal.mysql.file.ProjectFileContentMapper;
import cn.iocoder.yudao.module.project.dal.mysql.file.ProjectFileLinkMapper;
import cn.iocoder.yudao.module.project.dal.mysql.file.ProjectFileMapper;
import cn.iocoder.yudao.module.project.dal.mysql.file.ProjectFileUserMapper;
import cn.iocoder.yudao.module.project.service.file.ProjectFileService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.project.enums.ErrorCodeConstants.FILE_NOT_FOUND;

@Service
@Validated
public class ProjectFileServiceImpl implements ProjectFileService {

    @Resource
    private ProjectFileMapper projectFileMapper;
    @Resource
    private ProjectFileContentMapper projectFileContentMapper;
    @Resource
    private ProjectFileLinkMapper projectFileLinkMapper;
    @Resource
    private ProjectFileUserMapper projectFileUserMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long uploadFile(@Valid ProjectFileSaveReqVO createReqVO) {
        ProjectFileDO file = ProjectFileDO.builder()
                .name(createReqVO.getName())
                .ext(createReqVO.getExt())
                .size(createReqVO.getSize())
                .type(createReqVO.getType())
                .path(createReqVO.getPath())
                .url(createReqVO.getUrl())
                .build();
        projectFileMapper.insert(file);
        return file.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFile(Long id) {
        validateFile(id);
        projectFileMapper.deleteById(id);
        projectFileContentMapper.delete(ProjectFileContentDO::getFileId, id);
        projectFileLinkMapper.delete(ProjectFileLinkDO::getFileId, id);
        projectFileUserMapper.delete(ProjectFileUserDO::getFileId, id);
    }

    @Override
    public ProjectFileDO getFile(Long id) {
        return projectFileMapper.selectById(id);
    }

    @Override
    public List<ProjectFileDO> getFileList() {
        return projectFileMapper.selectList(new LambdaQueryWrapper<ProjectFileDO>()
                .orderByDesc(ProjectFileDO::getId));
    }

    private ProjectFileDO validateFile(Long id) {
        ProjectFileDO file = projectFileMapper.selectById(id);
        if (file == null) {
            throw exception(FILE_NOT_FOUND);
        }
        return file;
    }
}
