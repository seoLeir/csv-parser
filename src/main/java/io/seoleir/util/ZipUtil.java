package io.seoleir.util;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;
import lombok.extern.slf4j.Slf4j;
import io.seoleir.model.MNPModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@Component
public class ZipUtil {

    @Value("${parser.save:false}")
    private Boolean saveFlag;

    @Value("${parser.path}")
    private String pathToSave;


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

    public String getResultFromChannel(Channel channel) throws JSchException {

        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        ByteArrayOutputStream errorBuffer = new ByteArrayOutputStream();

        try {
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
            }

            if (errorBuffer.size() > 0) {
                log.info("error: " + errorBuffer.toString("UTF-8"));
            }

            log.info("output: " + outputBuffer.toString("UTF-8"));

            return outputBuffer.toString("UTF-8");
        } catch (IOException e) {
            log.error(e.getMessage(), e);

            throw new RuntimeException(e);
        } finally {
            if (channel.isConnected()) {
                channel.disconnect();
            }
        }
    }
}
