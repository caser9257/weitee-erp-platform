package cn.weitee.erp.module.infra.service.file;

import cn.weitee.erp.module.infra.controller.admin.file.vo.tag.FileTagSaveReqVO;
import cn.weitee.erp.module.infra.dal.dataobject.file.FileTagDO;
import cn.weitee.erp.module.infra.dal.dataobject.file.FileTagRelDO;
import cn.weitee.erp.module.infra.dal.mysql.file.FileTagMapper;
import cn.weitee.erp.module.infra.dal.mysql.file.FileTagRelMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件标签 Service 实现
 *
 * @author weitee
 */
@Service
@Validated
@Slf4j
public class FileTagServiceImpl implements FileTagService {

    @Resource
    private FileTagMapper fileTagMapper;

    @Resource
    private FileTagRelMapper fileTagRelMapper;

    @Override
    public Long createFileTag(FileTagSaveReqVO reqVO) {
        FileTagDO tag = new FileTagDO();
        tag.setName(reqVO.getName());
        tag.setColor(reqVO.getColor());
        tag.setIcon(reqVO.getIcon());
        tag.setSort(reqVO.getSort() != null ? reqVO.getSort() : 0);
        fileTagMapper.insert(tag);
        return tag.getId();
    }

    @Override
    public void updateFileTag(FileTagSaveReqVO reqVO) {
        FileTagDO tag = fileTagMapper.selectById(reqVO.getId());
        if (tag == null) {
            log.warn("[updateFileTag] 标签不存在：{}", reqVO.getId());
            return;
        }
        tag.setName(reqVO.getName());
        tag.setColor(reqVO.getColor());
        tag.setIcon(reqVO.getIcon());
        tag.setSort(reqVO.getSort());
        fileTagMapper.updateById(tag);
    }

    @Override
    public void deleteFileTag(Long id) {
        fileTagMapper.deleteById(id);
        // 同时删除该标签的所有关联关绯?
        fileTagRelMapper.deleteByTagId(id);
    }

    @Override
    public List<FileTagDO> getFileTagList() {
        return fileTagMapper.selectList();
    }

    @Override
    public void addFileTag(Long fileId, Long tagId) {
        // 检查是否已存在
        List<FileTagRelDO> existing = fileTagRelMapper.selectListByFileId(fileId);
        boolean exists = existing.stream().anyMatch(rel -> rel.getTagId().equals(tagId));
        if (exists) {
            log.debug("[addFileTag] 文件标签关联已存在，fileId={}, tagId={}", fileId, tagId);
            return;
        }

        // 创建关联
        FileTagRelDO rel = new FileTagRelDO();
        rel.setFileId(fileId);
        rel.setTagId(tagId);
        fileTagRelMapper.insert(rel);
        log.info("[addFileTag] 添加文件标签关联，fileId={}, tagId={}", fileId, tagId);
    }

    @Override
    public void removeFileTag(Long fileId, Long tagId) {
        fileTagRelMapper.deleteByFileIdAndTagId(fileId, tagId);
        log.info("[removeFileTag] 移除文件标签关联，fileId={}, tagId={}", fileId, tagId);
    }

    @Override
    public List<FileTagDO> getFileTags(Long fileId) {
        List<FileTagRelDO> rels = fileTagRelMapper.selectListByFileId(fileId);
        if (rels.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> tagIds = rels.stream()
                .map(FileTagRelDO::getTagId)
                .collect(Collectors.toList());
        return fileTagMapper.selectBatchIds(tagIds);
    }

}
