const toast = document.getElementById("toast");

// ── Utilities ────────────────────────────────────────────────────────────────

function hienToast(noiDung, thanhCong) {
    toast.textContent = noiDung;
    toast.className = thanhCong ? "toast show success" : "toast show error";
    setTimeout(() => { toast.className = "toast"; }, 2500);
}

async function goiApi(url, options = {}) {
    const res = await fetch(url, options);
    const data = await res.json();
    if (!res.ok || (data.thanhCong === false)) {
        throw new Error(data.thongBao || "Có lỗi xảy ra");
    }
    return data;
}

// ── Modals Toggle ────────────────────────────────────────────────────────────

function moModal(id) {
    document.getElementById(id).classList.add("show");
}

function dongModal(id) {
    document.getElementById(id).classList.remove("show");
}

// Close modal when clicking outside content area
window.addEventListener("click", (e) => {
    if (e.target.classList.contains("modal")) {
        e.target.classList.remove("show");
    }
});

// ── State ─────────────────────────────────────────────────────────────────────

let danhSachMonHoc = [];
let danhSachGiangVien = [];
let danhSachLopHoc = [];
let danhSachPhanCong = [];

// ── Load Data ─────────────────────────────────────────────────────────────────

async function taiDuLieu() {
    try {
        [danhSachMonHoc, danhSachGiangVien, danhSachLopHoc, danhSachPhanCong] = await Promise.all([
            goiApi("/api/khoa/mon-hoc"),
            goiApi("/api/khoa/giang-vien"),
            goiApi("/api/khoa/lop-hoc"),
            goiApi("/api/khoa/phan-cong")
        ]);
        dienDropdowns();
        hienBangPhanCong();
    } catch (e) {
        hienToast("Lỗi tải dữ liệu: " + e.message, false);
    }
}

// ── Dropdowns ─────────────────────────────────────────────────────────────────

function dienDropdowns() {
    const addMon = document.getElementById("addMonHoc");
    const addGV = document.getElementById("addGiangVien");
    const editGV = document.getElementById("editGiangVien");

    const selectMonHtml = '<option value="">-- Chọn môn học --</option>' +
        danhSachMonHoc.map(m => `<option value="${m.id}">${m.maMH} - ${m.tenMonHoc}</option>`).join("");
    addMon.innerHTML = selectMonHtml;

    const selectGVHtml = '<option value="">-- Chọn giảng viên --</option>' +
        danhSachGiangVien.map(g => `<option value="${g.maGiangVien}">${g.hoTen}</option>`).join("");
    addGV.innerHTML = selectGVHtml;
    editGV.innerHTML = selectGVHtml;
}

// ── Render Assignments Table ───────────────────────────────────────────────

function hienBangPhanCong() {
    const tbody = document.getElementById("bangPhanCongBody");
    if (danhSachPhanCong.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="empty-state">Chưa có phân công nào.</td></tr>';
        return;
    }
    tbody.innerHTML = danhSachPhanCong.map((pc, i) => `
        <tr>
            <td class="center">${i + 1}</td>
            <td style="font-weight: bold;">${pc.tenLop}</td>
            <td><strong>${pc.maMH}</strong> - ${pc.tenMonHoc}</td>
            <td>${pc.hoTenGiangVien}</td>
            <td class="center" style="color: #6b7280; font-weight: 500;">${pc.heSoGiuaKy}</td>
            <td class="center" style="color: #6b7280; font-weight: 500;">${pc.heSoCuoiKy}</td>
            <td class="center">
                <div class="btn-actions-container">
                    <button class="btn-icon view" title="Xem điểm & Xuất BM03" onclick="moViewModal(${i})">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>
                    </button>
                    <button class="btn-icon edit" title="Chỉnh sửa" onclick="moEditModal(${i})">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path><path d="M18.5 2.5a2.121 2.121 0 1 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path></svg>
                    </button>
                    <button class="btn-icon delete" title="Xóa phân công" onclick="xoaPhanCong(${pc.id})">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path><line x1="10" y1="11" x2="10" y2="17"></line><line x1="14" y1="11" x2="14" y2="17"></line></svg>
                    </button>
                </div>
            </td>
        </tr>
    `).join("");
}

// ── Live Calculation in Modals ──────────────────────────────────────────────

function setupLiveWeightCalculation(gkId, ckId) {
    const gkInput = document.getElementById(gkId);
    const ckInput = document.getElementById(ckId);

    gkInput.addEventListener("input", () => {
        const gk = parseFloat(gkInput.value);
        if (!isNaN(gk) && gk > 0 && gk < 1) {
            ckInput.value = (Math.round((1.0 - gk) * 100) / 100).toFixed(2);
        } else {
            ckInput.value = "";
        }
    });
}
setupLiveWeightCalculation("addHeSoGK", "addHeSoCK");
setupLiveWeightCalculation("editHeSoGK", "editHeSoCK");
setupLiveWeightCalculation("addMonHocHeSoGK", "addMonHocHeSoCK");

// ── Add Class Actions ────────────────────────────────────────────────────────

document.getElementById("btnMoAddModal").addEventListener("click", () => {
    document.getElementById("addLopHocInput").value = "";
    document.getElementById("addLopHocKhoa").value = "";
    document.getElementById("addHocKy").value = "";
    document.getElementById("addMonHoc").value = "";
    document.getElementById("addGiangVien").value = "";
    document.getElementById("addHeSoGK").value = "0.4";
    document.getElementById("addHeSoCK").value = "0.6";

    // Auto-calculate the next class code
    const numericClassCodes = danhSachLopHoc
        .map(l => parseInt(l.tenLop))
        .filter(code => !isNaN(code) && code.toString().length === 6);
    
    let nextCode = 166831;
    if (numericClassCodes.length > 0) {
        const maxCode = Math.max(...numericClassCodes);
        nextCode = maxCode + 1;
    }
    
    const yearStr = nextCode.toString().substring(2, 4);
    const nextCohort = "K" + yearStr;

    // Set placeholders
    document.getElementById("addLopHocInput").placeholder = nextCode;
    document.getElementById("addLopHocKhoa").placeholder = nextCohort;

    // Auto-calculate default semester placeholder
    let defaultSem = "2026.1";
    if (danhSachMonHoc.length > 0) {
        const sems = danhSachMonHoc.map(m => {
            if (!m.namHoc) return 20261;
            const startYear = parseInt(m.namHoc.split('-')[0]);
            return (isNaN(startYear) ? 2026 : startYear) * 10 + (m.hocKy || 1);
        });
        const maxSemVal = Math.max(...sems);
        const year = Math.floor(maxSemVal / 10);
        const sem = maxSemVal % 10;
        defaultSem = `${year}.${sem}`;
    }
    document.getElementById("addHocKy").placeholder = defaultSem;

    moModal("addModal");
});

document.getElementById("btnMoAddMonHocModal").addEventListener("click", () => {
    document.getElementById("addMaMHInput").value = "";
    document.getElementById("addTenMonHocInput").value = "";
    document.getElementById("addSoTinChiInput").value = "3";
    document.getElementById("addHinhThucThiInput").value = "Tự luận";
    document.getElementById("addMonHocHocKy").value = "";
    document.getElementById("addMonHocLanThi").value = "1";
    document.getElementById("addMonHocHeSoGK").value = "0.4";
    document.getElementById("addMonHocHeSoCK").value = "0.6";

    // Auto-calculate default semester placeholder
    let defaultSem = "2026.1";
    if (danhSachMonHoc.length > 0) {
        const sems = danhSachMonHoc.map(m => {
            if (!m.namHoc) return 20261;
            const startYear = parseInt(m.namHoc.split('-')[0]);
            return (isNaN(startYear) ? 2026 : startYear) * 10 + (m.hocKy || 1);
        });
        const maxSemVal = Math.max(...sems);
        const year = Math.floor(maxSemVal / 10);
        const sem = maxSemVal % 10;
        defaultSem = `${year}.${sem}`;
    }
    document.getElementById("addMonHocHocKy").placeholder = defaultSem;

    moModal("addMonHocModal");
});

document.getElementById("btnSubmitAddMonHoc").addEventListener("click", async () => {
    const maMH = document.getElementById("addMaMHInput").value.trim();
    const tenMonHoc = document.getElementById("addTenMonHocInput").value.trim();
    const soTinChi = parseInt(document.getElementById("addSoTinChiInput").value);
    const hinhThucThiKetThuc = document.getElementById("addHinhThucThiInput").value.trim();
    let semVal = document.getElementById("addMonHocHocKy").value.trim();
    if (!semVal) {
        semVal = document.getElementById("addMonHocHocKy").placeholder;
    }
    const lanThiThu = parseInt(document.getElementById("addMonHocLanThi").value);
    const heSoGiuaKy = parseFloat(document.getElementById("addMonHocHeSoGK").value);

    if (!maMH || !tenMonHoc || isNaN(soTinChi) || !hinhThucThiKetThuc || isNaN(lanThiThu) || isNaN(heSoGiuaKy)) {
        hienToast("Vui lòng nhập đầy đủ thông tin", false);
        return;
    }

    if (heSoGiuaKy <= 0 || heSoGiuaKy >= 1) {
        hienToast("Hệ số giữa kỳ phải trong khoảng (0, 1), ví dụ: 0.4", false);
        return;
    }

    // Parse semester (e.g. 2026.1)
    const semParts = semVal.split('.');
    const startYear = parseInt(semParts[0]);
    const hocKy = semParts.length > 1 ? parseInt(semParts[1]) : 1;
    if (isNaN(startYear) || isNaN(hocKy) || hocKy < 1 || hocKy > 3) {
         hienToast("Học kỳ không hợp lệ. Ví dụ: 2025.2", false);
         return;
    }
    const namHoc = `${startYear}-${startYear + 1}`;

    try {
        await goiApi("/api/khoa/mon-hoc", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ maMH, tenMonHoc, soTinChi, hinhThucThiKetThuc, hocKy, namHoc, lanThiThu, heSoGiuaKy })
        });
        hienToast("Thêm môn học thành công!", true);
        dongModal("addMonHocModal");
        taiDuLieu();
    } catch (e) {
        hienToast(e.message, false);
    }
});

document.getElementById("btnSubmitAdd").addEventListener("click", async () => {
    let tenLop = document.getElementById("addLopHocInput").value.trim();
    if (!tenLop) {
        tenLop = document.getElementById("addLopHocInput").placeholder;
    }

    let khoa = document.getElementById("addLopHocKhoa").value.trim();
    if (!khoa) {
        khoa = document.getElementById("addLopHocKhoa").placeholder;
    }

    let semVal = document.getElementById("addHocKy").value.trim();
    if (!semVal) {
        semVal = document.getElementById("addHocKy").placeholder;
    }

    const monHocId = document.getElementById("addMonHoc").value;
    const maGiangVien = document.getElementById("addGiangVien").value;
    const heSoGiuaKy = parseFloat(document.getElementById("addHeSoGK").value);

    if (!tenLop.match(/^\d{6}$/)) {
        hienToast("Mã lớp học phải là số gồm 6 chữ số", false);
        return;
    }

    if (!monHocId || !maGiangVien) {
        hienToast("Vui lòng chọn đầy đủ môn học và giảng viên", false);
        return;
    }

    if (isNaN(heSoGiuaKy) || heSoGiuaKy <= 0 || heSoGiuaKy >= 1) {
        hienToast("Hệ số giữa kỳ phải trong khoảng (0, 1), ví dụ: 0.4", false);
        return;
    }

    // Parse semester (e.g. 2026.1)
    const semParts = semVal.split('.');
    const startYear = parseInt(semParts[0]);
    const hocKyNum = semParts.length > 1 ? parseInt(semParts[1]) : 1;
    if (isNaN(startYear) || isNaN(hocKyNum) || hocKyNum < 1 || hocKyNum > 3) {
         hienToast("Học kỳ không hợp lệ. Ví dụ: 2025.2", false);
         return;
    }
    const namHocStr = `${startYear}-${startYear + 1}`;

    try {
        await goiApi("/api/khoa/phan-cong", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ tenLop, khoa, monHocId: +monHocId, maGiangVien: +maGiangVien, heSoGiuaKy, namHoc: namHocStr, hocKy: hocKyNum })
        });
        hienToast("Thêm lớp học và phân công thành công!", true);
        dongModal("addModal");
        taiDuLieu();
    } catch (e) {
        hienToast(e.message, false);
    }
});

// ── Edit Class Actions ───────────────────────────────────────────────────────

function moEditModal(idx) {
    const pc = danhSachPhanCong[idx];
    document.getElementById("editPhanCongId").value = pc.id;
    document.getElementById("editLopHocText").value = pc.tenLop;
    document.getElementById("editMonHocText").value = `${pc.maMH} - ${pc.tenMonHoc}`;
    document.getElementById("editGiangVien").value = pc.maGiangVien;
    document.getElementById("editHeSoGK").value = pc.heSoGiuaKy;
    document.getElementById("editHeSoCK").value = pc.heSoCuoiKy;
    document.getElementById("editNgayBatDauGK").value = pc.ngayBatDauNhapGiuaKy || "";
    document.getElementById("editNgayKetThucGK").value = pc.ngayKetThucNhapGiuaKy || "";
    document.getElementById("editNgayBatDauCK").value = pc.ngayBatDauNhapCuoiKy || "";
    document.getElementById("editNgayKetThucCK").value = pc.ngayKetThucNhapCuoiKy || "";
    moModal("editModal");
}

document.getElementById("btnSubmitEdit").addEventListener("click", async () => {
    const id = document.getElementById("editPhanCongId").value;
    const maGiangVien = document.getElementById("editGiangVien").value;
    const heSoGiuaKy = parseFloat(document.getElementById("editHeSoGK").value);
    const ngayBatDauNhapGiuaKy = document.getElementById("editNgayBatDauGK").value || null;
    const ngayKetThucNhapGiuaKy = document.getElementById("editNgayKetThucGK").value || null;
    const ngayBatDauNhapCuoiKy = document.getElementById("editNgayBatDauCK").value || null;
    const ngayKetThucNhapCuoiKy = document.getElementById("editNgayKetThucCK").value || null;

    if (!maGiangVien) {
        hienToast("Vui lòng chọn giảng viên", false);
        return;
    }

    if (isNaN(heSoGiuaKy) || heSoGiuaKy <= 0 || heSoGiuaKy >= 1) {
        hienToast("Hệ số giữa kỳ phải trong khoảng (0, 1), ví dụ: 0.4", false);
        return;
    }

    try {
        await goiApi(`/api/khoa/phan-cong/${id}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ maGiangVien: +maGiangVien, heSoGiuaKy, ngayBatDauNhapGiuaKy, ngayKetThucNhapGiuaKy, ngayBatDauNhapCuoiKy, ngayKetThucNhapCuoiKy })
        });
        hienToast("Cập nhật phân công và hệ số thành công!", true);
        dongModal("editModal");
        taiDuLieu();
    } catch (e) {
        hienToast(e.message, false);
    }
});

// ── View Score Actions ───────────────────────────────────────────────────────

async function moViewModal(idx) {
    const pc = danhSachPhanCong[idx];
    const metaContainer = document.getElementById("viewMetaInfo");
    metaContainer.innerHTML = `
        <strong>Lớp học:</strong> ${pc.tenLop}<br>
        <strong>Môn học:</strong> ${pc.maMH} - ${pc.tenMonHoc}<br>
        <strong>Giảng viên:</strong> ${pc.hoTenGiangVien} | <strong>Hệ số:</strong> GK ${pc.heSoGiuaKy} / CK ${pc.heSoCuoiKy}
    `;

    // Bind export button
    document.getElementById("btnXuatBM03").onclick = () => {
        window.open(`/phieu-bm03/khoa/${pc.id}`, "_blank");
    };

    // Bind import XML button
    const importInput = document.getElementById("importKhoaXmlInput");
    const importBtn = document.getElementById("btnImportKhoaXml");
    
    importBtn.onclick = () => importInput.click();
    importInput.onchange = async (e) => {
        const file = e.target.files[0];
        if (!file) return;
        
        const formData = new FormData();
        formData.append("file", file);
        
        try {
            const data = await fetch(`/api/khoa/phan-cong/${pc.id}/import-excel`, {
                method: "POST",
                body: formData
            });
            const res = await data.json();
            if (!data.ok || !res.thanhCong) throw new Error(res.thongBao || "Lỗi khi import Excel");
            
            hienToast(res.thongBao, true);
            // Reload view modal to show new scores
            moViewModal(idx);
        } catch (error) {
            hienToast(error.message, false);
        } finally {
            importInput.value = "";
        }
    };

    // Bind export Excel button
    document.getElementById("btnExportKhoaXml").onclick = () => {
        window.open(`/api/khoa/phan-cong/${pc.id}/export-excel`, "_blank");
    };

    // Load score sheet
    const tbody = document.getElementById("bangDiemBody");
    tbody.innerHTML = '<tr><td colspan="11" class="empty-state">Đang tải bảng điểm...</td></tr>';
    moModal("viewModal");

    try {
        const scores = await goiApi(`/api/khoa/phan-cong/${pc.id}/bang-diem`);
        if (scores.length === 0) {
            tbody.innerHTML = '<tr><td colspan="11" class="empty-state">Lớp học này chưa có sinh viên hoặc chưa nhập điểm.</td></tr>';
            return;
        }
        const total = scores.length;
        const missing = scores.filter(s => s.diemKTThuongXuyen === null || s.diemKTDinhKy === null).length;
        metaContainer.innerHTML += `<br><strong>Sĩ số:</strong> ${total} | <strong>Chưa nhập điểm:</strong> ${missing}`;
        tbody.innerHTML = scores.map((s, i) => `
            <tr>
                <td class="center">${i + 1}</td>
                <td class="center" style="font-weight: 500;">${s.mssv}</td>
                <td>${s.hoTen}</td>
                <td class="center">${s.diemKTThuongXuyen ?? ""}</td>
                <td class="center">${s.diemKTDinhKy ?? ""}</td>
                <td class="center" style="font-weight: 500;">${s.diemTBC ?? ""}</td>
                <td class="center">
                    ${s.trangThaiDuThi === true 
                        ? '<span class="badge-status success">Đủ ĐK</span>' 
                        : (s.trangThaiDuThi === false ? '<span class="badge-status error">Hỏng</span>' : "")}
                </td>
                <td class="center">${s.diemKTKetThuc ?? ""}</td>
                <td class="center" style="font-weight: bold; color: #1e3a8a;">${s.diemTongKet ?? ""}</td>
                <td class="center" style="font-weight: bold;">${s.diemChu ?? ""}</td>
                <td>${s.ghiChu ?? ""}</td>
            </tr>
        `).join("");
    } catch (e) {
        tbody.innerHTML = `<tr><td colspan="11" class="empty-state" style="color: #b91c1c;">Lỗi tải bảng điểm: ${e.message}</td></tr>`;
    }
}

// ── Delete Assignment ────────────────────────────────────────────────────────

async function xoaPhanCong(id) {
    if (!confirm("Xác nhận xóa phân công giảng dạy này?")) return;
    try {
        await goiApi(`/api/khoa/phan-cong/${id}`, { method: "DELETE" });
        hienToast("Đã xóa phân công", true);
        taiDuLieu();
    } catch (e) {
        hienToast(e.message, false);
    }
}

// ── Logout ────────────────────────────────────────────────────────────────────

document.getElementById("dangXuatBtn").addEventListener("click", async () => {
    try {
        await fetch("/dang-xuat", { method: "POST" });
        window.location.href = "/dang-nhap";
    } catch (e) {
        hienToast("Lỗi đăng xuất", false);
    }
});

// ── Init ──────────────────────────────────────────────────────────────────────
taiDuLieu();

