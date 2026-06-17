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

// ── Add Class Actions ────────────────────────────────────────────────────────

document.getElementById("btnMoAddModal").addEventListener("click", () => {
    document.getElementById("addLopHocInput").value = "";
    document.getElementById("addLopHocKhoa").value = "";
    document.getElementById("addMonHoc").value = "";
    document.getElementById("addGiangVien").value = "";
    document.getElementById("addHeSoGK").value = "0.4";
    document.getElementById("addHeSoCK").value = "0.6";
    moModal("addModal");
});

document.getElementById("btnTuDongTaoLop").addEventListener("click", () => {
    const numericClassCodes = danhSachLopHoc
        .map(l => parseInt(l.tenLop))
        .filter(code => !isNaN(code) && code.toString().length === 6);
    
    let nextCode = 166831;
    if (numericClassCodes.length > 0) {
        const maxCode = Math.max(...numericClassCodes);
        nextCode = maxCode + 1;
    }
    
    document.getElementById("addLopHocInput").value = nextCode;
    const yearStr = nextCode.toString().substring(2, 4);
    document.getElementById("addLopHocKhoa").value = "K" + yearStr;
});

document.getElementById("btnSubmitAdd").addEventListener("click", async () => {
    const tenLop = document.getElementById("addLopHocInput").value.trim();
    const khoa = document.getElementById("addLopHocKhoa").value.trim();
    const monHocId = document.getElementById("addMonHoc").value;
    const maGiangVien = document.getElementById("addGiangVien").value;
    const heSoGiuaKy = parseFloat(document.getElementById("addHeSoGK").value);

    if (!tenLop) {
        hienToast("Vui lòng nhập mã lớp học", false);
        return;
    }

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

    try {
        await goiApi("/api/khoa/phan-cong", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ tenLop, khoa, monHocId: +monHocId, maGiangVien: +maGiangVien, heSoGiuaKy })
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
    moModal("editModal");
}

document.getElementById("btnSubmitEdit").addEventListener("click", async () => {
    const id = document.getElementById("editPhanCongId").value;
    const maGiangVien = document.getElementById("editGiangVien").value;
    const heSoGiuaKy = parseFloat(document.getElementById("editHeSoGK").value);

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
            body: JSON.stringify({ maGiangVien: +maGiangVien, heSoGiuaKy })
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
        tbody.innerHTML = scores.map((s, i) => `
            <tr>
                <td class="center">${i + 1}</td>
                <td class="center" style="font-weight: 500;">${s.mssv}</td>
                <td>${s.hoTen}</td>
                <td class="center">${s.diemKTThuongXuyen !== null ? s.diemKTThuongXuyen : ""}</td>
                <td class="center">${s.diemKTDinhKy !== null ? s.diemKTDinhKy : ""}</td>
                <td class="center" style="font-weight: 500;">${s.diemTBC !== null ? s.diemTBC : ""}</td>
                <td class="center">
                    ${s.trangThaiDuThi === true 
                        ? '<span class="badge-status success">Đủ ĐK</span>' 
                        : (s.trangThaiDuThi === false ? '<span class="badge-status error">Hỏng</span>' : "")}
                </td>
                <td class="center">${s.diemKTKetThuc !== null ? s.diemKTKetThuc : ""}</td>
                <td class="center" style="font-weight: bold; color: #1e3a8a;">${s.diemTongKet !== null ? s.diemTongKet : ""}</td>
                <td class="center" style="font-weight: bold;">${s.diemChu !== null ? s.diemChu : ""}</td>
                <td>${s.ghiChu !== null ? s.ghiChu : ""}</td>
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

