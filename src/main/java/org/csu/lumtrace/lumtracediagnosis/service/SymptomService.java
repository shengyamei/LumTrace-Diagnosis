package org.csu.lumtrace.lumtracediagnosis.service;

import org.csu.lumtrace.lumtracediagnosis.entity.Symptom;

import java.util.List;

public interface SymptomService {
    List<Symptom> listAll();
    List<Symptom> listByCategory(String category);
    List<Symptom> search(String keyword);
    Symptom getById(Long id);
}
