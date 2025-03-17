package com.gumeng.controller;

import com.gumeng.entity.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 功能：
 * 作者：Z
 * 日期：2025/3/10 下午9:48
 */
@RestController
@RequestMapping("/pages")
public class PagesController {
    @GetMapping("/list")
    public Result<String> list() {

        return Result.success("访问数据");
    }
}
