package com.university.scorems.repository;

import com.university.scorems.model.HocSinh;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HocSinhRepository extends JpaRepository<HocSinh, Long> {
    List<HocSinh> findByLopHocIdOrderByHoTenAsc(Long lopHocId);
}
