const dangNhapForm = document.getElementById("dangNhapForm");
const toast = document.getElementById("toast");

function hienToast(noiDung, thanhCong) {
    toast.textContent = noiDung;
    toast.className = thanhCong ? "toast show success" : "toast show error";
    setTimeout(() => {
        toast.className = "toast";
    }, 2200);
}

async function goiApi(url, options = {}) {
    const res = await fetch(url, options);
    const data = await res.json();
    if (!res.ok || !data.thanhCong) {
        throw new Error(data.thongBao || "C\u00f3 l\u1ed7i x\u1ea3y ra");
    }
    return data;
}

dangNhapForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    try {
        await goiApi("/dang-nhap", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                tenDangNhap: document.getElementById("tenDangNhap").value.trim(),
                matKhau: document.getElementById("matKhau").value
            })
        });
        hienToast("\u0110\u0103ng nh\u1eadp th\u00e0nh c\u00f4ng", true);
        setTimeout(() => {
            window.location.href = "/";
        }, 400);
    } catch (e) {
        hienToast(e.message, false);
    }
});
