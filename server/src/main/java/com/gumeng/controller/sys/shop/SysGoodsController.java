package com.gumeng.controller.sys.shop;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gumeng.code.HttpResponse;
import com.gumeng.domain.shop.Product;
import com.gumeng.entity.vo.shop.GoodsVO;
import com.gumeng.service.shop.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 功能：后台商品管理
 * 作者：Z
 * 日期：2025/6/4 下午4:33
 */
@RestController
@RequestMapping("/sys/shop") //给当前控制器下的所有接口添加前缀
public class SysGoodsController {

    @Autowired
    private ProductService productService;

    @GetMapping("/getList")
    public HttpResponse getList(@RequestParam(defaultValue = "1") Integer current,
                                @RequestParam(defaultValue = "10") Integer size) {
        Page<Product> page = productService.pageProduct(new Page<>(current, size));
        return HttpResponse.success(page);
    }
}
