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
let thongTinBangDiem = null;
let phanCongHienTaiId = null; 
let isKhoaMode = window.location.pathname.includes('/khoa_CM');
let isDeleteMode = false;

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

// Hàm tính điểm tổng kết (giả định 30% quá trình, 70% cuối kỳ)
function tinhDiemTongKet(diemTBC, diemKT) {
    if (diemTBC == null || diemKT == null) {
        return null;
    }
    return lamTronHaiChuSo(diemTBC * 0.3 + diemKT * 0.7);
}

// Hàm quy đổi điểm chữ và hệ 4 theo thang điểm HUST
function quyDoiDiem(diemTK) {
    if (diemTK == null) return { diemChu: "", diemHe4: "" };
    if (diemTK >= 8.5) return { diemChu: "A", diemHe4: 4.0 };
    if (diemTK >= 8.0) return { diemChu: "B+", diemHe4: 3.5 };
    if (diemTK >= 7.0) return { diemChu: "B", diemHe4: 3.0 };
    if (diemTK >= 6.5) return { diemChu: "C+", diemHe4: 2.5 };
    if (diemTK >= 5.5) return { diemChu: "C", diemHe4: 2.0 };
    if (diemTK >= 5.0) return { diemChu: "D+", diemHe4: 1.5 };
    if (diemTK >= 4.0) return { diemChu: "D", diemHe4: 1.0 };
    return { diemChu: "F", diemHe4: 0.0 };
}

function hienThiGiaTriSo(value) {
    return value == null ? "" : value;
}

// Thêm tham số danhDauChuaLuu để kiểm soát trạng thái lưu của dòng
function capNhatDongTinhToan(hocSinhId, danhDauChuaLuu = false) {
    const hang = bangDiemBody.querySelector(`tr[data-hocsinhid='${hocSinhId}']`);
    if (!hang) {
        if (window.DEBUG_DIEM) console.debug("capNhatDongTinhToan: hang not found for", hocSinhId);
        return;
    }

    const inputDiemTX = hang.querySelector("input[data-field='diemKTThuongXuyen']");
    const inputDiemDK = hang.querySelector("input[data-field='diemKTDinhKy']");
    const inputDiemKT = hang.querySelector("input[data-field='diemKTKetThuc']");

    const oDiemTBC = hang.querySelector("[data-view='diemTBC']");
    const oTrangThaiDuThi = hang.querySelector("[data-view='trangThaiDuThi']");
    const oDiemTongKet = hang.querySelector("[data-view='diemTongKet']");
    const oDiemChu = hang.querySelector("[data-view='diemChu']");
    const oDiemHe4 = hang.querySelector("[data-view='diemHe4']");
    const oTrangThaiLuu = hang.querySelector("[data-view='trangThaiLuu']");

    const diemTX = inputDiemTX && inputDiemTX.value !== "" ? Number(inputDiemTX.value) : null;
    const diemDK = inputDiemDK && inputDiemDK.value !== "" ? Number(inputDiemDK.value) : null;
    const diemKT = inputDiemKT && inputDiemKT.value !== "" ? Number(inputDiemKT.value) : null;

    const diemTBC = tinhDiemTBC(diemTX, diemDK);
    const trangThaiDuThi = tinhTrangThaiDuThi(diemTBC);
    
    let diemTongKet = null;
    let diemChu = "";
    let diemHe4 = "";

    // Chỉ tính điểm tổng kết nếu đủ điều kiện dự thi
    if (trangThaiDuThi) {
        diemTongKet = tinhDiemTongKet(diemTBC, diemKT);
        const quyDoi = quyDoiDiem(diemTongKet);
        diemChu = quyDoi.diemChu;
        diemHe4 = quyDoi.diemHe4;
    }

    const dongDuLieu = duLieuBangDiem.find(d => String(d.hocSinhId) === String(hocSinhId));
    if (dongDuLieu) {
        dongDuLieu.diemTBC = diemTBC;
        dongDuLieu.trangThaiDuThi = trangThaiDuThi;
        dongDuLieu.diemTongKet = diemTongKet;
        dongDuLieu.diemChu = diemChu;
        dongDuLieu.diemHe4 = diemHe4;
        if (danhDauChuaLuu) {
            dongDuLieu.thayDoi = true;
        }
    }

    if (oDiemTBC) oDiemTBC.textContent = hienThiGiaTriSo(diemTBC);
    
    if (oTrangThaiDuThi) {
        oTrangThaiDuThi.textContent = trangThaiDuThi == null ? "" : (trangThaiDuThi ? "Đủ điều kiện" : "Không đủ điều kiện");
        if (trangThaiDuThi != null) {
            oTrangThaiDuThi.style.color = trangThaiDuThi ? "green" : "red";
        } else {
            oTrangThaiDuThi.style.color = "";
        }
    }

    // Hiển thị điểm ngay lập tức lên UI
    if (oDiemTongKet) oDiemTongKet.textContent = hienThiGiaTriSo(diemTongKet);
    if (oDiemChu) oDiemChu.textContent = diemChu;
    if (oDiemHe4) oDiemHe4.textContent = hienThiGiaTriSo(diemHe4);

    // Cập nhật trạng thái lưu của dòng
    if (oTrangThaiLuu) {
        if (dongDuLieu && dongDuLieu.thayDoi) {
            oTrangThaiLuu.textContent = "Chưa lưu";
            oTrangThaiLuu.style.color = "#d9534f"; // Đỏ
            oTrangThaiLuu.style.fontWeight = "bold";
        } else {
            oTrangThaiLuu.textContent = "Đã lưu";
            oTrangThaiLuu.style.color = "#5cb85c"; // Xanh lá
            oTrangThaiLuu.style.fontWeight = "normal";
        }
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
        const thongBao = data && data.thongBao ? data.thongBao : "Có lỗi xảy ra";
        throw new Error(thongBao);
    }
    if (data && data.thanhCong === false) {
        throw new Error(data.thongBao || "Có lỗi xảy ra");
    }
    return data;
}

function monHocDangChon() {
    return Number(monHocSelect.value);
}

function veDanhSachMonHoc() {
    monHocSelect.innerHTML = danhSachMonHoc
        .map(monHoc => `<option value="${monHoc.id}">${monHoc.maMH} - ${monHoc.tenMonHoc} - ${monHoc.tenLop}</option>`)
        .join("");
}

function capNhatStatsLive() {
    const monHoc = danhSachMonHoc.find(item => item.id === monHocDangChon());
    if (!monHoc || !duLieuBangDiem) return;

    const total = duLieuBangDiem.length;
    let missing = 0;
    
    duLieuBangDiem.forEach(dong => {
        const inputTX = document.querySelector(`input[data-field='diemKTThuongXuyen'][data-hocsinhid='${dong.hocSinhId}']`);
        const inputDK = document.querySelector(`input[data-field='diemKTDinhKy'][data-hocsinhid='${dong.hocSinhId}']`);
        
        const txVal = inputTX ? inputTX.value.trim() : "";
        const dkVal = inputDK ? inputDK.value.trim() : "";
        
        if (txVal === "" || dkVal === "") {
            missing++;
        }
    });

    thongTinMonHoc.textContent = `Học kỳ: ${monHoc.hocKy} | Năm học: ${monHoc.namHoc} | Sĩ số: ${total} | Chưa nhập điểm: ${missing}`;
}

function veThongTinMonHoc() {
    capNhatStatsLive();
}

const cheDoNhapDiemSelect = document.getElementById("cheDoNhapDiemSelect");

function apDungCheDoNhapDiem() {
    if (!cheDoNhapDiemSelect) return;
    const cheDo = cheDoNhapDiemSelect.value;
    const todayStr = new Date().toISOString().split('T')[0];
    
    const checkPeriod = (start, end) => {
        if (start && todayStr < start) return false;
        if (end && todayStr > end) return false;
        return true;
    };

    const isOpenGK = thongTinBangDiem ? checkPeriod(thongTinBangDiem.ngayBatDauNhapGiuaKy, thongTinBangDiem.ngayKetThucNhapGiuaKy) : true;
    const isOpenCK = thongTinBangDiem ? checkPeriod(thongTinBangDiem.ngayBatDauNhapCuoiKy, thongTinBangDiem.ngayKetThucNhapCuoiKy) : true;

    document.querySelectorAll("#bangDiemBody input").forEach(input => {
        const field = input.dataset.field;
        let isPeriodOpen = true;
        let isModeActive = true;

        if (field === "diemKTThuongXuyen" || field === "diemKTDinhKy") {
            isPeriodOpen = isOpenGK;
            if (cheDo === "tx" && field !== "diemKTThuongXuyen") isModeActive = false;
            if (cheDo === "dk" && field !== "diemKTDinhKy") isModeActive = false;
            if (cheDo === "thi") isModeActive = false;
        } else if (field === "diemKTKetThuc") {
            isPeriodOpen = isOpenCK;
            if (cheDo === "tx" || cheDo === "dk") isModeActive = false;
            if (cheDo === "thi" && field !== "diemKTKetThuc") isModeActive = false;
        }

        if (!isPeriodOpen) {
            input.disabled = true;
            input.classList.add("closed-period");
        } else if (!isModeActive) {
            input.disabled = true;
            input.classList.remove("closed-period");
            input.style.backgroundColor = "#e9ecef";
        } else {
            input.disabled = false;
            input.classList.remove("closed-period");
            input.style.backgroundColor = "";
        }
    });
}

if (cheDoNhapDiemSelect) {
    cheDoNhapDiemSelect.addEventListener("change", apDungCheDoNhapDiem);
}

function veBangDiem() {
    const todayStr = new Date().toISOString().split('T')[0];
    
    const checkPeriod = (start, end) => {
        if (start && todayStr < start) return false;
        if (end && todayStr > end) return false;
        return true;
    };

    const isOpenGK = thongTinBangDiem ? checkPeriod(thongTinBangDiem.ngayBatDauNhapGiuaKy, thongTinBangDiem.ngayKetThucNhapGiuaKy) : true;
    const isOpenCK = thongTinBangDiem ? checkPeriod(thongTinBangDiem.ngayBatDauNhapCuoiKy, thongTinBangDiem.ngayKetThucNhapCuoiKy) : true;

    // Remove disabledAttrs logic here, it is handled by apDungCheDoNhapDiem()
    
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
            <td><input data-field="diemKTThuongXuyen" data-hocsinhid="${dong.hocSinhId}" type="number" min="0" max="10" step="0.05" value="${diemTX}"></td>
            <td><input data-field="diemKTDinhKy" data-hocsinhid="${dong.hocSinhId}" type="number" min="0" max="10" step="0.05" value="${diemDK}"></td>
            <td class="center" data-view="diemTBC" data-hocsinhid="${dong.hocSinhId}">${dong.diemTBC ?? ""}</td>
            <td class="center" data-view="trangThaiDuThi" data-hocsinhid="${dong.hocSinhId}">${dong.trangThaiDuThi == null ? "" : (dong.trangThaiDuThi ? "Đủ điều kiện" : "Không đủ điều kiện")}</td>
            <td><input data-field="diemKTKetThuc" data-hocsinhid="${dong.hocSinhId}" type="number" min="0" max="10" step="0.05" value="${diemKTKetThuc}"></td>
            <td class="center" data-view="diemTongKet" data-hocsinhid="${dong.hocSinhId}">${dong.diemTongKet ?? ""}</td>
            <td class="center" data-view="diemChu" data-hocsinhid="${dong.hocSinhId}">${dong.diemChu ?? ""}</td>
            <td class="center" data-view="diemHe4" data-hocsinhid="${dong.hocSinhId}">${dong.diemHe4 ?? ""}</td>
            <td><input data-field="ghiChu" data-hocsinhid="${dong.hocSinhId}" type="text" maxlength="255" value="${ghiChu}" tabindex="-1"></td>
            <td class="center" data-view="trangThaiLuu" data-hocsinhid="${dong.hocSinhId}">Đã lưu</td>
            <td class="delete-col" style="display: ${isDeleteMode ? 'table-cell' : 'none'};">
                <button type="button" class="btn-delete" data-hocsinhid="${dong.hocSinhId}" style="background: none; border: none; cursor: pointer; color: red; font-size: 16px;">🗑️</button>
            </td>
        </tr>`;
    }).join("");

    // Kích hoạt tính toán ban đầu mà không đánh dấu là chưa lưu
    duLieuBangDiem.forEach(dong => capNhatDongTinhToan(dong.hocSinhId, false));
    
    // Apply initial dropdown state
    apDungCheDoNhapDiem();
}

// Bắt sự kiện thay đổi trên bất kỳ ô input nào thuộc bảng để cập nhật trạng thái ngay lập tức
bangDiemBody.addEventListener("input", event => {
    const input = event.target;
    if (!(input instanceof HTMLInputElement)) {
        return;
    }
    
    // Nếu có sự kiện nhập liệu xảy ra, truyền true để đánh dấu hàng đã thay đổi (chưa lưu)
    capNhatDongTinhToan(input.dataset.hocsinhid, true);
    
    // Cập nhật lại stats trực tiếp trên UI
    capNhatStatsLive();
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
        hienToast("Bạn chưa được phân công môn học", false);
        return;
    }
    veDanhSachMonHoc();
    veThongTinMonHoc();
    await taiBangDiem();
}

async function taiBangDiem() {
    const monHocId = monHocDangChon();
    const btnLuu = document.getElementById("luuBangDiemBtn");
    
    if (!monHocId) {
        document.getElementById("bangDiemBody").innerHTML = "";
        document.getElementById("thongTinMonHoc").textContent = "";
        if (btnLuu) btnLuu.disabled = true;
        return;
    }
    
    if (btnLuu) btnLuu.disabled = false;
    // Disable inputs while loading
    const tbody = document.getElementById("bangDiemBody");
    thongTinBangDiem = await goiApi(`/api/bang-diem/${monHocId}?_t=${Date.now()}`);
    
    // Xử lý trường hợp cache trình duyệt trả về mảng cũ hoặc format mới
    if (Array.isArray(thongTinBangDiem)) {
        duLieuBangDiem = thongTinBangDiem;
        thongTinBangDiem = null;
    } else {
        duLieuBangDiem = thongTinBangDiem.danhSachDiem || [];
    }
    veBangDiem();
    veThongTinMonHoc();
}

async function luuBangDiem() {
    const monHocId = monHocDangChon();
    const danhSachDiem = gomDuLieuNhap();

    const ketQua = await goiApi("/api/bang-diem/luu", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ monHocId, danhSachDiem })
    });

    // Khi gọi lại hàm taiBangDiem() danh sách từ máy chủ sẽ được cập nhật, reset lại các cờ thay đổi về "Đã lưu"
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
        hienToast("Vui lòng chọn môn học", false);
        return;
    }
    window.open(`/phieu-bm03/${monHocId}`, "_blank");
}

async function dangXuat() {
    await goiApi("/dang-xuat", { method: "POST" });
    window.location.href = "/dang-nhap";
}

// ── Setup ──────────────────────────────────────────────────────────────────
const importXmlBtn = document.getElementById("importXmlBtn");
const importXmlInput = document.getElementById("importXmlInput");

if (importXmlBtn && importXmlInput) {
    importXmlBtn.addEventListener("click", () => {
        const monHocId = monHocDangChon();
        if (!monHocId) {
            hienToast("Vui lòng chọn môn học trước khi import", false);
            return;
        }
        importXmlInput.click();
    });

    importXmlInput.addEventListener("change", async (e) => {
        const file = e.target.files[0];
        if (!file) return;

        const formData = new FormData();
        formData.append("file", file);

        try {
            const data = await fetch(`/api/bang-diem/import-excel/${monHocDangChon()}`, {
                method: "POST",
                body: formData
            });
            const res = await data.json();
            if (!data.ok || !res.thanhCong) throw new Error(res.thongBao || "Lỗi khi import Excel");
            
            hienToast(res.thongBao, true);
            taiBangDiem();
        } catch (error) {
            hienToast(error.message, false);
        } finally {
            importXmlInput.value = "";
        }
    });
}

const exportXmlBtn = document.getElementById("exportXmlBtn");
if (exportXmlBtn) {
    exportXmlBtn.addEventListener("click", () => {
        const monHocId = monHocDangChon();
        if (!monHocId) {
            hienToast("Vui lòng chọn môn học trước khi export", false);
            return;
        }
        window.open(`/api/bang-diem/export-excel/${monHocId}`, "_blank");
    });
}

const deleteStudentModeBtn = document.getElementById("deleteStudentModeBtn");
if (deleteStudentModeBtn) {
    deleteStudentModeBtn.addEventListener("click", () => {
        isDeleteMode = !isDeleteMode;
        if (isDeleteMode) {
            deleteStudentModeBtn.style.fontWeight = "bold";
            deleteStudentModeBtn.textContent = "Hủy xóa học sinh";
        } else {
            deleteStudentModeBtn.style.fontWeight = "normal";
            deleteStudentModeBtn.textContent = "Chế độ xóa học sinh";
        }
        
        document.querySelectorAll(".delete-col").forEach(col => {
            col.style.display = isDeleteMode ? "table-cell" : "none";
        });
    });
}

bangDiemBody.addEventListener("click", async (e) => {
    const btn = e.target.closest(".btn-delete");
    if (!btn) return;
    
    const hocSinhId = btn.dataset.hocsinhid;
    const monHocId = monHocDangChon();
    if (!hocSinhId || !monHocId) return;
    
    if (!confirm("Bạn có chắc chắn muốn xóa học sinh này khỏi lớp? Toàn bộ điểm của học sinh này trong môn sẽ bị xóa!")) return;
    
    try {
        const data = await fetch(`/api/bang-diem/${monHocId}/hoc-sinh/${hocSinhId}`, {
            method: "DELETE"
        });
        const res = await data.json();
        if (!data.ok || !res.thanhCong) throw new Error(res.thongBao || "Lỗi khi xóa học sinh");
        
        hienToast(res.thongBao, true);
        taiBangDiem();
    } catch (error) {
        hienToast(error.message, false);
    }
});
luuBangDiemBtn.addEventListener("click", () => luuBangDiem().catch(err => hienToast(err.message, false)));
nopPhieuBtn.addEventListener("click", () => nopPhieu().catch(err => hienToast(err.message, false)));
xemPhieuBtn.addEventListener("click", xemPhieu);
dangXuatBtn.addEventListener("click", () => dangXuat().catch(err => hienToast(err.message, false)));
monHocSelect.addEventListener("change", () => taiBangDiem().catch(err => hienToast(err.message, false)));

taiMonHoc().catch(err => hienToast(err.message, false));