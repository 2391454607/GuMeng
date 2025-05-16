package com.gumeng.controller.sys;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gumeng.code.HttpResponse;
import com.gumeng.domain.SysLog;
import com.gumeng.service.sys.SysLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author:Lorn(黎龙)
 * @Project：GuMeng
 * @Date：2025/5/14
 * @Time:14:21
 */
@RestController
@RequestMapping("/sys/log")
@PreAuthorize("hasAnyAuthority('admin','superAdmin')")
public class SysLogController {

    @Autowired
    private SysLogService sysLogService;

    /**
     * 分页查询系统操作日志
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param username 用户名（可选）
     * @param module 模块（可选）
     * @param operation 操作类型（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 日志分页结果
     */
    @GetMapping("/list")
    public HttpResponse getLogList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        return HttpResponse.success(sysLogService.getLogPage(pageNum, pageSize, username, module, operation, startTime, endTime));
    }

    /**
     * 获取日志详情
     * @param id 日志ID
     * @return 日志详情
     */
    @GetMapping("/{id}")
    public HttpResponse getLogDetail(@PathVariable Integer id) {
        // 从数据库查记录然后封装成SysLog对象返回
        SysLog log = sysLogService.getById(id);
        if (log == null) {
            return HttpResponse.error("日志不存在");
        }
        return HttpResponse.success(log);
    }

    /**
     * 删除单个日志
     * @param id 日志ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public HttpResponse deleteLog(@PathVariable Integer id) {
        boolean result = sysLogService.removeById(id);
        return result ? HttpResponse.success("删除成功") : HttpResponse.error("删除失败");
    }

    /**
     * 批量删除日志
     * @param ids 日志ID列表
     * @return 操作结果
     */

    @DeleteMapping("/batch")
    public HttpResponse batchDeleteLog(@RequestBody List<Integer> ids) {
        boolean result = sysLogService.removeByIds(ids);
        return result ? HttpResponse.success("批量删除成功") : HttpResponse.error("批量删除失败");
    }

    /**
     * 清空所有日志
     * @return 操作结果
     */
    @DeleteMapping("/clear")
    @PreAuthorize("hasAuthority('superAdmin')")
    public HttpResponse clearAllLogs() {
        boolean result = sysLogService.clearAllLogs();
        return result ? HttpResponse.success("清空日志成功") : HttpResponse.error("清空日志失败");
    }

    /**
     * 获取操作模块列表（用于筛选）
     * @return 模块列表
     */
    @GetMapping("/modules")
    public HttpResponse getModuleList() {
        List<String> modules = sysLogService.getDistinctModules();
        return HttpResponse.success(modules);
    }

    /**
     * 获取操作类型列表（用于筛选）
     * @return 操作类型列表
     */
    @GetMapping("/operations")
    public HttpResponse getOperationList() {
        List<String> operations = sysLogService.getDistinctOperations();
        return HttpResponse.success(operations);
    }
}
