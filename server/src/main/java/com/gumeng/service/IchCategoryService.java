package com.gumeng.service;

import com.gumeng.domain.pages.IchCategory;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Chine
* @description 针对表【ich_category(非遗类别附表)】的数据库操作Service
* @createDate 2025-03-26 10:16:14
*/
@Service
public interface IchCategoryService extends IService<IchCategory> {

    List<IchCategory> getIchCategory();
}
