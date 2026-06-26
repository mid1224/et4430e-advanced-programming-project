package com.university.scorems.service;

import com.university.scorems.dto.CapNhatHeSoRequest;
import com.university.scorems.dto.GiangVienTomTat;
import com.university.scorems.dto.LopHocTomTat;
import com.university.scorems.dto.MonHocKhoaView;
import com.university.scorems.dto.PhanCongChiTiet;
import com.university.scorems.dto.MonHocRequest;
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
import java.util.Optional;
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
                    ck,
                    pc.getNgayBatDauNhapGiuaKy(),
                    pc.getNgayKetThucNhapGiuaKy(),
                    pc.getNgayBatDauNhapCuoiKy(),
                    pc.getNgayKetThucNhapCuoiKy()
            );
        }).toList();
    }

    @Transactional
    public void phanCongGiangVien(PhanCongRequest request) {
        if (request.getMaGiangVien() == null || request.getMonHocId() == null ||
            (request.getLopHocId() == null && (request.getTenLop() == null || request.getTenLop().trim().isEmpty()))) {
            throw new LoiNghiepVuException(HttpStatus.BAD_REQUEST, "Thieu thong tin phan cong");
        }

        if (!taiKhoanGiangVienRepository.findByMaGiangVien(request.getMaGiangVien()).isPresent()) {
            throw new LoiNghiepVuException(HttpStatus.NOT_FOUND, "Khong tim thay giang vien");
        }

        MonHoc monHoc = monHocRepository.findById(request.getMonHocId())
                .orElseThrow(() -> new LoiNghiepVuException(HttpStatus.NOT_FOUND, "Khong tim thay mon hoc"));

        LopHoc lopHoc = null;
        if (request.getLopHocId() != null) {
            lopHoc = lopHocRepository.findById(request.getLopHocId())
                    .orElseThrow(() -> new LoiNghiepVuException(HttpStatus.NOT_FOUND, "Khong tim thay lop hoc"));
        } else {
            String tenLop = request.getTenLop().trim();
            if (!tenLop.matches("\\d{6}")) {
                throw new LoiNghiepVuException(HttpStatus.BAD_REQUEST, "Mã lớp học phải là số gồm 6 chữ số");
            }

            String khoa = request.getKhoa();
            if (khoa == null || khoa.trim().isEmpty()) {
                khoa = "K" + (Integer.parseInt(tenLop.substring(2, 4)));
            }
            khoa = khoa.trim();

            Optional<LopHoc> optionalLop = lopHocRepository.findByTenLop(tenLop);
            if (optionalLop.isPresent()) {
                LopHoc existingLop = optionalLop.get();
                List<PhanCongGiangDay> pcList = phanCongGiangDayRepository.findByLopHocId(existingLop.getId());
                boolean isCollision = false;
                for (PhanCongGiangDay pc : pcList) {
                    boolean daNopTieu = bangDiemRepository.findByMonHocIdOrderByHocSinhHoTenAsc(pc.getMonHoc().getId()).stream()
                            .allMatch(bd -> bd.getDaNopPhieu() != null && bd.getDaNopPhieu());
                    if (!daNopTieu) {
                        isCollision = true;
                        break;
                    }
                }
                if (isCollision) {
                    throw new LoiNghiepVuException(HttpStatus.BAD_REQUEST, "Lớp học " + tenLop + " đang hoạt động ở học kỳ này. Không thể tạo trùng!");
                }
                lopHoc = existingLop;
            } else {
                lopHoc = new LopHoc();
                lopHoc.setTenLop(tenLop);
                lopHoc.setKhoa(khoa);
                lopHoc = lopHocRepository.save(lopHoc);

                String[] hoTens = { "Nguyễn Văn Hùng", "Trần Thị Lan", "Lê Minh Triết", "Phạm Hoàng Anh" };
                for (int i = 0; i < hoTens.length; i++) {
                    HocSinh hs = new HocSinh();
                    hs.setLopHoc(lopHoc);
                    hs.setHoTen(hoTens[i]);
                    hs.setMssv("20" + tenLop.substring(2, 4) + tenLop.substring(4) + String.format("%03d", i + 1));
                    hocSinhRepository.save(hs);
                }
            }
        }

        if (phanCongGiangDayRepository.existsByMaGiangVienAndMonHocIdAndLopHocId(
                request.getMaGiangVien(), request.getMonHocId(), lopHoc.getId())) {
            throw new LoiNghiepVuException(HttpStatus.BAD_REQUEST, "Giang vien da duoc phan cong mon hoc nay cho lop nay roi");
        }

        boolean needSaveMonHoc = false;
        if (request.getHeSoGiuaKy() != null) {
            if (request.getHeSoGiuaKy() <= 0 || request.getHeSoGiuaKy() >= 1) {
                throw new LoiNghiepVuException(HttpStatus.BAD_REQUEST, "He so giua ky phai trong khoang (0, 1), vi du: 0.4");
            }
            monHoc.setHeSoGiuaKy(request.getHeSoGiuaKy());
            needSaveMonHoc = true;
        }

        if (request.getNamHoc() != null && !request.getNamHoc().trim().isEmpty()) {
            monHoc.setNamHoc(request.getNamHoc().trim());
            needSaveMonHoc = true;
        }

        if (request.getHocKy() != null) {
            monHoc.setHocKy(request.getHocKy());
            needSaveMonHoc = true;
        }

        if (needSaveMonHoc) {
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

        phanCong.setNgayBatDauNhapGiuaKy(request.getNgayBatDauNhapGiuaKy());
        phanCong.setNgayKetThucNhapGiuaKy(request.getNgayKetThucNhapGiuaKy());
        phanCong.setNgayBatDauNhapCuoiKy(request.getNgayBatDauNhapCuoiKy());
        phanCong.setNgayKetThucNhapCuoiKy(request.getNgayKetThucNhapCuoiKy());
        phanCongGiangDayRepository.save(phanCong);
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

    @Transactional
    public void taoMonHoc(MonHocRequest request) {
        if (request.getMaMH() == null || request.getMaMH().trim().isEmpty() ||
            request.getTenMonHoc() == null || request.getTenMonHoc().trim().isEmpty() ||
            request.getSoTinChi() == null || request.getHinhThucThiKetThuc() == null || request.getHinhThucThiKetThuc().trim().isEmpty() ||
            request.getHocKy() == null || request.getNamHoc() == null || request.getNamHoc().trim().isEmpty()) {
            throw new LoiNghiepVuException(HttpStatus.BAD_REQUEST, "Thiếu thông tin môn học");
        }

        String maMH = request.getMaMH().trim();
        if (monHocRepository.existsByMaMH(maMH)) {
            throw new LoiNghiepVuException(HttpStatus.BAD_REQUEST, "Mã môn học đã tồn tại");
        }

        MonHoc monHoc = new MonHoc();
        monHoc.setMaMH(maMH);
        monHoc.setTenMonHoc(request.getTenMonHoc().trim());
        monHoc.setSoTinChi(request.getSoTinChi());
        monHoc.setHinhThucThiKetThuc(request.getHinhThucThiKetThuc().trim());
        monHoc.setHocKy(request.getHocKy());
        monHoc.setNamHoc(request.getNamHoc().trim());

        Integer lanThi = request.getLanThiThu();
        monHoc.setLanThiThu(lanThi != null ? lanThi : 1);

        Double gk = request.getHeSoGiuaKy();
        if (gk != null) {
            if (gk <= 0 || gk >= 1) {
                throw new LoiNghiepVuException(HttpStatus.BAD_REQUEST, "Hệ số giữa kỳ phải trong khoảng (0, 1)");
            }
            monHoc.setHeSoGiuaKy(gk);
        } else {
            monHoc.setHeSoGiuaKy(0.4);
        }

        monHocRepository.save(monHoc);
    }
}

