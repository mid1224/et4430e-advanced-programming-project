package com.university.scorems.controller;

import com.university.scorems.dto.ApiThongBao;
import com.university.scorems.dto.CapNhatHeSoRequest;
import com.university.scorems.dto.CapNhatPhanCongRequest;
import com.university.scorems.dto.DongBangDiem;
import com.university.scorems.dto.GiangVienTomTat;
import com.university.scorems.dto.LopHocTomTat;
import com.university.scorems.dto.MonHocKhoaView;
import com.university.scorems.dto.MonHocRequest;
import com.university.scorems.dto.PhanCongChiTiet;
import com.university.scorems.dto.PhanCongRequest;
import com.university.scorems.dto.ThongTinDangNhap;
import com.university.scorems.exception.LoiNghiepVuException;
import com.university.scorems.service.KhoaService;
import com.university.scorems.service.DiemService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class KhoaController {

    private final KhoaService khoaService;
    private final DiemService diemService;

    // ── Page ──────────────────────────────────────────────────────────────────

    @GetMapping("/khoa")
    public String trangKhoa(Model model, HttpSession session) {
        ThongTinDangNhap thongTin = kiemTraKhoa(session);
        model.addAttribute("hoTen", thongTin.getHoTenGiangVien());
        return "khoa-dashboard";
    }

    // ── REST API ──────────────────────────────────────────────────────────────

    @GetMapping("/api/khoa/mon-hoc")
    @ResponseBody
    public List<MonHocKhoaView> layMonHoc(HttpSession session) {
        kiemTraKhoa(session);
        return khoaService.layDanhSachMonHoc();
    }

    @GetMapping("/api/khoa/giang-vien")
    @ResponseBody
    public List<GiangVienTomTat> layGiangVien(HttpSession session) {
        kiemTraKhoa(session);
        return khoaService.layDanhSachGiangVien();
    }

    @GetMapping("/api/khoa/lop-hoc")
    @ResponseBody
    public List<LopHocTomTat> layLopHoc(HttpSession session) {
        kiemTraKhoa(session);
        return khoaService.layDanhSachLopHoc();
    }

    @GetMapping("/api/khoa/phan-cong")
    @ResponseBody
    public List<PhanCongChiTiet> layPhanCong(HttpSession session) {
        kiemTraKhoa(session);
        return khoaService.layDanhSachPhanCong();
    }

    @PostMapping("/api/khoa/phan-cong")
    @ResponseBody
    public ApiThongBao taoPhanCong(@RequestBody PhanCongRequest request, HttpSession session) {
        kiemTraKhoa(session);
        khoaService.phanCongGiangVien(request);
        return new ApiThongBao(true, "Phan cong thanh cong");
    }

    @PostMapping("/api/khoa/mon-hoc")
    @ResponseBody
    public ApiThongBao taoMonHoc(@RequestBody MonHocRequest request, HttpSession session) {
        kiemTraKhoa(session);
        khoaService.taoMonHoc(request);
        return new ApiThongBao(true, "Tạo môn học thành công");
    }

    @DeleteMapping("/api/khoa/phan-cong/{id}")
    @ResponseBody
    public ApiThongBao xoaPhanCong(@PathVariable Long id, HttpSession session) {
        kiemTraKhoa(session);
        khoaService.xoaPhanCong(id);
        return new ApiThongBao(true, "Da xoa phan cong");
    }

    @PutMapping("/api/khoa/phan-cong/{id}")
    @ResponseBody
    public ApiThongBao capNhatPhanCongVaHeSo(@PathVariable Long id, @RequestBody CapNhatPhanCongRequest request, HttpSession session) {
        kiemTraKhoa(session);
        khoaService.capNhatPhanCongVaHeSo(id, request);
        return new ApiThongBao(true, "Cap nhat phan cong va he so thanh cong");
    }

    @GetMapping("/api/khoa/phan-cong/{id}/bang-diem")
    @ResponseBody
    public List<DongBangDiem> layBangDiemChoKhoa(@PathVariable Long id, HttpSession session) {
        kiemTraKhoa(session);
        return khoaService.layBangDiemChoPhanCong(id);
    }

    @PutMapping("/api/khoa/he-so")
    @ResponseBody
    public ApiThongBao capNhatHeSo(@RequestBody CapNhatHeSoRequest request, HttpSession session) {
        kiemTraKhoa(session);
        khoaService.capNhatHeSo(request);
        return new ApiThongBao(true, "Cap nhat he so thanh cong");
    }

    @PostMapping("/api/khoa/phan-cong/{id}/import-xml")
    @ResponseBody
    public ApiThongBao importXmlKhoa(@PathVariable Long id, @RequestParam("file") MultipartFile file, HttpSession session) {
        kiemTraKhoa(session);
        int soDong = diemService.importXmlForKhoa(id, file);
        return new ApiThongBao(true, "Đã import thành công " + soDong + " dòng điểm từ XML.");
    }

    @GetMapping("/api/khoa/phan-cong/{id}/export-xml")
    public ResponseEntity<byte[]> exportXmlKhoa(@PathVariable Long id, HttpSession session) {
        kiemTraKhoa(session);
        List<DongBangDiem> list = khoaService.layBangDiemChoPhanCong(id);
        return diemService.exportXmlResponseKhoa(id, list);
    }


    // ── Helper ────────────────────────────────────────────────────────────────

    private ThongTinDangNhap kiemTraKhoa(HttpSession session) {
        ThongTinDangNhap thongTin = (ThongTinDangNhap) session.getAttribute(DangNhapController.SESSION_KEY);
        if (thongTin == null) {
            throw new LoiNghiepVuException(HttpStatus.UNAUTHORIZED, "Ban chua dang nhap");
        }
        if (!"KHOA".equals(thongTin.getVaiTro())) {
            throw new LoiNghiepVuException(HttpStatus.FORBIDDEN, "Khong co quyen truy cap");
        }
        return thongTin;
    }
}
