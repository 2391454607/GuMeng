package com.gumeng.controller.sys;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 功能：
 * 作者：Z
 * 日期：2025/4/21 下午7:43
 */
@RestController
@RequestMapping("/sys") //给当前控制器下的所有接口添加前缀
@PreAuthorize("hasAnyAuthority('admin','superAdmin')")
public class SysUserController {



}
