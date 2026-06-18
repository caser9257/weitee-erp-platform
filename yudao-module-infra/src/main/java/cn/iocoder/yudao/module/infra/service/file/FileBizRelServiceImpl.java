package cn.iocoder.yudao.module.infra.service.file;

import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileBizRelDO;
import cn.iocoder.yudao.module.infra.dal.mysql.file.FileBizRelMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 文件-业务关联 Service 实现
 *
 * @author ruoyi-vue-pro
 */
@Service
@Validated
@Slf4j
public class FileBizRelServiceImpl implements FileBizRelService {

    @Resource
    private FileBizRelMapper fileBizRelMapper;

    @Override
    public Long createFileBizRel(Long fileId, String bizType, Long bizId, String bizNo) {
        // 检查是否已存在
        List<FileBizRelDO> existing = fileBizRelMapper.selectListByBiz(bizType, bizId);
        boolean exists = existing.stream().anyMatch(rel -> rel.getFileId().equals(fileId));
        if (exists) {
            log.debug("[createFileBizRel] 文件关联已存在，fileId={}, bizType={}, bizId={}", fileId, bizType, bizId);
            return existing.stream().filter(rel -> rel.getFileId().equals(fileId))
                    .findFirst().map(FileBizRelDO::getId).orElse(null);
        }

        FileBizRelDO rel = new FileBizRelDO();
        rel.setFileId(fileId);
        rel.setBizType(bizType);
        rel.setBizId(bizId);
        rel.setBizNo(bizNo);
        fileBizRelMapper.insert(rel);
        log.info("[createFileBizRel] 创建文件关联，fileId={}, bizType={}, bizId={}", fileId, bizType, bizId);
        return rel.getId();
    }

    @Override
    public void deleteFileBizRel(Long fileId, String bizType, Long bizId) {
        fileBizRelMapper.delete(new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<FileBizRelDO>()
                .eq(FileBizRelDO::getFileId, fileId)
                .eq(FileBizRelDO::getBizType, bizType)
                .eq(FileBizRelDO::getBizId, bizId));
        log.info("[deleteFileBizRel] 删除文件关联，fileId={}, bizType={}, bizId={}", fileId, bizType, bizId);
    }

    @Override
    public List<FileBizRelDO> getFileBizRelList(String bizType, Long bizId) {
        return fileBizRelMapper.selectListByBiz(bizType, bizId);
    }

    @Override
    public List<FileBizRelDO> getFileBizRelListByFileId(Long fileId) {
        return fileBizRelMapper.selectListByFileId(fileId);
    }

    @Override
    public void deleteAllByBiz(String bizType, Long bizId) {
        fileBizRelMapper.deleteByBiz(bizType, bizId);
        log.info("[deleteAllByBiz] 删除业务单据所有文件关联，bizType={}, bizId={}", bizType, bizId);
    }

}
