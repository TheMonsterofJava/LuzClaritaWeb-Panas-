document.addEventListener('DOMContentLoaded', () => {
    const formOpenBtn = document.querySelector("#form-open");
    const home = document.querySelector(".home");
    const formContainer = document.querySelector(".form_container");
    const formCloseBtn = document.querySelector(".form_close");
    const signupBtn = document.querySelector("#signup");
    const loginBtn = document.querySelector("#login");
    const pwShowHide = document.querySelectorAll(".pw_hide");
    const telefonoInput = document.querySelector("#telefono");
    const urlParams = new URLSearchParams(window.location.search);
    const error = urlParams.get('error');
    const success = urlParams.get('success');
   
    
    // Verificar si hay error al cargar la página
    if (document.body.classList.contains('show-error') && home) {
        home.classList.add("show");
    }
    
    // SweetAlert2 para mostrar mensajes de error
    if (error) {
        Swal.fire({
            icon: 'error',
            title: 'Error : Usuario o Contraseña incorrectos',
        });
    }
    
    if (success) {
        Swal.fire({
            icon: 'success',
            title: 'Inicio de sesión exitoso',
            confirmButtonColor: '#3085d6',
            confirmButtonText: 'Aceptar'
        }).then((result) => {
            if (result.value) {
                $.ajax({
                    //url: "/",
                    type: "GET",
                    success: function(response) {
                        window.location.href = "/home";
                    }
                });
            }
            
        });
    }

    // Abrir formulario
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
        });
    }
    
    // Cambiar a formulario de login
    if (loginBtn && formContainer) {
        loginBtn.addEventListener("click", (e) => {
            e.preventDefault();
            formContainer.classList.remove("active");
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
