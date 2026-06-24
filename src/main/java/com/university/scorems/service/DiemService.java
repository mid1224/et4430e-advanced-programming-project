package com.university.scorems.service;

import com.university.scorems.dto.DongBangDiem;
import com.university.scorems.dto.BangDiemResponse;
import com.university.scorems.dto.MonHocTomTat;
import com.university.scorems.dto.NhapBangDiemRequest;
import com.university.scorems.dto.NhapDiemDongRequest;
import com.university.scorems.dto.PhieuBM03Data;
import com.university.scorems.exception.LoiNghiepVuException;
import com.university.scorems.model.BangDiem;
import com.university.scorems.model.HocSinh;
import com.university.scorems.model.MonHoc;
import com.university.scorems.model.PhanCongGiangDay;
import com.university.scorems.model.TaiKhoanGiangVien;
import com.university.scorems.repository.BangDiemRepository;
import com.university.scorems.repository.HocSinhRepository;
import com.university.scorems.repository.PhanCongGiangDayRepository;
import com.university.scorems.repository.TaiKhoanGiangVienRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

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
    public BangDiemResponse layBangDiemTheoMon(Long maGiangVien, Long monHocId) {
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
        return new BangDiemResponse(
            phanCong.getNgayBatDauNhapGiuaKy(),
            phanCong.getNgayKetThucNhapGiuaKy(),
            phanCong.getNgayBatDauNhapCuoiKy(),
            phanCong.getNgayKetThucNhapCuoiKy(),
            ketQua
        );
    }

    @Transactional
    public int luuBangDiem(Long maGiangVien, NhapBangDiemRequest request, boolean bypassPeriodCheck) {
        if (request.getMonHocId() == null) {
            throw new LoiNghiepVuException(HttpStatus.BAD_REQUEST, "Thieu monHocId");
        }
        if (request.getDanhSachDiem() == null || request.getDanhSachDiem().isEmpty()) {
            throw new LoiNghiepVuException(HttpStatus.BAD_REQUEST, "Danh sach diem dang trong");
        }

        PhanCongGiangDay phanCong = layPhanCongHopLe(maGiangVien, request.getMonHocId());
        Long lopHocId = phanCong.getLopHoc().getId();
        MonHoc monHoc = phanCong.getMonHoc();

        LocalDate homNay = LocalDate.now();
        boolean choPhepNhapGiuaKy = bypassPeriodCheck;
        if (!choPhepNhapGiuaKy) {
            choPhepNhapGiuaKy = true;
            if (phanCong.getNgayBatDauNhapGiuaKy() != null && homNay.isBefore(phanCong.getNgayBatDauNhapGiuaKy())) choPhepNhapGiuaKy = false;
            if (phanCong.getNgayKetThucNhapGiuaKy() != null && homNay.isAfter(phanCong.getNgayKetThucNhapGiuaKy())) choPhepNhapGiuaKy = false;
        }

        boolean choPhepNhapCuoiKy = bypassPeriodCheck;
        if (!choPhepNhapCuoiKy) {
            choPhepNhapCuoiKy = true;
            if (phanCong.getNgayBatDauNhapCuoiKy() != null && homNay.isBefore(phanCong.getNgayBatDauNhapCuoiKy())) choPhepNhapCuoiKy = false;
            if (phanCong.getNgayKetThucNhapCuoiKy() != null && homNay.isAfter(phanCong.getNgayKetThucNhapCuoiKy())) choPhepNhapCuoiKy = false;
        }

        int soDongDaLuu = 0;
        for (NhapDiemDongRequest dong : request.getDanhSachDiem()) {
            HocSinh hocSinh = hocSinhRepository.findById(dong.getHocSinhId())
                    .orElseThrow(() -> new LoiNghiepVuException(HttpStatus.NOT_FOUND, "Khong tim thay hoc sinh"));



            Double diemTX = chuanHoaDiem(dong.getDiemKTThuongXuyen(), "diemKTThuongXuyen");
            Double diemDK = chuanHoaDiem(dong.getDiemKTDinhKy(), "diemKTDinhKy");
            Double diemKTKetThucNhap = chuanHoaDiem(dong.getDiemKTKetThuc(), "diemKTKetThuc");

            BangDiem bangDiem = bangDiemRepository.findByHocSinhIdAndMonHocId(hocSinh.getId(), monHoc.getId())
                    .orElseGet(BangDiem::new);

            bangDiem.setHocSinh(hocSinh);
            bangDiem.setMonHoc(monHoc);
            
            if (choPhepNhapGiuaKy) {
                bangDiem.setDiemKTThuongXuyen(diemTX);
                bangDiem.setDiemKTDinhKy(diemDK);
            } else {
                diemTX = bangDiem.getDiemKTThuongXuyen();
                diemDK = bangDiem.getDiemKTDinhKy();
            }

            if (diemTX != null && diemDK != null) {
                Double diemTBC = lamTron((diemTX + diemDK * 2) / 3);
                bangDiem.setDiemTBC(diemTBC);

                boolean duThi = diemTBC >= 4.0;
                bangDiem.setTrangThaiDuThi(duThi);

                Double diemKTKetThuc;
                if (!duThi) {
                    // Rule nghiep vu bat buoc: khong du dieu kien du thi -> diemKTKetThuc = 0.
                    diemKTKetThuc = 0.0;
                } else if (choPhepNhapCuoiKy && diemKTKetThucNhap != null) {
                    diemKTKetThuc = diemKTKetThucNhap;
                } else {
                    // Du dieu kien du thi: giu nguyen diem da co, neu chua nhap thi de trong.
                    diemKTKetThuc = bangDiem.getDiemKTKetThuc();
                }
                bangDiem.setDiemKTKetThuc(diemKTKetThuc);

                if (!duThi || diemKTKetThuc != null) {
                    double heSoGiuaKy = monHoc.getHeSoGiuaKy() != null ? monHoc.getHeSoGiuaKy() : 0.4;
                    double heSoCuoiKy = Math.round((1 - heSoGiuaKy) * 100.0) / 100.0;
                    Double diemTongKet = lamTron(diemTBC * heSoGiuaKy + diemKTKetThuc * heSoCuoiKy);
                    bangDiem.setDiemTongKet(diemTongKet);
                    bangDiem.setDiemChu(tinhDiemChu(diemTongKet));
                    bangDiem.setDiemHe4(tinhDiemHe4(bangDiem.getDiemChu()));
                } else {
                    // Du dieu kien du thi nhung chua co diem KT ket thuc: chua tinh ket qua cuoi cung.
                    bangDiem.setDiemTongKet(null);
                    bangDiem.setDiemChu(null);
                    bangDiem.setDiemHe4(null);
                }
            } else {
                // Truong hop chua nhap du diem thanh phan
                bangDiem.setDiemTBC(null);
                bangDiem.setTrangThaiDuThi(null);
                if (choPhepNhapCuoiKy) {
                    bangDiem.setDiemKTKetThuc(diemKTKetThucNhap != null ? diemKTKetThucNhap : bangDiem.getDiemKTKetThuc());
                }
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
    public int importExcel(Long giangVienId, Long monHocId, MultipartFile file) {
        PhanCongGiangDay phanCong = layPhanCongHopLe(giangVienId, monHocId);
        return processExcelFile(phanCong, file, false);
    }

    @Transactional
    public int importExcelForKhoa(Long phanCongId, MultipartFile file) {
        PhanCongGiangDay phanCong = phanCongGiangDayRepository.findById(phanCongId)
                .orElseThrow(() -> new LoiNghiepVuException(HttpStatus.NOT_FOUND, "Không tìm thấy phân công"));
        return processExcelFile(phanCong, file, true);
    }

    private int processExcelFile(PhanCongGiangDay phanCong, MultipartFile file, boolean isKhoa) {
        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            List<NhapDiemDongRequest> danhSachDiem = new ArrayList<>();
            
            // Skip header row (index 0)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                String mssv = getCellValueAsString(row.getCell(0));
                if (mssv == null || mssv.trim().isEmpty()) continue;
                
                String hoTen = getCellValueAsString(row.getCell(1));
                if (hoTen == null || hoTen.trim().isEmpty()) hoTen = "Chưa cập nhật tên";
                
                HocSinh hocSinh = hocSinhRepository.findByMssv(mssv).orElseGet(() -> {
                    HocSinh hs = new HocSinh();
                    hs.setMssv(mssv);
                    return hs;
                });
                hocSinh.setLopHoc(phanCong.getLopHoc());
                hocSinh.setHoTen(hoTen);
                hocSinhRepository.save(hocSinh);
                
                NhapDiemDongRequest dongReq = new NhapDiemDongRequest();
                dongReq.setHocSinhId(hocSinh.getId());
                dongReq.setDiemKTThuongXuyen(parseDoubleOrNull(getCellValueAsString(row.getCell(2))));
                dongReq.setDiemKTDinhKy(parseDoubleOrNull(getCellValueAsString(row.getCell(3))));
                dongReq.setDiemKTKetThuc(parseDoubleOrNull(getCellValueAsString(row.getCell(4))));
                // Column 5 is ghi chu if present, otherwise ignore
                dongReq.setGhiChu(getCellValueAsString(row.getCell(5)));
                
                danhSachDiem.add(dongReq);
            }
            
            if (!danhSachDiem.isEmpty()) {
                NhapBangDiemRequest req = new NhapBangDiemRequest();
                req.setMonHocId(phanCong.getMonHoc().getId());
                req.setDanhSachDiem(danhSachDiem);
                // Giả lập quyền để luuBangDiem không cần kiểm tra lại
                return luuBangDiem(phanCong.getMaGiangVien(), req, isKhoa);
            }
            return 0;
        } catch (Exception e) {
            throw new LoiNghiepVuException(HttpStatus.BAD_REQUEST, "Lỗi đọc file Excel: " + e.getMessage());
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell);
    }

    private Double parseDoubleOrNull(String str) {
        if (str == null || str.trim().isEmpty()) return null;
        try {
            return Double.parseDouble(str.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public byte[] generateExcelExport(List<DongBangDiem> bangDiemList) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Bang Diem");
            
            // Header
            Row headerRow = sheet.createRow(0);
            String[] headers = {"mssv", "ho_ten", "diem_kt_thuong_xuyen", "diem_kt_dinh_ky", "diem_kt_ket_thuc", "ghi_chu"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            
            // Data
            int rowIdx = 1;
            for (DongBangDiem dong : bangDiemList) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(dong.getMssv() != null ? dong.getMssv() : "");
                row.createCell(1).setCellValue(dong.getHoTen() != null ? dong.getHoTen() : "");
                if (dong.getDiemKTThuongXuyen() != null) row.createCell(2).setCellValue(dong.getDiemKTThuongXuyen());
                if (dong.getDiemKTDinhKy() != null) row.createCell(3).setCellValue(dong.getDiemKTDinhKy());
                if (dong.getDiemKTKetThuc() != null) row.createCell(4).setCellValue(dong.getDiemKTKetThuc());
                row.createCell(5).setCellValue(dong.getGhiChu() != null ? dong.getGhiChu() : "");
            }
            
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new LoiNghiepVuException(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi tạo file Excel: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> exportExcelResponse(Long maGiangVien, Long monHocId) {
        PhanCongGiangDay phanCong = layPhanCongHopLe(maGiangVien, monHocId);
        List<DongBangDiem> list = layBangDiemTheoMon(maGiangVien, monHocId).getDanhSachDiem();
        byte[] excelBytes = generateExcelExport(list);
        
        String filename = String.format("%s_%s_Ky%s.xlsx", 
            phanCong.getLopHoc().getTenLop(), 
            phanCong.getMonHoc().getMaMH(), 
            phanCong.getMonHoc().getHocKy()).replaceAll("\\s+", "_");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> exportExcelResponseKhoa(Long phanCongId, List<DongBangDiem> list) {
        PhanCongGiangDay phanCong = phanCongGiangDayRepository.findById(phanCongId)
                .orElseThrow(() -> new LoiNghiepVuException(HttpStatus.NOT_FOUND, "Không tìm thấy phân công"));
        byte[] excelBytes = generateExcelExport(list);
        
        String filename = String.format("%s_%s_Ky%s.xlsx", 
            phanCong.getLopHoc().getTenLop(), 
            phanCong.getMonHoc().getMaMH(), 
            phanCong.getMonHoc().getHocKy()).replaceAll("\\s+", "_");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
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
        List<DongBangDiem> danhSachDiem = layBangDiemTheoMon(maGiangVien, monHocId).getDanhSachDiem();

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
        if (diem == null) {
            return null;
        }
        if (diem < 0 || diem > 10) {
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

    @Transactional(readOnly = true)
    public PhieuBM03Data layDuLieuPhieuBM03ChoKhoa(Long phanCongId) {
        PhanCongGiangDay phanCong = phanCongGiangDayRepository.findById(phanCongId)
                .orElseThrow(() -> new LoiNghiepVuException(HttpStatus.NOT_FOUND, "Không tìm thấy phân công"));
        MonHoc monHoc = phanCong.getMonHoc();

        List<HocSinh> danhSachHocSinh = hocSinhRepository.findByLopHocIdOrderByHoTenAsc(phanCong.getLopHoc().getId());
        Map<Long, BangDiem> bangDiemMap = new HashMap<>();
        bangDiemRepository.findByMonHocIdOrderByHocSinhHoTenAsc(monHoc.getId())
                .forEach(bd -> bangDiemMap.put(bd.getHocSinh().getId(), bd));

        List<DongBangDiem> danhSachDiem = new ArrayList<>();
        for (HocSinh hocSinh : danhSachHocSinh) {
            BangDiem bd = bangDiemMap.get(hocSinh.getId());
            danhSachDiem.add(new DongBangDiem(
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

        LocalDate ngayLap = bangDiemRepository.findByMonHocIdOrderByHocSinhHoTenAsc(monHoc.getId()).stream()
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
}

