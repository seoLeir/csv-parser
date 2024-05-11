package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.service.SFTPService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CallBackController {

    private final SFTPService sftpService;

    @GetMapping("/call")
    public void test() {
        sftpService.downloadFileAndExtract();
    }

}
