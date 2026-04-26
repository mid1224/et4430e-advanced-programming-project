package com.university.scorems.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tai_khoan_giang_vien")
public class TaiKhoanGiangVien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ten_dang_nhap", nullable = false, unique = true, length = 50)
    private String tenDangNhap;

    @Column(name = "mat_khau", nullable = false, length = 120)
    private String matKhau;

    @Column(name = "ma_giang_vien", nullable = false, unique = true)
    private Long maGiangVien;

    @Column(name = "ho_ten", nullable = false, length = 120)
    private String hoTen;
}
