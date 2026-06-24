package com.university.scorems.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "bang_diem", uniqueConstraints = @UniqueConstraint(columnNames = {"hoc_sinh_id", "mon_hoc_id"}))
public class BangDiem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hoc_sinh_id", nullable = false)
    private HocSinh hocSinh;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mon_hoc_id", nullable = false)
    private MonHoc monHoc;

    @Column(name = "diem_kt_thuong_xuyen")
    private Double diemKTThuongXuyen;

    @Column(name = "diem_kt_dinh_ky")
    private Double diemKTDinhKy;

    @Column(name = "diem_tbc")
    private Double diemTBC;

    @Column(name = "trang_thai_du_thi")
    private Boolean trangThaiDuThi;

    @Column(name = "diem_kt_ket_thuc")
    private Double diemKTKetThuc;

    @Column(name = "diem_tong_ket")
    private Double diemTongKet;

    @Column(name = "diem_chu", length = 2)
    private String diemChu;

    @Column(name = "diem_he_4")
    private Double diemHe4;

    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;

    @Column(name = "da_nop_phieu", nullable = false)
    private Boolean daNopPhieu;

    @Column(name = "ngay_nop_phieu")
    private LocalDate ngayNopPhieu;
}

