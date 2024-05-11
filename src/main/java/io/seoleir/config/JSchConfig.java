package io.seoleir.config;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class JSchConfig {

    @Value("${jsch.host}")
    private String remoteHost;

    @Value("${jsch.username}")
    private String username;

    @Value("${jsch.password}")
    private String password;

    @Value("${jsch.known_hosts}")
    private String knownHosts;

    @Bean
    public Session setupJsch() throws JSchException {
        log.info("Configuring jsch Session bean");
        JSch jsch = new JSch();
        jsch.setKnownHosts(knownHosts);

        Session jschSession = jsch.getSession(username, remoteHost);
        jschSession.setConfig("StrictHostKeyChecking", "no");
        jschSession.setPassword(password);

        jschSession.connect();
        return jschSession;
    }
}
