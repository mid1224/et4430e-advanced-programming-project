package com.university.scorems.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ThongTinDangNhap {
    private Long maGiangVien; // null for KHOA role
    private String hoTenGiangVien;
    private String vaiTro; // "GIANG_VIEN" or "KHOA"
}
