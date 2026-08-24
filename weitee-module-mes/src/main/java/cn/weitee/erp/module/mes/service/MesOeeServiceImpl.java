package cn.weitee.erp.module.mes.service;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpWorkCenterDO;
import cn.weitee.erp.module.erp.service.mrp.ErpWorkCenterService;
import cn.weitee.erp.module.mes.controller.admin.vo.oee.MesOeeSummaryRespVO;
import cn.weitee.erp.module.mes.dal.mysql.MesOeeMapper;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MesOeeServiceImpl implements MesOeeService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    @Resource
    private MesOeeMapper mesOeeMapper;
    @Resource
    private ErpWorkCenterService workCenterService;

    @Override
    public List<MesOeeSummaryRespVO> getOeeSummary(Long workCenterId, LocalDate startDate, LocalDate endDate) {
        List<MesOeeSummaryRespVO> list = mesOeeMapper.selectOeeSummary(workCenterId, startDate, endDate);
        if (CollUtil.isEmpty(list)) {
            return list;
        }
        // 工作中心名称
        Map<Long, String> centerNameMap = workCenterService.getWorkCenterList(
                        list.stream().map(MesOeeSummaryRespVO::getWorkCenterId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(ErpWorkCenterDO::getId, ErpWorkCenterDO::getCenterName, (a, b) -> a));
        for (MesOeeSummaryRespVO vo : list) {
            vo.setWorkCenterName(centerNameMap.getOrDefault(vo.getWorkCenterId(), null));
            BigDecimal planMinutes = BigDecimal.valueOf(vo.getPlanMinutes() == null ? 0 : vo.getPlanMinutes());
            BigDecimal actualMinutes = BigDecimal.valueOf(vo.getActualMinutes() == null ? 0 : vo.getActualMinutes());
            BigDecimal planQty = nz(vo.getPlanQty());
            BigDecimal reportedQty = nz(vo.getReportedQty());
            BigDecimal qualifiedQty = nz(vo.getQualifiedQty());

            BigDecimal availability = planMinutes.signum() > 0
                    ? actualMinutes.divide(planMinutes, 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            BigDecimal achievement = planQty.signum() > 0
                    ? reportedQty.divide(planQty, 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            BigDecimal quality = reportedQty.signum() > 0
                    ? qualifiedQty.divide(reportedQty, 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;

            vo.setAvailabilityRate(availability.multiply(HUNDRED).setScale(2, RoundingMode.HALF_UP));
            vo.setAchievementRate(achievement.multiply(HUNDRED).setScale(2, RoundingMode.HALF_UP));
            vo.setQualityRate(quality.multiply(HUNDRED).setScale(2, RoundingMode.HALF_UP));
            vo.setOee(availability.multiply(achievement).multiply(quality)
                    .multiply(HUNDRED).setScale(2, RoundingMode.HALF_UP));
        }
        return list;
    }

    private BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
