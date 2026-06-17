package com.university.scorems.controller;

import com.university.scorems.dto.DangNhapRequest;
import com.university.scorems.dto.DangNhapResponse;
import com.university.scorems.dto.ThongTinDangNhap;
import com.university.scorems.exception.LoiNghiepVuException;
import com.university.scorems.service.DangNhapService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class DangNhapController {

    public static final String SESSION_KEY = "dangNhap";

    private final DangNhapService dangNhapService;

    @GetMapping("/dang-nhap")
    public String trangDangNhap(HttpSession session) {
        if (session.getAttribute(SESSION_KEY) != null) {
            return "redirect:/";
        }
        return "dang-nhap";
    }

    @PostMapping("/dang-nhap")
    @ResponseBody
    public DangNhapResponse xuLyDangNhap(@RequestBody DangNhapRequest request, HttpSession session) {
        if (request.getTenDangNhap() == null || request.getMatKhau() == null) {
            throw new LoiNghiepVuException(HttpStatus.BAD_REQUEST, "Ten dang nhap va mat khau khong duoc trong");
        }

        ThongTinDangNhap thongTin = dangNhapService.dangNhap(request.getTenDangNhap().trim(), request.getMatKhau());
        session.setAttribute(SESSION_KEY, thongTin);
        return new DangNhapResponse(true, "Dang nhap thanh cong", thongTin.getVaiTro());
    }

    @PostMapping("/dang-xuat")
    @ResponseBody
    public DangNhapResponse dangXuat(HttpSession session) {
        session.invalidate();
        return new DangNhapResponse(true, "Da dang xuat", null);
    }
}
