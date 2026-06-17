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
        throw new Error(data.thongBao || "C\u00f3 l\u1ed7i x\u1ea3y ra");
    }
    return data;
}

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
        hienBangHeSo();
    } catch (e) {
        hienToast("Loi tai du lieu: " + e.message, false);
    }
}

// ── Dropdowns ─────────────────────────────────────────────────────────────────

function dienDropdowns() {
    const selMon = document.getElementById("selMonHoc");
    const selGV  = document.getElementById("selGiangVien");
    const selLop = document.getElementById("selLopHoc");

    selMon.innerHTML = '<option value="">-- Ch\u1ecdn m\u00f4n h\u1ecdc --</option>' +
        danhSachMonHoc.map(m => `<option value="${m.id}">${m.maMH} - ${m.tenMonHoc}</option>`).join("");

    selGV.innerHTML = '<option value="">-- Ch\u1ecdn gi\u1ea3ng vi\u00ean --</option>' +
        danhSachGiangVien.map(g => `<option value="${g.maGiangVien}">${g.hoTen}</option>`).join("");

    selLop.innerHTML = '<option value="">-- Ch\u1ecdn l\u1edbp h\u1ecdc --</option>' +
        danhSachLopHoc.map(l => `<option value="${l.id}">${l.tenLop} (${l.khoa})</option>`).join("");
}

// ── Assignment Table ──────────────────────────────────────────────────────────

function hienBangPhanCong() {
    const tbody = document.getElementById("bangPhanCongBody");
    if (danhSachPhanCong.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="empty-state">Ch\u01b0a c\u00f3 ph\u00e2n c\u00f4ng n\u00e0o.</td></tr>';
        return;
    }
    tbody.innerHTML = danhSachPhanCong.map((pc, i) => `
        <tr>
            <td class="center">${i + 1}</td>
            <td>${pc.maMH} - ${pc.tenMonHoc}</td>
            <td>${pc.hoTenGiangVien}</td>
            <td>${pc.tenLop}</td>
            <td class="center">
                <button class="btn-danger" onclick="xoaPhanCong(${pc.id})" type="button" style="font-size:12px;height:28px;padding:0 10px;">X\u00f3a</button>
            </td>
        </tr>
    `).join("");
}

// ── Weight Table ──────────────────────────────────────────────────────────────

function hienBangHeSo() {
    const tbody = document.getElementById("bangHeSoBody");
    if (danhSachMonHoc.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="empty-state">Kh\u00f4ng c\u00f3 m\u00f4n h\u1ecdc n\u00e0o.</td></tr>';
        return;
    }
    tbody.innerHTML = danhSachMonHoc.map((m, i) => `
        <tr>
            <td class="center">${i + 1}</td>
            <td class="center">${m.maMH}</td>
            <td>${m.tenMonHoc}</td>
            <td class="center">
                <input class="weight-input" id="hs_${m.id}" type="number" min="0.01" max="0.99" step="0.01" value="${m.heSoGiuaKy}">
            </td>
            <td class="center" id="cuoiky_${m.id}" class="weight-display">${m.heSoCuoiKy}</td>
            <td class="center">
                <button class="btn-success" onclick="luuHeSo(${m.id})" type="button" style="font-size:12px;height:28px;padding:0 10px;">L\u01b0u</button>
            </td>
        </tr>
    `).join("");

    // Live-update the cuoiky display when user types
    danhSachMonHoc.forEach(m => {
        const inp = document.getElementById(`hs_${m.id}`);
        const cuoiKyCell = document.getElementById(`cuoiky_${m.id}`);
        inp.addEventListener("input", () => {
            const v = parseFloat(inp.value);
            if (!isNaN(v) && v > 0 && v < 1) {
                cuoiKyCell.textContent = Math.round((1 - v) * 100) / 100;
            }
        });
    });
}

// ── Actions ───────────────────────────────────────────────────────────────────

document.getElementById("btnPhanCong").addEventListener("click", async () => {
    const monHocId   = document.getElementById("selMonHoc").value;
    const maGiangVien = document.getElementById("selGiangVien").value;
    const lopHocId   = document.getElementById("selLopHoc").value;

    if (!monHocId || !maGiangVien || !lopHocId) {
        hienToast("Vui l\u00f2ng ch\u1ecdn \u0111\u1ee7 m\u00f4n h\u1ecdc, gi\u1ea3ng vi\u00ean v\u00e0 l\u1edbp h\u1ecdc", false);
        return;
    }

    try {
        await goiApi("/api/khoa/phan-cong", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ monHocId: +monHocId, maGiangVien: +maGiangVien, lopHocId: +lopHocId })
        });
        hienToast("Ph\u00e2n c\u00f4ng th\u00e0nh c\u00f4ng!", true);
        danhSachPhanCong = await goiApi("/api/khoa/phan-cong");
        hienBangPhanCong();
    } catch (e) {
        hienToast(e.message, false);
    }
});

async function xoaPhanCong(id) {
    if (!confirm("X\u00e1c nh\u1eadn x\u00f3a ph\u00e2n c\u00f4ng n\u00e0y?")) return;
    try {
        await goiApi(`/api/khoa/phan-cong/${id}`, { method: "DELETE" });
        hienToast("\u0110\u00e3 x\u00f3a ph\u00e2n c\u00f4ng", true);
        danhSachPhanCong = await goiApi("/api/khoa/phan-cong");
        hienBangPhanCong();
    } catch (e) {
        hienToast(e.message, false);
    }
}

async function luuHeSo(monHocId) {
    const inp = document.getElementById(`hs_${monHocId}`);
    const heSoGiuaKy = parseFloat(inp.value);

    if (isNaN(heSoGiuaKy) || heSoGiuaKy <= 0 || heSoGiuaKy >= 1) {
        hienToast("H\u1ec7 s\u1ed1 gi\u1eefa k\u1ef3 ph\u1ea3i trong kho\u1ea3ng (0, 1), v\u00ed d\u1ee5: 0.4", false);
        return;
    }

    try {
        await goiApi("/api/khoa/he-so", {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ monHocId, heSoGiuaKy })
        });
        hienToast("C\u1eadp nh\u1eadt h\u1ec7 s\u1ed1 th\u00e0nh c\u00f4ng!", true);
        // Refresh subject list to get updated values
        danhSachMonHoc = await goiApi("/api/khoa/mon-hoc");
        hienBangHeSo();
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
        hienToast("L\u1ed7i \u0111\u0103ng xu\u1ea5t", false);
    }
});

// ── Init ──────────────────────────────────────────────────────────────────────
taiDuLieu();
