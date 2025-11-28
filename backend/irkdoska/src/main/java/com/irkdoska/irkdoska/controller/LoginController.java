package com.irkdoska.irkdoska.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.irkdoska.irkdoska.security.TmaUserPrincipal;
import com.irkdoska.irkdoska.service.LoginService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequestMapping("/")
@RestController
@RequiredArgsConstructor
@Slf4j
public class LoginController {

    private final LoginService loginService;

    @GetMapping
    public ResponseEntity<?> login(@AuthenticationPrincipal TmaUserPrincipal principal) {
        loginService.login(principal.getTelegramId(), principal.getUsername());
        return ResponseEntity.ok().build();
    }
    
}
