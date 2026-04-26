package com.university.scorems.service;

import com.university.scorems.dto.DongBangDiem;
import com.university.scorems.dto.MonHocTomTat;
import com.university.scorems.dto.NhapBangDiemRequest;
import com.university.scorems.dto.NhapDiemDongRequest;
import com.university.scorems.dto.PhieuBM03Data;
import com.university.scorems.exception.LoiNghiepVuException;
import com.university.scorems.model.BangDiem;
import com.university.scorems.model.HocSinh;
import com.university.scorems.model.MonHoc;
import com.university.scorems.model.PhanCongGiangDay;
import com.university.scorems.repository.BangDiemRepository;
import com.university.scorems.repository.HocSinhRepository;
import com.university.scorems.repository.PhanCongGiangDayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DiemService {

    private final PhanCongGiangDayRepository phanCongGiangDayRepository;
    private final HocSinhRepository hocSinhRepository;
    private final BangDiemRepository bangDiemRepository;

    @Transactional(readOnly = true)
    public List<MonHocTomTat> layMonHocTheoGiangVien(Long maGiangVien) {
        return phanCongGiangDayRepository.findByMaGiangVien(maGiangVien).stream()
                .map(PhanCongGiangDay::getMonHoc)
                .distinct()
                .sorted(Comparator.comparing(MonHoc::getTenMonHoc))
                .map(mh -> new MonHocTomTat(mh.getId(), mh.getMaMH(), mh.getTenMonHoc(), mh.getHocKy(), mh.getNamHoc()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DongBangDiem> layBangDiemTheoMon(Long maGiangVien, Long monHocId) {
        PhanCongGiangDay phanCong = layPhanCongHopLe(maGiangVien, monHocId);
        List<HocSinh> danhSachHocSinh = hocSinhRepository.findByLopHocIdOrderByHoTenAsc(phanCong.getLopHoc().getId());
        Map<Long, BangDiem> bangDiemMap = new HashMap<>();
        bangDiemRepository.findByMonHocIdOrderByHocSinhHoTenAsc(monHocId)
                .forEach(bd -> bangDiemMap.put(bd.getHocSinh().getId(), bd));

        List<DongBangDiem> ketQua = new ArrayList<>();
        for (HocSinh hocSinh : danhSachHocSinh) {
            BangDiem bd = bangDiemMap.get(hocSinh.getId());
            ketQua.add(new DongBangDiem(
                    hocSinh.getId(),
                    hocSinh.getMssv(),
                    hocSinh.getHoTen(),
                    bd == null ? null : bd.getDiemKTThuongXuyen(),
                    bd == null ? null : bd.getDiemKTDinhKy(),
                    bd == null ? null : bd.getDiemTBC(),
                    bd == null ? null : bd.getTrangThaiDuThi(),
                    bd == null ? null : bd.getDiemKTKetThuc(),
                    bd == null ? null : bd.getDiemTongKet(),
                    bd == null ? null : bd.getDiemChu(),
                    bd == null ? null : bd.getDiemHe4(),
                    bd == null ? null : bd.getGhiChu()
            ));
        }
        return ketQua;
    }

    @Transactional
    public int luuBangDiem(Long maGiangVien, NhapBangDiemRequest request) {
        if (request.getMonHocId() == null) {
            throw new LoiNghiepVuException(HttpStatus.BAD_REQUEST, "Thieu monHocId");
        }
        if (request.getDanhSachDiem() == null || request.getDanhSachDiem().isEmpty()) {
            throw new LoiNghiepVuException(HttpStatus.BAD_REQUEST, "Danh sach diem dang trong");
        }

        PhanCongGiangDay phanCong = layPhanCongHopLe(maGiangVien, request.getMonHocId());
        Long lopHocId = phanCong.getLopHoc().getId();
        MonHoc monHoc = phanCong.getMonHoc();

        int soDongDaLuu = 0;
        for (NhapDiemDongRequest dong : request.getDanhSachDiem()) {
            HocSinh hocSinh = hocSinhRepository.findById(dong.getHocSinhId())
                    .orElseThrow(() -> new LoiNghiepVuException(HttpStatus.NOT_FOUND, "Khong tim thay hoc sinh"));

            if (!hocSinh.getLopHoc().getId().equals(lopHocId)) {
                throw new LoiNghiepVuException(HttpStatus.BAD_REQUEST, "Hoc sinh khong thuoc lop duoc phan cong");
            }

            Double diemTX = chuanHoaDiem(dong.getDiemKTThuongXuyen(), "diemKTThuongXuyen");
            Double diemDK = chuanHoaDiem(dong.getDiemKTDinhKy(), "diemKTDinhKy");
            Double diemKTKetThucNhap = dong.getDiemKTKetThuc() == null ? null : chuanHoaDiem(dong.getDiemKTKetThuc(), "diemKTKetThuc");

            BangDiem bangDiem = bangDiemRepository.findByHocSinhIdAndMonHocId(hocSinh.getId(), monHoc.getId())
                    .orElseGet(BangDiem::new);

            bangDiem.setHocSinh(hocSinh);
            bangDiem.setMonHoc(monHoc);
            bangDiem.setDiemKTThuongXuyen(diemTX);
            bangDiem.setDiemKTDinhKy(diemDK);

            Double diemTBC = lamTron((diemTX + diemDK * 2) / 3);
            bangDiem.setDiemTBC(diemTBC);

            boolean duThi = diemTBC >= 4.0;
            bangDiem.setTrangThaiDuThi(duThi);

            Double diemKTKetThuc;
            if (!duThi) {
                // Rule nghiep vu bat buoc: khong du dieu kien du thi -> diemKTKetThuc = 0.
                diemKTKetThuc = 0.0;
            } else if (diemKTKetThucNhap != null) {
                diemKTKetThuc = diemKTKetThucNhap;
            } else {
                // Du dieu kien du thi: giu nguyen diem da co, neu chua nhap thi de trong.
                diemKTKetThuc = bangDiem.getDiemKTKetThuc();
            }
            bangDiem.setDiemKTKetThuc(diemKTKetThuc);

            if (!duThi || diemKTKetThuc != null) {
                Double diemTongKet = lamTron(diemTBC * 0.4 + diemKTKetThuc * 0.6);
                bangDiem.setDiemTongKet(diemTongKet);
                bangDiem.setDiemChu(tinhDiemChu(diemTongKet));
                bangDiem.setDiemHe4(tinhDiemHe4(bangDiem.getDiemChu()));
            } else {
                // Du dieu kien du thi nhung chua co diem KT ket thuc: chua tinh ket qua cuoi cung.
                bangDiem.setDiemTongKet(null);
                bangDiem.setDiemChu(null);
                bangDiem.setDiemHe4(null);
            }
            bangDiem.setGhiChu(chuanHoaGhiChu(dong.getGhiChu()));

            if (bangDiem.getDaNopPhieu() == null) {
                bangDiem.setDaNopPhieu(false);
            }

            bangDiemRepository.save(bangDiem);
            soDongDaLuu++;
        }
        return soDongDaLuu;
    }

    @Transactional
    public LocalDate nopPhieuKetQua(Long maGiangVien, Long monHocId) {
        layPhanCongHopLe(maGiangVien, monHocId);
        List<BangDiem> danhSachBangDiem = bangDiemRepository.findByMonHocIdOrderByHocSinhHoTenAsc(monHocId);
        if (danhSachBangDiem.isEmpty()) {
            throw new LoiNghiepVuException(HttpStatus.BAD_REQUEST, "Chua co du lieu diem de nop phieu");
        }

        LocalDate homNay = LocalDate.now();
        danhSachBangDiem.forEach(bangDiem -> {
            bangDiem.setDaNopPhieu(true);
            bangDiem.setNgayNopPhieu(homNay);
        });
        bangDiemRepository.saveAll(danhSachBangDiem);
        return homNay;
    }

    @Transactional(readOnly = true)
    public PhieuBM03Data layDuLieuPhieuBM03(Long maGiangVien, Long monHocId) {
        PhanCongGiangDay phanCong = layPhanCongHopLe(maGiangVien, monHocId);
        MonHoc monHoc = phanCong.getMonHoc();
        List<DongBangDiem> danhSachDiem = layBangDiemTheoMon(maGiangVien, monHocId);

        LocalDate ngayLap = bangDiemRepository.findByMonHocIdOrderByHocSinhHoTenAsc(monHocId).stream()
                .map(BangDiem::getNgayNopPhieu)
                .filter(d -> d != null)
                .findFirst()
                .orElse(LocalDate.now());

        return new PhieuBM03Data(
                phanCong.getLopHoc().getKhoa(),
                phanCong.getLopHoc().getTenLop(),
                monHoc.getTenMonHoc(),
                monHoc.getMaMH(),
                monHoc.getHinhThucThiKetThuc(),
                monHoc.getHocKy(),
                monHoc.getNamHoc(),
                monHoc.getLanThiThu(),
                monHoc.getSoTinChi(),
                ngayLap,
                danhSachDiem
        );
    }

    private PhanCongGiangDay layPhanCongHopLe(Long maGiangVien, Long monHocId) {
        if (!phanCongGiangDayRepository.existsByMaGiangVienAndMonHocId(maGiangVien, monHocId)) {
            throw new LoiNghiepVuException(HttpStatus.FORBIDDEN, "Giang vien khong duoc phan cong mon hoc nay");
        }
        return phanCongGiangDayRepository.findFirstByMaGiangVienAndMonHocId(maGiangVien, monHocId);
    }

    private Double chuanHoaDiem(Double diem, String tenTruong) {
        if (diem == null || diem < 0 || diem > 10) {
            throw new LoiNghiepVuException(HttpStatus.BAD_REQUEST, tenTruong + " phai trong khoang 0..10");
        }
        return lamTron(diem);
    }

    private Double lamTron(Double giaTri) {
        return Math.round(giaTri * 100.0) / 100.0;
    }

    private String tinhDiemChu(Double diemTongKet) {
        if (diemTongKet >= 8.5) return "A";
        if (diemTongKet >= 7.0) return "B";
        if (diemTongKet >= 5.5) return "C";
        if (diemTongKet >= 4.0) return "D";
        return "F";
    }

    private String chuanHoaGhiChu(String ghiChu) {
        if (ghiChu == null) {
            return null;
        }
        String ketQua = ghiChu.trim();
        if (ketQua.isEmpty()) {
            return null;
        }
        if (ketQua.length() > 255) {
            throw new LoiNghiepVuException(HttpStatus.BAD_REQUEST, "GhiChu toi da 255 ky tu");
        }
        return ketQua;
    }

    private Double tinhDiemHe4(String diemChu) {
        return switch (diemChu) {
            case "A" -> 4.0;
            case "B" -> 3.0;
            case "C" -> 2.0;
            case "D" -> 1.0;
            default -> 0.0;
        };
    }
}

