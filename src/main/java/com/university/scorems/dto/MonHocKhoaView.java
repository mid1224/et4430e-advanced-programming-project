package com.university.scorems.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MonHocKhoaView {
    private Long id;
    private String maMH;
    private String tenMonHoc;
    private Double heSoGiuaKy;
    private Double heSoCuoiKy; // computed: 1 - heSoGiuaKy
}
