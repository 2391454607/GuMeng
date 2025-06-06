package com.gumeng.controller.sys.shop;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gumeng.code.HttpResponse;
import com.gumeng.domain.shop.Product;
import com.gumeng.entity.DTO.shop.AddProductDTO;
import com.gumeng.entity.DTO.shop.UpdateProductDTO;
import com.gumeng.service.shop.ProductService;
import com.gumeng.utils.QiniuUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 功能：后台商品管理
 * 作者：Z
 * 日期：2025/6/4 下午4:33
 */
@RestController
@RequestMapping("/sys/shop")
public class SysGoodsController {

    @Autowired
    private ProductService productService;
    
    @Autowired
    private QiniuUtils qiniuUtils;
    
    // 商品图片存储的文件夹路径
    private static final String PRODUCT_IMAGE_FOLDER = "product/images/";

    //获取商品列表
    @GetMapping("/getProductList")
    public HttpResponse getList(@RequestParam(defaultValue = "1") Integer current,
                                @RequestParam(defaultValue = "10") Integer size) {
        Page<Product> page = productService.pageProduct(new Page<>(current, size));
        return HttpResponse.success(page);
    }

    // 临时存储上传的图片信息，key是上传时间戳，value是文件名列表
    private static final Map<Long, List<String>> UPLOADED_FILES = new ConcurrentHashMap<>();
    // 图片保留时间（毫秒）
    private static final long IMAGE_RETENTION_TIME = 10 * 60 * 1000; // 10分钟

    @PostConstruct
    public void init() {
        // 启动定时清理任务
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(60000); // 每分钟检查一次
                    cleanupUnusedImages();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start();
    }

    //上传商品图片
    @PostMapping("/uploadImages")
    public HttpResponse uploadImages(@RequestParam("files") MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return HttpResponse.error("商品图片不能为空");
        }

        List<String> imageUrls = new ArrayList<>();
        List<String> uploadedFiles = new ArrayList<>();
        long timestamp = System.currentTimeMillis();
        
        try {
            for (int i = 0; i < files.length; i++) {
                MultipartFile file = files[i];
                // 检查文件类型
                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    return HttpResponse.error("只支持上传图片文件");
                }

                // 生成文件名（使用时间戳和原始文件扩展名）
                String originalFilename = file.getOriginalFilename();
                String extension = originalFilename != null ?
                        originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
                String fileName = PRODUCT_IMAGE_FOLDER + timestamp + "_" + (i + 1) + extension;

                // 上传图片到七牛云指定文件夹
                byte[] fileBytes = file.getBytes();
                String imageUrl = qiniuUtils.upload(fileBytes, fileName);
                if (imageUrl == null || imageUrl.isEmpty()) {
                    throw new RuntimeException("图片上传失败");
                }

                imageUrls.add(imageUrl);
                uploadedFiles.add(fileName);
            }

            // 保存上传的文件信息
            UPLOADED_FILES.put(timestamp, uploadedFiles);

            // 返回图片信息，包含URL和时间戳
            return HttpResponse.success(new HashMap<String, Object>() {{
                put("urls", imageUrls);
                put("timestamp", timestamp);
            }});

        } catch (Exception e) {
            // 发生异常时，删除所有已上传的图片
            deleteUploadedFiles(uploadedFiles);
            return HttpResponse.error("图片上传失败：" + e.getMessage());
        }
    }

    //新增商品信息
    @PostMapping("/addProductInfo")
    public HttpResponse addProductInfo(@RequestBody AddProductDTO addProductDTO,
                                     @RequestParam("timestamp") Long timestamp) {
        if (addProductDTO == null) {
            return HttpResponse.error("商品信息不能为空");
        }

        // 获取并移除上传的文件信息
        List<String> uploadedFiles = UPLOADED_FILES.remove(timestamp);
        if (uploadedFiles == null) {
            return HttpResponse.error("图片信息已过期，请重新上传");
        }
        
        try {
            // 保存商品信息
            boolean result = productService.save(addProductDTO);
            if (!result) {
                // 保存失败时删除已上传的图片
                deleteUploadedFiles(uploadedFiles);
                return HttpResponse.error("商品信息保存失败");
            }
            
            return HttpResponse.success("新增成功");
            
        } catch (Exception e) {
            // 发生异常时删除已上传的图片
            deleteUploadedFiles(uploadedFiles);
            return HttpResponse.error("新增失败：" + e.getMessage());
        }
    }

    // 清理未使用的图片
    private void cleanupUnusedImages() {
        long currentTime = System.currentTimeMillis();
        List<Long> expiredTimestamps = UPLOADED_FILES.entrySet().stream()
                .filter(entry -> (currentTime - entry.getKey()) > IMAGE_RETENTION_TIME)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        for (Long timestamp : expiredTimestamps) {
            List<String> files = UPLOADED_FILES.remove(timestamp);
            if (files != null) {
                deleteUploadedFiles(files);
            }
        }
    }

    // 删除已上传的图片文件
    private void deleteUploadedFiles(List<String> files) {
        if (files != null && !files.isEmpty()) {
            for (String fileName : files) {
                try {
                    qiniuUtils.delete(fileName);
                } catch (Exception ignored) {
                    // 忽略删除失败的异常
                }
            }
        }
    }

    //修改商品信息
    @PostMapping("/updateProduct")
    public HttpResponse updateProduct(@RequestBody UpdateProductDTO updateProductDTO,
                                    @RequestParam(required = false) Long timestamp) {
        if (updateProductDTO == null) {
            return HttpResponse.error("商品信息不能为空");
        }

        // 如果提供了新的图片（timestamp不为空），处理图片相关逻辑
        List<String> oldImages = null;
        List<String> uploadedFiles = null;
        if (timestamp != null) {
            // 获取并移除上传的新图片信息
            uploadedFiles = UPLOADED_FILES.remove(timestamp);
            if (uploadedFiles == null) {
                return HttpResponse.error("图片信息已过期，请重新上传");
            }

            // 获取商品旧图片信息
            Product oldProduct = productService.getById(updateProductDTO.getId());
            if (oldProduct != null && oldProduct.getImageUrl() != null) {
                oldImages = List.of(oldProduct.getImageUrl().split(","));
            }
        } else if (updateProductDTO.getImageUrl() == null || updateProductDTO.getImageUrl().trim().isEmpty()) {
            // 如果没有上传新图片，且imageUrl为null或空字符串，获取原商品信息并保持原有图片URL
            Product oldProduct = productService.getById(updateProductDTO.getId());
            if (oldProduct != null && oldProduct.getImageUrl() != null) {
                updateProductDTO.setImageUrl(oldProduct.getImageUrl());
            }
        }

        try {
            // 更新商品信息
            boolean result = productService.updateById(updateProductDTO);
            if (!result) {
                // 更新失败时，如果有新上传的图片，删除这些图片
                if (uploadedFiles != null) {
                    deleteUploadedFiles(uploadedFiles);
                }
                return HttpResponse.error("修改失败");
            }

            // 更新成功后，如果上传了新图片，才删除旧图片
            if (timestamp != null && oldImages != null && !oldImages.isEmpty()) {
                deleteUploadedFiles(oldImages);
            }
            
            return HttpResponse.success("修改成功");
            
        } catch (Exception e) {
            // 发生异常时，如果有新上传的图片，删除这些图片
            if (uploadedFiles != null) {
                deleteUploadedFiles(uploadedFiles);
            }
            return HttpResponse.error("修改失败：" + e.getMessage());
        }
    }

    //删除商品
    @PostMapping("/deleteProduct")
    public HttpResponse deleteProduct(@RequestParam Long id) {
        // 获取商品信息
        Product product = productService.getById(id);
        if (product == null) {
            return HttpResponse.error("商品不存在");
        }

        // 获取商品图片URL列表
        List<String> imageUrls = null;
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            imageUrls = List.of(product.getImageUrl().split(","));
        }

        // 删除商品
        boolean result = productService.removeById(id);
        if (!result) {
            return HttpResponse.error("删除失败");
        }

        // 删除成功后，删除相关的图片
        if (imageUrls != null && !imageUrls.isEmpty()) {
            deleteUploadedFiles(imageUrls);
        }

        return HttpResponse.success("删除成功");
    }

}
