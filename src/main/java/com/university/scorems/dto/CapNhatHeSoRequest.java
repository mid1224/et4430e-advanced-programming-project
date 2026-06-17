package com.university.scorems.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CapNhatHeSoRequest {
    private Long monHocId;
    private Double heSoGiuaKy; // e.g. 0.4 → final = 0.6
}
