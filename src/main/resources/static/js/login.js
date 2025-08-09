// src/main/resources/static/js/login.js

document.addEventListener('DOMContentLoaded', () => {
    // --- SELECTORS ---
    const formOpenBtn = document.querySelector("#form-open");
    const home = document.querySelector(".home");
    const formContainer = document.querySelector(".form_container");
    const formCloseBtn = document.querySelector(".form_close");
    const signupBtn = document.querySelector("#signup");
    const loginBtn = document.querySelector("#login");
    const pwShowHide = document.querySelectorAll(".pw_hide");
    const telefonoInput = document.querySelector("#telefono");


    //Verificar si estamos en la pagina de login
    const isLoginPage = window.location.pathname.includes('/inicioSesion/login') ||
    window.location.pathname.includes('/login');

    // --- URL PARAMETER HANDLING ---
    const urlParams = new URLSearchParams(window.location.search);
    const registroParam = urlParams.has('registro');
    const loginParam = urlParams.has('login');
    const errorParam = urlParams.has('error');
    const registroExitosoParam = urlParams.has('registroexitoso');
    const logoutParam = urlParams.has('logout');

    // Show form if 'registro' or 'login' param is present
    if ((registroParam || loginParam) && home && formContainer) {
        home.classList.add("show");
        if (registroParam) {
            formContainer.classList.add("active");
        } else {
            formContainer.classList.remove("active");
        }
    }

    // --- EVENT LISTENERS ---

    if (formOpenBtn && home) {
        formOpenBtn.addEventListener("click", () => home.classList.add("show"));
    }

    if (formCloseBtn && home) {
        formCloseBtn.addEventListener("click", () => home.classList.remove("show"));
    }

    pwShowHide.forEach((icon) => {
        icon.addEventListener("click", () => {
            const getPwInput = icon.parentElement.querySelector("input");
            if (getPwInput) {
                getPwInput.type = getPwInput.type === "password" ? "text" : "password";
                icon.classList.toggle("uil-eye-slash");
                icon.classList.toggle("uil-eye");
            }
        });
    });

    if (signupBtn && formContainer) {
        signupBtn.addEventListener("click", (e) => {
            e.preventDefault();
            formContainer.classList.add("active");
            updateUrlParams('registro', true);
        });
    }

    if (loginBtn && formContainer) {
        loginBtn.addEventListener("click", (e) => {
            e.preventDefault();
            formContainer.classList.remove("active");
            updateUrlParams('login', true);
        });
    }

    if (telefonoInput) {
        telefonoInput.addEventListener("input", () => {
            telefonoInput.value = telefonoInput.value.replace(/[^0-9]/g, '').slice(0, 10);
        });
    }
    
    // --- SWEETALERT NOTIFICATIONS ---
    //Mostrar alertas solo en la pagina de inicio de sesión

    // Handle successful registration
    if (registroExitosoParam && isLoginPage) {
        Swal.fire({
            icon: 'success',
            title: "¡Registro exitoso!",
            text: "¡Bienvenido a Luz Clarita! Ahora puedes iniciar sesión.",
            confirmButtonText: 'Aceptar'
        }).then(() => {
            updateUrlParams('registroexitoso', false); // Clean URL
            // Show login form after acknowledgement
            if (home && formContainer) {
                home.classList.add("show");
                formContainer.classList.remove("active");
            }
        });
    }

    // Handle logout
    if (logoutParam) {
        Swal.fire({
            icon: 'success',
            title: '¡Sesión cerrada!',
            text: 'Has cerrado sesión correctamente.',
            timer: 2000,
            showConfirmButton: false
        }).then(() => {
            updateUrlParams('logout', false); // Clean URL
        });
    }

    // Handle login error
    if (errorParam && isLoginPage) {
        // Show the form first
        if (home && formContainer) {
            home.classList.add("show");
            formContainer.classList.remove("active");
        }
        Swal.fire({
            icon: 'error',
            title: 'Error de inicio de sesión',
            text: 'Usuario o contraseña incorrectos. Por favor, inténtalo de nuevo.',
            confirmButtonText: 'Aceptar'
        }).then(() => {
            updateUrlParams('error', false); // Clean URL
            // Focus email/user field for better UX
            const userInput = document.querySelector('input[name="emailOrUser"]');
            if(userInput) userInput.focus();
        });
    }

    // Handle account update success
    const successParam = urlParams.has('accountUpdated');
    if (successParam && isLoginPage) {
        if (home && formContainer) {
            home.classList.add("show");
            formContainer.classList.remove("active");
        }
        Swal.fire({
            icon: 'success',
            title: 'Cuenta actualizada',
            text: 'Tu información ha sido actualizada correctamente. Por favor, inicia sesión nuevamente.',
            confirmButtonText: 'Aceptar'
        }).then(() => {
            //Esto sirve para que el usuario se redireccione a la página de inicio de sesión
            updateUrlParams('accountUpdated', false);
            const userInput = document.querySelector('input[name="emailOrUser"]');
            if(userInput) userInput.focus();
        });
    }

    const successpasswordParam = urlParams.has('passwordUpdated'); // Changed 'hast' to 'has'
    if (successpasswordParam && isLoginPage) {
        if (home && formContainer) {
            home.classList.add("show");
            formContainer.classList.remove("active");
        }
        Swal.fire({
            icon: 'success',
            title: 'Contraseña actualizada',
            text: 'Tu contraseña ha sido actualizada correctamente. Por favor, inicia sesión nuevamente.',
            confirmButtonText: 'Aceptar'
        }).then(() => {
            updateUrlParams('passwordUpdated', false);
            const userInput = document.querySelector('input[name="emailOrUser"]');
            if(userInput) userInput.focus();
        });
    }

    // --- UTILITY FUNCTIONS ---
    
    /**
     * Updates URL parameters without reloading the page.
     * @param {string} param - The parameter to add or remove.
     * @param {boolean} add - True to add/set the parameter, false to remove it.
     */
    function updateUrlParams(param, add) {
        const url = new URL(window.location);
        if (add) {
            // When adding a param, ensure its counterpart is removed.
            if (param === 'login') url.searchParams.delete('registro');
            if (param === 'registro') url.searchParams.delete('login');
            url.searchParams.set(param, '');
        } else {
            url.searchParams.delete(param);
        }
        history.replaceState({}, '', url.pathname + url.search);
    }
});
