package com.util.splitupload.controller;

import ch.qos.logback.core.util.FileUtil;
import com.alibaba.fastjson.JSON;
import com.sun.deploy.net.HttpResponse;
import com.util.splitupload.entiy.FileInfo;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

import org.apache.tomcat.util.http.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import sun.net.www.http.HttpClient;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RestController
public class GetData {

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    String uploadPath = System.getProperty("user.dir");//上传文件的保存路径

    /**
     * 处理上传文件
     *
     * @param fileMd5
     * @param file
     * @param fileSize
     * @param position
     * @param fileName
     * @throws IOException
     */
    @PostMapping("/upload")
    public void upload(@RequestParam("fileMd5") @ApiParam(value = "文件MD5值", required = true) String fileMd5,
                       @RequestParam("file") @ApiParam(value = "文件", required = true) MultipartFile file,
                       @RequestParam("fileSize") @ApiParam(value = "文件大小", required = true) String fileSize,
                       @RequestParam("position") @ApiParam(value = "文件位置", required = true) String position,
                       @RequestParam("fileName") @ApiParam(value = "文件名", required = true) String fileName) throws IOException {

        FileInfo fileInfo = JSON.parseObject(JSON.toJSONString(redisTemplate.opsForValue().get(fileMd5)), FileInfo.class);

        if (Objects.nonNull(fileInfo)) {
            RandomAccessFile randomAccessFile = new RandomAccessFile(fileInfo.getFilePath(), "rw");
            if (randomAccessFile.length() == Long.parseLong(fileSize)) {
                randomAccessFile.close();//文件已完整存储
                //TODO 将文件信息持久化进数据库
                return;
            } else {
                randomAccessFile.seek(Long.parseLong(position));
                randomAccessFile.write(file.getBytes());
                randomAccessFile.close();
                fileInfo.setFileSize(fileInfo.getFileSize() + file.getSize());
                redisTemplate.opsForValue().set(fileMd5, fileInfo);
            }
        } else {
            File filePrev = new File(uploadPath + fileMd5 + "." + fileName.split("\\.")[1]);
            if (!filePrev.exists()) {
                filePrev.createNewFile();
            }
            RandomAccessFile randomAccessFile = new RandomAccessFile(filePrev.getPath(), "rw");

            randomAccessFile.seek(Long.parseLong(position));
            randomAccessFile.write(file.getBytes());
            randomAccessFile.close();

            FileInfo fileInfoPrev = new FileInfo();
            fileInfoPrev.setFilePath(filePrev.getPath());//文件存储路径
            fileInfoPrev.setFileName(fileName);//文件原始名
            fileInfoPrev.setFileSize(file.getSize());//文件大小
            redisTemplate.opsForValue().set(fileMd5, fileInfoPrev);
        }
    }

    /**
     * 依据传入的md5获取此文件在服务器上的大小
     *
     * @param fileMd5
     * @return
     */
    @PostMapping("/getSize")
    public String getSize(@RequestParam("fileMd5") @ApiParam(value = "文件MD5值", required = true) String fileMd5) {
        System.out.println(uploadPath);
        System.out.println("请求文件MD5: " + fileMd5);
        FileInfo fileInfo = JSON.parseObject(JSON.toJSONString(redisTemplate.opsForValue().get(fileMd5)), FileInfo.class);
        return fileInfo == null ? "0" : String.valueOf(fileInfo.getFileSize());
    }

}
