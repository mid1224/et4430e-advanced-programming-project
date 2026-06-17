package com.university.scorems.controller;

import com.university.scorems.dto.PhieuBM03Data;
import com.university.scorems.dto.ThongTinDangNhap;
import com.university.scorems.exception.LoiNghiepVuException;
import com.university.scorems.model.PhanCongGiangDay;
import com.university.scorems.model.TaiKhoanGiangVien;
import com.university.scorems.repository.PhanCongGiangDayRepository;
import com.university.scorems.repository.TaiKhoanGiangVienRepository;
import com.university.scorems.service.DiemService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class PhieuBM03Controller {

    private final DiemService diemService;
    private final PhanCongGiangDayRepository phanCongGiangDayRepository;
    private final TaiKhoanGiangVienRepository taiKhoanGiangVienRepository;

    @GetMapping("/phieu-bm03/{monHocId}")
    public String hienThiPhieu(@PathVariable Long monHocId, HttpSession session, Model model) {
        ThongTinDangNhap thongTin = (ThongTinDangNhap) session.getAttribute(DangNhapController.SESSION_KEY);
        if (thongTin == null) {
            return "redirect:/dang-nhap";
        }

        PhieuBM03Data phieu = diemService.layDuLieuPhieuBM03(thongTin.getMaGiangVien(), monHocId);
        if (phieu.getDanhSachDiem().isEmpty()) {
            throw new LoiNghiepVuException(HttpStatus.BAD_REQUEST, "Chua co du lieu diem de in BM03");
        }

        model.addAttribute("phieu", phieu);
        model.addAttribute("hoTenGiangVien", thongTin.getHoTenGiangVien());
        return "phieu-bm03";
    }

    @GetMapping("/phieu-bm03/khoa/{phanCongId}")
    public String hienThiPhieuKhoa(@PathVariable Long phanCongId, HttpSession session, Model model) {
        ThongTinDangNhap thongTin = (ThongTinDangNhap) session.getAttribute(DangNhapController.SESSION_KEY);
        if (thongTin == null || !"KHOA".equals(thongTin.getVaiTro())) {
            return "redirect:/dang-nhap";
        }

        PhieuBM03Data phieu = diemService.layDuLieuPhieuBM03ChoKhoa(phanCongId);
        if (phieu.getDanhSachDiem().isEmpty()) {
            throw new LoiNghiepVuException(HttpStatus.BAD_REQUEST, "Chua co du lieu diem de in BM03");
        }

        PhanCongGiangDay phanCong = phanCongGiangDayRepository.findById(phanCongId)
                .orElseThrow(() -> new LoiNghiepVuException(HttpStatus.NOT_FOUND, "Khong tim thay phan cong"));

        String tenGV = taiKhoanGiangVienRepository.findByMaGiangVien(phanCong.getMaGiangVien())
                .map(TaiKhoanGiangVien::getHoTen)
                .orElse("Chua phan cong");

        model.addAttribute("phieu", phieu);
        model.addAttribute("hoTenGiangVien", tenGV);
        return "phieu-bm03";
    }
}

