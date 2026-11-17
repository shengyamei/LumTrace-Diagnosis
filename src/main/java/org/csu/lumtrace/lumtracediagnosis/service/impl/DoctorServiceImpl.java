package org.csu.lumtrace.lumtracediagnosis.service.impl;

import org.csu.lumtrace.lumtracediagnosis.entity.Doctor;
import org.csu.lumtrace.lumtracediagnosis.mapper.DoctorMapper;
import org.csu.lumtrace.lumtracediagnosis.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DoctorServiceImpl implements DoctorService {

    @Autowired
    private DoctorMapper doctorMapper;

    @Override
    public List<Doctor> listAll() {
        return doctorMapper.selectAll();
    }

    @Override
    public List<Doctor> listByDepartment(String department) {
        return doctorMapper.selectByDepartment(department);
    }

    @Override
    public Doctor getById(Long id) {
        return doctorMapper.selectById(id);
    }
}
