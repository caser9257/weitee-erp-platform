package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.security.core.util.SecurityFrameworkUtils;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.report.ErpProductionReportCreateReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.report.ErpProductionReportPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionOrderDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionOrderStepDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionReportDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionReportItemDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionOrderMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionOrderStepMapper;
import cn.weitee.erp.module.erp.framework.event.ErpProductionReportCreatedEvent;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionReportItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionReportMapper;
import cn.weitee.erp.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.weitee.erp.module.erp.enums.mrp.ErpProductionOrderStatusEnum;
import cn.weitee.erp.module.erp.enums.mrp.ErpProductionOrderStepStatusEnum;
import cn.weitee.erp.module.erp.enums.mrp.ErpProductionReportTypeEnum;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCTION_ORDER_NOT_EXISTS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCTION_ORDER_NOT_RELEASED_FOR_REPORT;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCTION_ORDER_STEP_NOT_EXISTS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCTION_ORDER_STEP_QTY_EXCEED;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCTION_ORDER_STEP_STATUS_INVALID;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCTION_REPORT_QTY_INVALID;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCTION_REPORT_STEP_MISMATCH;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCTION_REPORT_TYPE_INVALID;

@Service
@Validated
public class ErpProductionReportServiceImpl implements ErpProductionReportService {

    @Resource
    private ErpProductionReportMapper productionReportMapper;
    @Resource
    private ErpProductionReportItemMapper productionReportItemMapper;
    @Resource
    private ErpProductionOrderMapper productionOrderMapper;
    @Resource
    private ErpProductionOrderStepMapper productionOrderStepMapper;
    @Resource
    private ErpNoRedisDAO noRedisDAO;
    @Resource
    private ErpProductionStepQualityService productionStepQualityService;
    @Resource
    private ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createReport(@Valid ErpProductionReportCreateReqVO reqVO) {
        // 0. 当前仅支持工序报工；完工报工由"工单完工"接口触发，避免双路径
        if (!ErpProductionReportTypeEnum.STEP.getType().equals(reqVO.getReportType())) {
            throw exception(PRODUCTION_REPORT_TYPE_INVALID);
        }
        // 1. 校验工单处于已下达
        ErpProductionOrderDO order = productionOrderMapper.selectById(reqVO.getProductionOrderId());
        if (order == null) {
            throw exception(PRODUCTION_ORDER_NOT_EXISTS);
        }
        if (!ErpProductionOrderStatusEnum.RELEASED.getStatus().equals(order.getStatus())) {
            throw exception(PRODUCTION_ORDER_NOT_RELEASED_FOR_REPORT);
        }
        // 2. 校验明细工序：必须属于该工单且处于进行中；合格+报废必须等于报工数量
        Map<Long, ErpProductionOrderStepDO> stepMap = loadSteps(reqVO);
        for (ErpProductionReportCreateReqVO.Item item : reqVO.getItems()) {
            ErpProductionOrderStepDO step = stepMap.get(item.getProductionOrderStepId());
            if (step == null) {
                throw exception(PRODUCTION_REPORT_STEP_MISMATCH);
            }
            if (!ErpProductionOrderStepStatusEnum.PROCESSING.getStatus().equals(step.getStepStatus())) {
                throw exception(PRODUCTION_ORDER_STEP_STATUS_INVALID);
            }
            BigDecimal safeQualified = item.getQualifiedQty() == null ? BigDecimal.ZERO : item.getQualifiedQty();
            BigDecimal safeScrap = item.getScrapQty() == null ? BigDecimal.ZERO : item.getScrapQty();
            if (safeQualified.add(safeScrap).compareTo(item.getReportedQty()) != 0) {
                throw exception(PRODUCTION_REPORT_QTY_INVALID);
            }
        }
        // 3. CAS 累加工序数量：并发安全，超计划或非进行中时更新失败
        for (ErpProductionReportCreateReqVO.Item item : reqVO.getItems()) {
            BigDecimal safeQualified = item.getQualifiedQty() == null ? BigDecimal.ZERO : item.getQualifiedQty();
            BigDecimal safeScrap = item.getScrapQty() == null ? BigDecimal.ZERO : item.getScrapQty();
            int count = productionOrderStepMapper.updateStepQtyByCas(item.getProductionOrderStepId(),
                    item.getReportedQty(), safeQualified, safeScrap);
            if (count == 0) {
                throw exception(PRODUCTION_ORDER_STEP_QTY_EXCEED);
            }
        }
        // 4. 写入报工主单与明细
        ErpProductionReportDO report = new ErpProductionReportDO()
                .setReportNo(noRedisDAO.generate(ErpNoRedisDAO.PRODUCTION_REPORT_NO_PREFIX))
                .setProductionOrderId(order.getId())
                .setReportDate(LocalDateTime.now())
                .setReportUserId(SecurityFrameworkUtils.getLoginUserId())
                .setReportType(reqVO.getReportType() == null
                        ? ErpProductionReportTypeEnum.STEP.getType() : reqVO.getReportType())
                .setBatchNo(reqVO.getBatchNo())
                .setStatus(0)
                .setRemark(reqVO.getRemark());
        productionReportMapper.insert(report);
        List<ErpProductionReportItemDO> reportItems = reqVO.getItems().stream()
                .map(item -> buildReportItem(report.getId(), item))
                .collect(Collectors.toList());
        productionReportItemMapper.insertBatch(reportItems);
        // 5. 工序要求质检时，按明细生成待检工序质检单
        productionStepQualityService.createPendingFromReport(report.getId(), reportItems,
                new java.util.ArrayList<>(stepMap.values()));
        // 6. 发布报工事件：供 MES 模块回写任务实际时间与状态（AFTER_COMMIT 执行）
        eventPublisher.publishEvent(new ErpProductionReportCreatedEvent(order.getId(),
                reportItems.stream().map(ErpProductionReportItemDO::getProductionOrderStepId).collect(Collectors.toList()),
                report.getReportDate()));
        return report.getId();
    }

    @Override
    public ErpProductionReportDO getReport(Long id) {
        return productionReportMapper.selectById(id);
    }

    @Override
    public List<ErpProductionReportItemDO> getReportItemList(Long reportId) {
        return productionReportItemMapper.selectListByReportId(reportId);
    }

    @Override
    public PageResult<ErpProductionReportDO> getReportPage(ErpProductionReportPageReqVO pageReqVO) {
        return productionReportMapper.selectPage(pageReqVO);
    }

    private Map<Long, ErpProductionOrderStepDO> loadSteps(ErpProductionReportCreateReqVO reqVO) {
        List<Long> stepIds = reqVO.getItems().stream()
                .map(ErpProductionReportCreateReqVO.Item::getProductionOrderStepId)
                .collect(Collectors.toList());
        List<ErpProductionOrderStepDO> steps = productionOrderStepMapper.selectListByIds(stepIds);
        return steps.stream().collect(Collectors.toMap(ErpProductionOrderStepDO::getId, step -> step, (a, b) -> a));
    }

    private ErpProductionReportItemDO buildReportItem(Long reportId, ErpProductionReportCreateReqVO.Item item) {
        return new ErpProductionReportItemDO()
                .setReportId(reportId)
                .setProductionOrderStepId(item.getProductionOrderStepId())
                .setDeviceId(item.getDeviceId())
                .setWorkerUserId(item.getWorkerUserId())
                .setReportedQty(item.getReportedQty())
                .setQualifiedQty(item.getQualifiedQty() == null ? BigDecimal.ZERO : item.getQualifiedQty())
                .setScrapQty(item.getScrapQty() == null ? BigDecimal.ZERO : item.getScrapQty())
                .setWorkHour(item.getWorkHour())
                .setBatchNo(item.getBatchNo())
                .setRemark(item.getRemark());
    }

}
