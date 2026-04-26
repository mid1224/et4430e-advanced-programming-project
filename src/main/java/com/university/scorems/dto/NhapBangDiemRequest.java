package com.university.scorems.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class NhapBangDiemRequest {
    private Long monHocId;
    private List<NhapDiemDongRequest> danhSachDiem;
}
