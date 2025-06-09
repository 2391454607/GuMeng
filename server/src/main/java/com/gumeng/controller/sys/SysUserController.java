package com.gumeng.controller.sys;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gumeng.code.HttpResponse;
import com.gumeng.domain.user.User;
import com.gumeng.service.sys.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * 功能：
 * 作者：Z
 * 日期：2025/4/21 下午7:43
 */
@RestController
@RequestMapping("/sys/user") //给当前控制器下的所有接口添加前缀
@PreAuthorize("hasAnyAuthority('admin','superAdmin')")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    /**
     * 获取用户列表（分页）
     * @param current 当前页码
     * @param size 每页大小
     * @return 分页结果
     */
    @GetMapping("/getUserList")
    public HttpResponse getUserList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            //创建分页对象
            Page<User> page = new Page<>(current, size);
            // 创建查询条件
            QueryWrapper<User> queryWrapper = new QueryWrapper<User>()
                    .select("id", "nickname", "username", "user_pic",
                            "address", "email","create_time", "update_time")
                    .eq("is_deleted", 0); // 未删除条件;

            IPage<User> UserPage = sysUserService.page(page, queryWrapper);

            return HttpResponse.success(UserPage);

        }catch (Exception e){
            return HttpResponse.error("获取用户列表失败：" + e.getMessage());
        }

    }

}
