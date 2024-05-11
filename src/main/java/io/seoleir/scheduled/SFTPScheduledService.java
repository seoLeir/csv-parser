package io.seoleir.scheduled;

import com.jcraft.jsch.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.seoleir.dao.SFTPDao;
import io.seoleir.model.MNPModel;
import io.seoleir.util.ZipUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SFTPScheduledService {

    private final Session session;

    private final SFTPDao sftpDao;

    private final ZipUtil zipUtil;

    @Value("${parser.remote_dir_name}")
    private String remoteDirectoryName;


    @Scheduled(cron = "0 0 */2 * * *")
    public void downloadFileAndExtract() {
        try {
            Channel channelExec = session.openChannel("exec");

            // executing command to cd remote directory name and get first command order by created date desc
            ((ChannelExec) channelExec).setCommand("cd %s && ls -1t | head -n 1".formatted(remoteDirectoryName));

            String zipFile = zipUtil.getResultFromChannel(channelExec);

            ChannelSftp sftp = (ChannelSftp) session.openChannel("sftp");

            sftp.connect();
            sftp.cd(remoteDirectoryName);

            InputStream inputStream = sftp.get(zipFile.trim());

            List<MNPModel> mnpModels = zipUtil.extractMNPModels(inputStream);

            sftp.disconnect();

            sftpDao.saveNPMModels(mnpModels);
        } catch (JSchException e) {
            log.error("Connection attempt failed: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }


}
