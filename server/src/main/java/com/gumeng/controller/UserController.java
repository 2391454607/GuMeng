package com.gumeng.controller;

import com.gumeng.code.HttpResponse;
import com.gumeng.domain.User;
import com.gumeng.service.RoleService;
import com.gumeng.service.UserService;
import com.gumeng.utils.BCryptUtil;
import com.gumeng.utils.ThreadLocalUtil;
import org.hibernate.validator.constraints.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 功能：
 * 作者：Z
 * 日期：2025/3/4 下午7:59
 */

@RestController
@RequestMapping("/user") //给当前控制器下的所有接口添加前缀
@Validated
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    //查看当前用户的角色
    @GetMapping("/current/role")
    public HttpResponse getCurrentUserRoles() {
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");
        List<String> role = roleService.getRoleByUserId(userId);
        return HttpResponse.success(role);
    }
    //获取用户信息
    @GetMapping("/getInfo")
    public HttpResponse userInfo() {
        //根据用户名查询用户
        Map<String,Object> map = ThreadLocalUtil.get();
        String username = (String) map.get("username");

        User user = userService.findByUserName(username);
        return HttpResponse.success(user);
    }

    //更新用户信息
    @PutMapping("/update")
    public HttpResponse update(@RequestBody @Validated User user) {
        userService.update(user);
        return HttpResponse.success();
    }

    //更新用户头像
    @PatchMapping("/updateAvatar")
    public HttpResponse updateAvatar(@RequestParam @URL String avatarUrl){
        userService.updateAvatar(avatarUrl);
        return HttpResponse.success();
    }

    //更新用户密码
    @PatchMapping("/updatePwd")
    public HttpResponse updatePwd(@RequestBody Map<String,String> params, @RequestHeader("Authorization") String token){
        //校验参数
        String oldPwd = params.get("old_pwd");
        String newPwd = params.get("new_pwd");
        String rePwd = params.get("re_pwd");

        if (!StringUtils.hasLength(oldPwd) || !StringUtils.hasLength(newPwd) || !StringUtils.hasLength(rePwd)){
            return HttpResponse.error("不能为空");
        }

        //检验原密码是否正确
        //调用 userService 根据用户名拿到原密码，再和 old_pwd 比对
        Map<String,Object> map = ThreadLocalUtil.get();
        String username = (String) map.get("username");
        User loginUser = userService.findByUserName(username);
        if(!BCryptUtil.verify(oldPwd, loginUser.getPassword())){
            return HttpResponse.error("原密码错误");
        }

        //检查 rePwd 和 newPwd 是否一样
        if (!rePwd.equals(newPwd)){
            return HttpResponse.error("两次填写的密码不一样");
        }
        //对新密码进行哈希加密
        String hashedNewPwd = BCryptUtil.hash(newPwd);

        //调用 service 完成密码更新
        userService.updatePwd(hashedNewPwd);
        //删除 redis 中对应的 token
        ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
        operations.getOperations().delete(token);
        return HttpResponse.success();
    }

    //获取用户界面的菜单数据
    @GetMapping("/getMenu")
    public HttpResponse getMenu() {
        Object menuData = userService.getMenu();
        return HttpResponse.success(menuData);
    }




}
