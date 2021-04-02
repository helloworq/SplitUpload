package com.util.splitupload.controller;

import com.alibaba.fastjson.JSON;
import com.util.splitupload.dao.FileInfoRepository;
import com.util.splitupload.entiy.FileInfo;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.Objects;

@Slf4j
@RestController
public class GetData {

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @Autowired
    FileInfoRepository fileInfoRepository;

    private String uploadPath = System.getProperty("user.dir");//上传文件的保存路径

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
                       @RequestParam("fileName") @ApiParam(value = "文件名", required = true) String fileName,
                       HttpServletResponse response) throws IOException {

        FileInfo fileInfo = JSON.parseObject(JSON.toJSONString(redisTemplate.opsForValue().get(fileMd5)), FileInfo.class);

        if (Objects.nonNull(fileInfo)) {
            RandomAccessFile randomAccessFile = new RandomAccessFile(fileInfo.getFilePath(), "rw");
            //判断本地存储文件是否完整存储，是，则存储上传信息，不是则开始上传
            if (randomAccessFile.length() == Long.parseLong(fileSize)) {
                randomAccessFile.close();
                fileInfoRepository.save(fileInfo);
                response.setStatus(201);
            } else {
                //上传进度中
                randomAccessFile.seek(Long.parseLong(position));
                randomAccessFile.write(file.getBytes());
                fileInfo.setFileSize(fileInfo.getFileSize() + file.getSize());
                try {
                    //防止redis保存数据时出错丢失进度
                    redisTemplate.opsForValue().set(fileMd5, fileInfo);
                } catch (Exception e) {
                    System.out.println("redis缓存出错!");
                    fileInfoRepository.save(fileInfo);
                    randomAccessFile.close();
                }
                //判断是否完成存储
                if (randomAccessFile.length() == Long.parseLong(fileSize)) {
                    fileInfoRepository.save(fileInfo);
                    response.setStatus(201);
                }
                randomAccessFile.close();
            }
        } else {
            //初次上传
            File filePrev = new File(uploadPath + fileMd5 + getFileSuffix(fileName));
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
            fileInfoPrev.setFileMd5(fileMd5);
            FileInfo fileInfoSaved = fileInfoRepository.save(fileInfoPrev);
            fileInfoPrev.setId(fileInfoSaved.getId());
            redisTemplate.opsForValue().set(fileMd5, fileInfoPrev);
        }
    }

    /**
     * 依据传入的md5获取此文件在服务器上的大小
     * <p>
     * 可能是初次上传时请求，也可能是上传过程中的请求，也可能是断了很久之后再次上传的请求
     *
     * @param fileMd5
     * @return
     */
    @PostMapping("/getSize")
    public String getSize(@RequestParam("fileMd5") @ApiParam(value = "文件MD5值", required = true) String fileMd5) {
        //启用redis缓存支持，减少对数据的请求次数
        FileInfo fileInfo = JSON.parseObject(JSON.toJSONString(redisTemplate.opsForValue().get(fileMd5)), FileInfo.class);
        if (Objects.nonNull(fileInfo)) {
            //redis已缓存数据时直接走redis请求数据
            return String.valueOf(fileInfo.getFileSize());
        } else {
            FileInfo fileInfoSaved = fileInfoRepository.findByFileMd5(fileMd5);
            if (Objects.nonNull(fileInfoSaved)) {
                //如果redis未存储，但是数据库存储过则返回数据
                return String.valueOf(fileInfoSaved.getFileSize());
            }
        }
        return "0";//文件初次上传情况
    }

    public String getFileSuffix(String fileName) {
        int position = fileName.lastIndexOf(".");
        return fileName.substring(position);
    }

    /**
     * redis保存数据方式，直接看效果不使用数据库的话可以启用下面的接口
     */
//    /**
//     * 处理上传文件
//     *
//     * @param fileMd5
//     * @param file
//     * @param fileSize
//     * @param position
//     * @param fileName
//     * @throws IOException
//     */
//    @PostMapping("/upload")
//    public void upload(@RequestParam("fileMd5") @ApiParam(value = "文件MD5值", required = true) String fileMd5,
//                       @RequestParam("file") @ApiParam(value = "文件", required = true) MultipartFile file,
//                       @RequestParam("fileSize") @ApiParam(value = "文件大小", required = true) String fileSize,
//                       @RequestParam("position") @ApiParam(value = "文件位置", required = true) String position,
//                       @RequestParam("fileName") @ApiParam(value = "文件名", required = true) String fileName) throws IOException {
//
//        FileInfo fileInfo = JSON.parseObject(JSON.toJSONString(redisTemplate.opsForValue().get(fileMd5)), FileInfo.class);
//
//        if (Objects.nonNull(fileInfo)) {
//            RandomAccessFile randomAccessFile = new RandomAccessFile(fileInfo.getFilePath(), "rw");
//            if (randomAccessFile.length() == Long.parseLong(fileSize)) {
//                randomAccessFile.close();//文件已完整存储
//                //TODO 将文件信息持久化进数据库
//                return;
//            } else {
//                randomAccessFile.seek(Long.parseLong(position));
//                randomAccessFile.write(file.getBytes());
//                randomAccessFile.close();
//                fileInfo.setFileSize(fileInfo.getFileSize() + file.getSize());
//                redisTemplate.opsForValue().set(fileMd5, fileInfo);
//            }
//        } else {
//            File filePrev = new File(uploadPath + fileMd5 + "." + fileName.split("\\.")[1]);
//            if (!filePrev.exists()) {
//                filePrev.createNewFile();
//            }
//            RandomAccessFile randomAccessFile = new RandomAccessFile(filePrev.getPath(), "rw");
//
//            randomAccessFile.seek(Long.parseLong(position));
//            randomAccessFile.write(file.getBytes());
//            randomAccessFile.close();
//
//            FileInfo fileInfoPrev = new FileInfo();
//            fileInfoPrev.setFilePath(filePrev.getPath());//文件存储路径
//            fileInfoPrev.setFileName(fileName);//文件原始名
//            fileInfoPrev.setFileSize(file.getSize());//文件大小
//            redisTemplate.opsForValue().set(fileMd5, fileInfoPrev);
//        }
//    }

//    /**
//     * 依据传入的md5获取此文件在服务器上的大小
//     *
//     * @param fileMd5
//     * @return
//     */
//    @PostMapping("/getSize")
//    public String getSize(@RequestParam("fileMd5") @ApiParam(value = "文件MD5值", required = true) String fileMd5) {
//        System.out.println(uploadPath);
//        System.out.println("请求文件MD5: " + fileMd5);
//        FileInfo fileInfo = JSON.parseObject(JSON.toJSONString(redisTemplate.opsForValue().get(fileMd5)), FileInfo.class);
//        return fileInfo == null ? "0" : String.valueOf(fileInfo.getFileSize());
//    }

}
