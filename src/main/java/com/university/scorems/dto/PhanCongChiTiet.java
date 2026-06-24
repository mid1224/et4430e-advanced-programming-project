package com.university.scorems.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class PhanCongChiTiet {
    private Long id;
    private Long maGiangVien;
    private String hoTenGiangVien;
    private Long monHocId;
    private String tenMonHoc;
    private String maMH;
    private Long lopHocId;
    private String tenLop;
    private Double heSoGiuaKy;
    private Double heSoCuoiKy;
    private LocalDate ngayBatDauNhapGiuaKy;
    private LocalDate ngayKetThucNhapGiuaKy;
    private LocalDate ngayBatDauNhapCuoiKy;
    private LocalDate ngayKetThucNhapCuoiKy;
}

