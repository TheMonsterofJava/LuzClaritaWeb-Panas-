// src/main/resources/static/js/login.js
document.addEventListener('DOMContentLoaded', () => {
    const urlParams = new URLSearchParams(window.location.search);

    //Selectores existentes
    const formOpenBtn = document.querySelector("#form-open");
    const home = document.querySelector(".home");
    const formContainer = document.querySelector(".form_container");
    const formCloseBtn = document.querySelector(".form_close");
    const signupBtn = document.querySelector("#signup");
    const loginBtn = document.querySelector("#login");
    const pwShowHide = document.querySelectorAll(".pw_hide");
    const telefonoInput = document.querySelector("#telefono");

    //Parametros de la URL
    const registro = urlParams.has('registro');
    const login = urlParams.has('login');
    const error = urlParams.get('error'); //Manejado por Spring Security "error"
    const success = urlParams.get('success'); // Para el login exitoso
    const registroexitoso = urlParams.has('registroexitoso');
    const logoutSuccess = urlParams.has('logout');
    const syncCart = urlParams.has('syncCart'); // Para sincronizar el carrito desde localStorage

    if (registro && home && formContainer) {
        home.classList.add("show");
        formContainer.classList.add("active");
    }

    if (login && home && formContainer) {
        home.classList.add("show");
        formContainer.classList.remove("active");
    }

    if (document.body.classList.contains('show-error') && home) {
        home.classList.add("show");
    }

    // Manejo del registro exitoso
    if (registroexitoso) {
        if (home) {
            home.classList.remove("show");
        }

        Swal.fire({
            icon: 'success',
            title: "¡Registro exitoso!",
            text: "¡Bienvenido a Luz Clarita!",
            confirmButtonColor: '#3085d6',
            confirmButtonText: 'Aceptar',
            allowOutsideClick: false,
            allowEscapeKey: false
        }).then((result) => {
            if (result.isConfirmed) {
                const cleanUrlParams = new URLSearchParams(window.location.search);
                cleanUrlParams.delete('registroexitoso');
                cleanUrlParams.set('login', '');
                const newUrl = window.location.pathname + (cleanUrlParams.toString() ? '?' + cleanUrlParams.toString() : '');
                window.history.replaceState({}, document.title, newUrl);

                if (home && formContainer) {
                    home.classList.add("show");
                    formContainer.classList.remove("active");
                }
            }
        });
    }

    // Manejar cierre de sesión
    if (urlParams.has('cerrarSesion') || logoutSuccess) {
        const cleanUrlParams = new URLSearchParams(window.location.search);
        cleanUrlParams.delete('cerrarSesion');
        cleanUrlParams.delete('logout');
        const newUrl = window.location.pathname + (cleanUrlParams.toString() ? '?' + cleanUrlParams.toString() : '');
        window.history.replaceState({}, document.title, newUrl);

        if (window.gestionarLogout) {
            window.gestionarLogout();
        } else {
            localStorage.removeItem("carrito");
            console.warn("gestionarLogout no disponible. Carrito local limpiado directamente.");
            Swal.fire({
                icon: 'success',
                title: '¡Sesión cerrada!',
                text: 'Has cerrado sesión correctamente.',
                confirmButtonColor: '#3085d6',
                confirmButtonText: 'Aceptar'
            }).then(() => {
                if (!window.gestionarLogout) window.location.href = "/";
            });
        }
    }

    // Manejo mejorado del error de login
    if (error !== null) {
        // Mostrar el formulario primero
        if (home && formContainer) {
            home.classList.add("show");
            formContainer.classList.remove("active");
        }

        Swal.fire({
            icon: 'error',
            title: 'Error de inicio de sesión',
            text: 'Usuario o contraseña incorrectos. Por favor, inténtalo de nuevo.',
            confirmButtonText: 'Aceptar',
            allowOutsideClick: false,
            allowEscapeKey: false
        }).then((result) => {
            if (result.isConfirmed) {
                // Limpiar parámetro de error de la URL
                const cleanUrlParams = new URLSearchParams(window.location.search);
                cleanUrlParams.delete('error');
                cleanUrlParams.set('login', '');
                const newUrl = window.location.pathname + (cleanUrlParams.toString() ? '?' + cleanUrlParams.toString() : '');
                window.history.replaceState({}, document.title, newUrl);

                // Enfocar el campo de usuario para mejor UX
                const usuarioInput = document.querySelector('input[name="emailOrUser"]');
                if (usuarioInput) {
                    setTimeout(() => usuarioInput.focus(), 100);
                }
            }
        });
    }

    // Manejo de Login exitoso con Spring Security
    if (success || syncCart) {
        let title = "¡Sesión Iniciada Correctamente!"; // CORREGIDO: era titleText
        let shouldSyncCart = false;

        // Si viene de intentar pagar (syncCart = true), mostrar un mensaje diferente
        if (syncCart) {
            title = "¡Sesión Iniciada! Sincronizando carrito..."; // CORREGIDO: era titleText
            shouldSyncCart = true;
        }

        Swal.fire({
            icon: 'success',
            title: title, // CORREGIDO: era titleText
            confirmButtonColor: '#3085d6',
            confirmButtonText: 'Aceptar',
            timer: shouldSyncCart ? undefined : 2000,
            timerProgressBar: !shouldSyncCart,
            didOpen: () => {
                // Limpiar parámetros de la URL
                const cleanUrlParams = new URLSearchParams(window.location.search);
                cleanUrlParams.delete('success');
                cleanUrlParams.delete('syncCart');
                const newUrl = window.location.pathname + (cleanUrlParams.toString() ? '?' + cleanUrlParams.toString() : '');
                window.history.replaceState({}, document.title, newUrl);
            }
        }).then(async (result) => {
            try {
                // Si necesitamos sincronizar el carrito
                if (shouldSyncCart) {
                    await sincronizarCarritoConBackend();
                }
                
                // Llamar a la función de Post-Login del carrito
                if (window.gestionarPostLogin) {
                    console.log("Llamando a gestionarPostLogin desde login.js");
                    await window.gestionarPostLogin();
                } else {
                    console.warn("gestionarPostLogin no disponible.");
                }

                // Redirigir al home después de aceptar o al finalizar el timer
                setTimeout(() => {
                    window.location.href = "/home";
                }, 500);
            } catch (error) {
                console.error("Error al sincronizar el carrito:", error);
                // Aún así redirigir al home
                setTimeout(() => {
                    window.location.href = "/home";
                }, 500);
            }
        });
    }

    // Event listeners para abrir y cerrar el formulario
    if (formOpenBtn && home) {
        formOpenBtn.addEventListener("click", () => home.classList.add("show"));
    }

    if (formCloseBtn && home) {
        formCloseBtn.addEventListener("click", () => {
            home.classList.remove("show");
            const errorMessage = document.querySelector(".error-message");
            if (errorMessage) {
                errorMessage.style.display = 'none';
            }
        });
    }

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

    if (signupBtn && formContainer) {
        signupBtn.addEventListener("click", (e) => {
            e.preventDefault();
            formContainer.classList.add("active");
            const url = new URL(window.location);
            url.searchParams.set('registro', '');
            url.searchParams.delete('login');
            history.replaceState(null, '', url.pathname + url.search);
        });
    }

    if (loginBtn && formContainer) {
        loginBtn.addEventListener("click", (e) => {
            e.preventDefault();
            formContainer.classList.remove("active");
            const url = new URL(window.location);
            url.searchParams.delete('registro');
            url.searchParams.set('login', '');
            history.replaceState(null, '', url.pathname + url.search);
        });
    }

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
});

/**
 * Función: Sincronizar carrito del localStorage con el backend
 * Se llama cuando el usuario se loguea después de intentar pagar
 */
async function sincronizarCarritoConBackend() {
    const carritoLocalString = localStorage.getItem("carrito");

    if (!carritoLocalString) {
        console.log("No hay carrito local para sincronizar");
        return;
    }

    try {
        const carritoLocal = JSON.parse(carritoLocalString);

        if (!carritoLocal || carritoLocal.length === 0) {
            console.log("Carrito local está vacío");
            localStorage.removeItem("carrito");
            return;
        }

        console.log("Sincronizando carrito con backend:", carritoLocal);

        const response = await fetch('/api/carrito/sincronizar', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(carritoLocal)
        });

        if (response.ok) {
            localStorage.removeItem("carrito");
            console.log("Carrito sincronizado exitosamente");

            Swal.fire({
                icon: 'success',
                title: 'Carrito sincronizado',
                text: 'Tus productos se han guardado correctamente.',
                timer: 2000,
                timerProgressBar: true,
                showConfirmButton: false
            });

        } else {
            throw new Error(`Error HTTP: ${response.status}`);
        }

    } catch (error) {
        console.error("Error sincronizando carrito:", error);

        Swal.fire({
            icon: 'warning',
            title: 'Error al sincronizar',
            text: 'No se pudo sincronizar tu carrito. Los productos permanecen guardados localmente.',
            confirmButtonText: 'Entendido'
        });
    }
}

    //Manejo de login anterior manual
    // SweetAlert2 para mostrar mensaje de éxito de INICIO DE SESIÓN
    // if (success) { // 'success' suele ser el parámetro tras un login exitoso con Spring Security
    //     Swal.fire({
    //         icon: 'success',
    //         title: "¡Sesión Iniciada Correctamente!",
    //         confirmButtonColor: '#3085d6',
    //         confirmButtonText: 'Aceptar',
    //         timer: 2000, // Cierre automático opcional
    //         timerProgressBar: true,
    //         didOpen: () => { // Usar didOpen para asegurar que se ejecuta después de que el Swal está en el DOM
    //             // Limpiar el parámetro 'success' de la URL inmediatamente
    //             const cleanUrlParams = new URLSearchParams(window.location.search);
    //             cleanUrlParams.delete('success');
    //             const newUrl = window.location.pathname + (cleanUrlParams.toString() ? '?' + cleanUrlParams.toString() : '');
    //             window.history.replaceState({}, document.title, newUrl);
    //         }
    //     }).then(async (result) => {
    //         // Llamar a la función de gestión de post-login de carrito.js
    //         if (window.gestionarPostLogin) {
    //             console.log("Llamando a gestionarPostLogin desde login.js");
    //             await window.gestionarPostLogin();
    //         } else {
    //             console.warn("gestionarPostLogin no disponible.");
    //         }

    //         // Redirigir al home después de aceptar o al finalizar el timer
    //         // Esperar un breve momento para que la alerta del carrito (si la hay) se muestre.
    //         setTimeout(() => {
    //             window.location.href = "/home";
    //         }, 500); // Ajustar este tiempo si es necesario
    //     });
    // }


// document.addEventListener('DOMContentLoaded', () => {
//     //const urlParams = new URLSearchParams(window.location.search); // Obtener los parámetros de la URL
//     const urlParams = new URLSearchParams(window.location.search);
//     // ... (resto de tus selectores y variables existentes) ...
//     const formOpenBtn = document.querySelector("#form-open");
//     //const home = document.querySelector(".home"); // Contenedor principal del formulario
//     //const formContainer = document.querySelector(".form_container"); // Contenedor del formulario (login/registro)
//     const home = document.querySelector(".home");
//     const formContainer = document.querySelector(".form_container");
//     const formCloseBtn = document.querySelector(".form_close");
//     //const signupBtn = document.querySelector("#signup"); // Botón "¡Registrate!" en el formulario de login
//     //const loginBtn = document.querySelector("#login"); // Botón "Inicio De Sesion" en el formulario de registro
//     const signupBtn = document.querySelector("#signup");
//     const loginBtn = document.querySelector("#login");
//     const pwShowHide = document.querySelectorAll(".pw_hide");
//     const telefonoInput = document.querySelector("#telefono");
//     //const registro = urlParams.has('registro'); // Detectar si "registro" está presente en la URL
//     //const login = urlParams.has('login'); // Detectar si "login" está presente en la URL
//     const registro = urlParams.has('registro');
//     const login = urlParams.has('login');
//     const error = urlParams.get('error');
//     //const success = urlParams.get('success');
//     const success = urlParams.get('success'); // Usaremos este para el login exitoso
//     // const info = urlParams.get('info'); // No usado actualmente
//     const registroexitoso = urlParams.has('registroexitoso');
//     const logoutSuccess = urlParams.has('logout'); // Asumiendo que tu AuthController redirige con ?logout en el éxito del cierre de sesión



//     // Si hay un parámetro de registro en la URL, mostrar automáticamente el formulario de registro
//     if (registro && home && formContainer) {
//         // home.classList.add("show"); // Activa el contenedor principal del formulario
//         // formContainer.classList.add("active"); // Muestra el formulario de registro
//         home.classList.add("show");
//         formContainer.classList.add("active");
//     }

//     // Si hay un parámetro de login en la URL, mostrar automáticamente el formulario de inicio de sesión
//     if (login && home && formContainer) {
//         // home.classList.add("show"); // Activa el contenedor principal del formulario
//         // formContainer.classList.remove("active"); // Muestra el formulario de inicio de sesión
//         home.classList.add("show");
//         formContainer.classList.remove("active");
//     }

//     // Verificar si hay error al cargar la página
//     if (document.body.classList.contains('show-error') && home) {
//         home.classList.add("show");
//     }

//     // SweetAlert2 para mostrar el registro exitoso
//     if (registroexitoso) {
//         Swal.fire({
//             icon: 'success',
//             title: "¡Registro exitoso!",
//             text: "¡Bienvenido a Luz Clarita!",
//             confirmButtonColor: '#3085d6',
//             confirmButtonText: 'Aceptar'
//         });
//     }


//     // Manejar cierre de sesión
//     // if (urlParams.has('cerrarSesion')) {
//     //     // Limpiar parámetro usando History API
//     //     const cleanUrl = window.location.pathname + window.location.search.replace(/([?&])cerrarSesion=true&?/gi, "");
//     //     window.history.replaceState({}, document.title, cleanUrl);
//     // Manejar cierre de sesión (cuando se redirige DESPUÉS del logout en el backend)
//     // Esto es si tu /logout del backend redirige a una página con ?logout (o ?cerrarSesion como lo tenías)
//     if (urlParams.has('cerrarSesion') || logoutSuccess) { 
//         // Limpiar parámetro de la URL para evitar que se ejecute en cada recarga
//         const cleanUrlParams = new URLSearchParams(window.location.search);
//         cleanUrlParams.delete('cerrarSesion');
//         cleanUrlParams.delete('logout');
//         const newUrl = window.location.pathname + (cleanUrlParams.toString() ? '?' + cleanUrlParams.toString() : '');
//         window.history.replaceState({}, document.title, newUrl);

//         // Llamar a la función de gestión de logout del carrito.js
//         if (window.gestionarLogout) {
//             window.gestionarLogout(); // Esto ya muestra una alerta
//         } else {
//             // Fallback si carrito.js no cargó o la función no está disponible
//             localStorage.removeItem("carrito"); // Limpieza básica
//             console.warn("gestionarLogout no disponible. Carrito local limpiado directamente.");
//             Swal.fire({
//                 icon: 'success',
//                 title: '¡Sesión cerrada!',
//                 text: 'Has cerrado sesión correctamente',
//                 confirmButtonColor: '#3085d6',
//                 confirmButtonText: 'Aceptar'
//             }).then(() => {
//                 //window.location.href = "/"; // Forzar recarga limpia
//                 if (!window.gestionarLogout) window.location.href = "/"; // Recargar solo si no se gestionó por carrito.js
//             });
//         }

//         // SweetAlert2 para mostrar mensajes de error
//         if (error !== null) {
//             Swal.fire({
//                 icon: 'error',
//                 //title: 'Error : Usuario o Contraseña incorrectos',
//                 title: decodeURIComponent(error) || 'Error: Usuario o Contraseña incorrectos',
//                 confirmButtonText: 'Aceptar',
//             }).then((result) => {
//                 if (result.isConfirmed) {
//                     //window.location.href = "/inicioSesion/login?login"; // Redirigir a la página de inicio de sesión después de aceptar
//                     const cleanUrlParams = new URLSearchParams(window.location.search);
//                     cleanUrlParams.delete('error');
//                     const newUrl = window.location.pathname + (cleanUrlParams.toString() ? '?' + cleanUrlParams.toString() : '');
//                     window.history.replaceState({}, document.title, newUrl);
//                 }
//                 // }
//                 // );
//             });
//         }

//         // SweetAlert2 para mostrar mensaje de éxito
//         if (success) {
//             Swal.fire({
//                 icon: 'success',
//                 title: "¡¡Sesión Iniciada Correctamente!!",
//                 confirmButtonColor: '#3085d6',
//                 //     confirmButtonText: 'Aceptar'
//                 // }).then((result) => {
//                 //     if (result.isConfirmed) {
//                 //         window.location.href = "/home"; // Redirigir al home después de aceptar
//                 //     }
//                 // });
//                 confirmButtonText: 'Aceptar',
//                 timer: 2000, // Cierre automático opcional
//                 timerProgressBar: true,
//                 didOpen: () => { // Usar didOpen para asegurar que se ejecuta después de que el Swal está en el DOM
//                     // Limpiar el parámetro 'success' de la URL inmediatamente
//                     const cleanUrlParams = new URLSearchParams(window.location.search);
//                     cleanUrlParams.delete('success');
//                     const newUrl = window.location.pathname + (cleanUrlParams.toString() ? '?' + cleanUrlParams.toString() : '');
//                     window.history.replaceState({}, document.title, newUrl);
//                 }
//             }).then(async (result) => {
//                 // Llamar a la función de gestión de post-login de carrito.js
//                 if (window.gestionarPostLogin) {
//                     console.log("Llamando a gestionarPostLogin desde login.js");
//                     await window.gestionarPostLogin();
//                 } else {
//                     console.warn("gestionarPostLogin no disponible.");
//                 }


//                 // Abrir formulario manualmente cuando se presiona el botón "Login"

//                 // Redirigir al home después de aceptar o al finalizar el timer
//                 // Esperar un breve momento para que la alerta del carrito (si la hay) se muestre.
//                 setTimeout(() => {
//                     window.location.href = "/home";
//                 }, 500); // Ajustar este tiempo si es necesario
//             });
//         }


//         if (formOpenBtn && home) {
//             formOpenBtn.addEventListener("click", () => home.classList.add("show"));
//         }

//         // Cerrar formulario
//         if (formCloseBtn && home) {
//             formCloseBtn.addEventListener("click", () => {
//                 home.classList.remove("show");
//                 // Limpiar mensaje de error si existe
//                 const errorMessage = document.querySelector(".error-message");
//                 if (errorMessage) {
//                     errorMessage.style.display = 'none';
//                 }
//             });
//         }

//         // Mostrar/ocultar contraseña
//         if (pwShowHide) {
//             pwShowHide.forEach((icon) => {
//                 icon.addEventListener("click", () => {
//                     const getPwInput = icon.parentElement.querySelector("input");
//                     if (getPwInput) {
//                         if (getPwInput.type === "password") {
//                             getPwInput.type = "text";
//                             icon.classList.replace("uil-eye-slash", "uil-eye");
//                         } else {
//                             getPwInput.type = "password";
//                             icon.classList.replace("uil-eye", "uil-eye-slash");
//                         }
//                     }
//                 });
//             });
//         }

//         // Cambiar a formulario de registro
//         if (signupBtn && formContainer) {
//             signupBtn.addEventListener("click", (e) => {
//                 e.preventDefault();
//                 formContainer.classList.add("active");
//                 const url = new URL(window.location);
//                 url.searchParams.set('registro', '');
//                 url.searchParams.delete('login');
//                 history.replaceState(null, '', url.pathname + url.search);
//             });
//         }

//         // Cambiar a formulario de login
//         if (loginBtn && formContainer) {
//             loginBtn.addEventListener("click", (e) => {
//                 e.preventDefault();
//                 formContainer.classList.remove("active");

//                 // Quitar el parámetro 'registro' de la URL si existe
//                 const url = new URL(window.location);
//                 //if (url.searchParams.has('registro')) {
//                 url.searchParams.delete('registro');
//                 url.searchParams.set('login', '');
//                 history.replaceState(null, '', url.pathname + url.search);
//                 //}
//             });
//         }

//         // Validación del teléfono
//         if (telefonoInput) {
//             telefonoInput.addEventListener("input", () => {
//                 telefonoInput.value = telefonoInput.value.replace(/[^0-9]/g, '').slice(0, 10);
//             });

//             telefonoInput.addEventListener("keydown", (e) => {
//                 if (telefonoInput.value.length >= 10 && e.key !== "Backspace" && e.key !== "Delete") {
//                     e.preventDefault();
//                 }
//             });
//         }

//         // // SweetAlert2: Quitar la palabra "true" del mensaje de error
//         // setTimeout(() => {
//         //     let swalContainer = document.getElementById('swal2-html-container');
//         //     if (swalContainer) {
//         //         swalContainer.style.display = 'none';
//         //     }
//         // }, 1);

//     }
// });

// src/main/resources/static/js/login.js
