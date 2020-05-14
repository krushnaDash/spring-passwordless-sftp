package com.example.springpasswordlesssftp;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.UserInfo;

@Component
public class SFTPUtil {

	@Value("${sftp.host}")
	private String sftpHost;
	@Value("${sftp.user.name}")
	private String sftpUserName;
	@Value("${sftp.port}")
	private int sftpHostPort;
	@Value("${private.key.path}")
	private String keyPath;

	private Logger log = LoggerFactory.getLogger(this.getClass());

	public void sendFileToRemoteDirectory(InputStream in, String remoteDir) throws JSchException, SftpException {
		JSch jsch = new JSch();
		// set password less connectivity
		jsch.addIdentity(keyPath, "");
		log.info("private key added from path {}", keyPath);
		log.info("remote dir path {}", remoteDir);
		int retryCount = 0;
		sendFile(in, remoteDir, jsch, retryCount);
	}

	private void sendFile(InputStream in, String remoteDir, JSch jsch, int retryCount)
			throws JSchException, SftpException {
		log.info("Start sending file to SFTP Directory");
		Session session = null;
		ChannelSftp sftpChannel = null;
		try {
			session = jsch.getSession(sftpUserName, sftpHost, sftpHostPort);
			log.info("session created.");
			session.setConfig("StrictHostKeyChecking", "no");

			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);

			session.connect();
			Channel channel = session.openChannel("sftp");
			channel.connect();
			log.info("shell channel connected....");
			sftpChannel = (ChannelSftp) channel;
			sftpChannel.put(in, remoteDir);
			log.info("File Transfer Completed");
		} catch (JSchException | SftpException e) {
			e.printStackTrace();
			log.error("JSchException or SftpException ", e);
			closeSFTPSession(session, sftpChannel);
			waitBeforeRetry(20000);
			retryCount++;
			if (retryCount < 1) {
				log.info("Retry sending file to SFTP Directory : Retry Count = {}", retryCount);
				sendFile(in, remoteDir, jsch, retryCount);
			}
			throw e;
		} finally {
			closeSFTPSession(session, sftpChannel);
		}
	}

	private static void closeSFTPSession(Session session, ChannelSftp sftpChannel) {
		if (sftpChannel != null) {
			sftpChannel.exit();
			sftpChannel.disconnect();
		}
		if (session != null) {
			session.disconnect();
		}
	}

	public void waitBeforeRetry(long val) {
		try {
			TimeUnit.MILLISECONDS.sleep(val);
		} catch (InterruptedException e) {
			log.error("Thread interrupted", e);
		}
	}
}
