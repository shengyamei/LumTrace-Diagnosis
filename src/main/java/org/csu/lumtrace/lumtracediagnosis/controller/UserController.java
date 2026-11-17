package org.csu.lumtrace.lumtracediagnosis.controller;

import org.csu.lumtrace.lumtracediagnosis.common.util.UserContext;
import org.csu.lumtrace.lumtracediagnosis.dto.request.LoginRequest;
import org.csu.lumtrace.lumtracediagnosis.dto.request.RegisterRequest;
import org.csu.lumtrace.lumtracediagnosis.dto.response.Result;
import org.csu.lumtrace.lumtracediagnosis.entity.User;
import org.csu.lumtrace.lumtracediagnosis.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<User> register(@Valid @RequestBody RegisterRequest req) {
        return Result.success(userService.register(req));
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest req) {
        return Result.success(userService.login(req));
    }

    @GetMapping("/me")
    public Result<User> me() {
        return Result.success(userService.getById(UserContext.getUserId()));
    }
}
