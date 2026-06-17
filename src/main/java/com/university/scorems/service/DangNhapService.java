package com.university.scorems.service;

import com.university.scorems.dto.ThongTinDangNhap;
import com.university.scorems.exception.LoiNghiepVuException;
import com.university.scorems.model.TaiKhoanGiangVien;
import com.university.scorems.model.TaiKhoanKhoa;
import com.university.scorems.repository.TaiKhoanGiangVienRepository;
import com.university.scorems.repository.TaiKhoanKhoaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DangNhapService {

    private final TaiKhoanGiangVienRepository taiKhoanGiangVienRepository;
    private final TaiKhoanKhoaRepository taiKhoanKhoaRepository;

    public ThongTinDangNhap dangNhap(String tenDangNhap, String matKhau) {
        // Try Giang Vien first
        Optional<TaiKhoanGiangVien> gvOpt = taiKhoanGiangVienRepository.findByTenDangNhap(tenDangNhap);
        if (gvOpt.isPresent()) {
            TaiKhoanGiangVien gv = gvOpt.get();
            if (!gv.getMatKhau().equals(matKhau)) {
                throw new LoiNghiepVuException(HttpStatus.UNAUTHORIZED, "Sai ten dang nhap hoac mat khau");
            }
            return new ThongTinDangNhap(gv.getMaGiangVien(), gv.getHoTen(), "GIANG_VIEN");
        }

        // Try Khoa Chuyen Mon
        Optional<TaiKhoanKhoa> khoaOpt = taiKhoanKhoaRepository.findByTenDangNhap(tenDangNhap);
        if (khoaOpt.isPresent()) {
            TaiKhoanKhoa khoa = khoaOpt.get();
            if (!khoa.getMatKhau().equals(matKhau)) {
                throw new LoiNghiepVuException(HttpStatus.UNAUTHORIZED, "Sai ten dang nhap hoac mat khau");
            }
            return new ThongTinDangNhap(null, khoa.getHoTen(), "KHOA");
        }

        throw new LoiNghiepVuException(HttpStatus.UNAUTHORIZED, "Sai ten dang nhap hoac mat khau");
    }
}
