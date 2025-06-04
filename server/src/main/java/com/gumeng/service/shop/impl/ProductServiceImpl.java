package com.gumeng.service.shop.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumeng.domain.shop.Product;
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

    @Override
    public Page<Product> pageProduct(Page<Product> page) {
        return productMapper.selectProductPage(page);
    }

    @Override
    public Page<GoodsVO> pageGoodsVO(Page<GoodsVO> page) {
        return productMapper.selectGoodsVOPage(page);
    }

}




