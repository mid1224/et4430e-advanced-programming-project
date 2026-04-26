package com.university.scorems.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DongBangDiem {
    private Long hocSinhId;
    private String mssv;
    private String hoTen;
    private Double diemKTThuongXuyen;
    private Double diemKTDinhKy;
    private Double diemTBC;
    private Boolean trangThaiDuThi;
    private Double diemKTKetThuc;
    private Double diemTongKet;
    private String diemChu;
    private Double diemHe4;
    private String ghiChu;
}

