package org.example.service;

import com.jcraft.jsch.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.example.dao.SFTPDao;
import org.example.model.MNPModel;
import org.example.util.CSVParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class SFTPServiceImpl implements SFTPService {

    private final Session session;

    private final SFTPDao sftpDao;

    @Value("${parser.remote_dir_name}")
    private String remoteDirectoryName;

    @Value("${parser.save:false}")
    private Boolean saveFlag;

    @Value("${parser.path}")
    private String pathToSave;

    @Override
    public void downloadFileAndExtract() {
        try {
            Channel channelExec = session.openChannel("exec");

            ((ChannelExec) channelExec).setCommand("cd " + remoteDirectoryName + " && ls -1t | head -n 1");

            String zipFile = getZipFileNameFromExecChannel(channelExec);

            ChannelSftp sftp = (ChannelSftp) session.openChannel("sftp");

            sftp.connect();
            sftp.cd(remoteDirectoryName);

            InputStream inputStream = sftp.get(zipFile.trim());


            List<MNPModel> mnpModels = extractMNPModels(inputStream);
            System.out.print("MNP models: ");
            mnpModels.forEach(System.out::println);

            sftp.disconnect();
        } catch (JSchException e) {
            log.error("Connection attempt failed: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public List<MNPModel> extractMNPModels(InputStream in) {
        List<MNPModel> records = new ArrayList<>();

        try (in; ZipInputStream zis = new ZipInputStream(in)) {
            ZipEntry entry;

            if ((entry = zis.getNextEntry()) != null) {

                System.out.println(entry.getName());
                if (!entry.isDirectory() && entry.getName().endsWith(".csv")) {
                    if (saveFlag) {
                        Path localFilePath = Paths.get(pathToSave);
                        if (!Files.exists(localFilePath)) {
                            localFilePath = Files.createFile(Path.of(entry.getName()));
                        }

                        Files.copy(in, localFilePath, StandardCopyOption.REPLACE_EXISTING);
                    }

                    BufferedReader br = new BufferedReader(new InputStreamReader(in));
                    records = CSVParser.readLineByLine(br);

                    br.close();
                }

                zis.closeEntry();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return records;
    }

    @SneakyThrows
    private static String getZipFileNameFromExecChannel(Channel channel) {
        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        ByteArrayOutputStream errorBuffer = new ByteArrayOutputStream();

        InputStream in = channel.getInputStream();
        InputStream err = channel.getExtInputStream();

        channel.connect();

        byte[] tmp = new byte[1024];
        while (true) {
            while (in.available() > 0) {
                int i = in.read(tmp, 0, 1024);
                if (i < 0) break;
                outputBuffer.write(tmp, 0, i);
            }
            while (err.available() > 0) {
                int i = err.read(tmp, 0, 1024);
                if (i < 0) break;
                errorBuffer.write(tmp, 0, i);
            }
            if (channel.isClosed()) {
                if ((in.available() > 0) || (err.available() > 0)) continue;
                System.out.println("exit-status: " + channel.getExitStatus());
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (Exception ee) {
            }
        }

        log.info("error: " + errorBuffer.toString("UTF-8"));
        log.info("output: " + outputBuffer.toString("UTF-8"));

        channel.disconnect();
        return outputBuffer.toString("UTF-8");
    }
}
