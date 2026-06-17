package com.university.scorems.repository;

import com.university.scorems.model.LopHoc;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LopHocRepository extends JpaRepository<LopHoc, Long> {
    List<LopHoc> findAllByOrderByTenLopAsc();
}
