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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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
            Double diemKTKetThucNhap = chuanHoaDiem(dong.getDiemKTKetThuc(), "diemKTKetThuc");

            BangDiem bangDiem = bangDiemRepository.findByHocSinhIdAndMonHocId(hocSinh.getId(), monHoc.getId())
                    .orElseGet(BangDiem::new);

            bangDiem.setHocSinh(hocSinh);
            bangDiem.setMonHoc(monHoc);
            bangDiem.setDiemKTThuongXuyen(diemTX);
            bangDiem.setDiemKTDinhKy(diemDK);

            if (diemTX != null && diemDK != null) {
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
                bangDiem.setDiemKTKetThuc(diemKTKetThucNhap != null ? diemKTKetThucNhap : bangDiem.getDiemKTKetThuc());
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
    public int importXml(Long giangVienId, Long monHocId, MultipartFile file) {
        PhanCongGiangDay phanCong = layPhanCongHopLe(giangVienId, monHocId);
        return processXmlFile(phanCong, file);
    }

    @Transactional
    public int importXmlForKhoa(Long phanCongId, MultipartFile file) {
        PhanCongGiangDay phanCong = phanCongGiangDayRepository.findById(phanCongId)
                .orElseThrow(() -> new LoiNghiepVuException(HttpStatus.NOT_FOUND, "Không tìm thấy phân công"));
        return processXmlFile(phanCong, file);
    }

    private int processXmlFile(PhanCongGiangDay phanCong, MultipartFile file) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file.getInputStream());
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("SINH_VIEN");
            List<NhapDiemDongRequest> danhSachDiem = new ArrayList<>();

            for (int i = 0; i < nodeList.getLength(); i++) {
                org.w3c.dom.Node node = nodeList.item(i);
                if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String mssv = getTagValue("mssv", element);
                    
                    if (mssv == null || mssv.trim().isEmpty()) {
                        continue;
                    }
                    
                    String hoTen = getTagValue("ho_ten", element);
                    if (hoTen == null || hoTen.trim().isEmpty()) hoTen = "Chưa cập nhật tên";

                    HocSinh hocSinh = hocSinhRepository.findByMssv(mssv).orElseGet(() -> {
                        HocSinh hs = new HocSinh();
                        hs.setMssv(mssv);
                        hs.setLopHoc(phanCong.getLopHoc());
                        return hs;
                    });
                    hocSinh.setHoTen(hoTen);
                    hocSinhRepository.save(hocSinh);

                    NhapDiemDongRequest dongReq = new NhapDiemDongRequest();
                    dongReq.setHocSinhId(hocSinh.getId());
                    dongReq.setDiemKTThuongXuyen(parseDoubleOrNull(getTagValue("diem_kt_thuong_xuyen", element)));
                    dongReq.setDiemKTDinhKy(parseDoubleOrNull(getTagValue("diem_kt_dinh_ky", element)));
                    dongReq.setDiemKTKetThuc(parseDoubleOrNull(getTagValue("diem_kt_ket_thuc", element)));
                    dongReq.setGhiChu(getTagValue("ghi_chu", element));
                    
                    danhSachDiem.add(dongReq);
                }
            }

            if (!danhSachDiem.isEmpty()) {
                NhapBangDiemRequest req = new NhapBangDiemRequest();
                req.setMonHocId(phanCong.getMonHoc().getId());
                req.setDanhSachDiem(danhSachDiem);
                // Giả lập quyền để luuBangDiem không cần kiểm tra lại
                return luuBangDiem(phanCong.getMaGiangVien(), req);
            }
            return 0;
        } catch (Exception e) {
            throw new LoiNghiepVuException(HttpStatus.BAD_REQUEST, "Lỗi đọc file XML: " + e.getMessage());
        }
    }

    private String getTagValue(String tag, Element element) {
        NodeList nlList = element.getElementsByTagName(tag);
        if (nlList != null && nlList.getLength() > 0) {
            org.w3c.dom.Node nValue = nlList.item(0).getFirstChild();
            if (nValue != null) {
                return nValue.getNodeValue();
            }
        }
        return null;
    }

    private Double parseDoubleOrNull(String str) {
        if (str == null || str.trim().isEmpty()) return null;
        try {
            return Double.parseDouble(str.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public String generateXmlExport(List<DongBangDiem> bangDiemList) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<BANG_DIEM>\n");
        for (DongBangDiem dong : bangDiemList) {
            xml.append("    <SINH_VIEN>\n");
            xml.append("        <mssv>").append(dong.getMssv() != null ? dong.getMssv() : "").append("</mssv>\n");
            xml.append("        <ho_ten>").append(dong.getHoTen() != null ? dong.getHoTen() : "").append("</ho_ten>\n");
            xml.append("        <diem_kt_thuong_xuyen>").append(dong.getDiemKTThuongXuyen() != null ? dong.getDiemKTThuongXuyen() : "").append("</diem_kt_thuong_xuyen>\n");
            xml.append("        <diem_kt_dinh_ky>").append(dong.getDiemKTDinhKy() != null ? dong.getDiemKTDinhKy() : "").append("</diem_kt_dinh_ky>\n");
            xml.append("        <diem_kt_ket_thuc>").append(dong.getDiemKTKetThuc() != null ? dong.getDiemKTKetThuc() : "").append("</diem_kt_ket_thuc>\n");
            xml.append("        <ghi_chu>").append(dong.getGhiChu() != null ? dong.getGhiChu() : "").append("</ghi_chu>\n");
            xml.append("    </SINH_VIEN>\n");
        }
        xml.append("</BANG_DIEM>");
        return xml.toString();
    }

    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> exportXmlResponse(Long maGiangVien, Long monHocId) {
        PhanCongGiangDay phanCong = layPhanCongHopLe(maGiangVien, monHocId);
        List<DongBangDiem> list = layBangDiemTheoMon(maGiangVien, monHocId);
        String xml = generateXmlExport(list);
        
        String filename = String.format("%s_%s_Ky%s.xml", 
            phanCong.getLopHoc().getTenLop(), 
            phanCong.getMonHoc().getMaMH(), 
            phanCong.getMonHoc().getHocKy()).replaceAll("\\s+", "_");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_XML)
                .body(xml.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> exportXmlResponseKhoa(Long phanCongId, List<DongBangDiem> list) {
        PhanCongGiangDay phanCong = phanCongGiangDayRepository.findById(phanCongId)
                .orElseThrow(() -> new LoiNghiepVuException(HttpStatus.NOT_FOUND, "Không tìm thấy phân công"));
        String xml = generateXmlExport(list);
        
        String filename = String.format("%s_%s_Ky%s.xml", 
            phanCong.getLopHoc().getTenLop(), 
            phanCong.getMonHoc().getMaMH(), 
            phanCong.getMonHoc().getHocKy()).replaceAll("\\s+", "_");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_XML)
                .body(xml.getBytes(java.nio.charset.StandardCharsets.UTF_8));
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

