package com.util.splitupload.dao;

import com.util.splitupload.entiy.FileInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileInfoRepository extends JpaRepository<FileInfo, String> {
    FileInfo findByFileMd5(String md5);
}
