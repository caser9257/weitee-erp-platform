package cn.iocoder.yudao.module.erp.service.stock;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.warehouse.category.ErpWarehouseCategoryListReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.warehouse.category.ErpWarehouseCategorySaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpWarehouseCategoryDO;
import cn.iocoder.yudao.module.erp.dal.mysql.stock.ErpWarehouseCategoryMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.WAREHOUSE_CATEGORY_EXITS_WAREHOUSE;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.WAREHOUSE_CATEGORY_EXITS_CHILDREN;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.WAREHOUSE_CATEGORY_PARENT_ERROR;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.WAREHOUSE_CATEGORY_PARENT_IS_CHILD;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.WAREHOUSE_CATEGORY_PARENT_NOT_EXISTS;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.WAREHOUSE_CATEGORY_NAME_DUPLICATE;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.WAREHOUSE_CATEGORY_CODE_DUPLICATE;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.WAREHOUSE_CATEGORY_NOT_EXISTS;

@Service
@Validated
public class ErpWarehouseCategoryServiceImpl implements ErpWarehouseCategoryService {

    @Resource
    private ErpWarehouseCategoryMapper erpWarehouseCategoryMapper;

    @Resource
    @Lazy
    private ErpWarehouseService warehouseService;

    @Override
    public Long createWarehouseCategory(ErpWarehouseCategorySaveReqVO createReqVO) {
        validateParentWarehouseCategory(null, createReqVO.getParentId());
        validateWarehouseCategoryNameUnique(null, createReqVO.getParentId(), createReqVO.getName());
        validateWarehouseCategoryCodeUnique(null, createReqVO.getParentId(), createReqVO.getCode());
        ErpWarehouseCategoryDO category = BeanUtils.toBean(createReqVO, ErpWarehouseCategoryDO.class);
        erpWarehouseCategoryMapper.insert(category);
        return category.getId();
    }

    @Override
    public void updateWarehouseCategory(ErpWarehouseCategorySaveReqVO updateReqVO) {
        validateWarehouseCategoryExists(updateReqVO.getId());
        validateParentWarehouseCategory(updateReqVO.getId(), updateReqVO.getParentId());
        validateWarehouseCategoryNameUnique(updateReqVO.getId(), updateReqVO.getParentId(), updateReqVO.getName());
        validateWarehouseCategoryCodeUnique(updateReqVO.getId(), updateReqVO.getParentId(), updateReqVO.getCode());
        ErpWarehouseCategoryDO updateObj = BeanUtils.toBean(updateReqVO, ErpWarehouseCategoryDO.class);
        erpWarehouseCategoryMapper.updateById(updateObj);
    }

    @Override
    public void deleteWarehouseCategory(Long id) {
        validateWarehouseCategoryExists(id);
        if (erpWarehouseCategoryMapper.selectCountByParentId(id) > 0) {
            throw exception(WAREHOUSE_CATEGORY_EXITS_CHILDREN);
        }
        if (!warehouseService.getWarehouseListByCategoryId(id).isEmpty()) {
            throw exception(WAREHOUSE_CATEGORY_EXITS_WAREHOUSE);
        }
        erpWarehouseCategoryMapper.deleteById(id);
    }

    @Override
    public ErpWarehouseCategoryDO getWarehouseCategory(Long id) {
        return erpWarehouseCategoryMapper.selectById(id);
    }

    @Override
    public List<ErpWarehouseCategoryDO> getWarehouseCategoryList(ErpWarehouseCategoryListReqVO listReqVO) {
        return erpWarehouseCategoryMapper.selectList(listReqVO);
    }

    @Override
    public List<ErpWarehouseCategoryDO> getWarehouseCategoryList(Collection<Long> ids) {
        return erpWarehouseCategoryMapper.selectByIds(ids);
    }

    private void validateWarehouseCategoryExists(Long id) {
        if (erpWarehouseCategoryMapper.selectById(id) == null) {
            throw exception(WAREHOUSE_CATEGORY_NOT_EXISTS);
        }
    }

    private void validateParentWarehouseCategory(Long id, Long parentId) {
        if (parentId == null || ErpWarehouseCategoryDO.PARENT_ID_ROOT.equals(parentId)) {
            return;
        }
        if (Objects.equals(id, parentId)) {
            throw exception(WAREHOUSE_CATEGORY_PARENT_ERROR);
        }
        ErpWarehouseCategoryDO parentCategory = erpWarehouseCategoryMapper.selectById(parentId);
        if (parentCategory == null) {
            throw exception(WAREHOUSE_CATEGORY_PARENT_NOT_EXISTS);
        }
        if (id == null) {
            return;
        }
        for (int i = 0; i < Short.MAX_VALUE; i++) {
            parentId = parentCategory.getParentId();
            if (Objects.equals(id, parentId)) {
                throw exception(WAREHOUSE_CATEGORY_PARENT_IS_CHILD);
            }
            if (parentId == null || ErpWarehouseCategoryDO.PARENT_ID_ROOT.equals(parentId)) {
                break;
            }
            parentCategory = erpWarehouseCategoryMapper.selectById(parentId);
            if (parentCategory == null) {
                break;
            }
        }
    }

    private void validateWarehouseCategoryNameUnique(Long id, Long parentId, String name) {
        ErpWarehouseCategoryDO category = erpWarehouseCategoryMapper.selectByParentIdAndName(parentId, name);
        if (category == null) {
            return;
        }
        if (id == null || !Objects.equals(category.getId(), id)) {
            throw exception(WAREHOUSE_CATEGORY_NAME_DUPLICATE);
        }
    }

    private void validateWarehouseCategoryCodeUnique(Long id, Long parentId, String code) {
        ErpWarehouseCategoryDO category = erpWarehouseCategoryMapper.selectByParentIdAndCode(parentId, code);
        if (category == null) {
            return;
        }
        if (id == null || !Objects.equals(category.getId(), id)) {
            throw exception(WAREHOUSE_CATEGORY_CODE_DUPLICATE);
        }
    }

}
