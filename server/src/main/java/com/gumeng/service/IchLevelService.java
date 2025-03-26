package com.gumeng.service;

import com.gumeng.domain.pages.IchLevel;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Chine
* @description 针对表【ich_level(非遗保护级别附表)】的数据库操作Service
* @createDate 2025-03-26 10:16:14
*/
@Service
public interface IchLevelService extends IService<IchLevel> {

    List<IchLevel> getIchLevel();
}
