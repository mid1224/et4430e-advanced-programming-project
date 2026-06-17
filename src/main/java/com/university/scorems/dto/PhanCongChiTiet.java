package com.university.scorems.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PhanCongChiTiet {
    private Long id;
    private Long maGiangVien;
    private String hoTenGiangVien;
    private String tenMonHoc;
    private String maMH;
    private String tenLop;
}
