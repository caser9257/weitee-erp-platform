package cn.iocoder.yudao.module.erp.service.stock;

import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.warehouse.category.ErpWarehouseCategoryListReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.warehouse.category.ErpWarehouseCategorySaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpWarehouseCategoryDO;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

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
