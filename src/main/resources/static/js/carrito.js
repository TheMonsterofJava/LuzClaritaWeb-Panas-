// --- ESTADO GLOBAL ---
let IS_USER_AUTHENTICATED = false;
let carrito = [];

// --- PUNTO DE ENTRADA PRINCIPAL ---
document.addEventListener("DOMContentLoaded", async () => {
    console.log("DOM cargado. Iniciando script del carrito.");
    crearContenedorDeAlertasSiNoExiste();
    await checkAuthenticationStatus();

    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('sync') === 'true' && IS_USER_AUTHENTICATED) {
        window.history.replaceState({}, document.title, window.location.pathname);
        await gestionarSincronizacionPostLogin();
    } else {
        await cargarCarrito();
    }
    
    setupEventListeners();
});

// --- LÓGICA DE DATOS Y SERVICIOS ---

/**
 * Obtiene el carrito del backend y actualiza la vista.
 * ES VITAL QUE ESTA FUNCIÓN ESTÉ DEFINIDA ANTES DE SER LLAMADA.
 */
async function obtenerCarritoDelBackend() {
    try {
        const response = await fetch('/api/carrito');
        if (!response.ok) throw new Error(`Error del servidor: ${response.status}`);
        
        const data = await response.json();
        carrito = data;
        actualizarVisualizacionCarrito();
    } catch (error) {
        console.error("Error obteniendo carrito del backend:", error);
        IS_USER_AUTHENTICATED = false;
        carrito = [];
        actualizarVisualizacionCarrito();
    }
}

async function migrarCarritoLocalStorageAlBackend() {
    const carritoLocal = JSON.parse(localStorage.getItem("carrito") || '[]');
    if (carritoLocal.length === 0) {
        console.log("No hay carrito local para migrar.");
        return;
    }
    console.log("Enviando carrito local al backend para sincronizar:", carritoLocal);
    try {
        const response = await authenticatedFetch('/api/carrito/sincronizar', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(carritoLocal)
        });
        if (response.ok) {
            console.log("El backend confirmó la sincronización.");
            localStorage.removeItem("carrito");
        } else {
            throw new Error(`El servidor respondió con error: ${response.status}`);
        }
    } catch (error) {
        console.error("Falló la migración del carrito:", error);
        mostrarAlerta("Hubo un problema al sincronizar tu carrito. Por favor, recarga la página.");
    }
}

// --- FLUJO PRINCIPAL DE LA APLICACIÓN ---

async function gestionarSincronizacionPostLogin() {
    console.log("Iniciando flujo de sincronización controlado...");
    const swalPromise = Swal.fire({
        title: '¡Sesión Iniciada!',
        text: 'Sincronizando tu carrito...',
        icon: 'info',
        allowOutsideClick: false,
        showConfirmButton: false,
        willOpen: () => { Swal.showLoading(); }
    });

    await migrarCarritoLocalStorageAlBackend();
    await obtenerCarritoDelBackend(); // AHORA ESTA FUNCIÓN ESTÁ DEFINIDA

    Swal.update({
        title: '¡Sincronización Completa!',
        text: 'Tus productos se han guardado en tu cuenta.',
        icon: 'success',
        showConfirmButton: false,
        timer: 2000
    });
    
    console.log("Flujo de sincronización controlado finalizado.");
}

async function cargarCarrito() {
    if (IS_USER_AUTHENTICATED) {
        await obtenerCarritoDelBackend();
    } else {
        const carritoLocal = localStorage.getItem("carrito");
        carrito = carritoLocal ? JSON.parse(carritoLocal) : [];
        console.log("Usuario no autenticado, usando carrito local.");
        actualizarVisualizacionCarrito();
    }
}

async function agregarAlCarrito(productoId, nombre, precio, imagen) {
    if (IS_USER_AUTHENTICATED) {
        try {
            await authenticatedFetch(`/api/carrito/agregar?productoId=${productoId}&cantidad=1`, { method: 'POST' });
            await obtenerCarritoDelBackend();
            mostrarAlerta(`"${nombre}" se agregó a tu carrito.`);
        } catch (error) {
            console.error("Error agregando producto al backend:", error);
            mostrarAlerta(`Error al agregar "${nombre}".`);
        }
    } else {
        const idNumerico = Number(productoId);
        let existe = carrito.find(p => p.id === idNumerico);
        if (existe) existe.cantidad++;
        else carrito.push({ id: idNumerico, nombre, precio, cantidad: 1, imagen });
        localStorage.setItem("carrito", JSON.stringify(carrito));
        actualizarVisualizacionCarrito();
        mostrarAlerta(`"${nombre}" se agregó. ¡Inicia sesión o regístrate para guardarlo!`);
    }
}

function manejarProcesoCompra() {
    if (!carrito || carrito.length === 0) {
        mostrarAlerta("Tu carrito está vacío. Agrega productos antes de pagar.");
        return;
    }
    if (!IS_USER_AUTHENTICATED) {
        Swal.fire({
            icon: 'info',
            title: 'Regístrate para continuar',
            text: 'Para proceder con la compra, necesitas una cuenta. Tu carrito se guardará.',
            confirmButtonText: 'Registrarme',
            showCancelButton: true,
            cancelButtonText: 'Cancelar',
            allowOutsideClick: false
        }).then((result) => {
            if (result.isConfirmed) {
                localStorage.setItem("carrito", JSON.stringify(carrito));
                window.location.href = '/inicioSesion/login?registro&fromCheckout=true';
            }
        });
    } else {
        procederAlPago();
    }
}

function procederAlPago() {
    if (typeof submitForm === "function") submitForm();
    else console.error("La función submitForm() no está definida.");
}

async function eliminarProducto(productoId) {
    if (!IS_USER_AUTHENTICATED) {
        carrito = carrito.filter(p => p.id !== productoId);
        localStorage.setItem("carrito", JSON.stringify(carrito));
        actualizarVisualizacionCarrito();
    } else {
        try {
            await authenticatedFetch(`/api/carrito/eliminar?productoId=${productoId}`, { method: 'DELETE' });
            await obtenerCarritoDelBackend();
        } catch (error) {
            console.error("Error eliminando producto del backend:", error);
        }
    }
}

// --- FUNCIONES DE RED Y UI ---

async function checkAuthenticationStatus() {
    try {
        const response = await fetch('/api/usuario/actual');
        IS_USER_AUTHENTICATED = response.ok;
    } catch (error) {
        IS_USER_AUTHENTICATED = false;
    }
    console.log(`Estado de autenticación verificado: ${IS_USER_AUTHENTICATED}`);
}

async function authenticatedFetch(url, options = {}) {
    function getCookie(name) {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) return parts.pop().split(';').shift();
    }
    const token = getCookie('XSRF-TOKEN');
    const newOptions = { ...options, headers: { ...options.headers }, credentials: 'include' };
    if (token && options.method && options.method !== 'GET') {
        newOptions.headers['X-XSRF-TOKEN'] = token;
    }
    return fetch(url, newOptions);
}

function actualizarVisualizacionCarrito() {
    const tablaCarrito = document.querySelector("#lista-carrito tbody");
    if (!tablaCarrito) return;
    tablaCarrito.innerHTML = "";
    if (carrito.length === 0) {
        tablaCarrito.innerHTML = `<tr><td colspan="6" class="text-center">El carrito está vacío.</td></tr>`;
    } else {
        carrito.forEach(item => {
            const p = item.producto ? {
                id: item.producto.productoId,
                nombre: item.producto.productoNombre,
                precio: item.producto.productoPrecio,
                imagen: item.producto.productoLinkImagen
            } : item;
            
            const fila = document.createElement("tr");
            fila.innerHTML = `
                <td><img src="/img/${p.imagen || 'default.jpg'}" alt="${p.nombre}" width="50"></td>
                <td>${p.nombre}</td>
                <td>$${p.precio.toFixed(2)}</td>
                <td>${item.cantidad}</td>
                <td>$${(p.precio * item.cantidad).toFixed(2)}</td>
                <td><button class="btn btn-danger btn-sm bi bi-trash-fill" onclick="eliminarProducto(${p.id})"></button></td>
            `;
            tablaCarrito.appendChild(fila);
        });
    }
    actualizarTotalesYContador();
}

function actualizarTotalesYContador() {
    const totalCarritoEl = document.getElementById("totalCarrito");
    const cantidadIconoEl = document.getElementById('carrito-cantidad');
    const totalGeneral = carrito.reduce((acc, item) => acc + (item.producto ? item.producto.productoPrecio : item.precio) * item.cantidad, 0);
    const cantidadTotal = carrito.reduce((acc, item) => acc + item.cantidad, 0);
    if (totalCarritoEl) totalCarritoEl.textContent = `Total: $${totalGeneral.toFixed(2)}`;
    if (cantidadIconoEl) cantidadIconoEl.textContent = cantidadTotal;
}

function mostrarAlerta(mensaje) {
    const alertaContainer = document.getElementById('alertas-carrito-container');
    if (!alertaContainer) return;
    const alerta = document.createElement('div');
    alerta.className = 'alert alert-info alert-dismissible fade show';
    alerta.role = 'alert';
    alerta.innerHTML = `${mensaje}<button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>`;
    alertaContainer.appendChild(alerta);
    setTimeout(() => {
        const bsAlert = bootstrap.Alert.getOrCreateInstance(alerta);
        if (bsAlert) bsAlert.close();
    }, 3000);
}

function crearContenedorDeAlertasSiNoExiste() {
    if (!document.getElementById('alertas-carrito-container')) {
        const container = document.createElement('div');
        container.id = 'alertas-carrito-container';
        container.style.position = 'fixed';
        container.style.top = '80px';
        container.style.right = '20px';
        container.style.zIndex = '2000';
        document.body.appendChild(container);
    }
}

function setupEventListeners() {
    document.querySelectorAll(".agregar-carrito").forEach(boton => {
        boton.addEventListener("click", (event) => {
            event.preventDefault();
            const productoId = boton.getAttribute("data-id");
            const nombre = boton.getAttribute("data-nombre");
            const precio = parseFloat(boton.getAttribute("data-precio"));
            const imagen = boton.getAttribute("data-imagen") || "default.jpg";
            agregarAlCarrito(productoId, nombre, precio, imagen);
        });
    });

    const vaciarCarritoBtn = document.getElementById("vaciar-carrito");
    if (vaciarCarritoBtn) vaciarCarritoBtn.addEventListener("click", async () => {
        if (!IS_USER_AUTHENTICATED) {
            carrito = [];
            localStorage.removeItem("carrito");
            actualizarVisualizacionCarrito();
        } else {
            await authenticatedFetch('/api/carrito/vaciar', { method: 'DELETE' });
            await obtenerCarritoDelBackend();
        }
    });

    const pagarBtn = document.getElementById("pagar");
    if (pagarBtn) {
        pagarBtn.addEventListener("click", (event) => {
            event.preventDefault();
            manejarProcesoCompra();
        });
    }
}

// // --- CONFIGURACIÓN Y ESTADO GLOBAL ---

// // Variable global para el estado de autenticación.
// let IS_USER_AUTHENTICATED = false;
// // Variable global para el carrito de compras.
// let carrito = [];

// // --- FUNCIONES DE INICIALIZACIÓN Y AUTENTICACIÓN ---

// /**
//  * Event listener que se ejecuta cuando el DOM está completamente cargado.
//  * Es el punto de entrada principal del script.
//  */
// document.addEventListener("DOMContentLoaded", async () => {
//     console.log("DOM cargado. Iniciando script del carrito.");
    
//     // Asegurarse de que el contenedor de alertas exista en el layout.
//     crearContenedorDeAlertasSiNoExiste();
    
//     // Verificar si el usuario está autenticado.
//     await checkAuthenticationStatus();
    
//     // Comprobar si la URL indica que se debe sincronizar el carrito (después de login/registro).
//     const urlParams = new URLSearchParams(window.location.search);
//     const needsSync = urlParams.get('sync') === 'true';

//     if (needsSync && IS_USER_AUTHENTICATED) {
//         console.log("Parámetro 'sync=true' detectado y usuario autenticado. Procediendo a sincronizar.");
        
//         // Mostrar alerta de bienvenida y sincronización.
//         const swalPromise = Swal.fire({
//             icon: 'success',
//             title: '¡Sesión Iniciada!',
//             text: 'Sincronizando tu carrito...',
//             timer: 10000, // 10 segundos de duración
//             timerProgressBar: true,
//             allowOutsideClick: false,
//             showConfirmButton: false
//         });

//         await migrarCarritoLocalStorageAlBackend();
        
//         // Limpiar el parámetro de la URL para evitar resincronizaciones.
//         window.history.replaceState({}, document.title, window.location.pathname);
        
//         // Esperar a que la alerta de "sincronizando" se cierre antes de continuar.
//         await swalPromise;
//     }
    
//     // Cargar el carrito desde el backend (si está autenticado) o localStorage.
//     await cargarCarrito();
    
//     // Asignar los event listeners a los botones.
//     setupEventListeners();
// });

// /**
//  * Verifica el estado de autenticación del usuario haciendo una llamada a un endpoint del backend.
//  */
// async function checkAuthenticationStatus() {
//     try {
//         const response = await fetch('/api/usuario/actual');
//         IS_USER_AUTHENTICATED = response.ok;
//         console.log(`Estado de autenticación: ${IS_USER_AUTHENTICATED}`);
//     } catch (error) {
//         IS_USER_AUTHENTICATED = false;
//         console.warn('Error verificando autenticación, asumiendo no autenticado.', error);
//     }
// }

// /**
//  * Asigna los event listeners a los botones de la interfaz (agregar, vaciar, pagar).
//  */
// function setupEventListeners() {
//     document.querySelectorAll(".agregar-carrito").forEach(boton => {
//         boton.addEventListener("click", (event) => {
//             event.preventDefault();
//             const productoId = boton.getAttribute("data-id");
//             const nombre = boton.getAttribute("data-nombre");
//             const precio = parseFloat(boton.getAttribute("data-precio"));
//             const imagen = boton.getAttribute("data-imagen") || "default.jpg";
//             agregarAlCarrito(productoId, nombre, precio, imagen);
//         });
//     });

//     const vaciarCarritoBtn = document.getElementById("vaciar-carrito");
//     if (vaciarCarritoBtn) vaciarCarritoBtn.addEventListener("click", vaciarCarrito);

//     const pagarBtn = document.getElementById("pagar");
//     if (pagarBtn) {
//         pagarBtn.addEventListener("click", (event) => {
//             event.preventDefault();
//             manejarProcesoCompra();
//         });
//     }
// }

// // --- LÓGICA PRINCIPAL DEL CARRITO ---

// /**
//  * Carga el contenido del carrito. Si el usuario está autenticado, lo carga desde el backend.
//  * Si no, lo carga desde el localStorage.
//  */
// async function cargarCarrito() {
//     if (IS_USER_AUTHENTICATED) {
//         await obtenerCarritoDelBackend();
//     } else {
//         const carritoLocal = localStorage.getItem("carrito");
//         carrito = carritoLocal ? JSON.parse(carritoLocal) : [];
//         console.log("Usuario no autenticado, usando carrito local.");
//         actualizarVisualizacionCarrito();
//     }
// }

// /**
//  * Agrega un producto al carrito.
//  */
// async function agregarAlCarrito(productoId, nombre, precio, imagen) {
//     if (IS_USER_AUTHENTICATED) {
//         try {
//             const response = await authenticatedFetch(`/api/carrito/agregar?productoId=${productoId}&cantidad=1`, {
//                 method: 'POST'
//             });
//             if (!response.ok) throw new Error(`Error del servidor: ${response.status}`);
            
//             await obtenerCarritoDelBackend();
//             mostrarAlerta(`"${nombre}" se agregó a tu carrito.`);
//         } catch (error) {
//             console.error("Error agregando producto al backend:", error);
//             mostrarAlerta(`Error al agregar "${nombre}".`);
//         }
//     } else {
//         const idNumerico = Number(productoId);
//         let existe = carrito.find(p => p.id === idNumerico);
//         if (existe) {
//             existe.cantidad++;
//         } else {
//             carrito.push({ id: idNumerico, nombre, precio, cantidad: 1, imagen });
//         }
//         localStorage.setItem("carrito", JSON.stringify(carrito));
//         actualizarVisualizacionCarrito();
//         mostrarAlerta(`"${nombre}" se agregó. ¡Inicia sesión o regístrate para guardarlo!`);
//     }
// }

// /**
//  * Elimina todos los productos del carrito.
//  */
// async function vaciarCarrito() {
//     if (IS_USER_AUTHENTICATED) {
//         try {
//             await authenticatedFetch('/api/carrito/vaciar', { method: 'DELETE' });
//             await obtenerCarritoDelBackend();
//             mostrarAlerta("Tu carrito ha sido vaciado.");
//         } catch (error) {
//             console.error("Error vaciando carrito en backend:", error);
//         }
//     } else {
//         carrito = [];
//         localStorage.removeItem("carrito");
//         actualizarVisualizacionCarrito();
//         mostrarAlerta("Carrito local vaciado.");
//     }
// }

// // --- PROCESO DE PAGO Y SINCRONIZACIÓN ---

// /**
//  * Gestiona el clic en el botón "Pagar".
//  */
// function manejarProcesoCompra() {
//     if (!carrito || carrito.length === 0) {
//         mostrarAlerta("Tu carrito está vacío. Agrega productos antes de pagar.");
//         return;
//     }

//     if (!IS_USER_AUTHENTICATED) {
//         Swal.fire({
//             icon: 'info',
//             title: 'Regístrate para continuar',
//             text: 'Para proceder con la compra, necesitas una cuenta. Tu carrito se guardará.',
//             confirmButtonText: 'Registrarme',
//             showCancelButton: true,
//             cancelButtonText: 'Cancelar',
//             allowOutsideClick: false
//         }).then((result) => {
//             if (result.isConfirmed) {
//                 localStorage.setItem("carrito", JSON.stringify(carrito));
//                 window.location.href = '/inicioSesion/login?registro&fromCheckout=true';
//             }
//         });
//     } else {
//         procederAlPago();
//     }
// }

// /**
//  * Prepara y envía el formulario para el checkout.
//  */
// function procederAlPago() {
//     if (typeof submitForm === "function") {
//         submitForm();
//     } else {
//         console.error("La función submitForm() no está definida. Revisa layout.html.");
//         mostrarAlerta("Error al procesar el pago. Por favor, contacta a soporte.");
//     }
// }

// /**
//  * Migra los productos del localStorage al carrito del backend.
//  */
// async function migrarCarritoLocalStorageAlBackend() {
//     const carritoLocal = JSON.parse(localStorage.getItem("carrito") || '[]');
//     if (carritoLocal.length === 0) {
//         console.log("No hay carrito local para migrar.");
//         return;
//     }

//     console.log("Migrando carrito de localStorage al backend:", carritoLocal);
//     try {
//         const response = await authenticatedFetch('/api/carrito/sincronizar', {
//             method: 'POST',
//             headers: { 'Content-Type': 'application/json' },
//             body: JSON.stringify(carritoLocal)
//         });

//         if (response.ok) {
//             console.log("Migración completada exitosamente.");
//             localStorage.removeItem("carrito");
//         } else {
//             throw new Error(`Error en la sincronización: ${response.status}`);
//         }
//     } catch (error) {
//         console.error("Error durante la migración del carrito:", error);
//         mostrarAlerta("Hubo un problema al sincronizar tu carrito local.");
//     }
// }

// // --- FUNCIONES AUXILIARES Y DE VISUALIZACIÓN ---

// /**
//  * Obtiene el carrito del backend y actualiza la vista.
//  */
// async function obtenerCarritoDelBackend() {
//     try {
//         const response = await fetch('/api/carrito');
//         if (!response.ok) throw new Error(`Error del servidor: ${response.status}`);
        
//         const data = await response.json();
//         carrito = data;
//         actualizarVisualizacionCarrito();
//     } catch (error) {
//         console.error("Error obteniendo carrito del backend:", error);
//         // Si falla (ej. sesión expirada), limpiar el carrito local.
//         IS_USER_AUTHENTICATED = false;
//         carrito = [];
//         actualizarVisualizacionCarrito();
//     }
// }

// /**
//  * Actualiza la tabla del carrito en el HTML.
//  */
// function actualizarVisualizacionCarrito() {
//     const tablaCarrito = document.querySelector("#lista-carrito tbody");
//     if (!tablaCarrito) return;

//     tablaCarrito.innerHTML = "";
//     if (carrito.length === 0) {
//         tablaCarrito.innerHTML = `<tr><td colspan="6" class="text-center">El carrito está vacío.</td></tr>`;
//     } else {
//         carrito.forEach(item => {
//             const producto = item.producto || item; // Compatible con DTO y objeto local
//             const fila = document.createElement("tr");
//             fila.innerHTML = `
//                 <td><img src="/img/${producto.linkImagen || producto.imagen}" alt="${producto.nombre}" width="50"></td>
//                 <td>${producto.productoNombre || producto.nombre}</td>
//                 <td>$${(producto.productoPrecio || producto.precio).toFixed(2)}</td>
//                 <td>${item.cantidad}</td>
//                 <td>$${((producto.productoPrecio || producto.precio) * item.cantidad).toFixed(2)}</td>
//                 <td><button class="btn btn-danger btn-sm bi bi-trash-fill" onclick="eliminarProducto(${producto.productoId || producto.id})"></button></td>
//             `;
//             tablaCarrito.appendChild(fila);
//         });
//     }
//     actualizarTotalesYContador();
// }

// /**
//  * Actualiza el contador del ícono del carrito y el total monetario.
//  */
// function actualizarTotalesYContador() {
//     const totalCarritoEl = document.getElementById("totalCarrito");
//     const cantidadIconoEl = document.getElementById('carrito-cantidad');
    
//     const totalGeneral = carrito.reduce((acc, item) => {
//         const precio = item.producto ? item.producto.productoPrecio : item.precio;
//         return acc + (precio * item.cantidad);
//     }, 0);

//     const cantidadTotal = carrito.reduce((acc, item) => acc + item.cantidad, 0);

//     if (totalCarritoEl) totalCarritoEl.textContent = `Total: $${totalGeneral.toFixed(2)}`;
//     if (cantidadIconoEl) cantidadIconoEl.textContent = cantidadTotal;
// }

// /**
//  * Muestra una alerta Bootstrap temporal.
//  */
// function mostrarAlerta(mensaje) {
//     const alertaContainer = document.getElementById('alertas-carrito-container');
//     if (!alertaContainer) {
//         console.error("El contenedor de alertas no se encontró.");
//         return;
//     }
    
//     const alerta = document.createElement('div');
//     alerta.className = 'alert alert-success alert-dismissible fade show';
//     alerta.role = 'alert';
//     alerta.innerHTML = `${mensaje}<button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>`;
    
//     alertaContainer.appendChild(alerta);
    
//     setTimeout(() => {
//         const bsAlert = new bootstrap.Alert(alerta);
//         bsAlert.close();
//     }, 3000);
// }

// /**
//  * Crea el div contenedor de alertas si no existe en el DOM.
//  */
// function crearContenedorDeAlertasSiNoExiste() {
//     if (!document.getElementById('alertas-carrito-container')) {
//         const container = document.createElement('div');
//         container.id = 'alertas-carrito-container';
//         container.style.position = 'fixed';
//         container.style.top = '80px';
//         container.style.right = '20px';
//         container.style.zIndex = '2000';
//         document.body.appendChild(container);
//     }
// }

// /**
//  * Wrapper para la API fetch que incluye automáticamente el token CSRF en las cabeceras.
//  */
// async function authenticatedFetch(url, options = {}) {
//     // Helper function to get a cookie by name
//     function getCookie(name) {
//         const value = `; ${document.cookie}`;
//         const parts = value.split(`; ${name}=`);
//         if (parts.length === 2) return parts.pop().split(';').shift();
//     }

//     const token = getCookie('XSRF-TOKEN');

//     if (!token) {
//         console.error("Cookie XSRF-TOKEN no encontrada. Las peticiones seguras fallarán.");
//         // No lanzar un error aquí, ya que podría ser una petición GET pública.
//         // El servidor se encargará de rechazar si es necesario.
//     }

//     const newOptions = {
//         ...options,
//         headers: {
//             ...options.headers
//         },
//         credentials: 'include' // Forzar el envío de cookies de sesión.
//     };

//     // Adjuntar el token solo si existe y es una petición que lo necesita (no GET, etc.)
//     if (token && options.method && options.method !== 'GET' && options.method !== 'HEAD') {
//         newOptions.headers['X-XSRF-TOKEN'] = token;
//     }

//     return fetch(url, newOptions);
// }


//CODIGO QUE ME DIO LA IA:
// // --- ESTADO GLOBAL ---
// let IS_USER_AUTHENTICATED = false;
// let carrito = [];

// // --- PUNTO DE ENTRADA PRINCIPAL ---
// document.addEventListener("DOMContentLoaded", async () => {
//     console.log("DOM cargado. Iniciando script del carrito.");
//     crearContenedorDeAlertasSiNoExiste();
//     await checkAuthenticationStatus();

//     const urlParams = new URLSearchParams(window.location.search);
//     if (urlParams.get('sync') === 'true' && IS_USER_AUTHENTICATED) {
//         // Limpiar URL para evitar re-sincronización en recargas.
//         window.history.replaceState({}, document.title, window.location.pathname);
//         // Iniciar el flujo de sincronización controlado.
//         await gestionarSincronizacionPostLogin();
//     } else {
//         // Carga normal del carrito si no hay que sincronizar.
//         await cargarCarrito();
//     }
    
//     setupEventListeners();
// });

// // --- FLUJO CONTROLADO DE SINCRONIZACIÓN ---
// async function gestionarSincronizacionPostLogin() {
//     console.log("Iniciando flujo de sincronización controlado...");

//     // 1. Mostrar alerta de "Sincronizando..." que no se cierra.
//     const swalPromise = Swal.fire({
//         title: '¡Sesión Iniciada!',
//         text: 'Sincronizando tu carrito...',
//         icon: 'info',
//         allowOutsideClick: false,
//         showConfirmButton: false,
//         willOpen: () => {
//             Swal.showLoading();
//         }
//     });

//     // 2. Migrar el carrito del localStorage al backend y ESPERAR a que termine.
//     await migrarCarritoLocalStorageAlBackend();

//     // 3. Cargar el carrito fresco desde el backend y ESPERAR a que termine.
//     // Esto también actualizará la interfaz gráfica con el resultado final.
//     await obtenerCarritoDelBackend();

//     // 4. Mostrar mensaje de éxito y cerrar la alerta.
//     Swal.update({
//         title: '¡Sincronización Completa!',
//         text: 'Tus productos se han guardado en tu cuenta.',
//         icon: 'success',
//         showConfirmButton: false,
//         timer: 2000 // Se cierra sola después de 2 segundos.
//     });
    
//     console.log("Flujo de sincronización controlado finalizado.");
// }

// // --- LÓGICA DEL CARRITO ---
// async function cargarCarrito() {
//     if (IS_USER_AUTHENTICATED) {
//         await obtenerCarritoDelBackend();
//     } else {
//         const carritoLocal = localStorage.getItem("carrito");
//         carrito = carritoLocal ? JSON.parse(carritoLocal) : [];
//         console.log("Usuario no autenticado, usando carrito local.");
//         actualizarVisualizacionCarrito();
//     }
// }

// async function agregarAlCarrito(productoId, nombre, precio, imagen) {
//     if (IS_USER_AUTHENTICATED) {
//         try {
//             await authenticatedFetch(`/api/carrito/agregar?productoId=${productoId}&cantidad=1`, { method: 'POST' });
//             await obtenerCarritoDelBackend();
//             mostrarAlerta(`"${nombre}" se agregó a tu carrito.`);
//         } catch (error) {
//             console.error("Error agregando producto al backend:", error);
//             mostrarAlerta(`Error al agregar "${nombre}".`);
//         }
//     } else {
//         const idNumerico = Number(productoId);
//         let existe = carrito.find(p => p.id === idNumerico);
//         if (existe) existe.cantidad++;
//         else carrito.push({ id: idNumerico, nombre, precio, cantidad: 1, imagen });
//         localStorage.setItem("carrito", JSON.stringify(carrito));
//         actualizarVisualizacionCarrito();
//         mostrarAlerta(`"${nombre}" se agregó. ¡Inicia sesión o regístrate para guardarlo!`);
//     }
// }

// // --- LÓGICA DE PAGO Y MIGRACIÓN ---
// function manejarProcesoCompra() {
//     if (!carrito || carrito.length === 0) {
//         mostrarAlerta("Tu carrito está vacío. Agrega productos antes de pagar.");
//         return;
//     }
//     if (!IS_USER_AUTHENTICATED) {
//         Swal.fire({
//             icon: 'info',
//             title: 'Regístrate para continuar',
//             text: 'Para proceder con la compra, necesitas una cuenta. Tu carrito se guardará.',
//             confirmButtonText: 'Registrarme',
//             showCancelButton: true,
//             cancelButtonText: 'Cancelar',
//             allowOutsideClick: false
//         }).then((result) => {
//             if (result.isConfirmed) {
//                 localStorage.setItem("carrito", JSON.stringify(carrito));
//                 window.location.href = '/inicioSesion/login?registro&fromCheckout=true';
//             }
//         });
//     } else {
//         procederAlPago();
//     }
// }

// function procederAlPago() {
//     if (typeof submitForm === "function") submitForm();
//     else console.error("La función submitForm() no está definida.");
// }

// async function migrarCarritoLocalStorageAlBackend() {
//     const carritoLocal = JSON.parse(localStorage.getItem("carrito") || '[]');
//     if (carritoLocal.length === 0) {
//         console.log("No hay carrito local para migrar.");
//         return;
//     }
//     console.log("Enviando carrito local al backend para sincronizar:", carritoLocal);
//     try {
//         const response = await authenticatedFetch('/api/carrito/sincronizar', {
//             method: 'POST',
//             headers: { 'Content-Type': 'application/json' },
//             body: JSON.stringify(carritoLocal)
//         });
//         if (response.ok) {
//             console.log("El backend confirmó la sincronización.");
//             localStorage.removeItem("carrito");
//         } else {
//             throw new Error(`El servidor respondió con error: ${response.status}`);
//         }
//     } catch (error) {
//         console.error("Falló la migración del carrito:", error);
//         mostrarAlerta("Hubo un problema al sincronizar tu carrito. Por favor, recarga la página.");
//     }
// }

// // --- FUNCIONES DE RED Y AUTENTICACIÓN ---
// async function checkAuthenticationStatus() {
//     try {
//         const response = await fetch('/api/usuario/actual');
//         IS_USER_AUTHENTICATED = response.ok;
//     } catch (error) {
//         IS_USER_AUTHENTICATED = false;
//     }
//     console.log(`Estado de autenticación verificado: ${IS_USER_AUTHENTICATED}`);
// }

// async function authenticatedFetch(url, options = {}) {
//     function getCookie(name) {
//         const value = `; ${document.cookie}`;
//         const parts = value.split(`; ${name}=`);
//         if (parts.length === 2) return parts.pop().split(';').shift();
//     }
//     const token = getCookie('XSRF-TOKEN');
//     const newOptions = { ...options, headers: { ...options.headers }, credentials: 'include' };
//     if (token && options.method && options.method !== 'GET') {
//         newOptions.headers['X-XSRF-TOKEN'] = token;
//     }
//     return fetch(url, newOptions);
// }

// // --- FUNCIONES DE UI (VISTA) ---
// function actualizarVisualizacionCarrito() {
//     const tablaCarrito = document.querySelector("#lista-carrito tbody");
//     if (!tablaCarrito) return;
//     tablaCarrito.innerHTML = "";
//     if (carrito.length === 0) {
//         tablaCarrito.innerHTML = `<tr><td colspan="6" class="text-center">El carrito está vacío.</td></tr>`;
//     } else {
//         carrito.forEach(item => {
//             // Unifica el acceso a las propiedades del producto (local vs. DTO del backend)
//             const p = item.producto ? {
//                 id: item.producto.productoId,
//                 nombre: item.producto.productoNombre,
//                 precio: item.producto.productoPrecio,
//                 imagen: item.producto.productoLinkImagen
//             } : item;
            
//             const fila = document.createElement("tr");
//             fila.innerHTML = `
//                 <td><img src="/img/${p.imagen || 'default.jpg'}" alt="${p.nombre}" width="50"></td>
//                 <td>${p.nombre}</td>
//                 <td>$${p.precio.toFixed(2)}</td>
//                 <td>${item.cantidad}</td>
//                 <td>$${(p.precio * item.cantidad).toFixed(2)}</td>
//                 <td><button class="btn btn-danger btn-sm bi bi-trash-fill" onclick="eliminarProducto(${p.id})"></button></td>
//             `;
//             tablaCarrito.appendChild(fila);
//         });
//     }
//     actualizarTotalesYContador();
// }

// function actualizarTotalesYContador() {
//     const totalCarritoEl = document.getElementById("totalCarrito");
//     const cantidadIconoEl = document.getElementById('carrito-cantidad');
//     const totalGeneral = carrito.reduce((acc, item) => acc + (item.producto ? item.producto.productoPrecio : item.precio) * item.cantidad, 0);
//     const cantidadTotal = carrito.reduce((acc, item) => acc + item.cantidad, 0);
//     if (totalCarritoEl) totalCarritoEl.textContent = `Total: $${totalGeneral.toFixed(2)}`;
//     if (cantidadIconoEl) cantidadIconoEl.textContent = cantidadTotal;
// }

// function mostrarAlerta(mensaje) {
//     const alertaContainer = document.getElementById('alertas-carrito-container');
//     if (!alertaContainer) return;
//     const alerta = document.createElement('div');
//     alerta.className = 'alert alert-info alert-dismissible fade show';
//     alerta.role = 'alert';
//     alerta.innerHTML = `${mensaje}<button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>`;
//     alertaContainer.appendChild(alerta);
//     setTimeout(() => {
//         const bsAlert = bootstrap.Alert.getOrCreateInstance(alerta);
//         if (bsAlert) bsAlert.close();
//     }, 3000);
// }

// function crearContenedorDeAlertasSiNoExiste() {
//     if (!document.getElementById('alertas-carrito-container')) {
//         const container = document.createElement('div');
//         container.id = 'alertas-carrito-container';
//         container.style.position = 'fixed';
//         container.style.top = '80px';
//         container.style.right = '20px';
//         container.style.zIndex = '2000';
//         document.body.appendChild(container);
//     }
// }

// // Se necesita la función eliminarProducto para que los botones en el HTML funcionen
// async function eliminarProducto(productoId) {
//     if (!IS_USER_AUTHENTICATED) {
//         // Lógica para localStorage
//         carrito = carrito.filter(p => p.id !== productoId);
//         localStorage.setItem("carrito", JSON.stringify(carrito));
//         actualizarVisualizacionCarrito();
//     } else {
//         // Lógica para backend
//         try {
//             await authenticatedFetch(`/api/carrito/eliminar?productoId=${productoId}`, { method: 'DELETE' });
//             await obtenerCarritoDelBackend();
//         } catch (error) {
//             console.error("Error eliminando producto del backend:", error);
//         }
//     }
// }

// function setupEventListeners() {
//     document.querySelectorAll(".agregar-carrito").forEach(boton => {
//         boton.addEventListener("click", (event) => {
//             event.preventDefault();
//             const productoId = boton.getAttribute("data-id");
//             const nombre = boton.getAttribute("data-nombre");
//             const precio = parseFloat(boton.getAttribute("data-precio"));
//             const imagen = boton.getAttribute("data-imagen") || "default.jpg";
//             agregarAlCarrito(productoId, nombre, precio, imagen);
//         });
//     });

//     const vaciarCarritoBtn = document.getElementById("vaciar-carrito");
//     if (vaciarCarritoBtn) vaciarCarritoBtn.addEventListener("click", async () => {
//         if (!IS_USER_AUTHENTICATED) {
//             carrito = [];
//             localStorage.removeItem("carrito");
//             actualizarVisualizacionCarrito();
//         } else {
//             await authenticatedFetch('/api/carrito/vaciar', { method: 'DELETE' });
//             await obtenerCarritoDelBackend();
//         }
//     });

//     const pagarBtn = document.getElementById("pagar");
//     if (pagarBtn) {
//         pagarBtn.addEventListener("click", (event) => {
//             event.preventDefault();
//             manejarProcesoCompra();
//         });
//     }
// }
