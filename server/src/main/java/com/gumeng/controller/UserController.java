package com.gumeng.controller;

import com.gumeng.entity.Menu;
import com.gumeng.entity.User;
import com.gumeng.entity.Result;
import com.gumeng.service.UserService;
import com.github.pagehelper.PageInfo;
import com.gumeng.utils.Argon2Util;
import com.gumeng.utils.JwtUtil;
import com.gumeng.utils.ThreadLocalUtil;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
    private StringRedisTemplate stringRedisTemplate;

    //用户注册
    @PostMapping("/register")
    public Result register(@Pattern(regexp = "^\\S{5,16}$") String username, @Pattern(regexp = "^(?=.*[A-Za-z])\\S{8,16}$") String password) {
        //查询用户是否注册
        User u = userService.findByUserName(username);
        if (u==null){
            //注册
            userService.register(username,password);
            return Result.success();
        }else{
            //该用户名已被注册
            return Result.error("该用户名已被注册！");
        }
    }

    //用户登录
    @PostMapping("/login")
    public Result<String> login(@RequestBody Map<String,String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        //根据用户名查询用户
        User loginUser = userService.findByUserName(username);
        //判断用户是否存在
        if (loginUser==null){
            return Result.error("用户名不存在");
        }
        //判断密码是否正确
        if (Argon2Util.verify(password, loginUser.getPassword())){
            //登录成功
            Map<String,Object> claims = new HashMap<>();
            claims.put("id",loginUser.getId());
            claims.put("username",loginUser.getUsername());
            String token = JwtUtil.genToken(claims);

            //将token注入到redis中存储
            ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
            operations.set(token,token,1, TimeUnit.HOURS);

            return Result.success(token);
        }
        return Result.error("密码错误");
    }

    @GetMapping("/userInfo")
    public Result<User> userInfo() {
        //根据用户名查询用户
        Map<String,Object> map = ThreadLocalUtil.get();
        String username = (String) map.get("username");

        User user = userService.findByUserName(username);
        return Result.success(user);
    }

    //更新用户信息
    @PutMapping("/update")
    public Result update(@RequestBody @Validated User user) {
        userService.update(user);
        return Result.success();
    }

    //更新用户头像
    @PatchMapping("/updateAvatar")
    public Result updateAvatar(@RequestParam @URL String avatarUrl){
        userService.updateAvatar(avatarUrl);
        return Result.success();
    }

    //更新用户密码
    @PatchMapping("/updatePwd")
    public Result updatePwd(@RequestBody Map<String,String> params, @RequestHeader("Authorization") String token){
        //校验参数
        String oldPwd = params.get("old_pwd");
        String newPwd = params.get("new_pwd");
        String rePwd = params.get("re_pwd");

        if (!StringUtils.hasLength(oldPwd) || !StringUtils.hasLength(newPwd) || !StringUtils.hasLength(rePwd)){
            return Result.error("不能为空");
        }

        //检验原密码是否正确
        //调用 userService 根据用户名拿到原密码，再和 old_pwd 比对
        Map<String,Object> map = ThreadLocalUtil.get();
        String username = (String) map.get("username");
        User loginUser = userService.findByUserName(username);
        if(!Argon2Util.verify(oldPwd, loginUser.getPassword())){
            return Result.error("原密码错误");
        }

        //检查 rePwd 和 newPwd 是否一样
        if (!rePwd.equals(newPwd)){
            return Result.error("两次填写的密码不一样");
        }
        //对新密码进行哈希加密
        String hashedNewPwd = Argon2Util.hash(newPwd);

        //调用 service 完成密码更新
        userService.updatePwd(hashedNewPwd);
        //删除 redis 中对应的 token
        ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
        operations.getOperations().delete(token);
        return Result.success();
    }

    //获取用户界面的菜单数据
    @GetMapping("/getMenu")
    public List<Menu> getUserMenu() {
        return userService.getUserMenu();
    }


    //查询所有用户信息
    @GetMapping("/findAll")
    public List<User> findAll() {
        //数据返回
        return userService.findAll();
    }

    //插入用户信息
    @PostMapping("/insert")
    public Boolean insert(@RequestBody User user) {
        userService.insert(user);
        return true;
    }

    //删除用户信息
    @DeleteMapping("/delete")
    public Boolean delete(@RequestBody Integer[] ids) { // 通过 id 删除用户
        userService.delete(ids);
        return true;
    }

    //分页查询
    @GetMapping("/findPage")
    public PageInfo<User> findPage(
            @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "username", defaultValue = "") String username
            ){
        return userService.findPage(pageNum,pageSize,username);
    }


}
