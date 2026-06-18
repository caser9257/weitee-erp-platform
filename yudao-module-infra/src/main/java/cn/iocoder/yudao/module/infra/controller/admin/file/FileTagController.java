package cn.iocoder.yudao.module.infra.controller.admin.file;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.tag.FileTagSaveReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileTagDO;
import cn.iocoder.yudao.module.infra.service.file.FileTagService;
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

@Tag(name = "管理后台 - 文件标签管理")
@RestController
@RequestMapping("/infra/file-tag")
@Validated
@Slf4j
public class FileTagController {

    @Resource
    private FileTagService fileTagService;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('infra:file:create')")
    @Operation(summary = "创建文件标签")
    public CommonResult<Long> createFileTag(@Valid @RequestBody FileTagSaveReqVO reqVO) {
        return success(fileTagService.createFileTag(reqVO));
    }

    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('infra:file:update')")
    @Operation(summary = "更新文件标签")
    public CommonResult<Boolean> updateFileTag(@Valid @RequestBody FileTagSaveReqVO reqVO) {
        fileTagService.updateFileTag(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("@ss.hasPermission('infra:file:delete')")
    @Operation(summary = "删除文件标签")
    public CommonResult<Boolean> deleteFileTag(@RequestParam("id") Long id) {
        fileTagService.deleteFileTag(id);
        return success(true);
    }

    @GetMapping("/list")
    @PreAuthorize("@ss.hasPermission('infra:file:query')")
    @Operation(summary = "获取文件标签列表")
    public CommonResult<List<FileTagDO>> getFileTagList() {
        return success(fileTagService.getFileTagList());
    }

}
