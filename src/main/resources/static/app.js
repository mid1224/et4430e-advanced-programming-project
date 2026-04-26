const teacherIdInput = document.getElementById("teacherId");
const courseSelect = document.getElementById("courseSelect");

const classesTableBody = document.getElementById("classesTableBody");
const coursesTableBody = document.getElementById("coursesTableBody");
const scoresTableBody = document.getElementById("scoresTableBody");
const eligibilityTableBody = document.getElementById("eligibilityTableBody");
const finalResultTableBody = document.getElementById("finalResultTableBody");
const bm03SubmittedAt = document.getElementById("bm03SubmittedAt");

const scoreForm = document.getElementById("scoreForm");
const studentIdInput = document.getElementById("studentId");
const regularScoreInput = document.getElementById("regularScore");
const periodicScoreInput = document.getElementById("periodicScore");
const examForm = document.getElementById("examForm");
const examStudentIdInput = document.getElementById("examStudentId");
const examScoreInput = document.getElementById("examScore");
let editingStudentId = null;

function teacherId() {
    return Number(teacherIdInput.value);
}

function selectedCourseId() {
    return Number(courseSelect.value);
}

function setMessage(text) {
    if (text) {
        alert(text);
    }
}

async function request(url, options = {}) {
    const res = await fetch(url, options);
    const isJson = (res.headers.get("content-type") || "").includes("application/json");
    const body = isJson ? await res.json() : await res.text();

    if (!res.ok) {
        const msg = body && body.message ? body.message : `${res.status} ${res.statusText}`;
        throw new Error(msg);
    }
    return body;
}

function renderClasses(classes) {
    classesTableBody.innerHTML = classes.map(c => `
        <tr>
            <td>${c.id}</td>
            <td>${c.name}</td>
        </tr>
    `).join("");
}

function renderCourses(courses) {
    coursesTableBody.innerHTML = courses.map(c => `
        <tr>
            <td>${c.id}</td>
            <td>${c.name}</td>
            <td>${c.credits}</td>
        </tr>
    `).join("");

    courseSelect.innerHTML = courses.map(c => `<option value="${c.id}">${c.id} - ${c.name}</option>`).join("");
}

function renderScores(scores) {
    scoresTableBody.innerHTML = scores.map(s => `
        <tr>
            <td>${s.studentName} (#${s.studentId})</td>
            <td>${s.className}</td>
            <td>${s.regularScore}</td>
            <td>${s.periodicScore}</td>
            <td>${s.averageScore}</td>
            <td>${s.examScore ?? ""}</td>
            <td>${s.finalScore ?? ""}</td>
            <td>${s.finalized}</td>
            <td>
                <button class="secondary" type="button" onclick="fillScoreForm(${s.studentId}, ${s.regularScore}, ${s.periodicScore})">Edit</button>
                <button type="button" onclick="fillExamForm(${s.studentId})">Set Exam</button>
            </td>
        </tr>
    `).join("");
}

function renderEligibility(items) {
    eligibilityTableBody.innerHTML = items.map(i => `
        <tr>
            <td>${i.studentId}</td>
            <td>${i.studentName}</td>
            <td>${i.averageScore}</td>
            <td>${i.eligible ? "Eligible" : "Not eligible"}</td>
        </tr>
    `).join("");
}

function renderFinalResults(items) {
    finalResultTableBody.innerHTML = items.map(i => `
        <tr>
            <td>${i.studentId}</td>
            <td>${i.studentName}</td>
            <td>${i.finalScore}</td>
            <td>${i.gradeLetter}</td>
            <td>${i.grade4Scale}</td>
        </tr>
    `).join("");
}

function renderForms(items) {
    const submittedDates = items
        .map(i => i.submittedAt)
        .filter(v => !!v)
        .sort();

    const latestSubmittedAt = submittedDates.length > 0 ? submittedDates[submittedDates.length - 1] : null;
    bm03SubmittedAt.textContent = `Submission date: ${latestSubmittedAt ?? "N/A"}`;
}

async function loadTeacherData() {
    try {
        const id = teacherId();
        if (!id) throw new Error("Teacher ID is required");

        const classes = await request(`/classes/${id}`);
        const courses = await request(`/courses/${id}`);
        renderClasses(classes);
        renderCourses(courses);

        if (courses.length > 0) {
            await loadCourseData();
        } else {
            scoresTableBody.innerHTML = "";
            eligibilityTableBody.innerHTML = "";
            finalResultTableBody.innerHTML = "";
            bm03SubmittedAt.textContent = "Submission date: N/A";
        }
    } catch (e) {
        setMessage(e.message);
    }
}

async function loadCourseData() {
    try {
        const tId = teacherId();
        const cId = selectedCourseId();
        if (!cId) throw new Error("Select a class first");

        const [scores, eligibility, finalResults, forms] = await Promise.all([
            request(`/scores/course/${cId}?teacherId=${tId}`),
            request(`/eligibility/${cId}?teacherId=${tId}`),
            request(`/final-result/${cId}?teacherId=${tId}`),
            request(`/result-form/${cId}?teacherId=${tId}`)
        ]);

        renderScores(scores);
        renderEligibility(eligibility);
        renderFinalResults(finalResults);
        renderForms(forms);
    } catch (e) {
        setMessage(e.message);
    }
}

window.fillScoreForm = function (studentId, regularScore, periodicScore) {
    editingStudentId = studentId;
    studentIdInput.value = studentId;
    regularScoreInput.value = regularScore;
    periodicScoreInput.value = periodicScore;
};

window.fillExamForm = function (studentId) {
    examStudentIdInput.value = studentId;
};

scoreForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    try {
        const payload = {
            studentId: Number(studentIdInput.value),
            courseId: selectedCourseId(),
            regularScore: Number(regularScoreInput.value),
            periodicScore: Number(periodicScoreInput.value)
        };
        const wasEditing = editingStudentId !== null;
        if (wasEditing) {
            await request(`/scores/student/${editingStudentId}?teacherId=${teacherId()}&courseId=${selectedCourseId()}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });
        } else {
            await request(`/scores?teacherId=${teacherId()}`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });
        }
        scoreForm.reset();
        editingStudentId = null;
        await loadCourseData();
        setMessage(wasEditing ? `Updated score for student ${payload.studentId}` : "Created score");
    } catch (e) {
        setMessage(e.message);
    }
});

examForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    try {
        const payload = {
            studentId: Number(examStudentIdInput.value),
            courseId: selectedCourseId(),
            examScore: Number(examScoreInput.value)
        };
        await request(`/exam-score?teacherId=${teacherId()}`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
        examForm.reset();
        await loadCourseData();
        setMessage(`Finalized score for student ${payload.studentId}`);
    } catch (e) {
        setMessage(e.message);
    }
});

document.getElementById("loadTeacherBtn").addEventListener("click", loadTeacherData);
document.getElementById("loadCourseBtn").addEventListener("click", loadCourseData);
document.getElementById("openBm03Btn").addEventListener("click", openBm03Form);
document.getElementById("submitBm03ClassBtn").addEventListener("click", submitBm03ForClass);
document.getElementById("clearScoreBtn").addEventListener("click", () => {
    scoreForm.reset();
    editingStudentId = null;
    setMessage("Score form cleared");
});

function openBm03Form() {
    const cId = selectedCourseId();
    if (!cId) {
        setMessage("Select a class first");
        return;
    }
    const url = `/forms/bm03/${cId}?teacherId=${teacherId()}&classId=${cId}`;
    window.open(url, "_blank");
}

async function submitBm03ForClass() {
    try {
        const cId = selectedCourseId();
        if (!cId) {
            throw new Error("Select class before submitting BM03");
        }
        const response = await request(`/result-form/submit/class/${cId}?teacherId=${teacherId()}&courseId=${cId}`, {
            method: "POST"
        });
        await loadCourseData();
        setMessage(response.message || `Submitted BM03 for class ${cId}`);
    } catch (e) {
        setMessage(e.message);
    }
}

loadTeacherData();
