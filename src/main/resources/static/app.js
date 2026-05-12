const monHocSelect = document.getElementById("monHocSelect");
const luuBangDiemBtn = document.getElementById("luuBangDiemBtn");
const nopPhieuBtn = document.getElementById("nopPhieuBtn");
const xemPhieuBtn = document.getElementById("xemPhieuBtn");
const dangXuatBtn = document.getElementById("dangXuatBtn");
const thongTinMonHoc = document.getElementById("thongTinMonHoc");
const bangDiemBody = document.getElementById("bangDiemBody");
const toast = document.getElementById("toast");

let danhSachMonHoc = [];
let duLieuBangDiem = [];

function lamTronHaiChuSo(value) {
    return Math.round(value * 100) / 100;
}

function tinhDiemTBC(diemTX, diemDK) {
    if (diemTX == null || diemDK == null) {
        return null;
    }
    return lamTronHaiChuSo((diemTX + diemDK * 2) / 3);
}

function tinhTrangThaiDuThi(diemTBC) {
    if (diemTBC == null) {
        return null;
    }
    return diemTBC >= 4;
}

function hienThiGiaTriSo(value) {
    return value == null ? "" : value;
}

function capNhatDongTinhToan(hocSinhId) {
    const hang = bangDiemBody.querySelector(`tr[data-hocsinhid='${hocSinhId}']`);
    if (!hang) {
        if (window.DEBUG_DIEM) console.debug("capNhatDongTinhToan: hang not found for", hocSinhId);
        return;
    }

    const inputDiemTX = hang.querySelector("input[data-field='diemKTThuongXuyen']");
    const inputDiemDK = hang.querySelector("input[data-field='diemKTDinhKy']");
    const oDiemTBC = hang.querySelector("[data-view='diemTBC']");
    const oTrangThaiDuThi = hang.querySelector("[data-view='trangThaiDuThi']");

    const diemTX = inputDiemTX && inputDiemTX.value !== "" ? Number(inputDiemTX.value) : null;
    const diemDK = inputDiemDK && inputDiemDK.value !== "" ? Number(inputDiemDK.value) : null;
    const diemTBC = tinhDiemTBC(diemTX, diemDK);
    const trangThaiDuThi = tinhTrangThaiDuThi(diemTBC);

    // keep in-memory model in sync so other code can rely on it
    const dongDuLieu = duLieuBangDiem.find(d => String(d.hocSinhId) === String(hocSinhId));
    if (dongDuLieu) {
        dongDuLieu.diemTBC = diemTBC;
        dongDuLieu.trangThaiDuThi = trangThaiDuThi;
    }

    if (window.DEBUG_DIEM) console.debug('capNhatDongTinhToan', {hocSinhId, diemTX, diemDK, diemTBC, trangThaiDuThi});

    if (oDiemTBC) {
        oDiemTBC.textContent = hienThiGiaTriSo(diemTBC);
    }
    if (oTrangThaiDuThi) {
        oTrangThaiDuThi.textContent = trangThaiDuThi == null ? "" : (trangThaiDuThi ? "\u0110\u1ee7 \u0111i\u1ec1u ki\u1ec7n" : "Kh\u00f4ng \u0111\u1ee7 \u0111i\u1ec1u ki\u1ec7n");
    }
}

function escapeThuocTinh(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("\"", "&quot;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;");
}

function hienToast(noiDung, thanhCong) {
    toast.textContent = noiDung;
    toast.className = thanhCong ? "toast show success" : "toast show error";
    setTimeout(() => {
        toast.className = "toast";
    }, 2200);
}

async function goiApi(url, options = {}) {
    const res = await fetch(url, options);
    const isJson = (res.headers.get("content-type") || "").includes("application/json");
    const data = isJson ? await res.json() : null;

    if (!res.ok) {
        const thongBao = data && data.thongBao ? data.thongBao : "C\u00f3 l\u1ed7i x\u1ea3y ra";
        throw new Error(thongBao);
    }
    if (data && data.thanhCong === false) {
        throw new Error(data.thongBao || "C\u00f3 l\u1ed7i x\u1ea3y ra");
    }
    return data;
}

function monHocDangChon() {
    return Number(monHocSelect.value);
}

function veDanhSachMonHoc() {
    monHocSelect.innerHTML = danhSachMonHoc
        .map(monHoc => `<option value="${monHoc.id}">${monHoc.maMH} - ${monHoc.tenMonHoc}</option>`)
        .join("");
}

function veThongTinMonHoc() {
    const monHoc = danhSachMonHoc.find(item => item.id === monHocDangChon());
    if (!monHoc) {
        thongTinMonHoc.textContent = "";
        return;
    }
    thongTinMonHoc.textContent = `H\u1ecdc k\u1ef3: ${monHoc.hocKy} | N\u0103m h\u1ecdc: ${monHoc.namHoc}`;
}

function veBangDiem() {
    bangDiemBody.innerHTML = duLieuBangDiem.map((dong, index) => {
        const diemTX = dong.diemKTThuongXuyen ?? "";
        const diemDK = dong.diemKTDinhKy ?? "";
        const diemKTKetThuc = dong.diemKTKetThuc ?? "";
        const ghiChu = escapeThuocTinh(dong.ghiChu ?? "");

        return `
        <tr data-hocsinhid="${dong.hocSinhId}">
            <td class="center">${index + 1}</td>
            <td class="center">${dong.mssv}</td>
            <td>${dong.hoTen}</td>
            <td><input data-field="diemKTThuongXuyen" data-hocsinhid="${dong.hocSinhId}" type="number" min="0" max="10" step="0.01" value="${diemTX}"></td>
            <td><input data-field="diemKTDinhKy" data-hocsinhid="${dong.hocSinhId}" type="number" min="0" max="10" step="0.01" value="${diemDK}"></td>
            <td class="center" data-view="diemTBC" data-hocsinhid="${dong.hocSinhId}">${dong.diemTBC ?? ""}</td>
            <td class="center" data-view="trangThaiDuThi" data-hocsinhid="${dong.hocSinhId}">${dong.trangThaiDuThi == null ? "" : (dong.trangThaiDuThi ? "\u0110\u1ee7 \u0111i\u1ec1u ki\u1ec7n" : "Kh\u00f4ng \u0111\u1ee7 \u0111i\u1ec1u ki\u1ec7n")}</td>
            <td><input data-field="diemKTKetThuc" data-hocsinhid="${dong.hocSinhId}" type="number" min="0" max="10" step="0.01" value="${diemKTKetThuc}"></td>
            <td class="center">${dong.diemTongKet ?? ""}</td>
            <td class="center">${dong.diemChu ?? ""}</td>
            <td class="center">${dong.diemHe4 ?? ""}</td>
            <td><input data-field="ghiChu" data-hocsinhid="${dong.hocSinhId}" type="text" maxlength="255" value="${ghiChu}"></td>
        </tr>`;
    }).join("");

    duLieuBangDiem.forEach(dong => capNhatDongTinhToan(dong.hocSinhId));
}

bangDiemBody.addEventListener("input", event => {
    const input = event.target;
    if (!(input instanceof HTMLInputElement)) {
        return;
    }
    if (input.dataset.field !== "diemKTThuongXuyen" && input.dataset.field !== "diemKTDinhKy") {
        return;
    }
    capNhatDongTinhToan(input.dataset.hocsinhid);
});

function gomDuLieuNhap() {
    return duLieuBangDiem.map(dong => {
        const layGiaTri = (field) => {
            const input = document.querySelector(`input[data-field='${field}'][data-hocsinhid='${dong.hocSinhId}']`);
            return input && input.value !== "" ? Number(input.value) : null;
        };

        return {
            hocSinhId: dong.hocSinhId,
            diemKTThuongXuyen: layGiaTri("diemKTThuongXuyen"),
            diemKTDinhKy: layGiaTri("diemKTDinhKy"),
            diemKTKetThuc: layGiaTri("diemKTKetThuc"),
            ghiChu: (() => {
                const input = document.querySelector(`input[data-field='ghiChu'][data-hocsinhid='${dong.hocSinhId}']`);
                return input ? input.value : null;
            })()
        };
    });
}

async function taiMonHoc() {
    danhSachMonHoc = await goiApi("/api/mon-hoc");
    if (danhSachMonHoc.length === 0) {
        hienToast("B\u1ea1n ch\u01b0a \u0111\u01b0\u1ee3c ph\u00e2n c\u00f4ng m\u00f4n h\u1ecdc", false);
        return;
    }
    veDanhSachMonHoc();
    veThongTinMonHoc();
    await taiBangDiem();
}

async function taiBangDiem() {
    const monHocId = monHocDangChon();
    if (!monHocId) {
        hienToast("Vui l\u00f2ng ch\u1ecdn m\u00f4n h\u1ecdc", false);
        return;
    }
    duLieuBangDiem = await goiApi(`/api/bang-diem/${monHocId}`);
    veThongTinMonHoc();
    veBangDiem();
}

async function luuBangDiem() {
    const monHocId = monHocDangChon();
    const danhSachDiem = gomDuLieuNhap();

    const ketQua = await goiApi("/api/bang-diem/luu", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ monHocId, danhSachDiem })
    });

    await taiBangDiem();
    hienToast(ketQua.thongBao, true);
}

async function nopPhieu() {
    const monHocId = monHocDangChon();
    const ketQua = await goiApi(`/api/phieu-bm03/nop/${monHocId}`, { method: "POST" });
    await taiBangDiem();
    hienToast(ketQua.thongBao, true);
}

function xemPhieu() {
    const monHocId = monHocDangChon();
    if (!monHocId) {
        hienToast("Vui l\u00f2ng ch\u1ecdn m\u00f4n h\u1ecdc", false);
        return;
    }
    window.open(`/phieu-bm03/${monHocId}`, "_blank");
}

async function dangXuat() {
    await goiApi("/dang-xuat", { method: "POST" });
    window.location.href = "/dang-nhap";
}

luuBangDiemBtn.addEventListener("click", () => luuBangDiem().catch(err => hienToast(err.message, false)));
nopPhieuBtn.addEventListener("click", () => nopPhieu().catch(err => hienToast(err.message, false)));
xemPhieuBtn.addEventListener("click", xemPhieu);
dangXuatBtn.addEventListener("click", () => dangXuat().catch(err => hienToast(err.message, false)));
monHocSelect.addEventListener("change", () => taiBangDiem().catch(err => hienToast(err.message, false)));

taiMonHoc().catch(err => hienToast(err.message, false));
