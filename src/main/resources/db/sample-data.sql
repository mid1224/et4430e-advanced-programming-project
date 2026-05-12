USE student_score_management;

INSERT INTO lop_hoc (id, ten_lop, khoa)
VALUES (1, '166831', 'K68'),
       (2, '166832', 'K69')
ON DUPLICATE KEY UPDATE ten_lop = VALUES(ten_lop), khoa = VALUES(khoa);

INSERT INTO hoc_sinh (id, mssv, ho_ten, lop_hoc_id)
VALUES (1, '20232349', 'Nguyễn Văn A', 1),
       (2, '20242224', 'Trần Thị B', 1),
       (3, '20233943', 'Lê Văn C', 1),
       (4, '20233869', 'Phạm Thị D', 2)
ON DUPLICATE KEY UPDATE mssv = VALUES(mssv), ho_ten = VALUES(ho_ten), lop_hoc_id = VALUES(lop_hoc_id);

INSERT INTO mon_hoc (id, ma_mh, ten_mon_hoc, so_tin_chi, hoc_ky, nam_hoc, hinh_thuc_thi_ket_thuc, lan_thi_thu)
VALUES (1, 'AP301', 'Lập trình nâng cao', 3, 1, '2026-2027', 'Tự luận', 1),
       (2, 'DB302', 'Hệ quản trị cơ sở dữ liệu', 3, 1, '2026-2027', 'Trắc nghiệm', 1)
ON DUPLICATE KEY UPDATE
    ma_mh = VALUES(ma_mh),
    ten_mon_hoc = VALUES(ten_mon_hoc),
    so_tin_chi = VALUES(so_tin_chi),
    hoc_ky = VALUES(hoc_ky),
    nam_hoc = VALUES(nam_hoc),
    hinh_thuc_thi_ket_thuc = VALUES(hinh_thuc_thi_ket_thuc),
    lan_thi_thu = VALUES(lan_thi_thu);

INSERT INTO phan_cong_giang_day (id, ma_giang_vien, mon_hoc_id, lop_hoc_id)
VALUES (1, 1, 1, 1),
       (2, 1, 2, 2)
ON DUPLICATE KEY UPDATE
    ma_giang_vien = VALUES(ma_giang_vien),
    mon_hoc_id = VALUES(mon_hoc_id),
    lop_hoc_id = VALUES(lop_hoc_id);

INSERT INTO tai_khoan_giang_vien (id, ten_dang_nhap, mat_khau, ma_giang_vien, ho_ten)
VALUES (1, 'gv01', '123456', 1, 'Nguyễn Văn Giang')
ON DUPLICATE KEY UPDATE
    ten_dang_nhap = VALUES(ten_dang_nhap),
    mat_khau = VALUES(mat_khau),
    ma_giang_vien = VALUES(ma_giang_vien),
    ho_ten = VALUES(ho_ten);
