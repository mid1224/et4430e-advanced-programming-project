package com.university.scorems.repository;

import com.university.scorems.model.MonHoc;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonHocRepository extends JpaRepository<MonHoc, Long> {
    boolean existsByMaMH(String maMH);
}

