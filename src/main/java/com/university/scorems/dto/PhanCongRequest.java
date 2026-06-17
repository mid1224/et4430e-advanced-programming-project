package com.university.scorems.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PhanCongRequest {
    private Long maGiangVien;
    private Long monHocId;
    private Long lopHocId;
    private String tenLop;
    private String khoa;
    private Double heSoGiuaKy;
}


