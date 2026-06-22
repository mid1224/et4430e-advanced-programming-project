package com.university.scorems.repository;

import com.university.scorems.model.HocSinh;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import java.util.Optional;

public interface HocSinhRepository extends JpaRepository<HocSinh, Long> {
    List<HocSinh> findByLopHocIdOrderByHoTenAsc(Long lopHocId);
    Optional<HocSinh> findByMssv(String mssv);
}
