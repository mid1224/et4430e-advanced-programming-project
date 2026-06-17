package com.university.scorems.repository;

import com.university.scorems.model.TaiKhoanKhoa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaiKhoanKhoaRepository extends JpaRepository<TaiKhoanKhoa, Long> {
    Optional<TaiKhoanKhoa> findByTenDangNhap(String tenDangNhap);
}
