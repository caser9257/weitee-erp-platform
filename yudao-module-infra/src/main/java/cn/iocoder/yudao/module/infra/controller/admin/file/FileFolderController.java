package cn.iocoder.yudao.module.infra.controller.admin.file;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.folder.FileFolderRespVO;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.folder.FileFolderSaveReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileFolderDO;
import cn.iocoder.yudao.module.infra.service.file.FileFolderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 文件夹管理")
@RestController
@RequestMapping("/infra/file-folder")
@Validated
@Slf4j
public class FileFolderController {

    @Resource
    private FileFolderService fileFolderService;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('infra:file:create')")
    @Operation(summary = "创建文件夹")
    public CommonResult<Long> createFileFolder(@Valid @RequestBody FileFolderSaveReqVO reqVO) {
        return success(fileFolderService.createFileFolder(reqVO));
    }

    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('infra:file:update')")
    @Operation(summary = "更新文件夹")
    public CommonResult<Boolean> updateFileFolder(@Valid @RequestBody FileFolderSaveReqVO reqVO) {
        fileFolderService.updateFileFolder(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("@ss.hasPermission('infra:file:delete')")
    @Operation(summary = "删除文件夹")
    public CommonResult<Boolean> deleteFileFolder(@RequestParam("id") Long id) {
        fileFolderService.deleteFileFolder(id);
        return success(true);
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('infra:file:query')")
    @Operation(summary = "获取文件夹")
    public CommonResult<FileFolderRespVO> getFileFolder(@RequestParam("id") Long id) {
        FileFolderDO folder = fileFolderService.getFileFolder(id);
        return success(BeanUtils.toBean(folder, FileFolderRespVO.class));
    }

    @GetMapping("/list")
    @PreAuthorize("@ss.hasPermission('infra:file:query')")
    @Operation(summary = "获取文件夹列表")
    public CommonResult<List<FileFolderRespVO>> getFileFolderList() {
        List<FileFolderDO> list = fileFolderService.getFileFolderList();
        return success(BeanUtils.toBean(list, FileFolderRespVO.class));
    }

    @GetMapping("/tree")
    @PreAuthorize("@ss.hasPermission('infra:file:query')")
    @Operation(summary = "获取文件夹树")
    public CommonResult<List<FileFolderRespVO>> getFileFolderTree() {
        List<FileFolderDO> tree = fileFolderService.getFileFolderTree();
        return success(BeanUtils.toBean(tree, FileFolderRespVO.class));
    }

}
