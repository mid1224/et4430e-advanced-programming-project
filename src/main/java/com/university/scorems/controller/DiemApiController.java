package com.university.scorems.controller;

import com.university.scorems.dto.ApiThongBao;
import com.university.scorems.dto.DongBangDiem;
import com.university.scorems.dto.MonHocTomTat;
import com.university.scorems.dto.NhapBangDiemRequest;
import com.university.scorems.dto.PhieuBM03Data;
import com.university.scorems.dto.ThongTinDangNhap;
import com.university.scorems.exception.LoiNghiepVuException;
import com.university.scorems.service.DiemService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DiemApiController {

    private final DiemService diemService;

    @GetMapping("/mon-hoc")
    public List<MonHocTomTat> layMonHoc(HttpSession session) {
        return diemService.layMonHocTheoGiangVien(layMaGiangVien(session));
    }

    @GetMapping("/bang-diem/{monHocId}")
    public List<DongBangDiem> layBangDiem(@PathVariable Long monHocId, HttpSession session) {
        return diemService.layBangDiemTheoMon(layMaGiangVien(session), monHocId);
    }

    @PostMapping("/bang-diem/luu")
    public ApiThongBao luuBangDiem(@RequestBody NhapBangDiemRequest request, HttpSession session) {
        int soDong = diemService.luuBangDiem(layMaGiangVien(session), request);
        return new ApiThongBao(true, "Da luu " + soDong + " dong diem thanh cong");
    }

    @PostMapping("/phieu-bm03/nop/{monHocId}")
    public ApiThongBao nopPhieu(@PathVariable Long monHocId, HttpSession session) {
        LocalDate ngayNop = diemService.nopPhieuKetQua(layMaGiangVien(session), monHocId);
        return new ApiThongBao(true, "Da nop phieu BM03 ngay " + ngayNop);
    }

    @PostMapping("/bang-diem/import-xml/{monHocId}")
    public ApiThongBao importXml(@PathVariable Long monHocId, @RequestParam("file") MultipartFile file, HttpSession session) {
        int soDong = diemService.importXml(layMaGiangVien(session), monHocId, file);
        return new ApiThongBao(true, "Đã import thành công " + soDong + " dòng điểm từ XML.");
    }

    @GetMapping("/bang-diem/export-xml/{monHocId}")
    public ResponseEntity<byte[]> exportXml(@PathVariable Long monHocId, HttpSession session) {
        Long maGiangVien = layMaGiangVien(session);
        return diemService.exportXmlResponse(maGiangVien, monHocId);
    }

    @GetMapping("/phieu-bm03/{monHocId}")
    public PhieuBM03Data layDuLieuPhieu(@PathVariable Long monHocId, HttpSession session) {
        return diemService.layDuLieuPhieuBM03(layMaGiangVien(session), monHocId);
    }

    private Long layMaGiangVien(HttpSession session) {
        ThongTinDangNhap thongTin = (ThongTinDangNhap) session.getAttribute(DangNhapController.SESSION_KEY);
        if (thongTin == null || !"GIANG_VIEN".equals(thongTin.getVaiTro())) {
            throw new LoiNghiepVuException(HttpStatus.UNAUTHORIZED, "Ban chua dang nhap hoac khong co quyen");
        }
        return thongTin.getMaGiangVien();
    }
}
