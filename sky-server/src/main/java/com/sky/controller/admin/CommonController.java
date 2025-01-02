package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController//类中每个处理 HTTP 请求的方法返回的对象会直接序列化为 JSON
@RequestMapping("//admin/common")
@Api(tags="通用接口")
@Slf4j
public class CommonController {

    @Autowired//依赖注入
    private AliOssUtil aliOssUtil;
    //前端传入参数file 二进制
    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file){
        log.info("文件上传:{}",file);

        try {
            //原始文件名
            String orginalFilename=file.getOriginalFilename();
            //截取后缀
            String extention=orginalFilename.substring(orginalFilename.lastIndexOf("."));
            //UUID后的新文件路径
            String objectName= UUID.randomUUID().toString()+extention;

            //文件的请求路径
            String filePath=aliOssUtil.upload(file.getBytes(), objectName);
            return Result.success(filePath);

        } catch (IOException e) {
            log.error("文件上传失败：{}",e);
        }

        return Result.success(MessageConstant.UPLOAD_FAILED);
    }
}
