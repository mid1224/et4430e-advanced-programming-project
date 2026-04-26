package com.university.scorems.repository;

import com.university.scorems.model.TaiKhoanGiangVien;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaiKhoanGiangVienRepository extends JpaRepository<TaiKhoanGiangVien, Long> {
    Optional<TaiKhoanGiangVien> findByTenDangNhap(String tenDangNhap);
}
