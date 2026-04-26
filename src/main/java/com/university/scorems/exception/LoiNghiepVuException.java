package com.university.scorems.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class LoiNghiepVuException extends RuntimeException {

    private final HttpStatus trangThai;

    public LoiNghiepVuException(HttpStatus trangThai, String message) {
        super(message);
        this.trangThai = trangThai;
    }
}
