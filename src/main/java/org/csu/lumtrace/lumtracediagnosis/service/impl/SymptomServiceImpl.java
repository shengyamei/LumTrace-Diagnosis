package org.csu.lumtrace.lumtracediagnosis.service.impl;

import org.csu.lumtrace.lumtracediagnosis.entity.Symptom;
import org.csu.lumtrace.lumtracediagnosis.mapper.SymptomMapper;
import org.csu.lumtrace.lumtracediagnosis.service.SymptomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SymptomServiceImpl implements SymptomService {

    @Autowired
    private SymptomMapper symptomMapper;

    @Override
    public List<Symptom> listAll() {
        return symptomMapper.selectAll();
    }

    @Override
    public List<Symptom> listByCategory(String category) {
        return symptomMapper.selectByCategory(category);
    }

    @Override
    public List<Symptom> search(String keyword) {
        if (keyword == null || keyword.isBlank()) return symptomMapper.selectAll();
        return symptomMapper.search(keyword);
    }

    @Override
    public Symptom getById(Long id) {
        return symptomMapper.selectById(id);
    }
}
