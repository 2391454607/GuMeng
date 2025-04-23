package com.gumeng.controller.visualization;

import com.gumeng.code.HttpResponse;
import com.gumeng.entity.vo.DatasetVO;
import com.gumeng.service.DatasetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/visualization")
public class DatasetController {

    @Autowired
    private DatasetService datasetService;

    @GetMapping("/stats")
    public HttpResponse getDatasetStats() {
        DatasetVO stats = datasetService.getDatasetStats();
        return HttpResponse.success(stats);
    }
} 