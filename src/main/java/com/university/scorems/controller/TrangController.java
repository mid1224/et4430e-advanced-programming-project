package com.university.scorems.controller;

import com.university.scorems.dto.ThongTinDangNhap;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TrangController {

    @GetMapping("/")
    public String trangQuanLy(Model model, HttpSession session) {
        ThongTinDangNhap thongTin = (ThongTinDangNhap) session.getAttribute(DangNhapController.SESSION_KEY);
        if (thongTin == null) {
            return "redirect:/dang-nhap";
        }
        model.addAttribute("hoTenGiangVien", thongTin.getHoTenGiangVien());
        return "quan-ly-diem";
    }
}
