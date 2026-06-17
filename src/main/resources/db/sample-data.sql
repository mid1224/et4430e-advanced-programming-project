USE student_score_management;

INSERT INTO lop_hoc (id, ten_lop, khoa)
VALUES (1, '166831', 'K68'),
       (2, '166832', 'K69'),
       (3, '166833', 'K68'),
       (4, '166834', 'K69'),
       (5, '166835', 'K69')
ON DUPLICATE KEY UPDATE ten_lop = VALUES(ten_lop), khoa = VALUES(khoa);

INSERT INTO hoc_sinh (id, mssv, ho_ten, lop_hoc_id)
VALUES (1, '20232349', 'Nguyễn Văn A', 1),
              (2, '20242224', 'Trần Thị B', 1),
       (3, '20233943', 'Lê Văn C', 1),
       (4, '20233944', 'Phạm Thị D', 2),
       (5, '20233945', 'Trần Văn An', 3),
       (6, '20233946', 'Lê Quang Bình', 2),
       (7, '20233947', 'Phùng Quốc Cường', 3),
       (8, '20233948', 'Đỗ Duy Mạnh', 2),
       (9, '20233949', 'Nguyễn Tiến Linh', 1),
       (10, '20233950', 'Quế Ngọc Hải', 3,
       (11, '20233951', 'Đặng Văn Lâm', 1),
       (12, '20233952', 'Nguyễn Quang Hải', 2),
       (13, '20233953', 'Đoàn Văn Hậu', 3),
       (14, '20233954', 'Bùi Tiến Dũng', 2),
       (15, '20233955', 'Nguyễn Phong Hồng Duy', 4),
       (16, '20233956', 'Vũ Văn Thanh', 2),
       (17, '20233957', 'Lương Xuân Trường', 4),
       (18, '20233958', 'Nguyễn Tuấn Anh', 2),
       (19, '20233959', 'Nguyễn Công Phượng', 4),
       (20, '20233960', 'Nguyễn Anh Đức', 2),
       (21, '20233961', 'Phan Văn Đức', 3),
       (22, '20233962', 'Phạm Đức Huy', 2),
       (23, '20233963', 'Đỗ Hùng Dũng', 4),
       (24, '20233964', 'Hồ Tấn Tài', 2),
       (25, '20233965', 'Nguyễn Thành Chung', 1),
       (26, '20233966', 'Trần Đình Trọng', 2),
       (27, '20233967', 'Đỗ Thanh Thịnh', 3),
       (28, '20233968', 'Nguyễn Đức Chiến', 2),
       (29, '20233969', 'Nguyễn Hoàng Đức', 1),
       (30, '20233970', 'Triệu Việt Hưng', 2),
       (31, '20233971', 'Trương Văn Thái Quý', 4),
       (32, '20233972', 'Nguyễn Trọng Hùng', 2),
       (33, '20233973', 'Bùi Tiến Dụng', 1),
       (34, '20233974', 'Huỳnh Tấn Sinh', 2),
       (35, '20233975', 'Nguyễn Hữu Thắng', 3),
       (36, '20233976', 'Lê Ngọc Bảo', 2),
       (37, '20233977', 'Nguyễn Đức Nam', 1),
       (38, '20233978', 'Nguyễn Văn Toản', 2)
ON DUPLICATE KEY UPDATE mssv = VALUES(mssv), ho_ten = VALUES(ho_ten), lop_hoc_id = VALUES(lop_hoc_id);

INSERT INTO mon_hoc (id, ma_mh, ten_mon_hoc, so_tin_chi, hoc_ky, nam_hoc, hinh_thuc_thi_ket_thuc, lan_thi_thu, he_so_giua_ky)
VALUES (1, 'AP301', 'Lập trình nâng cao', 3, 1, '2026-2027', 'Tự luận', 1, 0.4),
       (2, 'DB302', 'Hệ quản trị cơ sở dữ liệu', 3, 1, '2026-2027', 'Trắc nghiệm', 1, 0.4),
       (3, 'DA301', 'Xử lý ảnh số', 3, 1, '2026-2027', 'Tự luận', 1, 0.4),
       (4, 'DC302', 'Kiến trúc máy tính', 3, 1, '2026-2027', 'Trắc nghiệm', 1, 0.4),
       (5, 'DF303', 'Hệ điều hành', 3, 1, '2026-2027', 'Tự luận', 1, 0.4)
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
       (2, 2, 2, 2),
       (3, 3, 3, 3),
       (4, 4, 4, 4),
       (5, 5, 5, 5)
ON DUPLICATE KEY UPDATE
    ma_giang_vien = VALUES(ma_giang_vien),
    mon_hoc_id = VALUES(mon_hoc_id),
    lop_hoc_id = VALUES(lop_hoc_id);

INSERT INTO tai_khoan_giang_vien (id, ten_dang_nhap, mat_khau, ma_giang_vien, ho_ten)
VALUES (1, 'gv01', '123456', 1, 'Nguyễn Văn Giang'),
       (2, 'gv02', '123456', 2, 'Trần Thị Tuyết'),
       (3, 'gv03', '123456', 3, 'Lê Minh Hùng'),
       (4, 'gv04', '123456', 4, 'Phạm Văn Phong'),
       (5, 'gv05', '123456', 5, 'Nguyễn Thị Thảo')
ON DUPLICATE KEY UPDATE
    ten_dang_nhap = VALUES(ten_dang_nhap),
    mat_khau = VALUES(mat_khau),
    ma_giang_vien = VALUES(ma_giang_vien),
    ho_ten = VALUES(ho_ten);

INSERT INTO tai_khoan_khoa (id, ten_dang_nhap, mat_khau, ho_ten)
VALUES (1, 'khoa01', '123456', 'Phòng Khoa Chuyên Môn')
ON DUPLICATE KEY UPDATE
    ten_dang_nhap = VALUES(ten_dang_nhap),
    mat_khau = VALUES(mat_khau),
    ho_ten = VALUES(ho_ten);
