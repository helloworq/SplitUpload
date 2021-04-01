package com.util.splitupload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan("com.util.splitupload.*")
public class SplituploadApplication {

    public static void main(String[] args) {
        SpringApplication.run(SplituploadApplication.class, args);
    }

}
