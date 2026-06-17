package com.university.scorems.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MonHocRequest {
    private String maMH;
    private String tenMonHoc;
    private Integer soTinChi;
    private String hinhThucThiKetThuc;
    private Integer lanThiThu;
    private Double heSoGiuaKy;
    private String namHoc;
    private Integer hocKy;
}
