package com.example.springpasswordlesssftp;

import java.io.File;
import java.io.FileInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringPasswordlessSftpApplication implements CommandLineRunner {

	@Autowired
	SFTPUtil sftpUtil;
	
	@Value("${sftp.path}")
	private String path;
	
	private Logger log = LoggerFactory.getLogger(this.getClass());
	

	public static void main(String[] args) {
		SpringApplication.run(SpringPasswordlessSftpApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		String filetoUpload = args[0];
		log.info("Uploading the file {}", filetoUpload);
		File file = new File(filetoUpload);
		sftpUtil.sendFileToRemoteDirectory(new FileInputStream(file), path+"/"+file.getName());
		log.info("Uploading Done");
	
	}

}
