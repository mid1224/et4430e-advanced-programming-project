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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiThongBao> xuLyLoiChung(Exception ex) {
        return ResponseEntity.internalServerError()
                .body(new ApiThongBao(false, "He thong tam thoi bi loi. Vui long thu lai."));
    }
}
