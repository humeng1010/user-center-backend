package com.usercenter.controller;

import cn.hutool.core.lang.UUID;
import com.usercenter.common.BaseResponse;
import com.usercenter.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/upload")
@Slf4j
public class UploadController {


    @Value("${upload-img.path}")
    private String basePath;


    @PostMapping
    public BaseResponse<String> uploadAvatar(MultipartFile file) {
        try {
//            防止目录不存在
            File file1 = new File(basePath);
            if (!file1.exists()) {
                boolean mkdirs = file1.mkdirs();
                if (!mkdirs) throw new BusinessException(50008, "服务器创建文件夹失败", "请检查路径是否正确");
            }
            String uuid = UUID.randomUUID().toString();
            String originalFilename = file.getOriginalFilename();
            log.info("文件的原始名称:{}", originalFilename);
            assert originalFilename != null;
            // 获取文件后缀 带 .
            String substring = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = uuid + substring;
//            文件转储
            file.transferTo(new File(basePath + fileName));
            return BaseResponse.ok(fileName, "上传文件成功");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
