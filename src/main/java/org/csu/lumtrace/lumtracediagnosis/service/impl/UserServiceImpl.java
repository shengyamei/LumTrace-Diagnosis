package org.csu.lumtrace.lumtracediagnosis.service.impl;

import org.csu.lumtrace.lumtracediagnosis.common.constant.AppConstants;
import org.csu.lumtrace.lumtracediagnosis.common.exception.BusinessException;
import org.csu.lumtrace.lumtracediagnosis.common.util.JwtUtil;
import org.csu.lumtrace.lumtracediagnosis.dto.request.LoginRequest;
import org.csu.lumtrace.lumtracediagnosis.dto.request.RegisterRequest;
import org.csu.lumtrace.lumtracediagnosis.entity.User;
import org.csu.lumtrace.lumtracediagnosis.mapper.UserMapper;
import org.csu.lumtrace.lumtracediagnosis.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Map<String, Object> login(LoginRequest req) {
        User user = userMapper.selectByUsername(req.getUsername());
        if (user == null) throw new BusinessException(401, "用户名或密码错误");
        if (!sha256(req.getPassword()).equals(user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == AppConstants.STATUS_DISABLED) {
            throw new BusinessException(403, "账号已被禁用");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("nickname", user.getNickname());
        result.put("role", user.getRole());
        return result;
    }

    @Override
    public User register(RegisterRequest req) {
        if (userMapper.countByUsername(req.getUsername()) > 0) {
            throw new BusinessException("用户名已存在");
        }
        User u = new User();
        u.setUsername(req.getUsername());
        u.setPassword(sha256(req.getPassword()));
        u.setNickname(req.getNickname() == null ? req.getUsername() : req.getNickname());
        u.setPhone(req.getPhone());
        u.setEmail(req.getEmail());
        u.setRole(AppConstants.ROLE_PATIENT);
        u.setStatus(AppConstants.STATUS_ENABLED);
        u.setGender(0);
        userMapper.insert(u);
        return u;
    }

    @Override
    public User getById(Long id) {
        User u = userMapper.selectById(id);
        if (u == null) throw new BusinessException(404, "用户不存在");
        u.setPassword(null);
        return u;
    }

    /** 简化密码摘要，生产请使用 BCrypt */
    private static String sha256(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new BusinessException("密码摘要失败");
        }
    }
}
