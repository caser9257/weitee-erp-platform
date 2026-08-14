package cn.weitee.erp.module.erp.controller.admin.mrp;

import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.report.ErpProductionReportCreateReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.report.ErpProductionReportPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.report.ErpProductionReportRespVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionOrderDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionOrderStepDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionReportDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionReportItemDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionOrderMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionOrderStepMapper;
import cn.weitee.erp.module.erp.service.mrp.ErpProductionReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - ERP 生产报工")
@RestController
@RequestMapping("/erp/production-report")
@Validated
public class ErpProductionReportController {

    @Resource
    private ErpProductionReportService productionReportService;
    @Resource
    private ErpProductionOrderMapper productionOrderMapper;
    @Resource
    private ErpProductionOrderStepMapper productionOrderStepMapper;

    @PostMapping("/create")
    @Operation(summary = "创建生产报工")
    @PreAuthorize("@ss.hasPermission('erp:production-report:create')")
    public CommonResult<Long> createReport(@Valid @RequestBody ErpProductionReportCreateReqVO reqVO) {
        return success(productionReportService.createReport(reqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获得生产报工")
    @PreAuthorize("@ss.hasPermission('erp:production-report:query')")
    public CommonResult<ErpProductionReportRespVO> getReport(@RequestParam("id") Long id) {
        ErpProductionReportDO report = productionReportService.getReport(id);
        if (report == null) {
            return success(null);
        }
        return success(buildRespVO(report));
    }

    @GetMapping("/items")
    @Operation(summary = "获得生产报工明细")
    @PreAuthorize("@ss.hasPermission('erp:production-report:query')")
    public CommonResult<List<ErpProductionReportRespVO.Item>> getReportItems(@RequestParam("reportId") Long reportId) {
        List<ErpProductionReportItemDO> items = productionReportService.getReportItemList(reportId);
        Map<Long, ErpProductionOrderStepDO> stepMap = productionOrderStepMapper
                .selectListByIds(convertSet(items, ErpProductionReportItemDO::getProductionOrderStepId))
                .stream().collect(Collectors.toMap(ErpProductionOrderStepDO::getId, step -> step, (a, b) -> a));
        List<ErpProductionReportRespVO.Item> list = items.stream().map(item -> {
            ErpProductionReportRespVO.Item resp = BeanUtils.toBean(item, ErpProductionReportRespVO.Item.class);
            ErpProductionOrderStepDO step = stepMap.get(item.getProductionOrderStepId());
            if (step != null) {
                resp.setStepCode(step.getStepCode());
                resp.setStepName(step.getStepName());
            }
            return resp;
        }).collect(Collectors.toList());
        return success(list);
    }

    @GetMapping("/page")
    @Operation(summary = "获得生产报工分页")
    @PreAuthorize("@ss.hasPermission('erp:production-report:query')")
    public CommonResult<PageResult<ErpProductionReportRespVO>> getReportPage(@Valid ErpProductionReportPageReqVO pageReqVO) {
        PageResult<ErpProductionReportDO> pageResult = productionReportService.getReportPage(pageReqVO);
        Map<Long, ErpProductionOrderDO> orderMap = productionOrderMapper.selectBatchIds(
                        convertSet(pageResult.getList(), ErpProductionReportDO::getProductionOrderId))
                .stream().collect(Collectors.toMap(ErpProductionOrderDO::getId, order -> order, (a, b) -> a));
        List<ErpProductionReportRespVO> list = pageResult.getList().stream().map(report -> {
            ErpProductionReportRespVO resp = BeanUtils.toBean(report, ErpProductionReportRespVO.class);
            ErpProductionOrderDO order = orderMap.get(report.getProductionOrderId());
            if (order != null) {
                resp.setProductionOrderNo(order.getOrderNo());
            }
            return resp;
        }).collect(Collectors.toList());
        return success(new PageResult<>(list, pageResult.getTotal()));
    }

    private ErpProductionReportRespVO buildRespVO(ErpProductionReportDO report) {
        ErpProductionReportRespVO respVO = BeanUtils.toBean(report, ErpProductionReportRespVO.class);
        ErpProductionOrderDO order = productionOrderMapper.selectById(report.getProductionOrderId());
        if (order != null) {
            respVO.setProductionOrderNo(order.getOrderNo());
        }
        return respVO;
    }

}
