package org.csu.lumtrace.lumtracediagnosis.controller;

import org.csu.lumtrace.lumtracediagnosis.dto.response.Result;
import org.csu.lumtrace.lumtracediagnosis.entity.Doctor;
import org.csu.lumtrace.lumtracediagnosis.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/doctor")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @GetMapping("/list")
    public Result<List<Doctor>> list() {
        return Result.success(doctorService.listAll());
    }

    @GetMapping("/department")
    public Result<List<Doctor>> byDept(@RequestParam String department) {
        return Result.success(doctorService.listByDepartment(department));
    }

    @GetMapping("/{id}")
    public Result<Doctor> detail(@PathVariable Long id) {
        return Result.success(doctorService.getById(id));
    }
}
