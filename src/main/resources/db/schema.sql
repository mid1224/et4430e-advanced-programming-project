CREATE DATABASE IF NOT EXISTS student_score_management;
USE student_score_management;

CREATE TABLE IF NOT EXISTS lop_hoc (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ten_lop VARCHAR(100) NOT NULL UNIQUE,
    khoa VARCHAR(30) NOT NULL
);

CREATE TABLE IF NOT EXISTS hoc_sinh (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    mssv VARCHAR(20) NOT NULL UNIQUE,
    ho_ten VARCHAR(120) NOT NULL,
    lop_hoc_id BIGINT NOT NULL,
    CONSTRAINT fk_hoc_sinh_lop FOREIGN KEY (lop_hoc_id) REFERENCES lop_hoc(id)
);

CREATE TABLE IF NOT EXISTS mon_hoc (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ma_mh VARCHAR(30) NOT NULL UNIQUE,
    ten_mon_hoc VARCHAR(150) NOT NULL,
    so_tin_chi INT NOT NULL,
    hoc_ky INT NOT NULL,
    nam_hoc VARCHAR(20) NOT NULL,
    hinh_thuc_thi_ket_thuc VARCHAR(100) NOT NULL,
    lan_thi_thu INT NOT NULL,
    he_so_giua_ky DOUBLE NOT NULL DEFAULT 0.4
);

CREATE TABLE IF NOT EXISTS phan_cong_giang_day (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ma_giang_vien BIGINT NOT NULL,
    mon_hoc_id BIGINT NOT NULL,
    lop_hoc_id BIGINT NOT NULL,
    CONSTRAINT uq_phan_cong UNIQUE (ma_giang_vien, mon_hoc_id, lop_hoc_id),
    CONSTRAINT fk_phan_cong_mon FOREIGN KEY (mon_hoc_id) REFERENCES mon_hoc(id),
    CONSTRAINT fk_phan_cong_lop FOREIGN KEY (lop_hoc_id) REFERENCES lop_hoc(id)
);

CREATE TABLE IF NOT EXISTS bang_diem (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    hoc_sinh_id BIGINT NOT NULL,
    mon_hoc_id BIGINT NOT NULL,
    diem_kt_thuong_xuyen DOUBLE NULL,
    diem_kt_dinh_ky DOUBLE NULL,
    diem_tbc DOUBLE NULL,
    trang_thai_du_thi BIT(1) NULL,
    diem_kt_ket_thuc DOUBLE NULL,
    diem_tong_ket DOUBLE NULL,
    diem_chu VARCHAR(2) NULL,
    diem_he_4 DOUBLE NULL,
    ghi_chu VARCHAR(255) NULL,
    da_nop_phieu BIT(1) NOT NULL DEFAULT 0,
    ngay_nop_phieu DATE NULL,
    CONSTRAINT uq_bang_diem UNIQUE (hoc_sinh_id, mon_hoc_id),
    CONSTRAINT fk_bang_diem_hoc_sinh FOREIGN KEY (hoc_sinh_id) REFERENCES hoc_sinh(id),
    CONSTRAINT fk_bang_diem_mon FOREIGN KEY (mon_hoc_id) REFERENCES mon_hoc(id)
);

CREATE TABLE IF NOT EXISTS tai_khoan_giang_vien (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ten_dang_nhap VARCHAR(50) NOT NULL UNIQUE,
    mat_khau VARCHAR(120) NOT NULL,
    ma_giang_vien BIGINT NOT NULL UNIQUE,
    ho_ten VARCHAR(120) NOT NULL
);

CREATE TABLE IF NOT EXISTS tai_khoan_khoa (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ten_dang_nhap VARCHAR(50) NOT NULL UNIQUE,
    mat_khau VARCHAR(120) NOT NULL,
    ho_ten VARCHAR(120) NOT NULL
);

CREATE INDEX idx_bang_diem_mon ON bang_diem(mon_hoc_id);
CREATE INDEX idx_phan_cong ON phan_cong_giang_day(ma_giang_vien, mon_hoc_id);

ALTER TABLE bang_diem MODIFY diem_kt_thuong_xuyen DOUBLE NULL;
ALTER TABLE bang_diem MODIFY diem_kt_dinh_ky DOUBLE NULL;
ALTER TABLE bang_diem MODIFY diem_tbc DOUBLE NULL;
ALTER TABLE bang_diem MODIFY trang_thai_du_thi BIT(1) NULL;
