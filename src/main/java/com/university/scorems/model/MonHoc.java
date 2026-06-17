package com.university.scorems.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "mon_hoc")
public class MonHoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_mh", nullable = false, unique = true, length = 30)
    private String maMH;

    @Column(name = "ten_mon_hoc", nullable = false, length = 150)
    private String tenMonHoc;

    @Column(name = "so_tin_chi", nullable = false)
    private Integer soTinChi;

    @Column(name = "hoc_ky", nullable = false)
    private Integer hocKy;

    @Column(name = "nam_hoc", nullable = false, length = 20)
    private String namHoc;

    @Column(name = "hinh_thuc_thi_ket_thuc", nullable = false, length = 100)
    private String hinhThucThiKetThuc;

    @Column(name = "lan_thi_thu", nullable = false)
    private Integer lanThiThu;

    @Column(name = "he_so_giua_ky", nullable = false)
    private Double heSoGiuaKy = 0.4;
}
