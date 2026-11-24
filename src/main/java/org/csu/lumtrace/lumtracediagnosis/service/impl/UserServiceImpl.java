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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public Map<String, Object> login(LoginRequest req) {
        User user = userMapper.selectByUsername(req.getUsername());
        if (user == null) throw new BusinessException(401, "用户名或密码错误");
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == AppConstants.STATUS_DISABLED) {
            throw new BusinessException(403, "账号已被禁用");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), "PATIENT");
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("nickname", user.getNickname());
        return result;
    }

    @Override
    public User register(RegisterRequest req) {
        if (userMapper.countByUsername(req.getUsername()) > 0) {
            throw new BusinessException("用户名已存在");
        }
        User u = new User();
        u.setUsername(req.getUsername());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setNickname(req.getNickname() == null ? req.getUsername() : req.getNickname());
        u.setPhone(req.getPhone());
        u.setStatus(AppConstants.STATUS_ENABLED);
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
}
