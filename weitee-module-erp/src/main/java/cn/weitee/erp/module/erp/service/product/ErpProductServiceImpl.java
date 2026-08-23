package cn.weitee.erp.module.erp.service.product;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.collection.MapUtils;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ProductSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductCategoryDO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductUnitDO;
import cn.weitee.erp.module.erp.dal.mysql.product.ErpProductMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_AUDIT_STATUS_ILLEGAL;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_NOT_ENABLE;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCT_NOT_EXISTS;

/**
 * ERP 产品 Service 实现类
 *
 * @author WeTai
 */
@Service
@Validated
@Slf4j
public class ErpProductServiceImpl implements ErpProductService {

    @Resource
    private ErpProductMapper erpProductMapper;

    @Resource
    private ErpProductCategoryService productCategoryService;
    @Resource
    private ErpProductUnitService productUnitService;

    @Override
    public Long createProduct(ProductSaveReqVO createReqVO) {
        // TODO 芋艿：校验分类
        // 插入（新物料默认草稿，需审核后才能被 BOM 引用）
        ErpProductDO product = BeanUtils.toBean(createReqVO, ErpProductDO.class)
                .setAuditStatus(ErpAuditStatus.DRAFT.getStatus())
                .setProcessInstanceId(null);
        erpProductMapper.insert(product);
        // 返回
        return product.getId();
    }

    @Override
    public void updateProduct(ProductSaveReqVO updateReqVO) {
        // TODO 芋艿：校验分类
        // 校验存在
        validateProductExists(updateReqVO.getId());
        // 更新
        ErpProductDO updateObj = BeanUtils.toBean(updateReqVO, ErpProductDO.class);
        erpProductMapper.updateById(updateObj);
    }

    @Override
    public void deleteProduct(Long id) {
        // 校验存在
        validateProductExists(id);
        // 删除
        erpProductMapper.deleteById(id);
    }

    @Override
    public List<ErpProductDO> validProductList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpProductDO> list = erpProductMapper.selectByIds(ids);
        Map<Long, ErpProductDO> productMap = convertMap(list, ErpProductDO::getId);
        for (Long id : ids) {
            ErpProductDO product = productMap.get(id);
            if (productMap.get(id) == null) {
                throw exception(PRODUCT_NOT_EXISTS);
            }
            if (CommonStatusEnum.isDisable(product.getStatus())) {
                throw exception(PRODUCT_NOT_ENABLE, product.getName());
            }
            // P5：仅已审批物料可被 BOM 引用（未审核的 DRAFT 也不放行，需先走 erp.product.create 审批）
            Integer auditStatus = product.getAuditStatus();
            if (!ErpAuditStatus.APPROVE.getStatus().equals(auditStatus)) {
                throw exception(PRODUCT_AUDIT_STATUS_ILLEGAL);
            }
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProductAuditStatusByBpm(Long id, String processInstanceId, Integer status, String reason) {
        ErpProductDO product = erpProductMapper.selectById(id);
        if (product == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
        if (!StrUtil.equals(processInstanceId, product.getProcessInstanceId())) {
            throw exception(PRODUCT_AUDIT_STATUS_ILLEGAL);
        }
        if (!ErpAuditStatus.PROCESS.getStatus().equals(product.getAuditStatus())) {
            log.warn("[updateProductAuditStatusByBpm] 忽略非审批中回调，id={}, auditStatus={}", id, product.getAuditStatus());
            return;
        }
        int count = erpProductMapper.update(null, new LambdaUpdateWrapper<ErpProductDO>()
                .eq(ErpProductDO::getId, id)
                .eq(ErpProductDO::getAuditStatus, ErpAuditStatus.PROCESS.getStatus())
                .eq(ErpProductDO::getProcessInstanceId, processInstanceId)
                .set(ErpProductDO::getAuditStatus, status)
                .set(ErpProductDO::getProcessInstanceId, processInstanceId));
        if (count == 0) {
            throw exception(PRODUCT_AUDIT_STATUS_ILLEGAL);
        }
        // 审批通过时同步启用
        if (ErpAuditStatus.APPROVE.getStatus().equals(status)) {
            erpProductMapper.updateById(new ErpProductDO().setId(id).setStatus(CommonStatusEnum.ENABLE.getStatus()));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rollbackProductAuditStatusToDraftByBpm(Long id, String processInstanceId, String reason) {
        ErpProductDO product = erpProductMapper.selectById(id);
        if (product == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
        if (!ErpAuditStatus.PROCESS.getStatus().equals(product.getAuditStatus())) {
            log.warn("[rollbackProductAuditStatusToDraftByBpm] 忽略非审批中回退，id={}, auditStatus={}", id, product.getAuditStatus());
            return;
        }
        if (processInstanceId != null && !processInstanceId.equals(product.getProcessInstanceId())) {
            log.warn("[rollbackProductAuditStatusToDraftByBpm] processInstanceId 不匹配，id={}, expected={}, actual={}", id, processInstanceId, product.getProcessInstanceId());
            return;
        }
        int count = erpProductMapper.update(null, new LambdaUpdateWrapper<ErpProductDO>()
                .eq(ErpProductDO::getId, id)
                .eq(ErpProductDO::getAuditStatus, ErpAuditStatus.PROCESS.getStatus())
                .set(ErpProductDO::getAuditStatus, ErpAuditStatus.DRAFT.getStatus())
                .set(ErpProductDO::getProcessInstanceId, null));
        if (count == 0) {
            throw exception(PRODUCT_AUDIT_STATUS_ILLEGAL);
        }
    }

    private void validateProductExists(Long id) {
        if (erpProductMapper.selectById(id) == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
    }

    @Override
    public ErpProductDO getProduct(Long id) {
        return erpProductMapper.selectById(id);
    }

    @Override
    public List<ErpProductRespVO> getProductVOListByStatus(Integer status) {
        List<ErpProductDO> list = erpProductMapper.selectListByStatus(status);
        return buildProductVOList(list);
    }

    @Override
    public List<ErpProductRespVO> getProductVOList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpProductDO> list = erpProductMapper.selectByIds(ids);
        return buildProductVOList(list);
    }

    @Override
    public PageResult<ErpProductRespVO> getProductVOPage(ErpProductPageReqVO pageReqVO) {
        PageResult<ErpProductDO> pageResult = erpProductMapper.selectPage(pageReqVO);
        return new PageResult<>(buildProductVOList(pageResult.getList()), pageResult.getTotal());
    }

    private List<ErpProductRespVO> buildProductVOList(List<ErpProductDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        Map<Long, ErpProductCategoryDO> categoryMap = productCategoryService.getProductCategoryMap(
                convertSet(list, ErpProductDO::getCategoryId));
        Map<Long, ErpProductUnitDO> unitMap = productUnitService.getProductUnitMap(
                convertSet(list, ErpProductDO::getUnitId));
        return BeanUtils.toBean(list, ErpProductRespVO.class, product -> {
            MapUtils.findAndThen(categoryMap, product.getCategoryId(),
                    category -> product.setCategoryName(category.getName()));
            MapUtils.findAndThen(unitMap, product.getUnitId(),
                    unit -> {
                        product.setUnitName(unit.getName());
                        product.setQuantityPrecision(unit.getQuantityPrecision());
                    });
        });
    }

    @Override
    public Long getProductCountByCategoryId(Long categoryId) {
        return erpProductMapper.selectCountByCategoryId(categoryId);
    }

    @Override
    public Long getProductCountByUnitId(Long unitId) {
        return erpProductMapper.selectCountByUnitId(unitId);
    }

    @Override
    public List<ErpProductRespVO> getApprovedProductSimpleList() {
        List<ErpProductDO> list = erpProductMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ErpProductDO>()
                        .eq(ErpProductDO::getAuditStatus, ErpAuditStatus.APPROVE.getStatus())
                        .eq(ErpProductDO::getStatus, CommonStatusEnum.ENABLE.getStatus()));
        return buildProductVOList(list);
    }

}
