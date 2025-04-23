package com.gumeng.controller.model;

import com.gumeng.code.HttpResponse;
import com.gumeng.service.web.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 功能：3D模型相关接口
 * 作者：Z
 * 日期：2025/4/22 下午6:26
 */
@RestController
@RequestMapping("/web") //给当前控制器下的所有接口添加前缀
public class ModelController {

    @Autowired
    private ModelService modelService;


    @GetMapping("/getModelList")
    public HttpResponse getModelList(){
        Object ModelList = modelService.getModelList();
        return HttpResponse.success(ModelList);
    }

    @GetMapping("/getModel")
    public HttpResponse getModel(@RequestParam Integer modelId){
        Object Model = modelService.getModel(modelId);
        return HttpResponse.success(Model);
    }

}
