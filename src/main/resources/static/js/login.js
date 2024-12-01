document.addEventListener('DOMContentLoaded', () => {
    const formOpenBtn = document.querySelector("#form-open");
    const home = document.querySelector(".home");
    const formContainer = document.querySelector(".form_container");
    const formCloseBtn = document.querySelector(".form_close");
    const signupBtn = document.querySelector("#signup");
    const loginBtn = document.querySelector("#login");
    const pwShowHide = document.querySelectorAll(".pw_hide");
    const telefonoInput = document.querySelector("#telefono");

    // Verificar si hay error al cargar la página
    if (document.body.classList.contains('show-error')) {
        home.classList.add("show");
    }

    formOpenBtn.addEventListener("click", () => home.classList.add("show"));
    formCloseBtn.addEventListener("click", () => {
        home.classList.remove("show");
        // Limpiar mensaje de error si existe
        const errorMessage = document.querySelector(".error-message");
        if (errorMessage) {
            errorMessage.style.display = 'none';
        }
    });

    pwShowHide.forEach((icon) => {
        icon.addEventListener("click", () => {
            const getPwInput = icon.parentElement.querySelector("input");
            if (getPwInput.type === "password") {
                getPwInput.type = "text";
                icon.classList.replace("uil-eye-slash", "uil-eye");
            } else {
                getPwInput.type = "password";
                icon.classList.replace("uil-eye", "uil-eye-slash");
            }
        });
    });

    signupBtn.addEventListener("click", (e) => {
        e.preventDefault();
        formContainer.classList.add("active");
    });

    loginBtn.addEventListener("click", (e) => {
        e.preventDefault();
        formContainer.classList.remove("active");
    });

    // Validación del teléfono
    telefonoInput.addEventListener("input", () => {
        const valor = telefonoInput.value;
        if (valor.length > 10) {
            telefonoInput.value = valor.slice(0, 10);
        }
        telefonoInput.value = telefonoInput.value.replace(/[^0-9]/g, '');
    });

    telefonoInput.addEventListener("keydown", (e) => {
        if (telefonoInput.value.length >= 10 && e.key !== "Backspace" && e.key !== "Delete") {
            e.preventDefault();
        }
    });
});
