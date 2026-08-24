package cn.weitee.erp.module.erp.controller.admin.mrp;

import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.device.ErpDevicePageReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.device.ErpDeviceRespVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.device.ErpDeviceSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpDeviceDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpWorkCenterDO;
import cn.weitee.erp.module.erp.service.mrp.ErpDeviceService;
import cn.weitee.erp.module.erp.service.mrp.ErpWorkCenterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertList;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - ERP 设备台账")
@RestController
@RequestMapping("/erp/device")
@Validated
public class ErpDeviceController {

    @Resource
    private ErpDeviceService deviceService;
    @Resource
    private ErpWorkCenterService workCenterService;

    @PostMapping("/create")
    @Operation(summary = "创建设备")
    @PreAuthorize("@ss.hasPermission('erp:device:create')")
    public CommonResult<Long> createDevice(@Valid @RequestBody ErpDeviceSaveReqVO createReqVO) {
        return success(deviceService.create(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新设备")
    @PreAuthorize("@ss.hasPermission('erp:device:update')")
    public CommonResult<Boolean> updateDevice(@Valid @RequestBody ErpDeviceSaveReqVO updateReqVO) {
        deviceService.update(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除设备")
    @PreAuthorize("@ss.hasPermission('erp:device:delete')")
    public CommonResult<Boolean> deleteDevice(@RequestParam("id") Long id) {
        deviceService.delete(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得设备")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:device:query')")
    public CommonResult<ErpDeviceRespVO> getDevice(@RequestParam("id") Long id) {
        return success(buildDeviceRespVO(deviceService.get(id)));
    }

    @GetMapping("/page")
    @Operation(summary = "获得设备分页")
    @PreAuthorize("@ss.hasPermission('erp:device:query')")
    public CommonResult<PageResult<ErpDeviceRespVO>> getDevicePage(@Valid ErpDevicePageReqVO pageReqVO) {
        PageResult<ErpDeviceDO> pageResult = deviceService.getPage(pageReqVO);
        return success(new PageResult<>(buildDeviceRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    private List<ErpDeviceRespVO> buildDeviceRespVOList(List<ErpDeviceDO> list) {
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        Set<Long> centerIds = convertSet(list, ErpDeviceDO::getWorkCenterId);
        centerIds.remove(null);
        Map<Long, ErpWorkCenterDO> centerMap = centerIds.isEmpty() ? Map.of()
                : workCenterService.getWorkCenterList(centerIds).stream()
                        .collect(Collectors.toMap(ErpWorkCenterDO::getId, center -> center));
        return convertList(list, item -> {
            ErpDeviceRespVO respVO = BeanUtils.toBean(item, ErpDeviceRespVO.class);
            ErpWorkCenterDO center = centerMap.get(item.getWorkCenterId());
            if (center != null) {
                respVO.setWorkCenterName(center.getCenterName());
            }
            return respVO;
        });
    }

    private ErpDeviceRespVO buildDeviceRespVO(ErpDeviceDO device) {
        if (device == null) {
            return null;
        }
        return buildDeviceRespVOList(List.of(device)).getFirst();
    }

}
