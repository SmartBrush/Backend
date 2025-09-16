package com.smartbrush.smartbrush_backend.service;

import com.smartbrush.smartbrush_backend.entity.DiagnosisEntity;
import com.smartbrush.smartbrush_backend.repository.DiagnosisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final DiagnosisRepository diagnosisRepository;

    @Override
    public int getCurrentStreak(String email) {
        List<DiagnosisEntity> records =
                diagnosisRepository.findByEmailOrderByDiagnosedDateDesc(email);

        if (records.isEmpty()) return 0;

        int streak = 1;
        LocalDate prev = records.get(0).getDiagnosedDate();

        for (int i = 1; i < records.size(); i++) {
            LocalDate cur = records.get(i).getDiagnosedDate();
            if (cur.equals(prev.minusDays(1))) {
                streak++;
                prev = cur;
            } else {
                break;
            }
        }
        return streak;
    }
}
