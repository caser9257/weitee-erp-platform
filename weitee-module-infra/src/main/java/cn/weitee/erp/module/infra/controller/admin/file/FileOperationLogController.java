package cn.weitee.erp.module.infra.controller.admin.file;

import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.infra.controller.admin.file.vo.log.FileOperationLogPageReqVO;
import cn.weitee.erp.module.infra.controller.admin.file.vo.log.FileOperationLogRespVO;
import cn.weitee.erp.module.infra.dal.dataobject.file.FileOperationLogDO;
import cn.weitee.erp.module.infra.service.file.FileOperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 文件操作日志")
@RestController
@RequestMapping("/infra/file-operation-log")
@Validated
public class FileOperationLogController {

    @Resource
    private FileOperationLogService fileOperationLogService;

    @GetMapping("/page")
    @Operation(summary = "获得文件操作日志分页")
    @PreAuthorize("@ss.hasPermission('infra:file:query')")
    public CommonResult<PageResult<FileOperationLogRespVO>> getFileOperationLogPage(
            @Valid FileOperationLogPageReqVO reqVO) {
        PageResult<FileOperationLogDO> pageResult = fileOperationLogService.getFileOperationLogPage(reqVO);
        return success(BeanUtils.toBean(pageResult, FileOperationLogRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得文件操作日志列表")
    @PreAuthorize("@ss.hasPermission('infra:file:query')")
    public CommonResult<List<FileOperationLogRespVO>> getFileOperationLogList(
            @RequestParam("fileId") Long fileId) {
        List<FileOperationLogDO> list = fileOperationLogService.getFileOperationLogs(fileId);
        return success(BeanUtils.toBean(list, FileOperationLogRespVO.class));
    }

}
