package com.gumeng.controller.web.shop;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gumeng.code.HttpResponse;
import com.gumeng.entity.vo.shop.GoodsVO;
import com.gumeng.service.shop.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 功能：文创商城
 * 作者：Z
 * 日期：2025/6/4 下午3:59
 */
@RestController
@RequestMapping("/web/shop")
public class ShopController {

    @Autowired
    ProductService productService;

    @GetMapping("/getList")
    public HttpResponse getList(@RequestParam(defaultValue = "1") Integer current,
                                @RequestParam(defaultValue = "10") Integer size) {
        Page<GoodsVO> page = productService.pageGoodsVO(new Page<>(current, size));
        return HttpResponse.success(page);
    }
}
