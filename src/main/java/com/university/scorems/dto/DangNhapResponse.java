package com.university.scorems.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DangNhapResponse {
    private boolean thanhCong;
    private String thongBao;
    private String vaiTro; // returned to frontend so auth.js knows where to redirect
}
