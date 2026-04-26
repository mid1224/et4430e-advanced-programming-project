package com.university.scorems.repository;

import com.university.scorems.model.PhanCongGiangDay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhanCongGiangDayRepository extends JpaRepository<PhanCongGiangDay, Long> {
    List<PhanCongGiangDay> findByMaGiangVien(Long maGiangVien);

    boolean existsByMaGiangVienAndMonHocId(Long maGiangVien, Long monHocId);

    PhanCongGiangDay findFirstByMaGiangVienAndMonHocId(Long maGiangVien, Long monHocId);
}
