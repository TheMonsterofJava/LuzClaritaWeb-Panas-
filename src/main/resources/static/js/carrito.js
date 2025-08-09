// --- GLOBAL STATE ---
let IS_USER_AUTHENTICATED = false;
let carrito = [];

// --- INITIALIZATION ---
document.addEventListener("DOMContentLoaded", async () => {
    console.log("DOM cargado. Iniciando script del carrito.");
    crearContenedorDeAlertasSiNoExiste();
    await checkAuthenticationStatus();

    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('sync') === 'true' && IS_USER_AUTHENTICATED) {
        // Clean the URL parameter immediately to prevent re-triggering on refresh.
        window.history.replaceState({}, document.title, window.location.pathname);
        await gestionarSincronizacionPostLogin();
    } else {
        await cargarCarrito();
    }
    
    setupEventListeners();
});

// --- CORE CART LOGIC ---

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
            const response = await authenticatedFetch(`/api/carrito/agregar?productoId=${productoId}&cantidad=1`, { method: 'POST' });
            if (!response.ok) {
                // Si la respuesta no es OK, intentamos leer el cuerpo del error
                const errorData = await response.json();
                throw new Error(errorData.error || `El servidor respondió con error: ${response.status}`);
            }
            await obtenerCarritoDelBackend();
            mostrarAlerta(`"${nombre}" se agregó a tu carrito.`, 'success');
        } catch (error) {
            console.error("Error agregando producto al backend:", error);
            mostrarAlerta(`No se pudo agregar "${nombre}". Razón: ${error.message}`, 'danger');
        }
    } else {
        // Logic for unauthenticated user (LocalStorage)
        const idNumerico = Number(productoId);
        let existe = carrito.find(p => p.id === idNumerico);
        if (existe) {
            existe.cantidad++;
        } else {
            carrito.push({ id: idNumerico, nombre, precio, cantidad: 1, imagen });
        }
        localStorage.setItem("carrito", JSON.stringify(carrito));
        actualizarVisualizacionCarrito();
        mostrarAlerta(`"${nombre}" se agregó. ¡Inicia sesión para guardarlo!`);
    }
}

async function eliminarProducto(productoId) {
    const idNumerico = Number(productoId);
    if (!IS_USER_AUTHENTICATED) {
        carrito = carrito.filter(p => p.id !== idNumerico);
        localStorage.setItem("carrito", JSON.stringify(carrito));
        actualizarVisualizacionCarrito();
    } else {
        try {
            await authenticatedFetch(`/api/carrito/eliminar?productoId=${idNumerico}`, { method: 'DELETE' });
            await obtenerCarritoDelBackend();
        } catch (error) {
            console.error("Error eliminando producto del backend:", error);
        }
    }
}

async function vaciarCarrito() {
    if (!IS_USER_AUTHENTICATED) {
        carrito = [];
        localStorage.removeItem("carrito");
        actualizarVisualizacionCarrito();
    } else {
        try {
            await authenticatedFetch('/api/carrito/vaciar', { method: 'DELETE' });
            await obtenerCarritoDelBackend();
        } catch (error) {
            console.error("Error vaciando carrito en el backend:", error);
        }
    }
}
// --LOGICA DE CANTIDADES -- //
//Incrementar la cantidad de productos que hay en el carrito desde el mismo modal. 
function incrementarCantidad(productoId) {
    const idNumerico = Number(productoId);
    const producto = carrito.find(p => (p.productoId || p.id) === idNumerico);
    if (producto) {
        actualizarCantidad(idNumerico, producto.cantidad + 1);
    }
}

//Decrementar la cantidad de productos que hay en el carrito desde el mismo modal.
function decrementarCantidad(productoId) {
    const idNumerico = Number(productoId);
    const producto = carrito.find(p => (p.productoId || p.id) === idNumerico);
    if (producto && producto.cantidad > 1) {
        actualizarCantidad(idNumerico, producto.cantidad - 1);
    } else if (producto && producto.cantidad === 1) {
        Swal.fire({
            title: '¿Eliminar producto?',
            text: "Vas a quitar este producto de tu carrito.",
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#d33',
            cancelButtonColor: '#3085d6',
            confirmButtonText: 'Sí, eliminar',
            cancelButtonText: 'Cancelar'
        }).then((result) => {
            if (result.isConfirmed) {
                eliminarProducto(idNumerico);
            }
        });
    }
}

async function actualizarCantidad(productoId, cantidad) {
    const idNumerico = Number(productoId);
    if (IS_USER_AUTHENTICATED) {
        try {
            const response = await authenticatedFetch(`/api/carrito/actualizar?productoId=${idNumerico}&cantidad=${cantidad}`, { method: 'POST' });
            if (!response.ok) {
                const errorData = await response.json().catch(() => ({ error: 'Error al actualizar la cantidad.' }));
                throw new Error(errorData.error);
            }
            await obtenerCarritoDelBackend(); 
        } catch (error) {
            console.error("Error actualizando cantidad en el backend:", error);
            mostrarAlerta(`No se pudo actualizar la cantidad. Razón: ${error.message}`, 'danger');
        }
    } else {
        const producto = carrito.find(p => p.id === idNumerico);
        if (producto) {
            producto.cantidad = cantidad;
            localStorage.setItem("carrito", JSON.stringify(carrito));

            //Actualizar solo la fila especifica en lugar de todo el carrito
            const filaProducto = document.querySelector(`tr[data-product-id="${idNumerico}"]`);
            if (filaProducto) {
                //Actualizar el input de cantidad
                const inputCantidad = filaProducto.querySelector('input.form-control');
                if (inputCantidad) inputCantidad.value = cantidad;
                
                // Actualizar total del producto
                const totalProducto = filaProducto.querySelector('td:nth-child(5)');
                if (totalProducto) totalProducto.textContent = `$${(producto.precio * cantidad).toFixed(2)}`;
            }
        }
        localStorage.setItem("carrito", JSON.stringify(carrito));
        actualizarVisualizacionCarrito();
    }
}

// --- SYNCHRONIZATION LOGIC ---

async function gestionarSincronizacionPostLogin() {
    console.log("Iniciando flujo de sincronización...");
    const carritoLocal = JSON.parse(localStorage.getItem("carrito") || '[]');

    // Si el carrito esta en 0 , no hay nada que sincronizar y se muestra el Sweet Alert de inicio de sesion.
    if (carritoLocal.length === 0) {
        console.log("No hay carrito local para sincronizar. Mostrando alerta de bienvenida.");
        await cargarCarrito(); // Cargar el carrito del backend primero
        Swal.fire({
            title: '¡Sesión Iniciada!',
            text: '¡¡Bienvenido a la web de LuzClarita!!',
            icon: 'success',
            timer: 2000,
            showConfirmButton: false,
            timerProgressBar: true
        });
        return;
    }

    let timerInterval;
    Swal.fire({
        title: '¡Sesión Iniciada!',
        text: 'Sincronizando tu carrito...',
        icon: 'info',
        allowOutsideClick: false,
        showConfirmButton: false,
        timer: 2000, // Tiempo inicial por si algo falla
        timerProgressBar: true,
        willOpen: () => { Swal.showLoading(); }
    }).then((result) => {
        if (result.dismiss === Swal.DismissReason.timer) {
            console.log('Alerta cerrada por el temporizador');
        }
    });

    try {
        await migrarCarritoLocalStorageAlBackend();
        await obtenerCarritoDelBackend(); // Carga el carrito final y fusionado
        
        Swal.close(); // Cierra la alerta de "sincronizando"
        Swal.fire({
            title: '¡Sincronización Completa!',
            text: 'Tus productos se han guardado en tu cuenta.',
            icon: 'success',
            showConfirmButton: true,
            timer: 2500,
            timerProgressBar: true
        });

    } catch (error) {
        console.error("La sincronización falló:", error);
        Swal.fire({
            title: 'Error de Sincronización',
            text: `No pudimos guardar tu carrito en tu cuenta. Tus productos siguen guardados en este navegador. Razón: ${error.message}`,
            icon: 'error',
            confirmButtonText: 'Entendido'
        });
    } finally {
        console.log("Flujo de sincronización finalizado.");
    }
}

async function migrarCarritoLocalStorageAlBackend() {
    const carritoLocal = JSON.parse(localStorage.getItem("carrito") || '[]');
    if (carritoLocal.length === 0) {
        return;
    }

    console.log("Enviando carrito local al backend para sincronizar:", carritoLocal);
    const response = await authenticatedFetch('/api/carrito/sincronizar', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(carritoLocal)
    });

    if (response.ok) {
        console.log("El backend confirmó la sincronización.");
        localStorage.removeItem("carrito"); // Limpiar solo en caso de éxito
    } else {
        // Si el backend devuelve un error, lo propagamos para que sea manejado por la función que llama.
        const errorData = await response.json().catch(() => ({ error: 'Error desconocido del servidor.' }));
        throw new Error(errorData.error || `El servidor respondió con error: ${response.status}`);
    }
}

// --- CHECKOUT LOGIC ---

function manejarProcesoCompra() {
    if (!carrito || carrito.length === 0) {
        mostrarAlerta("Tu carrito está vacío. Agrega productos antes de pagar.", "warning");
        return;
    }

    if (!IS_USER_AUTHENTICATED) {
        Swal.fire({
            title: '¡Un momento!',
            text: "Para continuar con la compra, necesitas iniciar sesión o crear una cuenta.",
            icon: 'info',
            showCancelButton: true,
            confirmButtonColor: '#3085d6',
            cancelButtonColor: '#d33',
            confirmButtonText: 'Iniciar Sesión / Registrarse',
            cancelButtonText: 'Cancelar'
        }).then((result) => {
            if (result.isConfirmed) {
                // Guardamos el carrito en localStorage por si acaso antes de redirigir
                localStorage.setItem("carrito", JSON.stringify(carrito));
                window.location.href = '/inicioSesion/login?registro=true';
            }
        });
    } else {
        // Para usuarios autenticados, preparamos y enviamos el formulario.
        const form = document.getElementById('form-pagar');
        const cartDataInput = document.getElementById('cartData');
        
        if (form && cartDataInput) {
            // Convertimos el carrito a un string JSON para enviarlo en un solo campo.
            cartDataInput.value = JSON.stringify(carrito);
            form.submit();
        } else {
            console.error("No se encontró el formulario de pago (#form-pagar) o el campo de datos (#cartData).");
            mostrarAlerta("Error: No se pudo iniciar el proceso de pago.", "danger");
        }
    }
}

// --- UI/VIEW FUNCTIONS ---

function actualizarVisualizacionCarrito() {
    const tablaCarrito = document.querySelector("#lista-carrito tbody");
    if (!tablaCarrito) return;

    tablaCarrito.innerHTML = "";
    if (!carrito || carrito.length === 0) {
        tablaCarrito.innerHTML = `<tr><td colspan="6" class="text-center">El carrito está vacío.</td></tr>`;
    } else {
        carrito.forEach(item => {
            // Unifica el objeto producto si viene del backend (DTO) o de localStorage
            const p = item.productoId ? {
                id: item.productoId,
                nombre: item.productoNombre,
                precio: item.productoPrecio,
                imagen: item.productoLinkImagen
            } : item;
            
            //Actualizamos el html para que pueda incremenatar la cantidad en el carrito. 
            const fila = document.createElement("tr");
            fila.setAttribute('data-product-id', p.id); // Agregar identificador
            fila.innerHTML = `
                <td><img src="${p.imagen}" alt="${p.nombre}" width="50" onerror="this.onerror=null;this.src='/img/default.jpg';"></td>
                <td>${p.nombre}</td>
                <td>$${p.precio.toFixed(2)}</td>
                <td class="text-center">
                    <div class="input-group input-group-sm" style="width: 100px; margin: auto;">
                        <button class="btn btn-outline-secondary" type="button" onclick="decrementarCantidad(${p.id})">-</button>
                        <input type="text" class="form-control text-center" value="${item.cantidad}" readonly style="background-color: white;">
                        <button class="btn btn-outline-secondary" type="button" onclick="incrementarCantidad(${p.id})">+</button>
                    </div>
                </td>
                <td>$${(p.precio * item.cantidad).toFixed(2)}</td>
                <td class="text-center"><button class="btn btn-danger btn-sm bi bi-trash-fill" onclick="eliminarProducto(${p.id})"></button></td>
            `;
            tablaCarrito.appendChild(fila);
        });
    }
    actualizarTotalesYContador();
}

function actualizarTotalesYContador() {
    const totalCarritoEl = document.getElementById("totalCarrito");
    const cantidadIconoEl = document.getElementById('carrito-cantidad');
    
    const totalGeneral = carrito.reduce((acc, item) => {
        const precio = item.productoPrecio !== undefined ? item.productoPrecio : item.precio;
        return acc + (precio * item.cantidad);
    }, 0);

    const cantidadTotal = carrito.reduce((acc, item) => acc + item.cantidad, 0);

    if (totalCarritoEl) totalCarritoEl.textContent = `Total: $${totalGeneral.toFixed(2)}`;
    if (cantidadIconoEl) cantidadIconoEl.textContent = cantidadTotal;
}

function mostrarAlerta(mensaje, tipo = 'info') {
    const alertaContainer = document.getElementById('alertas-carrito-container');
    if (!alertaContainer) return;

    const alerta = document.createElement('div');
    alerta.className = `alert alert-${tipo} alert-dismissible fade show`;
    alerta.role = 'alert';
    alerta.innerHTML = `${mensaje}<button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>`;
    
    alertaContainer.appendChild(alerta);
    
    setTimeout(() => {
        const bsAlert = bootstrap.Alert.getOrCreateInstance(alerta);
        if (bsAlert) bsAlert.close();
    }, 4000);
}

function crearContenedorDeAlertasSiNoExiste() {
    if (document.getElementById('alertas-carrito-container')) return;
    const container = document.createElement('div');
    container.id = 'alertas-carrito-container';
    container.style.position = 'fixed';
    container.style.top = '80px';
    container.style.right = '20px';
    container.style.zIndex = '2000';
    document.body.appendChild(container);
}

function setupEventListeners() {
    document.querySelectorAll(".agregar-carrito").forEach(boton => {
        boton.addEventListener("click", (event) => {
            event.preventDefault();
            const productoId = boton.getAttribute("data-id");
            const nombre = boton.getAttribute("data-nombre");
            const precio = parseFloat(boton.getAttribute("data-precio"));
            const imagen = boton.getAttribute("data-imagen");
            agregarAlCarrito(productoId, nombre, precio, imagen);
        });
    });

    const vaciarCarritoBtn = document.getElementById("vaciar-carrito");
    if (vaciarCarritoBtn) {
        vaciarCarritoBtn.addEventListener("click", (e) => {
            e.preventDefault();
            vaciarCarrito();
        });
    }

    const pagarBtn = document.getElementById("pagar");
    if (pagarBtn) {
        pagarBtn.addEventListener("click", (e) => {
            e.preventDefault();
            manejarProcesoCompra();
        });
    }
}


// --- UTILITY & NETWORK FUNCTIONS ---

async function checkAuthenticationStatus() {
    try {
        // Usamos 'no-cache' para obtener siempre la información más reciente.
        const response = await fetch('/api/usuario/actual', { cache: 'no-cache' });
        IS_USER_AUTHENTICATED = response.ok;
    } catch (error) {
        IS_USER_AUTHENTICATED = false;
    }
    console.log(`Estado de autenticación verificado: ${IS_USER_AUTHENTICATED}`);
}

async function obtenerCarritoDelBackend() {
    try {
        const response = await authenticatedFetch('/api/carrito');
        if (!response.ok) {
            // Si el backend devuelve 401 (no autorizado), la sesión pudo haber expirado.
            if (response.status === 401) IS_USER_AUTHENTICATED = false;
            throw new Error(`Error del servidor: ${response.status}`);
        }
        const data = await response.json();
        // El DTO anida los detalles del producto, necesitamos mapearlos.
        carrito = data.map(item => ({
            cantidad: item.cantidad,
            productoId: item.productoId,
            productoNombre: item.productoNombre,
            productoPrecio: item.productoPrecio,
            productoLinkImagen: item.productoLinkImagen
        }));
        actualizarVisualizacionCarrito();
    } catch (error) {
        console.error("Error obteniendo carrito del backend:", error);
        // Si hay un error, asumimos que el usuario no está (o ya no está) logueado.
        IS_USER_AUTHENTICATED = false;
        carrito = [];
        actualizarVisualizacionCarrito();
    }
}

async function authenticatedFetch(url, options = {}) {
    // Obtener el token y el nombre del header desde las etiquetas meta
    const token = document.querySelector("meta[name='_csrf']").getAttribute("content");
    const headerName = document.querySelector("meta[name='_csrf_header']").getAttribute("content");

    const newOptions = {
        ...options,
        headers: {
            ...options.headers
        },
        // 'include' es crucial para que el navegador envíe cookies (como el JSESSIONID)
        credentials: 'include' 
    };

    // Adjuntar el token CSRF solo si existe y el método no es 'GET'.
    if (token && headerName && options.method && options.method.toUpperCase() !== 'GET') {
        newOptions.headers[headerName] = token;
    }

    return fetch(url, newOptions);
}
