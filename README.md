# Quan Ly Diem Hoc Tap
## Bo sung Khoa Chuyen Mon
1. Tao lop hoc, gan giang vien/sinh vien tuong ung
2. Quan ly toan bo mon hoc lien quan cung voi lop mo ky do
## Luong su dung
1. Dang nhap giao vien.
2. Chon MonHoc/MoDun da phan cong.
3. He thong hien bang danh sach HSSV theo lop.
4. Nhap `DiemKTThuongXuyen`, `DiemKTDinhKy`, `DiemThiKetThuc` theo tung dong.
5. Bam **Luu bang diem**.
6. Bam **Nop phieu BM03** de gan ngay/thang/nam hien tai.
7. Bam **Xem phieu BM03** de in/doi chieu.

## BM03 fields
- Khoa
- MaMH
- Hinh thuc thi ket thuc
- Hoc ky
- Nam hoc
- Lan thi thu
- Ngay/thang/nam tu dong khi nop phieu

## Chay du an
1. Tao CSDL MySQL va cap quyen tai khoan.
2. Chinh `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` trong `application.yml` (neu can).
3. Chay:
   - `mvn spring-boot:run`
4. Mo trinh duyet:
   - `http://localhost:8080`
