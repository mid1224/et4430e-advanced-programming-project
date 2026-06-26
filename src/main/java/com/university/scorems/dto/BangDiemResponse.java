package com.university.scorems.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class BangDiemResponse {
    private LocalDate ngayBatDauNhapGiuaKy;
    private LocalDate ngayKetThucNhapGiuaKy;
    private LocalDate ngayBatDauNhapCuoiKy;
    private LocalDate ngayKetThucNhapCuoiKy;
    private List<DongBangDiem> danhSachDiem;
}
