package cn.weitee.erp.module.erp.service.stock;

import cn.weitee.erp.module.erp.controller.admin.stock.vo.warehouse.category.ErpWarehouseCategoryListReqVO;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.warehouse.category.ErpWarehouseCategorySaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpWarehouseCategoryDO;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertMap;

public interface ErpWarehouseCategoryService {

    Long createWarehouseCategory(@Valid ErpWarehouseCategorySaveReqVO createReqVO);

    void updateWarehouseCategory(@Valid ErpWarehouseCategorySaveReqVO updateReqVO);

    void deleteWarehouseCategory(Long id);

    ErpWarehouseCategoryDO getWarehouseCategory(Long id);

    List<ErpWarehouseCategoryDO> getWarehouseCategoryList(ErpWarehouseCategoryListReqVO listReqVO);

    List<ErpWarehouseCategoryDO> getWarehouseCategoryList(Collection<Long> ids);

    default Map<Long, ErpWarehouseCategoryDO> getWarehouseCategoryMap(Collection<Long> ids) {
        return convertMap(getWarehouseCategoryList(ids), ErpWarehouseCategoryDO::getId);
    }

}
