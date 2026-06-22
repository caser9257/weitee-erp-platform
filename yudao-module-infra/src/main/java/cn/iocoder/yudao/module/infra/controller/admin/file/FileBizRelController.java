package cn.iocoder.yudao.module.infra.controller.admin.file;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.FileBizRelRespVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileBizRelDO;
import cn.iocoder.yudao.module.infra.service.file.FileBizRelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 文件业务关联")
@RestController
@RequestMapping("/infra/file-biz-rel")
@Validated
@Slf4j
public class FileBizRelController {

    @Resource
    private FileBizRelService fileBizRelService;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('infra:file:create')")
    @Operation(summary = "创建文件业务关联")
    public CommonResult<Long> createFileBizRel(
            @RequestParam("fileId") Long fileId,
            @RequestParam("bizType") String bizType,
            @RequestParam("bizId") Long bizId,
            @RequestParam(value = "bizNo", required = false) String bizNo) {
        return success(fileBizRelService.createFileBizRel(fileId, bizType, bizId, bizNo));
    }

    @DeleteMapping("/delete")
    @PreAuthorize("@ss.hasPermission('infra:file:delete')")
    @Operation(summary = "删除文件业务关联")
    public CommonResult<Boolean> deleteFileBizRel(
            @RequestParam("fileId") Long fileId,
            @RequestParam("bizType") String bizType,
            @RequestParam("bizId") Long bizId) {
        fileBizRelService.deleteFileBizRel(fileId, bizType, bizId);
        return success(true);
    }

    @GetMapping("/list-by-biz")
    @PreAuthorize("@ss.hasPermission('infra:file:query')")
    @Operation(summary = "获取业务单据关联的文件列表")
    public CommonResult<List<FileBizRelRespVO>> getFileBizRelList(
            @RequestParam("bizType") String bizType,
            @RequestParam("bizId") Long bizId) {
        List<FileBizRelDO> list = fileBizRelService.getFileBizRelList(bizType, bizId);
        return success(BeanUtils.toBean(list, FileBizRelRespVO.class));
    }

    @GetMapping("/list-by-file")
    @PreAuthorize("@ss.hasPermission('infra:file:query')")
    @Operation(summary = "获取文件关联的业务单据列表")
    public CommonResult<List<FileBizRelRespVO>> getFileBizRelListByFileId(
            @RequestParam("fileId") Long fileId) {
        List<FileBizRelDO> list = fileBizRelService.getFileBizRelListByFileId(fileId);
        return success(BeanUtils.toBean(list, FileBizRelRespVO.class));
    }

}
