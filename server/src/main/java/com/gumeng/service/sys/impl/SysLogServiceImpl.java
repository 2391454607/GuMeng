package com.gumeng.service.sys.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumeng.domain.SysLog;
import com.gumeng.mapper.sys.SysLogMapper;
import com.gumeng.service.sys.SysLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lorn
 * @description 针对表【sys_log(系统操作日志表)】的数据库操作Service实现
 * @createDate 2025-05-14 16:30:01
 */
@Service
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog>
        implements SysLogService{

    @Override
    public Page<SysLog> getLogPage(Integer pageNum, Integer pageSize, String username,
                                   String module, String operation, LocalDateTime startTime, LocalDateTime endTime) {

        LambdaQueryWrapper<SysLog> queryWrapper = new LambdaQueryWrapper<>();

        // 构建查询条件
        if (StringUtils.hasText(username)) {
            queryWrapper.like(SysLog::getUsername, username);
        }

        if (StringUtils.hasText(module)) {
            queryWrapper.eq(SysLog::getModule, module);
        }

        if (StringUtils.hasText(operation)) {
            queryWrapper.eq(SysLog::getOperation, operation);
        }

        if (startTime != null) {
            queryWrapper.ge(SysLog::getCreateTime, startTime);
        }

        if (endTime != null) {
            queryWrapper.le(SysLog::getCreateTime, endTime);
        }

        // 未删除的记录
        queryWrapper.eq(SysLog::getIsDelete, 0);

        // 按照创建时间降序排序
        queryWrapper.orderByDesc(SysLog::getCreateTime);

        // 执行分页查询
        return this.page(new Page<>(pageNum, pageSize), queryWrapper);
    }

    @Override
    public boolean clearAllLogs() {
        LambdaQueryWrapper<SysLog> queryWrapper = new LambdaQueryWrapper<>();
        return this.remove(queryWrapper);
    }

    @Override
    public List<String> getDistinctModules() {
        LambdaQueryWrapper<SysLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(SysLog::getModule)
                .eq(SysLog::getIsDelete, 0)
                .groupBy(SysLog::getModule);

        return this.list(queryWrapper)
                // 将列表转换成 Stream 流
                .stream()
                // 提取每条日志的 module 字段
                .map(SysLog::getModule)
                // 过滤掉空字符串或 null
                .filter(StringUtils::hasText)
                // 去重
                .distinct()
                // 收集成 List 返回
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getDistinctOperations() {
        LambdaQueryWrapper<SysLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(SysLog::getOperation)
                .eq(SysLog::getIsDelete, 0)
                .groupBy(SysLog::getOperation);

        return this.list(queryWrapper)
                .stream()
                .map(SysLog::getOperation)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
    }
}




