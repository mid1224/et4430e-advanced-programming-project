package com.university.scorems.exception;

import com.university.scorems.dto.ApiThongBao;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LoiNghiepVuException.class)
    public ResponseEntity<ApiThongBao> xuLyLoiNghiepVu(LoiNghiepVuException ex) {
        return ResponseEntity.status(ex.getTrangThai())
                .body(new ApiThongBao(false, ex.getMessage()));
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ApiThongBao> xuLyLoiChuyenDoiDuLieu(org.springframework.http.converter.HttpMessageNotReadableException ex) {
        return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST)
                .body(new ApiThongBao(false, "Dữ liệu gửi lên không hợp lệ hoặc thiếu thông tin bắt buộc."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiThongBao> xuLyLoiChung(Exception ex) {
        ex.printStackTrace();
        return ResponseEntity.internalServerError()
                .body(new ApiThongBao(false, "He thong tam thoi bi loi. Vui long thu lai."));
    }
}
