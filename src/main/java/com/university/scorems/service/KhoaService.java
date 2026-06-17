package com.university.scorems.service;

import com.university.scorems.dto.CapNhatHeSoRequest;
import com.university.scorems.dto.GiangVienTomTat;
import com.university.scorems.dto.LopHocTomTat;
import com.university.scorems.dto.MonHocKhoaView;
import com.university.scorems.dto.PhanCongChiTiet;
import com.university.scorems.dto.PhanCongRequest;
import com.university.scorems.exception.LoiNghiepVuException;
import com.university.scorems.dto.CapNhatPhanCongRequest;
import com.university.scorems.dto.DongBangDiem;
import com.university.scorems.model.BangDiem;
import com.university.scorems.model.HocSinh;
import com.university.scorems.model.LopHoc;
import com.university.scorems.model.MonHoc;
import com.university.scorems.model.PhanCongGiangDay;
import com.university.scorems.repository.BangDiemRepository;
import com.university.scorems.repository.HocSinhRepository;
import com.university.scorems.repository.LopHocRepository;
import com.university.scorems.repository.MonHocRepository;
import com.university.scorems.repository.PhanCongGiangDayRepository;
import com.university.scorems.repository.TaiKhoanGiangVienRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KhoaService {

    private final MonHocRepository monHocRepository;
    private final LopHocRepository lopHocRepository;
    private final TaiKhoanGiangVienRepository taiKhoanGiangVienRepository;
    private final PhanCongGiangDayRepository phanCongGiangDayRepository;
    private final HocSinhRepository hocSinhRepository;
    private final BangDiemRepository bangDiemRepository;

    @Transactional(readOnly = true)
    public List<MonHocKhoaView> layDanhSachMonHoc() {
        return monHocRepository.findAll().stream()
                .sorted((a, b) -> a.getTenMonHoc().compareTo(b.getTenMonHoc()))
                .map(mh -> new MonHocKhoaView(
                        mh.getId(),
                        mh.getMaMH(),
                        mh.getTenMonHoc(),
                        mh.getHeSoGiuaKy(),
                        Math.round((1 - mh.getHeSoGiuaKy()) * 100.0) / 100.0
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GiangVienTomTat> layDanhSachGiangVien() {
        return taiKhoanGiangVienRepository.findAllByOrderByHoTenAsc().stream()
                .map(gv -> new GiangVienTomTat(gv.getMaGiangVien(), gv.getHoTen()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LopHocTomTat> layDanhSachLopHoc() {
        return lopHocRepository.findAllByOrderByTenLopAsc().stream()
                .map(lh -> new LopHocTomTat(lh.getId(), lh.getTenLop(), lh.getKhoa()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PhanCongChiTiet> layDanhSachPhanCong() {
        List<PhanCongGiangDay> all = phanCongGiangDayRepository.findAll();

        // Batch-load teacher names to avoid N+1
        Set<Long> maGVSet = all.stream().map(PhanCongGiangDay::getMaGiangVien).collect(Collectors.toSet());
        Map<Long, String> hoTenMap = new HashMap<>();
        maGVSet.forEach(maGV ->
                taiKhoanGiangVienRepository.findByMaGiangVien(maGV)
                        .ifPresent(gv -> hoTenMap.put(maGV, gv.getHoTen()))
        );

        return all.stream().map(pc -> {
            Double gk = pc.getMonHoc().getHeSoGiuaKy() != null ? pc.getMonHoc().getHeSoGiuaKy() : 0.4;
            Double ck = Math.round((1.0 - gk) * 100.0) / 100.0;
            return new PhanCongChiTiet(
                    pc.getId(),
                    pc.getMaGiangVien(),
                    hoTenMap.getOrDefault(pc.getMaGiangVien(), "Không rõ"),
                    pc.getMonHoc().getId(),
                    pc.getMonHoc().getTenMonHoc(),
                    pc.getMonHoc().getMaMH(),
                    pc.getLopHoc().getId(),
                    pc.getLopHoc().getTenLop(),
                    gk,
                    ck
            );
        }).toList();
    }

    @Transactional
    public void phanCongGiangVien(PhanCongRequest request) {
        if (request.getMaGiangVien() == null || request.getMonHocId() == null || request.getLopHocId() == null) {
            throw new LoiNghiepVuException(HttpStatus.BAD_REQUEST, "Thieu thong tin phan cong");
        }

        if (!taiKhoanGiangVienRepository.findByMaGiangVien(request.getMaGiangVien()).isPresent()) {
            throw new LoiNghiepVuException(HttpStatus.NOT_FOUND, "Khong tim thay giang vien");
        }

        MonHoc monHoc = monHocRepository.findById(request.getMonHocId())
                .orElseThrow(() -> new LoiNghiepVuException(HttpStatus.NOT_FOUND, "Khong tim thay mon hoc"));

        LopHoc lopHoc = lopHocRepository.findById(request.getLopHocId())
                .orElseThrow(() -> new LoiNghiepVuException(HttpStatus.NOT_FOUND, "Khong tim thay lop hoc"));

        if (phanCongGiangDayRepository.existsByMaGiangVienAndMonHocIdAndLopHocId(
                request.getMaGiangVien(), request.getMonHocId(), request.getLopHocId())) {
            throw new LoiNghiepVuException(HttpStatus.BAD_REQUEST, "Giang vien da duoc phan cong mon hoc nay cho lop nay roi");
        }

        if (request.getHeSoGiuaKy() != null) {
            if (request.getHeSoGiuaKy() <= 0 || request.getHeSoGiuaKy() >= 1) {
                throw new LoiNghiepVuException(HttpStatus.BAD_REQUEST, "He so giua ky phai trong khoang (0, 1), vi du: 0.4");
            }
            monHoc.setHeSoGiuaKy(request.getHeSoGiuaKy());
            monHocRepository.save(monHoc);
        }

        PhanCongGiangDay phanCong = new PhanCongGiangDay();
        phanCong.setMaGiangVien(request.getMaGiangVien());
        phanCong.setMonHoc(monHoc);
        phanCong.setLopHoc(lopHoc);
        phanCongGiangDayRepository.save(phanCong);
    }

    @Transactional
    public void capNhatHeSo(CapNhatHeSoRequest request) {
        if (request.getHeSoGiuaKy() == null || request.getHeSoGiuaKy() <= 0 || request.getHeSoGiuaKy() >= 1) {
            throw new LoiNghiepVuException(HttpStatus.BAD_REQUEST, "He so giua ky phai trong khoang (0, 1), vi du: 0.4");
        }

        MonHoc monHoc = monHocRepository.findById(request.getMonHocId())
                .orElseThrow(() -> new LoiNghiepVuException(HttpStatus.NOT_FOUND, "Khong tim thay mon hoc"));

        monHoc.setHeSoGiuaKy(request.getHeSoGiuaKy());
        monHocRepository.save(monHoc);
    }

    @Transactional
    public void xoaPhanCong(Long phanCongId) {
        if (!phanCongGiangDayRepository.existsById(phanCongId)) {
            throw new LoiNghiepVuException(HttpStatus.NOT_FOUND, "Khong tim thay phan cong");
        }
        phanCongGiangDayRepository.deleteById(phanCongId);
    }

    @Transactional
    public void capNhatPhanCongVaHeSo(Long id, CapNhatPhanCongRequest request) {
        PhanCongGiangDay phanCong = phanCongGiangDayRepository.findById(id)
                .orElseThrow(() -> new LoiNghiepVuException(HttpStatus.NOT_FOUND, "Không tìm thấy phân công"));

        if (request.getMaGiangVien() != null) {
            if (!taiKhoanGiangVienRepository.findByMaGiangVien(request.getMaGiangVien()).isPresent()) {
                throw new LoiNghiepVuException(HttpStatus.NOT_FOUND, "Không tìm thấy giảng viên");
            }
            phanCong.setMaGiangVien(request.getMaGiangVien());
            phanCongGiangDayRepository.save(phanCong);
        }

        if (request.getHeSoGiuaKy() != null) {
            if (request.getHeSoGiuaKy() <= 0 || request.getHeSoGiuaKy() >= 1) {
                throw new LoiNghiepVuException(HttpStatus.BAD_REQUEST, "Hệ số giữa kỳ phải trong khoảng (0, 1)");
            }
            MonHoc monHoc = phanCong.getMonHoc();
            monHoc.setHeSoGiuaKy(request.getHeSoGiuaKy());
            monHocRepository.save(monHoc);
        }
    }

    @Transactional(readOnly = true)
    public List<DongBangDiem> layBangDiemChoPhanCong(Long phanCongId) {
        PhanCongGiangDay phanCong = phanCongGiangDayRepository.findById(phanCongId)
                .orElseThrow(() -> new LoiNghiepVuException(HttpStatus.NOT_FOUND, "Không tìm thấy phân công"));

        List<HocSinh> danhSachHocSinh = hocSinhRepository.findByLopHocIdOrderByHoTenAsc(phanCong.getLopHoc().getId());
        Map<Long, BangDiem> bangDiemMap = new HashMap<>();
        bangDiemRepository.findByMonHocIdOrderByHocSinhHoTenAsc(phanCong.getMonHoc().getId())
                .forEach(bd -> bangDiemMap.put(bd.getHocSinh().getId(), bd));

        return danhSachHocSinh.stream().map(hs -> {
            BangDiem bd = bangDiemMap.get(hs.getId());
            return new DongBangDiem(
                    hs.getId(),
                    hs.getMssv(),
                    hs.getHoTen(),
                    bd == null ? null : bd.getDiemKTThuongXuyen(),
                    bd == null ? null : bd.getDiemKTDinhKy(),
                    bd == null ? null : bd.getDiemTBC(),
                    bd == null ? null : bd.getTrangThaiDuThi(),
                    bd == null ? null : bd.getDiemKTKetThuc(),
                    bd == null ? null : bd.getDiemTongKet(),
                    bd == null ? null : bd.getDiemChu(),
                    bd == null ? null : bd.getDiemHe4(),
                    bd == null ? null : bd.getGhiChu()
            );
        }).toList();
    }
}

