package org.csu.lumtrace.lumtracediagnosis.service;

import org.csu.lumtrace.lumtracediagnosis.dto.request.LoginRequest;
import org.csu.lumtrace.lumtracediagnosis.dto.request.RegisterRequest;
import org.csu.lumtrace.lumtracediagnosis.entity.User;

import java.util.Map;

public interface UserService {
    Map<String, Object> login(LoginRequest req);
    User register(RegisterRequest req);
    User getById(Long id);
}
