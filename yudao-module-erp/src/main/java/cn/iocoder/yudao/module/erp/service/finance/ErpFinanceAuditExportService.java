package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.voucher.ErpFinanceVoucherPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceVoucherDO;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceVoucherMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 审计数据导出服务
 * 只导出外部账簿数据
 */
@Slf4j
@Service
public class ErpFinanceAuditExportService {

    @Resource
    private ErpFinanceVoucherMapper voucherMapper;

    @Resource
    private FinanceDataPermissionService financeDataPermissionService;

    @Resource
    private ErpFinanceAuditOperationLogService auditOperationLogService;

    /**
     * 导出外部账凭证
     */
    public void exportExternalVouchers(Long userId, Long ledgerId, ErpFinanceVoucherPageReqVO reqVO,
                                        HttpServletResponse response) throws IOException {
        // 检查是否有权限访问该账簿
        if (!financeDataPermissionService.canAccessLedger(ledgerId)) {
            throw new RuntimeException("无权访问该账簿");
        }

        // 查询凭证数据
        reqVO.setLedgerId(ledgerId);
        PageResult<ErpFinanceVoucherDO> pageResult = voucherMapper.selectPage(reqVO);
        List<ErpFinanceVoucherDO> vouchers = pageResult.getList();

        // 生成 CSV 内容
        StringBuilder csv = new StringBuilder();
        csv.append("凭证号,业务类型,业务单号,凭证时间,借方金额,贷方金额,状态\n");
        for (ErpFinanceVoucherDO voucher : vouchers) {
            csv.append(voucher.getVoucherNo()).append(",");
            csv.append(voucher.getBizType()).append(",");
            csv.append(voucher.getBizNo()).append(",");
            csv.append(voucher.getVoucherTime()).append(",");
            csv.append(voucher.getTotalDebitAmount()).append(",");
            csv.append(voucher.getTotalCreditAmount()).append(",");
            csv.append(voucher.getStatus()).append("\n");
        }

        // 设置响应头
        response.setContentType("text/csv;charset=utf-8");
        String fileName = URLEncoder.encode("外部账凭证导出.csv", StandardCharsets.UTF_8.name());
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        // 写入响应
        try (OutputStream out = response.getOutputStream()) {
            // 写入 BOM 以支持 Excel 打开中文
            out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
            out.write(csv.toString().getBytes(StandardCharsets.UTF_8));
            out.flush();
        }

        // 记录审计日志
        auditOperationLogService.logExport(userId, "导出外部账凭证", ledgerId,
                "ledgerId=" + ledgerId, vouchers.size());

        log.info("审计数据导出完成。userId={}, ledgerId={}, count={}", userId, ledgerId, vouchers.size());
    }

    /**
     * 导出外部账科目余额表
     */
    public void exportExternalBalance(Long userId, Long ledgerId, String period,
                                       HttpServletResponse response) throws IOException {
        // 检查权限
        if (!financeDataPermissionService.canAccessLedger(ledgerId)) {
            throw new RuntimeException("无权访问该账簿");
        }

        // TODO: 实现科目余额表导出逻辑

        // 记录审计日志
        auditOperationLogService.logExport(userId, "导出外部账科目余额表", ledgerId,
                "ledgerId=" + ledgerId + "&period=" + period, 0);
    }

    /**
     * 导出外部账试算平衡表
     */
    public void exportTrialBalance(Long userId, Long ledgerId, String period,
                                    HttpServletResponse response) throws IOException {
        // 检查权限
        if (!financeDataPermissionService.canAccessLedger(ledgerId)) {
            throw new RuntimeException("无权访问该账簿");
        }

        // TODO: 实现试算平衡表导出逻辑

        // 记录审计日志
        auditOperationLogService.logExport(userId, "导出外部账试算平衡表", ledgerId,
                "ledgerId=" + ledgerId + "&period=" + period, 0);
    }

}
