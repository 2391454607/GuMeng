package com.gumeng.service.shop.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumeng.domain.shop.Product;
import com.gumeng.entity.DTO.shop.AddProductDTO;
import com.gumeng.entity.DTO.shop.UpdateProductDTO;
import com.gumeng.entity.vo.shop.GoodsVO;
import com.gumeng.service.shop.ProductService;
import com.gumeng.mapper.shop.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
* @author Chine
* @description 针对表【product(文创商品表)】的数据库操作Service实现
* @createDate 2025-04-26 23:23:52
*/
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product>
    implements ProductService{

    @Autowired
    private ProductMapper productMapper;

    //后台管理
    @Override
    public Page<Product> pageProduct(Page<Product> page) {
        return productMapper.selectProductPage(page);
    }
    //添加商品
    @Override
    public boolean save(AddProductDTO addProductDTO) {
        return productMapper.addProduct(addProductDTO);
    }
    //修改
    @Override
    public boolean updateById(UpdateProductDTO updateProductDTO) {
        return productMapper.updateProduct(updateProductDTO);
    }

    //文创商城
    @Override
    public Page<GoodsVO> pageGoodsVO(Page<GoodsVO> page) {
        return productMapper.selectGoodsVOPage(page);
    }



}




