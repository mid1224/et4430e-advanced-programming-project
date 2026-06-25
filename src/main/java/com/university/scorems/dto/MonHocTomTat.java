package com.university.scorems.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MonHocTomTat {
    private Long id;
    private String maMH;
    private String tenMonHoc;
    private Integer hocKy;
    private String namHoc;
    private String tenLop;
}
