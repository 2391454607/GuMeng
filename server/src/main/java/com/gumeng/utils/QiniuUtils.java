package com.gumeng.utils;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
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
            String upToken = auth.uploadToken(bucket);
            Response response = uploadManager.put(fileBytes, fileName, upToken);
            
            if (response.isOK()) {
                return "http://" + domain + "/" + fileName;
            }
        } catch (QiniuException e) {
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
}