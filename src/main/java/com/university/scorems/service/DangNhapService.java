package com.university.scorems.service;

import com.university.scorems.exception.LoiNghiepVuException;
import com.university.scorems.model.TaiKhoanGiangVien;
import com.university.scorems.repository.TaiKhoanGiangVienRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DangNhapService {

    private final TaiKhoanGiangVienRepository taiKhoanGiangVienRepository;

    public TaiKhoanGiangVien dangNhap(String tenDangNhap, String matKhau) {
        TaiKhoanGiangVien taiKhoan = taiKhoanGiangVienRepository.findByTenDangNhap(tenDangNhap)
                .orElseThrow(() -> new LoiNghiepVuException(HttpStatus.UNAUTHORIZED, "Sai ten dang nhap hoac mat khau"));

        if (!taiKhoan.getMatKhau().equals(matKhau)) {
            throw new LoiNghiepVuException(HttpStatus.UNAUTHORIZED, "Sai ten dang nhap hoac mat khau");
        }
        return taiKhoan;
    }
}
