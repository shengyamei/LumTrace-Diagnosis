package org.csu.lumtrace.lumtracediagnosis.service;

import org.csu.lumtrace.lumtracediagnosis.entity.Doctor;

import java.util.List;

public interface DoctorService {
    List<Doctor> listAll();
    List<Doctor> listByDepartment(String department);
    Doctor getById(Long id);
}
