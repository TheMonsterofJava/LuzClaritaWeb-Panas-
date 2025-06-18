document.addEventListener('DOMContentLoaded', () => {
    const urlParams = new URLSearchParams(window.location.search); // Obtener los parámetros de la URL
    const formOpenBtn = document.querySelector("#form-open");
    const home = document.querySelector(".home"); // Contenedor principal del formulario
    const formContainer = document.querySelector(".form_container"); // Contenedor del formulario (login/registro)
    const formCloseBtn = document.querySelector(".form_close");
    const signupBtn = document.querySelector("#signup"); // Botón "¡Registrate!" en el formulario de login
    const loginBtn = document.querySelector("#login"); // Botón "Inicio De Sesion" en el formulario de registro
    const pwShowHide = document.querySelectorAll(".pw_hide");
    const telefonoInput = document.querySelector("#telefono");
    const registro = urlParams.has('registro'); // Detectar si "registro" está presente en la URL
    const login = urlParams.has('login'); // Detectar si "login" está presente en la URL
    const error = urlParams.get('error');
    const success = urlParams.get('success');
    const info = urlParams.get('info');
    const registroexitoso = urlParams.has('registroexitoso');



    // Si hay un parámetro de registro en la URL, mostrar automáticamente el formulario de registro
    if (registro && home && formContainer) {
        home.classList.add("show"); // Activa el contenedor principal del formulario
        formContainer.classList.add("active"); // Muestra el formulario de registro
    }

    // Si hay un parámetro de login en la URL, mostrar automáticamente el formulario de inicio de sesión
    if (login && home && formContainer) {
        home.classList.add("show"); // Activa el contenedor principal del formulario
        formContainer.classList.remove("active"); // Muestra el formulario de inicio de sesión
    }

    // Verificar si hay error al cargar la página
    if (document.body.classList.contains('show-error') && home) {
        home.classList.add("show");
    }

    // SweetAlert2 para mostrar el registro exitoso
    if (registroexitoso) {
        Swal.fire({
            icon: 'success',
            title: "¡Registro exitoso!",
            text: "¡Bienvenido a Luz Clarita!",
            confirmButtonColor: '#3085d6',
            confirmButtonText: 'Aceptar'
        });
    }


    // Manejar cierre de sesión
    if (urlParams.has('cerrarSesion')) {
        // Limpiar parámetro usando History API
        const cleanUrl = window.location.pathname + window.location.search.replace(/([?&])cerrarSesion=true&?/gi, "");
        window.history.replaceState({}, document.title, cleanUrl);

        Swal.fire({
            icon: 'success',
            title: '¡Sesión cerrada!',
            text: 'Has cerrado sesión correctamente',
            confirmButtonColor: '#3085d6',
            confirmButtonText: 'Aceptar'
        }).then(() => {
            window.location.href = "/"; // Forzar recarga limpia
        });
    }

    // SweetAlert2 para mostrar mensajes de error
    if (error !== null) {
        Swal.fire({
            icon: 'error',
            title: 'Error : Usuario o Contraseña incorrectos',
            confirmButtonText: 'Aceptar',
        }).then((result) => {
            if (result.isConfirmed) {
                window.location.href = "/inicioSesion/login?login"; // Redirigir a la página de inicio de sesión después de aceptar
            }
        }
        );
    }

    // SweetAlert2 para mostrar mensaje de éxito
    if (success) {
        Swal.fire({
            icon: 'success',
            title: "¡¡Sesión Iniciada Correctamente!!",
            confirmButtonColor: '#3085d6',
            confirmButtonText: 'Aceptar'
        }).then((result) => {
            if (result.isConfirmed) {
                window.location.href = "/home"; // Redirigir al home después de aceptar
            }
        });
    }


    // Abrir formulario manualmente cuando se presiona el botón "Login"
    if (formOpenBtn && home) {
        formOpenBtn.addEventListener("click", () => home.classList.add("show"));
    }

    // Cerrar formulario
    if (formCloseBtn && home) {
        formCloseBtn.addEventListener("click", () => {
            home.classList.remove("show");
            // Limpiar mensaje de error si existe
            const errorMessage = document.querySelector(".error-message");
            if (errorMessage) {
                errorMessage.style.display = 'none';
            }
        });
    }

    // Mostrar/ocultar contraseña
    if (pwShowHide) {
        pwShowHide.forEach((icon) => {
            icon.addEventListener("click", () => {
                const getPwInput = icon.parentElement.querySelector("input");
                if (getPwInput) {
                    if (getPwInput.type === "password") {
                        getPwInput.type = "text";
                        icon.classList.replace("uil-eye-slash", "uil-eye");
                    } else {
                        getPwInput.type = "password";
                        icon.classList.replace("uil-eye", "uil-eye-slash");
                    }
                }
            });
        });
    }

    // Cambiar a formulario de registro
    if (signupBtn && formContainer) {
        signupBtn.addEventListener("click", (e) => {
            e.preventDefault();
            formContainer.classList.add("active");
            const url = new URL(window.location);
            url.searchParams.set('registro', '');
            history.replaceState(null, '', url.pathname + url.search);
        });
    }

    // Cambiar a formulario de login
    if (loginBtn && formContainer) {
        loginBtn.addEventListener("click", (e) => {
            e.preventDefault();
            formContainer.classList.remove("active");

            // Quitar el parámetro 'registro' de la URL si existe
            const url = new URL(window.location);
            if (url.searchParams.has('registro')) {
                url.searchParams.delete('registro');
                history.replaceState(null, '', url.pathname + url.search);
            }
        });
    }

    // Validación del teléfono
    if (telefonoInput) {
        telefonoInput.addEventListener("input", () => {
            telefonoInput.value = telefonoInput.value.replace(/[^0-9]/g, '').slice(0, 10);
        });

        telefonoInput.addEventListener("keydown", (e) => {
            if (telefonoInput.value.length >= 10 && e.key !== "Backspace" && e.key !== "Delete") {
                e.preventDefault();
            }
        });
    }

    // SweetAlert2: Quitar la palabra "true" del mensaje de error
    setTimeout(() => {
        let swalContainer = document.getElementById('swal2-html-container');
        if (swalContainer) {
            swalContainer.style.display = 'none';
        }
    }, 1);
});