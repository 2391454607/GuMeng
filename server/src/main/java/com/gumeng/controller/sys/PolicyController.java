package com.gumeng.controller.sys;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gumeng.code.HttpResponse;
import com.gumeng.domain.Policy;
import com.gumeng.entity.DTO.PolicyDTO;
import com.gumeng.service.PolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;

/**
 * 功能：政策信息后台管理
 * 作者：Z
 * 日期：2025/4/11 上午9:20
 */
@RestController
@RequestMapping("/sys/policy") //给当前控制器下的所有接口添加前缀
@PreAuthorize("hasAnyAuthority('admin','superAdmin')")
public class PolicyController {

    @Autowired
    private PolicyService policyService;


    //获取非遗政策列表
    @GetMapping("/getList")
    public HttpResponse getPolicyList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            // 创建分页对象
            Page<Policy> page = new Page<>(current, size);

            // 创建查询条件
            QueryWrapper<Policy> queryWrapper = new QueryWrapper<Policy>()
                    .select("id", "title", "type", "document_number", "publish_org",
                            "publish_date", "effective_date","create_time", "update_time")
                    .orderByDesc("id");

            // 执行查询
            IPage<Policy> policyPage = policyService.page(page, queryWrapper);

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

            if (dto.getPublishDate() != null) {
                policy.setPublishDate(dto.getPublishDate());
            }
            if (dto.getEffectiveDate() != null) {
                policy.setEffectiveDate(dto.getEffectiveDate());
            }

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

            // 创建UpdateWrapper
            UpdateWrapper<Policy> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", dto.getId())
                    .set("title", dto.getTitle())
                    .set("type", dto.getType())
                    .set("document_number", dto.getDocumentNumber())
                    .set("publish_org", dto.getPublishOrg())
                    .set("publish_date", dto.getPublishDate())
                    .set("effective_date", dto.getEffectiveDate())
                    .set("update_time", LocalDateTime.now());

            // 如果有新的PDF文件
            if (dto.getBase64File() != null && !dto.getBase64File().trim().isEmpty()) {
                updateWrapper.set("content", Base64.getDecoder().decode(dto.getBase64File()));
            }

            // 更新数据库
            boolean result = policyService.update(null, updateWrapper);
            if (!result) {
                return HttpResponse.error("更新失败");
            }

            return HttpResponse.success("更新成功");
        } catch (Exception e) {
            return HttpResponse.error("更新失败：" + e.getMessage());
        }
    }

    //删除
    @PostMapping("/deletePolicy")
    public HttpResponse deletePolicy(@RequestParam Integer id) {
        return HttpResponse.success();
    }
}
