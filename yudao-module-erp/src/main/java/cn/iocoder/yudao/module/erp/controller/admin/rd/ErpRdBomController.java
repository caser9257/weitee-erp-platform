package cn.iocoder.yudao.module.erp.controller.admin.rd;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.rd.vo.bom.ErpRdBomPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.rd.vo.bom.ErpRdBomRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.rd.vo.bom.ErpRdBomSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.rd.ErpRdBomDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.rd.ErpRdBomItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.rd.ErpRdBomItemSubstituteDO;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import cn.iocoder.yudao.module.erp.service.rd.ErpRdBomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - ERP 研发 BOM")
@RestController
@RequestMapping("/erp/rd-bom")
@Validated
public class ErpRdBomController {

    @Resource
    private ErpRdBomService rdBomService;
    @Resource
    private ErpProductService productService;

    @PostMapping("/create")
    @Operation(summary = "创建研发 BOM")
    @PreAuthorize("@ss.hasPermission('erp:rd-bom:create')")
    public CommonResult<Long> createRdBom(@Valid @RequestBody ErpRdBomSaveReqVO createReqVO) {
        return success(rdBomService.createRdBom(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新研发 BOM")
    @PreAuthorize("@ss.hasPermission('erp:rd-bom:update')")
    public CommonResult<Boolean> updateRdBom(@Valid @RequestBody ErpRdBomSaveReqVO updateReqVO) {
        rdBomService.updateRdBom(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除研发 BOM")
    @PreAuthorize("@ss.hasPermission('erp:rd-bom:delete')")
    public CommonResult<Boolean> deleteRdBom(@RequestParam("id") Long id) {
        rdBomService.deleteRdBom(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得研发 BOM")
    @PreAuthorize("@ss.hasPermission('erp:rd-bom:query')")
    public CommonResult<ErpRdBomRespVO> getRdBom(@RequestParam("id") Long id) {
        return success(buildRespVO(rdBomService.getRdBom(id)));
    }

    @GetMapping("/page")
    @Operation(summary = "获得研发 BOM 分页")
    @PreAuthorize("@ss.hasPermission('erp:rd-bom:query')")
    public CommonResult<PageResult<ErpRdBomRespVO>> getRdBomPage(@Valid ErpRdBomPageReqVO pageReqVO) {
        PageResult<ErpRdBomDO> pageResult = rdBomService.getRdBomPage(pageReqVO);
        return success(new PageResult<>(buildRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @PutMapping("/publish")
    @Operation(summary = "发布研发 BOM 到制造 BOM 草稿")
    @PreAuthorize("@ss.hasPermission('erp:rd-bom:publish')")
    public CommonResult<Boolean> publishRdBom(@RequestParam("id") Long id) {
        rdBomService.publishRdBom(id);
        return success(true);
    }

    private List<ErpRdBomRespVO> buildRespVOList(List<ErpRdBomDO> list) {
        return convertList(list, this::buildRespVO);
    }

    private ErpRdBomRespVO buildRespVO(ErpRdBomDO rdBom) {
        if (rdBom == null) {
            return null;
        }
        ErpRdBomRespVO respVO = BeanUtils.toBean(rdBom, ErpRdBomRespVO.class);
        fillRespVO(respVO, rdBomService.getRdBomItemList(rdBom.getId()));
        return respVO;
    }

    private void fillRespVO(ErpRdBomRespVO respVO, List<ErpRdBomItemDO> itemList) {
        if (itemList == null) {
            itemList = List.of();
        }
        Map<Long, ErpProductRespVO> productMap = productService.getProductVOMap(convertSet(itemList, ErpRdBomItemDO::getMaterialId));
        List<ErpRdBomItemSubstituteDO> substituteList = rdBomService.getRdBomItemSubstituteList(convertSet(itemList, ErpRdBomItemDO::getId));
        Map<Long, ErpProductRespVO> substituteProductMap = productService.getProductVOMap(convertSet(substituteList, ErpRdBomItemSubstituteDO::getSubstituteMaterialId));
        Map<Long, List<ErpRdBomItemSubstituteDO>> substituteMap = new java.util.HashMap<>();
        for (ErpRdBomItemSubstituteDO substitute : substituteList) {
            substituteMap.computeIfAbsent(substitute.getBomItemId(), key -> new ArrayList<>()).add(substitute);
        }
        ErpProductRespVO product = productService.getProductVOMap(List.of(respVO.getProductId())).get(respVO.getProductId());
        if (product != null) {
            respVO.setProductName(product.getName());
        }
        respVO.setItems(BeanUtils.toBean(itemList, ErpRdBomRespVO.Item.class, item -> {
            ErpProductRespVO material = productMap.get(item.getMaterialId());
            if (material != null) {
                item.setMaterialName(material.getName());
                item.setUnitName(material.getUnitName());
            }
            List<ErpRdBomItemSubstituteDO> itemSubstitutes = substituteMap.get(item.getId());
            if (itemSubstitutes == null || itemSubstitutes.isEmpty()) {
                item.setSubstitutes(List.of());
                return;
            }
            item.setSubstitutes(BeanUtils.toBean(itemSubstitutes, ErpRdBomRespVO.Item.Substitute.class, substitute -> {
                ErpProductRespVO substituteProduct = substituteProductMap.get(substitute.getSubstituteMaterialId());
                if (substituteProduct != null) {
                    substitute.setSubstituteMaterialName(substituteProduct.getName());
                }
            }));
        }));
    }

}
