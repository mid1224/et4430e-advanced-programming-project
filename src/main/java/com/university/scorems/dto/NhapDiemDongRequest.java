package com.university.scorems.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NhapDiemDongRequest {
    private Long hocSinhId;
    private Double diemKTThuongXuyen;
    private Double diemKTDinhKy;
    private Double diemKTKetThuc;
    private String ghiChu;
}

