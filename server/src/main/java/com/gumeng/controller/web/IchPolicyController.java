package com.gumeng.controller.web;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gumeng.code.HttpResponse;
import com.gumeng.domain.Policy;
import com.gumeng.entity.DTO.PolicyDTO;
import com.gumeng.service.PolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * 功能：用户非遗政策相关功能
 * 作者：Z
 * 日期：2025/5/5 下午5:01
 */
@RestController
@RequestMapping("/web/policy")
public class IchPolicyController {

    @Autowired
    private PolicyService policyService;


    //获取非遗政策列表
    @GetMapping("/getList")
    public HttpResponse getPolicyList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String publishOrg) {
        try {
            // 创建分页对象
            Page<Policy> page = new Page<>(current, size);
            
            // 创建查询条件
            LambdaQueryWrapper<Policy> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Policy::getIsDelete, "0");
            
            // 添加可选的查询条件
            if (StringUtils.hasText(title)) {
                queryWrapper.like(Policy::getTitle, title);
            }
            if (StringUtils.hasText(type)) {
                queryWrapper.eq(Policy::getType, type);
            }
            if (StringUtils.hasText(publishOrg)) {
                queryWrapper.eq(Policy::getPublishOrg, publishOrg);
            }
            
            // 按发布日期降序排序
            queryWrapper.orderByDesc(Policy::getId);
            
            // 执行查询
            IPage<Policy> policyPage = policyService.page(page, queryWrapper);
            
            // 处理返回结果，移除content字段
            policyPage.getRecords().forEach(policy -> policy.setContent(null));
            
            return HttpResponse.success(policyPage);
        } catch (Exception e) {
            return HttpResponse.error("获取政策列表失败：" + e.getMessage());
        }
    }

    //上传政策信息
    @PostMapping("/upload")
    public HttpResponse uploadPolicyPdf(@RequestBody PolicyDTO dto) {
        try {
            if (dto.getBase64File() == null || dto.getBase64File().trim().isEmpty()) {
                return HttpResponse.error("请选择文件");
            }

            // 检查是否为PDF文件
            // 通过检查 base64 文件的头部信息来判断文件类型
            // JVBERi0 是 PDF 文件 base64 编码后的特征头
            String base64Data = dto.getBase64File();
            if (!base64Data.startsWith("JVBERi0")) {
                return HttpResponse.error("只支持PDF文件");
            }

            // 解码base64文件
            byte[] fileContent = Base64.getDecoder().decode(dto.getBase64File());
            
            // 创建政策对象
            Policy policy = new Policy();
            policy.setTitle(dto.getTitle());
            policy.setType(dto.getType());
            policy.setDocumentNumber(dto.getDocumentNumber());
            policy.setPublishOrg(dto.getPublishOrg());
            
            // 修改时间格式解析
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            policy.setPublishDate(LocalDateTime.parse(dto.getPublishDate(), formatter));
            policy.setEffectiveDate(LocalDateTime.parse(dto.getEffectiveDate(), formatter));
            
            policy.setContent(fileContent);
            policy.setCreateTime(LocalDateTime.now());
            policy.setUpdateTime(LocalDateTime.now());
            policy.setIsDelete("0");

            // 保存到数据库
            policyService.save(policy);

            return HttpResponse.success("上传成功");
        } catch (Exception e) {
            return HttpResponse.error("上传失败：" + e.getMessage());
        }
    }

    //修改政策信息
    @PostMapping("/update")
    public HttpResponse updatePolicy(@RequestBody PolicyDTO dto) {
        try {
            // 检查政策是否存在
            Policy existPolicy = policyService.getById(dto.getId());
            if (existPolicy == null) {
                return HttpResponse.error("政策不存在");
            }

            // 如果上传了新的PDF文件
            if (dto.getBase64File() != null && !dto.getBase64File().trim().isEmpty()) {
                if (!dto.getBase64File().startsWith("JVBERi0")) {
                    return HttpResponse.error("只支持PDF文件");
                }
                existPolicy.setContent(Base64.getDecoder().decode(dto.getBase64File()));
            }

            // 更新政策信息
            existPolicy.setTitle(dto.getTitle());
            existPolicy.setType(dto.getType());
            existPolicy.setDocumentNumber(dto.getDocumentNumber());
            existPolicy.setPublishOrg(dto.getPublishOrg());

            // 修改时间格式解析
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            existPolicy.setPublishDate(LocalDateTime.parse(dto.getPublishDate(), formatter));
            existPolicy.setEffectiveDate(LocalDateTime.parse(dto.getEffectiveDate(), formatter));

            existPolicy.setUpdateTime(LocalDateTime.now());

            // 更新数据库
            policyService.updateById(existPolicy);

            return HttpResponse.success("更新成功");
        } catch (Exception e) {
            return HttpResponse.error("更新失败：" + e.getMessage());
        }
    }
}
