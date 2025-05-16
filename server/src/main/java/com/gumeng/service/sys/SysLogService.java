package com.gumeng.service.sys;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gumeng.domain.SysLog;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Lorn
 * @description 针对表【sys_log(系统操作日志表)】的数据库操作Service
 * @createDate 2025-05-14 16:30:01
 */
public interface SysLogService extends IService<SysLog> {

    /**
     * 分页查询日志
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param username 用户名（可选）
     * @param module 模块（可选）
     * @param operation 操作类型（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 日志分页结果
     */
    Page<SysLog> getLogPage(Integer pageNum, Integer pageSize, String username,
                            String module, String operation, LocalDateTime startTime, LocalDateTime endTime);


    /**
     * 清空所有日志
     * @return 操作结果
     */
    boolean clearAllLogs();

    /**
     * 获取所有不重复的操作模块列表
     * @return 模块列表
     */
    List<String> getDistinctModules();

    /**
     * 获取所有不重复的操作类型列表
     * @return 操作类型列表
     */
    List<String> getDistinctOperations();
}
