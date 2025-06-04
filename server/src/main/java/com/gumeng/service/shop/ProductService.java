package com.gumeng.service.shop;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gumeng.domain.shop.Product;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gumeng.entity.DTO.shop.AddProductDTO;
import com.gumeng.entity.DTO.shop.UpdateProductDTO;
import com.gumeng.entity.vo.shop.GoodsVO;
import org.springframework.stereotype.Service;

/**
* @author Chine
* @description 针对表【product(文创商品表)】的数据库操作Service
* @createDate 2025-04-26 23:23:52
*/
@Service
public interface ProductService extends IService<Product> {
    //后台管理
    Page<Product> pageProduct(Page<Product> page);

    boolean save(AddProductDTO addProductDTO);

    boolean updateById(UpdateProductDTO updateProductDTO);


    //文创商城
    Page<GoodsVO> pageGoodsVO(Page<GoodsVO> page);

}
