package com.gumeng.utils;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class QiniuUtils {
    @Value("${qiniu.accessKey}")
    private String accessKey;

    @Value("${qiniu.secretKey}")
    private String secretKey;

    @Value("${qiniu.bucket}")
    private String bucket;

    @Value("${qiniu.domain}")
    private String domain;

    public String upload(byte[] fileBytes, String fileName) {
        // 创建配置对象
        Configuration cfg = new Configuration(Region.huanan());
        UploadManager uploadManager = new UploadManager(cfg);

        try {
            Auth auth = Auth.create(accessKey, secretKey);
            // 修改上传策略，添加覆盖上传的选项
            StringMap putPolicy = new StringMap();
            putPolicy.put("insertOnly", 0); // 设置为0表示允许覆盖上传
            String upToken = auth.uploadToken(bucket, fileName, 3600, putPolicy);
            
            Response response = uploadManager.put(fileBytes, fileName, upToken);
            
            if (response.isOK()) {
                return "http://" + domain + "/" + fileName;
            }
        } catch (QiniuException e) {
            // 如果是文件已存在的错误，尝试返回文件的URL
            if (e.code() == 614) {
                return "http://" + domain + "/" + fileName;
            }
            e.printStackTrace();
        }
        return null;
    }

    public boolean delete(String fileName) {
        try {
            Configuration cfg = new Configuration(Region.huanan());
            Auth auth = Auth.create(accessKey, secretKey);
            BucketManager bucketManager = new BucketManager(auth, cfg);
            bucketManager.delete(bucket, fileName);
            return true;
        } catch (QiniuException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 上传图片到七牛云 model/image/ 目录
     */
    public String uploadModelImage(byte[] fileBytes, String fileName) {
        return upload(fileBytes, "model/image/" + fileName);
    }

    /**
     * 上传模型文件到七牛云 model/model/ 目录
     */
    public String uploadModelFile(byte[] fileBytes, String fileName) {
        return upload(fileBytes, "model/model/" + fileName);
    }
}