package cn.iocoder.yudao.module.infra.service.file;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.folder.FileFolderSaveReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileFolderDO;
import cn.iocoder.yudao.module.infra.dal.mysql.file.FileFolderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 文件夹 Service 实现
 *
 * @author ruoyi-vue-pro
 */
@Service
@Validated
@Slf4j
public class FileFolderServiceImpl implements FileFolderService {

    @Resource
    private FileFolderMapper fileFolderMapper;

    @Override
    public Long createFileFolder(FileFolderSaveReqVO reqVO) {
        FileFolderDO folder = new FileFolderDO();
        folder.setName(reqVO.getName());
        folder.setParentId(reqVO.getParentId() != null ? reqVO.getParentId() : 0L);
        folder.setIcon(reqVO.getIcon());
        folder.setSort(reqVO.getSort() != null ? reqVO.getSort() : 0);
        folder.setStatus(0);
        folder.setRemark(reqVO.getRemark());

        // 构建路径（处理特殊字符）
        String safeName = reqVO.getName().replaceAll("[/\\\\:*?\"<>|]", "_");
        if (folder.getParentId() == 0) {
            folder.setPath("/" + safeName);
        } else {
            FileFolderDO parent = fileFolderMapper.selectById(folder.getParentId());
            if (parent != null) {
                folder.setPath(parent.getPath() + "/" + safeName);
            } else {
                folder.setPath("/" + safeName);
            }
        }

        fileFolderMapper.insert(folder);
        return folder.getId();
    }

    @Override
    public void updateFileFolder(FileFolderSaveReqVO reqVO) {
        FileFolderDO folder = fileFolderMapper.selectById(reqVO.getId());
        if (folder == null) {
            log.warn("[updateFileFolder] 文件夹不存在：{}", reqVO.getId());
            return;
        }
        folder.setName(reqVO.getName());
        folder.setIcon(reqVO.getIcon());
        folder.setSort(reqVO.getSort());
        folder.setRemark(reqVO.getRemark());
        fileFolderMapper.updateById(folder);
    }

    @Override
    public void deleteFileFolder(Long id) {
        fileFolderMapper.deleteById(id);
    }

    @Override
    public FileFolderDO getFileFolder(Long id) {
        return fileFolderMapper.selectById(id);
    }

    @Override
    public List<FileFolderDO> getFileFolderList() {
        return fileFolderMapper.selectList();
    }

    @Override
    public List<FileFolderDO> getFileFolderTree() {
        List<FileFolderDO> allFolders = fileFolderMapper.selectList();
        return buildTree(allFolders, 0L);
    }

    private List<FileFolderDO> buildTree(List<FileFolderDO> allFolders, Long parentId) {
        List<FileFolderDO> tree = new ArrayList<>();
        for (FileFolderDO folder : allFolders) {
            if (parentId.equals(folder.getParentId())) {
                // 递归构建子节点
                List<FileFolderDO> children = buildTree(allFolders, folder.getId());
                folder.setChildren(children);
                tree.add(folder);
            }
        }
        return tree;
    }

}
