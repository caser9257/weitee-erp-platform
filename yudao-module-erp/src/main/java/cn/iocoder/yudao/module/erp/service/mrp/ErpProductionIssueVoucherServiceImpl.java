package cn.iocoder.yudao.module.erp.service.mrp;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.issuevoucher.ErpProductionIssueVoucherPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionIssueDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionIssueItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionIssueVoucherDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionIssueVoucherItemDO;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpProductionIssueVoucherItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpProductionIssueVoucherMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
@Validated
public class ErpProductionIssueVoucherServiceImpl implements ErpProductionIssueVoucherService {

    private static final Integer GENERATED_STATUS = 10;

    @Resource
    private ErpProductionIssueVoucherMapper erpProductionIssueVoucherMapper;
    @Resource
    private ErpProductionIssueVoucherItemMapper erpProductionIssueVoucherItemMapper;
    @Resource
    private ErpNoRedisDAO noRedisDAO;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createVoucher(ErpProductionIssueDO issue, List<ErpProductionIssueItemDO> items) {
        ErpProductionIssueVoucherDO existed = erpProductionIssueVoucherMapper.selectByIssueId(issue.getId());
        if (existed != null) {
            return existed.getId();
        }
        ErpProductionIssueVoucherDO voucher = new ErpProductionIssueVoucherDO()
                .setVoucherNo(noRedisDAO.generate(ErpNoRedisDAO.PRODUCTION_ISSUE_VOUCHER_NO_PREFIX))
                .setIssueId(issue.getId())
                .setIssueNo(issue.getIssueNo())
                .setProductionOrderId(issue.getProductionOrderId())
                .setVoucherTime(issue.getIssueTime())
                .setStatus(GENERATED_STATUS)
                .setTotalAmount(issue.getIssueAmount())
                .setRemark(issue.getRemark());
        erpProductionIssueVoucherMapper.insert(voucher);
        if (CollUtil.isNotEmpty(items)) {
            items.forEach(item -> erpProductionIssueVoucherItemMapper.insert(new ErpProductionIssueVoucherItemDO()
                    .setVoucherId(voucher.getId())
                    .setIssueItemId(item.getId())
                    .setMaterialId(item.getMaterialId())
                    .setWarehouseId(item.getWarehouseId())
                    .setIssueQty(item.getIssueQty())
                    .setIssueAmount(item.getIssueAmount())
                    .setRemark(item.getRemark())));
        }
        return voucher.getId();
    }

    @Override
    public ErpProductionIssueVoucherDO getVoucher(Long id) {
        return erpProductionIssueVoucherMapper.selectById(id);
    }

    @Override
    public ErpProductionIssueVoucherDO getVoucherByIssueId(Long issueId) {
        return erpProductionIssueVoucherMapper.selectByIssueId(issueId);
    }

    @Override
    public PageResult<ErpProductionIssueVoucherDO> getVoucherPage(ErpProductionIssueVoucherPageReqVO pageReqVO) {
        return erpProductionIssueVoucherMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpProductionIssueVoucherItemDO> getVoucherItemListByVoucherId(Long voucherId) {
        return erpProductionIssueVoucherItemMapper.selectListByVoucherId(voucherId);
    }

    @Override
    public List<ErpProductionIssueVoucherItemDO> getVoucherItemListByVoucherIds(Collection<Long> voucherIds) {
        if (CollUtil.isEmpty(voucherIds)) {
            return Collections.emptyList();
        }
        return erpProductionIssueVoucherItemMapper.selectListByVoucherIds(voucherIds);
    }

}
