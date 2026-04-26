package com.university.scorems.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class PhieuBM03Data {
    private String khoa;
    private String tenLop;
    private String tenMonHoc;
    private String maMH;
    private String hinhThucThiKetThuc;
    private Integer hocKy;
    private String namHoc;
    private Integer lanThiThu;
    private Integer soTinChi;
    private LocalDate ngayLap;
    private List<DongBangDiem> danhSachDiem;
}
