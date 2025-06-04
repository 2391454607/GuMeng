package com.gumeng.mapper.shop;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gumeng.domain.shop.Product;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gumeng.entity.DTO.shop.AddProductDTO;
import com.gumeng.entity.DTO.shop.UpdateProductDTO;
import com.gumeng.entity.vo.shop.GoodsVO;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Chine
* @description 针对表【product(文创商品表)】的数据库操作Mapper
* @createDate 2025-04-26 23:23:52
* @Entity com.gumeng.domain.shop.Product
*/
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    Page<GoodsVO> selectGoodsVOPage(Page<?> page);

    Page<Product> selectProductPage(Page<Product> page);

    boolean addProduct(AddProductDTO addProductDTO);

    boolean updateProduct(UpdateProductDTO updateProductDTO);
}




