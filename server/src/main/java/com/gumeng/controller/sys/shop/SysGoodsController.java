package com.gumeng.controller.sys.shop;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gumeng.code.HttpResponse;
import com.gumeng.domain.shop.Product;
import com.gumeng.entity.DTO.shop.AddProductDTO;
import com.gumeng.entity.DTO.shop.UpdateProductDTO;
import com.gumeng.service.shop.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    //获取商品列表
    @GetMapping("/getList")
    public HttpResponse getList(@RequestParam(defaultValue = "1") Integer current,
                                @RequestParam(defaultValue = "10") Integer size) {
        Page<Product> page = productService.pageProduct(new Page<>(current, size));
        return HttpResponse.success(page);
    }

    //新增商品
    @PostMapping("/addProduct")
    public HttpResponse add(@RequestBody AddProductDTO addProductDTO) {
        boolean result = productService.save(addProductDTO);
        return result ? HttpResponse.success("新增成功") : HttpResponse.error("新增失败");
    }

    //修改商品
    @PostMapping("/updateProduct")
    public HttpResponse update(@RequestBody UpdateProductDTO updateProductDTO) {
        boolean result = productService.updateById(updateProductDTO);
        return result ? HttpResponse.success("修改成功") : HttpResponse.error("修改失败");
    }

    //删除商品
    @PostMapping("/delete")
    public HttpResponse delete(@RequestParam Long id) {
        boolean result = productService.removeById(id);
        return result ? HttpResponse.success("删除成功") : HttpResponse.error("删除失败");
    }

}
