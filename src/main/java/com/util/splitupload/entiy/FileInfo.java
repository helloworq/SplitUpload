package com.util.splitupload.entiy;

import lombok.Data;

@Data
public class FileInfo {
    private String filePath;
    private String fileName;
    private Long fileSize;
}
