package cn.iocoder.yudao.module.erp.service.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.PhoneUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.validation.ValidationUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.supplier.ErpSupplierBatchUpdateReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.supplier.ErpSupplierBatchUpdateResultVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.supplier.ErpSupplierPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.supplier.ErpSupplierSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpSupplierDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpSupplierMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.regex.Pattern;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.SUPPLIER_BATCH_UPDATE_FIELD_NOT_SUPPORT;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.SUPPLIER_BATCH_UPDATE_FIELD_VALUE_INVALID;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.SUPPLIER_NOT_ENABLE;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.SUPPLIER_NOT_EXISTS;

/**
 * ERP 供应商 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ErpSupplierServiceImpl implements ErpSupplierService {

    private static final String BATCH_EDIT_MODE_OVERWRITE = "overwrite";
    private static final String BATCH_FIELD_CONTACT = "contact";
    private static final String BATCH_FIELD_MOBILE = "mobile";
    private static final String BATCH_FIELD_TELEPHONE = "telephone";
    private static final String BATCH_FIELD_EMAIL = "email";
    private static final String BATCH_FIELD_FAX = "fax";
    private static final String BATCH_FIELD_REMARK = "remark";
    private static final String BATCH_FIELD_STATUS = "status";
    private static final String BATCH_FIELD_SORT = "sort";
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    @Resource
    private ErpSupplierMapper erpSupplierMapper;

    @Override
    public Long createSupplier(ErpSupplierSaveReqVO createReqVO) {
        ErpSupplierDO supplier = BeanUtils.toBean(createReqVO, ErpSupplierDO.class);
        erpSupplierMapper.insert(supplier);
        return supplier.getId();
    }

    @Override
    public void updateSupplier(ErpSupplierSaveReqVO updateReqVO) {
        // 校验存在
        validateSupplierExists(updateReqVO.getId());
        // 更新
        ErpSupplierDO updateObj = BeanUtils.toBean(updateReqVO, ErpSupplierDO.class);
        erpSupplierMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErpSupplierBatchUpdateResultVO updateSupplierBatch(ErpSupplierBatchUpdateReqVO reqVO) {
        if (!BATCH_EDIT_MODE_OVERWRITE.equals(reqVO.getMode())) {
            throw exception(SUPPLIER_BATCH_UPDATE_FIELD_NOT_SUPPORT, reqVO.getFieldKey());
        }
        String fieldKey = normalizeBatchFieldKey(reqVO.getFieldKey());
        Object value = parseBatchValue(fieldKey, reqVO.getValue());
        List<Long> uniqueIds = new ArrayList<>(new LinkedHashSet<>(reqVO.getIds()));
        List<ErpSupplierDO> suppliers = erpSupplierMapper.selectByIds(uniqueIds);
        Map<Long, ErpSupplierDO> supplierMap = convertMap(suppliers, ErpSupplierDO::getId);
        for (Long id : uniqueIds) {
            if (!supplierMap.containsKey(id)) {
                throw exception(SUPPLIER_NOT_EXISTS);
            }
        }

        uniqueIds.forEach(id -> {
            ErpSupplierDO updateObj = new ErpSupplierDO().setId(id);
            applyBatchValue(updateObj, fieldKey, value);
            erpSupplierMapper.updateById(updateObj);
        });

        ErpSupplierBatchUpdateResultVO result = new ErpSupplierBatchUpdateResultVO();
        result.setSuccessCount(uniqueIds.size());
        result.setFailureCount(0);
        result.setUpdatedIds(uniqueIds);
        result.setFailedItems(Collections.emptyList());
        return result;
    }

    @Override
    public void deleteSupplier(Long id) {
        // 校验存在
        validateSupplierExists(id);
        // 删除
        erpSupplierMapper.deleteById(id);
    }

    private void validateSupplierExists(Long id) {
        if (erpSupplierMapper.selectById(id) == null) {
            throw exception(SUPPLIER_NOT_EXISTS);
        }
    }

    @Override
    public ErpSupplierDO getSupplier(Long id) {
        return erpSupplierMapper.selectById(id);
    }

    @Override
    public ErpSupplierDO validateSupplier(Long id) {
        ErpSupplierDO supplier = erpSupplierMapper.selectById(id);
        if (supplier == null) {
            throw exception(SUPPLIER_NOT_EXISTS);
        }
        if (CommonStatusEnum.isDisable(supplier.getStatus())) {
            throw exception(SUPPLIER_NOT_ENABLE, supplier.getName());
        }
        return supplier;
    }

    @Override
    public List<ErpSupplierDO> getSupplierList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return erpSupplierMapper.selectByIds(ids);
    }

    @Override
    public PageResult<ErpSupplierDO> getSupplierPage(ErpSupplierPageReqVO pageReqVO) {
        return erpSupplierMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpSupplierDO> getSupplierListByStatus(Integer status) {
        return erpSupplierMapper.selectListByStatus(status);
    }

    private String normalizeBatchFieldKey(String fieldKey) {
        if (!BATCH_FIELD_CONTACT.equals(fieldKey)
                && !BATCH_FIELD_MOBILE.equals(fieldKey)
                && !BATCH_FIELD_TELEPHONE.equals(fieldKey)
                && !BATCH_FIELD_EMAIL.equals(fieldKey)
                && !BATCH_FIELD_FAX.equals(fieldKey)
                && !BATCH_FIELD_REMARK.equals(fieldKey)
                && !BATCH_FIELD_STATUS.equals(fieldKey)
                && !BATCH_FIELD_SORT.equals(fieldKey)) {
            throw exception(SUPPLIER_BATCH_UPDATE_FIELD_NOT_SUPPORT, fieldKey);
        }
        return fieldKey;
    }

    private Object parseBatchValue(String fieldKey, String value) {
        String trimmedValue = StrUtil.trim(value);
        if (BATCH_FIELD_STATUS.equals(fieldKey)) {
            return parseStatusValue(fieldKey, trimmedValue);
        }
        if (BATCH_FIELD_SORT.equals(fieldKey)) {
            return parseSortValue(fieldKey, trimmedValue);
        }
        if (BATCH_FIELD_MOBILE.equals(fieldKey)) {
            if (!ValidationUtils.isMobile(trimmedValue)) {
                throw exception(SUPPLIER_BATCH_UPDATE_FIELD_VALUE_INVALID, fieldKey, value);
            }
            return trimmedValue;
        }
        if (BATCH_FIELD_TELEPHONE.equals(fieldKey)) {
            if (!(PhoneUtil.isTel(trimmedValue) || PhoneUtil.isPhone(trimmedValue))) {
                throw exception(SUPPLIER_BATCH_UPDATE_FIELD_VALUE_INVALID, fieldKey, value);
            }
            return trimmedValue;
        }
        if (BATCH_FIELD_EMAIL.equals(fieldKey)) {
            if (!EMAIL_PATTERN.matcher(trimmedValue).matches()) {
                throw exception(SUPPLIER_BATCH_UPDATE_FIELD_VALUE_INVALID, fieldKey, value);
            }
            return trimmedValue;
        }
        if (CharSequenceUtil.isBlank(trimmedValue)) {
            throw exception(SUPPLIER_BATCH_UPDATE_FIELD_VALUE_INVALID, fieldKey, value);
        }
        return trimmedValue;
    }

    private Integer parseStatusValue(String fieldKey, String value) {
        try {
            Integer status = Integer.valueOf(value);
            if (!CommonStatusEnum.isEnable(status) && !CommonStatusEnum.isDisable(status)) {
                throw exception(SUPPLIER_BATCH_UPDATE_FIELD_VALUE_INVALID, fieldKey, value);
            }
            return status;
        } catch (NumberFormatException ex) {
            throw exception(SUPPLIER_BATCH_UPDATE_FIELD_VALUE_INVALID, fieldKey, value);
        }
    }

    private Integer parseSortValue(String fieldKey, String value) {
        try {
            Integer sort = Integer.valueOf(value);
            if (sort < 0) {
                throw exception(SUPPLIER_BATCH_UPDATE_FIELD_VALUE_INVALID, fieldKey, value);
            }
            return sort;
        } catch (NumberFormatException ex) {
            throw exception(SUPPLIER_BATCH_UPDATE_FIELD_VALUE_INVALID, fieldKey, value);
        }
    }

    private void applyBatchValue(ErpSupplierDO updateObj, String fieldKey, Object value) {
        if (BATCH_FIELD_CONTACT.equals(fieldKey)) {
            updateObj.setContact((String) value);
            return;
        }
        if (BATCH_FIELD_MOBILE.equals(fieldKey)) {
            updateObj.setMobile((String) value);
            return;
        }
        if (BATCH_FIELD_TELEPHONE.equals(fieldKey)) {
            updateObj.setTelephone((String) value);
            return;
        }
        if (BATCH_FIELD_EMAIL.equals(fieldKey)) {
            updateObj.setEmail((String) value);
            return;
        }
        if (BATCH_FIELD_FAX.equals(fieldKey)) {
            updateObj.setFax((String) value);
            return;
        }
        if (BATCH_FIELD_REMARK.equals(fieldKey)) {
            updateObj.setRemark((String) value);
            return;
        }
        if (BATCH_FIELD_STATUS.equals(fieldKey)) {
            updateObj.setStatus((Integer) value);
            return;
        }
        if (BATCH_FIELD_SORT.equals(fieldKey)) {
            updateObj.setSort((Integer) value);
        }
    }

}
