package com.util.splitupload.controller;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;

/**
 * 客户端之间的分片上传工具
 */
public class splitUpload2Client {

    public static final String basePath = "C:\\Users\\12733\\Desktop\\工作台\\unkonw\\";
    public static final String fileOrigin = "甘肃省国土空间规划“一张图”实施监督信息系统-详细设计0312.docx";
    public static final String txtOrigin = "test.txt";

    public static void main(String[] args) throws IOException {
        Path path = Files.createTempFile(Paths.get(basePath), "temp", ".txt");
        RandomAccessFile randomAccessFile = new RandomAccessFile(path.toString(), "rw");

        File file = path.toFile();

        byte[] bytesA = new byte[1];
        byte[] bytesB = new byte[2];
        byte[] bytesC = new byte[2];

        RandomAccessFile randomAccessFileBase = new RandomAccessFile(basePath + txtOrigin, "rw");
        //0   1 2   3 4
        //randomAccessFileBase.read(bytesA, 0, 1);
        //randomAccessFileBase.read(bytesB, 0, 2);
        System.out.println(randomAccessFile.getFilePointer());
        randomAccessFileBase.read(bytesC, 0, 2);
        System.out.println(randomAccessFile.getFilePointer());

//        randomAccessFile.seek(1);
//        randomAccessFile.write(bytesB);
        randomAccessFile.seek(3);
        randomAccessFile.write(bytesC);
//        randomAccessFile.seek(0);
//        randomAccessFile.write(bytesA);
        randomAccessFile.close();
    }

    public void upload() {

    }
}
