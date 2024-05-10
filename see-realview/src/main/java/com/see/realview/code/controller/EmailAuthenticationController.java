package com.see.realview.code.controller;

import com.see.realview._core.response.Response;
import com.see.realview.code.dto.EmailCodeRequest;
import com.see.realview.code.dto.VerifyEmailRequest;
import com.see.realview.code.service.EmailAuthenticationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/email")
public class EmailAuthenticationController {

    private final EmailAuthenticationServiceImpl emailCodeService;


    public EmailAuthenticationController(@Autowired EmailAuthenticationServiceImpl emailCodeService) {
        this.emailCodeService = emailCodeService;
    }

    @PostMapping("/send")
    public ResponseEntity<?> send(@RequestBody EmailCodeRequest request) {
        emailCodeService.send(request.email());
        return ResponseEntity.ok().body(Response.success(null));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody VerifyEmailRequest request) {
        emailCodeService.check(request);
        return ResponseEntity.ok().body(Response.success(null));
    }
}
