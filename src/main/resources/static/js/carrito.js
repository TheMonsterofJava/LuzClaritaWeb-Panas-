// src/main/resources/static/js/carrito.js

// Variable global para el estado de autenticación (simplificado)
// Idealmente, esto se establecería de forma más robusta (ej. desde el backend al cargar la página)
let IS_USER_AUTHENTICATED = false; // Se actualizará dinámicamente

// Función para verificar el estado de autenticación (marcador de posición)
// Deberías reemplazar esto con tu lógica real para verificar la autenticación.
// Por ejemplo, verificar la existencia de un token, o un endpoint del backend.
async function checkAuthenticationStatus() {
    try {
        const response = await fetch('/api/usuario/actual');
        if (response.status === 200) {
            const data = await response.json();
            IS_USER_AUTHENTICATED = data.autenticado === true;
        } else {
            IS_USER_AUTHENTICATED = false;
        }
    } catch (error) {
        console.warn('Error verificando autenticación:', error);
        IS_USER_AUTHENTICATED = false;
    }
}

document.addEventListener("DOMContentLoaded", async () => {
    // Primero, verifica el estado de autenticación
    await checkAuthenticationStatus(); 

    // Luego carga el carrito basado en el estado
    await cargarCarrito(); // Cambiado a cargarCarrito

    // Event listeners existentes
    document.querySelectorAll(".agregar-carrito").forEach(boton => {
        boton.addEventListener("click", (event) => {
            event.preventDefault();
            const productoId = boton.getAttribute("data-id");
            const nombre = boton.getAttribute("data-nombre");
            const precio = parseFloat(boton.getAttribute("data-precio"));
            const imagen = boton.getAttribute("data-imagen") || "default.jpg"; // Asegúrate que tus botones tengan data-imagen
            agregarAlCarrito(productoId, nombre, precio, imagen);
        });
    });

    const vaciarCarritoBtn = document.getElementById("vaciar-carrito");
    if (vaciarCarritoBtn) {
        vaciarCarritoBtn.addEventListener("click", () => {
            vaciarCarrito();
        });
    }

    const mostrarCarritoBtn = document.getElementById("mostrar-carrito");
    if (mostrarCarritoBtn) {
        mostrarCarritoBtn.addEventListener("click", () => {
            const carritoElement = document.getElementById("carrito");
            if (carritoElement) {
                carritoElement.classList.toggle("mostrar");
            }
        });
    }

    //Event listener para el boton de pagar:
    const pagarBtn = document.getElementById("pagar");
    if (pagarBtn) {
        pagarBtn.addEventListener("click", async () => {
           event.preventDefault();
           await manejarProcesoCompra();
        });
    }

});

//Función para manejar el estado de autenticación y cargar el carrito
let carrito = []; // Ya no se carga directamente de localStorage aquí

async function cargarCarrito() {
    // Primero cargar desde localStorage siempre
    const carritoLocal = localStorage.getItem("carrito");
    carrito = carritoLocal ? JSON.parse(carritoLocal) : [];

    // Luego verificar autenticación
    await checkAuthenticationStatus();

    if (IS_USER_AUTHENTICATED) {
        //Si hay productos en el carrito local, migrar al backend
        if (carrito.length > 0) {
            await migrarCarritoLocalStorageAlBackend();
        }
        //Luego cargar el carrito del backend
        await obtenerCarritoDelBackend();
    } else {
        //Si el usuario no esta autenticado, solo usar el carrito local
        console.log("Usuario no autenticado, usando carrito local.");
        actualizarVisualizacionCarrito();
        actualizarCantidadCarritoIcono();
    }
}

//Función para migrar el carrito de localStorage al backend
async function obtenerCarritoDelBackend() {
    try {
        const response = await fetch('/api/carrito');
        if (!response.ok) {
            if (response.status === 401) { // No autorizado / No logueado
                console.warn("Usuario no autenticado o sesión expirada al obtener carrito.");
                IS_USER_AUTHENTICATED = false; // Actualizar estado
                carrito = []; // Limpiar carrito local si la sesión expiró
                localStorage.removeItem("carrito"); // Limpiar localStorage también
            } else {
                throw new Error(`Error del servidor al obtener carrito: ${response.status}`);
            }
        } else {
            const contentType = response.headers.get("content-type");
            if (contentType && contentType.indexOf("application/json") !== -1) {
                carrito = await response.json();
            } else {
                console.warn("Respuesta de obtenerCarritoDelBackend no es JSON. Contenido:", await response.text());
                carrito = []; // Asumir carrito vacío si la respuesta no es JSON
            }
        }
    } catch (error) {
        console.error("Error en obtenerCarritoDelBackend:", error);
        carrito = []; // En caso de error, carrito vacío
    }
    //Actualizar la visualización del carrito y el icono de cantidad
    actualizarVisualizacionCarrito();
    actualizarCantidadCarritoIcono();
}

//Funcion para agregar un producto al carrito
async function agregarAlCarrito(productoId, nombre, precio, imagen) {
    //Convertir ID a numero para consistencia con el backend
    const idNumerico = Number(productoId);

    if (IS_USER_AUTHENTICATED) {
        try {
            const response = await fetch(`/api/carrito/agregar?productoId=${productoId}&cantidad=1`, {
                method: 'POST',
            });
            if (!response.ok) {
                if (response.status === 401) {
                    mostrarAlerta("Debes iniciar sesión para agregar productos al carrito.");
                    IS_USER_AUTHENTICATED = false;
                    return;
                }
                throw new Error(`Error al agregar producto al carrito del backend: ${response.status}`);
            }
            await obtenerCarritoDelBackend();
            mostrarAlerta(`"${nombre}" se agregó al carrito.`);
        } catch (error) {
            console.error("Error en agregarAlCarrito (backend):", error);
            mostrarAlerta(`Error al agregar "${nombre}". Intenta de nuevo.`);
        }
    } else {
        let existe = carrito.find(p => p.id === idNumerico);
        if (existe) {
            existe.cantidad++;
        } else {
            carrito.push({
                id: idNumerico, //Usar ID numérico para consistencia en vez de productoId
                nombre: nombre,
                precio: parseFloat(precio),
                cantidad: 1,
                imagen: imagen
            });
        }
        localStorage.setItem("carrito", JSON.stringify(carrito));
        actualizarVisualizacionCarrito();
        actualizarCantidadCarritoIcono();
        mostrarAlerta(`"${nombre}" se agregó al carrito local. Inicia sesión para guardar.`);
    }
}

async function eliminarProducto(productoId) {
    if (IS_USER_AUTHENTICATED) {
        try {
            const response = await fetch(`/api/carrito/eliminar?productoId=${productoId}`, {
                method: 'DELETE',
            });
            if (!response.ok) {
                if (response.status === 401) {
                    mostrarAlerta("Debes iniciar sesión para modificar el carrito.");
                    IS_USER_AUTHENTICATED = false;
                    return;
                }
                throw new Error(`Error al eliminar producto del backend: ${response.status}`);
            }
            await obtenerCarritoDelBackend();
            mostrarAlerta(`Producto eliminado del carrito.`);
        } catch (error) {
            console.error("Error en eliminarProducto (backend):", error);
            mostrarAlerta(`Error al eliminar producto.`);
        }
    } else {
        const index = carrito.findIndex(p => p.id === productoId);
        if (index > -1) {
            carrito.splice(index, 1);
            localStorage.setItem("carrito", JSON.stringify(carrito));
            actualizarVisualizacionCarrito();
            actualizarCantidadCarritoIcono();
            mostrarAlerta(`Producto eliminado del carrito local.`);
        }
    }
}

async function actualizarCantidadEnCarrito(productoId, nuevaCantidad) {
    nuevaCantidad = parseInt(nuevaCantidad);
    if (isNaN(nuevaCantidad) || nuevaCantidad < 0) {
        if (nuevaCantidad === 0) {
            await eliminarProducto(productoId);
        }
        return;
    }

    if (IS_USER_AUTHENTICATED) {
        try {
            const response = await fetch(`/api/carrito/agregar?productoId=${productoId}&cantidad=${nuevaCantidad}`, {
                method: 'POST',
            });

            if (!response.ok) {
                if (response.status === 401) {
                    mostrarAlerta("Debes iniciar sesión para modificar el carrito.");
                    IS_USER_AUTHENTICATED = false;
                    return;
                }
                throw new Error(`Error al actualizar cantidad en backend: ${response.status}`);
            }
            await obtenerCarritoDelBackend();
            mostrarAlerta(`Cantidad actualizada.`);

        } catch (error) {
            console.error("Error actualizando cantidad (backend):", error);
            mostrarAlerta(`Error al actualizar cantidad.`);
        }
    } else {
        const productoEnCarrito = carrito.find(p => p.id === productoId);
        if (productoEnCarrito) {
            if (nuevaCantidad > 0) {
                productoEnCarrito.cantidad = nuevaCantidad;
            } else {
                const index = carrito.findIndex(p => p.id === productoId);
                carrito.splice(index, 1);
            }
            localStorage.setItem("carrito", JSON.stringify(carrito));
            actualizarVisualizacionCarrito();
            actualizarCantidadCarritoIcono();
        }
    }
}


async function vaciarCarrito() {
    if (IS_USER_AUTHENTICATED) {
        try {
            const response = await fetch('/api/carrito/vaciar', { method: 'DELETE' });
            if (!response.ok) {
                if (response.status === 401) {
                    mostrarAlerta("Debes iniciar sesión para vaciar el carrito.");
                    IS_USER_AUTHENTICATED = false;
                    return;
                }
                throw new Error(`Error al vaciar carrito en backend: ${response.status}`);
            }
            await obtenerCarritoDelBackend();
            mostrarAlerta("Carrito vaciado.");
        } catch (error) {
            console.error("Error vaciando carrito (backend):", error);
            mostrarAlerta("Error al vaciar el carrito.");
        }
    } else {
        carrito = [];
        localStorage.removeItem("carrito");
        actualizarVisualizacionCarrito();
        actualizarCantidadCarritoIcono();
        mostrarAlerta("Carrito local vaciado.");
    }
}

/**
 * NUEVA función: Manejar el proceso de compra
 * Si el usuario no está logueado, redirige al login con parámetro especial
 */

async function manejarProcesoCompra() {
    //Verificar que hay productos en el carrito
    if(!carrito || carrito.length === 0) {
        mostrarAlerta("Tu carrito está vacío. Agrega productos antes de pagar.");
        return;
    }

    if (!IS_USER_AUTHENTICATED) {
        Swal.fire({
            icon: 'info',
            title: 'Iniciar sesión para continuar',
            text: 'Debes iniciar sesion para proceder con la compra, los productos del carrito se guardarán',
            confirmButtonText: 'Ir al Login',
            confirmButtonColor: '#3085d6',
            //Ver despues si agregamos un boton de cancelar
            timer: 5000, // 5 segundos
            timerProgressBar: true,
            allowOutsideClick: false,
        }).then((result) => {
            if (result.isConfirmed) {
                //Asegurar que el carrito este cguardado en el LocalStorage
                localStorage.setItem("carrito", JSON.stringify(carrito));
                //Redirigir al login con un parámetro especial para la sincronización del carrito
                window.location.href = '/inicioSesion/login?login&fromCheckout=true'; 
            }
        });
    } else {
        //Si el usuario está logueado, proceder con la compra
        procederAlPago();
    }
}

//IMPLEMENTAR LÓGICA DE PAGO AQUÍ    
//Funcion para proceder al pago
async function procederAlPago() {
    //Implementar la lógica de pago con mercado pago para mas adelante
    window.location.href = '/pago'; // Redirigir a la página de pago
}



async function migrarCarritoLocalStorageAlBackend() {
    const carritoLocalSerializado = localStorage.getItem("carrito");
    if (!carritoLocalSerializado) return;

    const carritoLocal = JSON.parse(carritoLocalSerializado);
    if (carritoLocal && carritoLocal.length > 0) {
        console.log("Migrando carrito de localStorage al backend:", carritoLocal);
        try {
            const promesasDeMigracion = carritoLocal.map(producto => {
                return fetch(`/api/carrito/agregar?productoId=${producto.id}&cantidad=${producto.cantidad}`, {
                    method: 'POST',
                }).then(response => {
                    if (!response.ok) {
                        console.warn(`Error migrando producto ${producto.id}: ${response.status}, ${response.statusText}`);
                    }
                    return response;
                });
            });

            await Promise.all(promesasDeMigracion);
            console.log("Migración completada.");
            // mostrarAlerta("Carrito local sincronizado con el servidor."); // Se muestra después de obtenerCarritoDelBackend
        } catch (error) {
            console.error("Error durante la migración del carrito:", error);
            mostrarAlerta("Hubo un problema al sincronizar tu carrito local.");
        } finally {
            localStorage.removeItem("carrito");
            console.log("localStorage limpiado después de la migración.");
        }
    }
}

//Funcion para actualizar la visualización del carrito
// Esta función actualiza la tabla del carrito y el total
function actualizarVisualizacionCarrito() {
    let tablaCarrito = document.querySelector("#lista-carrito tbody");
    let totalCarritoEl = document.getElementById("totalCarrito");

    if (!tablaCarrito || !totalCarritoEl) {
        return;
    }

    tablaCarrito.innerHTML = "";
    let totalGeneral = 0;

    if (carrito && carrito.length > 0) {
        carrito.forEach(item => {
            const esProductoDeBackend = item.hasOwnProperty('producto') && item.producto !== null;

            const idProductoParaAcciones = esProductoDeBackend ? item.producto.id : item.id;
            const nombreProducto = esProductoDeBackend ? item.producto.nombre : item.nombre;
            // Asegúrate de que el precio exista y sea un número
            const precioProductoBruto = esProductoDeBackend ? item.producto.precioVenta : item.precio;
            const precioProducto = parseFloat(precioProductoBruto);

            const imagenProducto = esProductoDeBackend ? (item.producto.rutaImagen || 'default.jpg') : (item.imagen || 'default.jpg');
            const cantidadProducto = parseInt(item.cantidad);

            const pagarBtn = document.getElementById("pagar");
            // Mostrar el botón de pagar solo si hay productos en el carrito
            if (pagarBtn) {
                pagarBtn.style.display = carrito.length > 0 ? "inline-block" : "none";
            }

            if (isNaN(precioProducto) || isNaN(cantidadProducto)) {
                console.error("Producto con datos inválidos en el carrito:", item);
                return; // Saltar este producto si los datos son inválidos
            }

            let fila = document.createElement("tr");

            fila.innerHTML = `
                <td><img src="/images/${imagenProducto}" ...></td>
                <td>${nombreProducto}</td>
                <td>$${precioProducto.toFixed(2)}</td>
                <td>
                    <input type="number" class="cantidad form-control form-control-sm" style="width: 70px;" value="${cantidadProducto}" min="1" data-product-id="${idProductoParaAcciones}">
                </td>
                <td>$${(precioProducto * cantidadProducto).toFixed(2)}</td>
                 <td><button class="btn btn-danger btn-sm bi bi-trash-fill" 
                   onclick="eliminarProducto(${idProductoParaAcciones})"></button></td>
            `;
            tablaCarrito.appendChild(fila);
            totalGeneral += precioProducto * cantidadProducto;
        });
    } else {
        let filaVacia = document.createElement("tr");
        filaVacia.innerHTML = `<td colspan="6" class="text-center">El carrito está vacío.</td>`;
        tablaCarrito.appendChild(filaVacia);
    }

    totalCarritoEl.textContent = `Total: $${totalGeneral.toFixed(2)}`;

    document.querySelectorAll("#lista-carrito .cantidad").forEach(input => {
        input.removeEventListener('change', handleCantidadChange); // Remover listener anterior para evitar duplicados
        input.addEventListener('change', handleCantidadChange);
    });
}

function handleCantidadChange(e) {
    const productoId = e.target.getAttribute("data-product-id");
    const nuevaCantidad = parseInt(e.target.value);
    actualizarCantidadEnCarrito(productoId, nuevaCantidad);
}


function actualizarCantidadCarritoIcono() {
    const cantidadTotal = carrito.reduce((acc, item) => acc + parseInt(item.cantidad), 0);
    const carritoCantidadEl = document.getElementById('carrito-cantidad');
    if (carritoCantidadEl) {
        carritoCantidadEl.textContent = isNaN(cantidadTotal) ? 0 : cantidadTotal;
    }
}

function mostrarAlerta(mensaje) {
    const alertaContainer = document.getElementById('alertas-carrito-container');
    if (!alertaContainer) {
        console.warn("Contenedor de alertas 'alertas-carrito-container' no encontrado. Creando uno temporal.");
        const tempContainer = document.createElement('div');
        tempContainer.id = 'alertas-carrito-container';
        tempContainer.style.position = 'fixed';
        tempContainer.style.top = '20px';
        tempContainer.style.right = '20px';
        tempContainer.style.zIndex = '2000';
        document.body.appendChild(tempContainer);
        // Reintentar mostrar la alerta con el contenedor recién creado
        mostrarAlerta(mensaje);
        return;
    }


    const alerta = document.createElement('div');
    // Usar clases de Bootstrap para alertas si Bootstrap está disponible
    alerta.className = 'alert alert-success alert-dismissible fade show';
    alerta.setAttribute('role', 'alert');
    alerta.innerHTML = `
        ${mensaje}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    `;

    alertaContainer.appendChild(alerta);

    // Bootstrap maneja el cierre. Si no usas Bootstrap, necesitas tu propia lógica de timeout.
    // Este timeout es para quitarlo del DOM después de que se desvanece, si no se cierra manualmente.
    setTimeout(() => {
        if (alerta.parentNode) {
            // Bootstrap 5 podría ya haberlo quitado si se usó el botón de cerrar.
            // Para desvanecerlo suavemente si aún existe:
            alerta.classList.remove('show');
            setTimeout(() => {
                if (alerta.parentNode) {
                    alerta.parentNode.removeChild(alerta);
                }
            }, 150); // Esperar la transición de desvanecimiento de Bootstrap
        }
    }, 3000);
}

//Funcion para verificar el estado de autenticación
// Esta función se llama al cargar la página y actualiza la variable IS_USER_AUTHENTICATED
// Idealmente, esto se establecería de forma más robusta (ej. desde el backend al cargar la página)
async function checkAuthenticationStatus() {
    try {
        const response = await fetch('/api/usuario/actual');
        if (response.status === 200) {
            const data = await response.json();
            IS_USER_AUTHENTICATED = data.autenticado === true;
        } else {
            IS_USER_AUTHENTICATED = false;
        }
    } catch (error) {
        console.warn('Error verificando autenticación:', error);
        IS_USER_AUTHENTICATED = false;
    }
}



/**
 * Función para gestionar post-login
 * Se llama desde login.js después del login exitoso
 */
async function gestionarPostLogin() {
    console.log("Gestionando post-login...");
    
    // Recargar el estado de autenticación y carrito
    await checkAuthenticationStatus();
    
    if (IS_USER_AUTHENTICATED) {
        // Recargar carrito desde el backend
        await cargarCarrito();
        console.log("Carrito recargado después del login");
    }
}

// Función para gestionar logout
function gestionarLogout() {
    // Limpiar carrito local
    carrito = [];
    localStorage.removeItem("carrito");
    
    // Actualizar estado de autenticación
    IS_USER_AUTHENTICATED = false;
    
    // Actualizar visualización
    actualizarVisualizacionCarrito();
    actualizarCantidadCarritoIcono();
    
    console.log("Logout gestionado - carrito limpiado");
}

// Hacer la función disponible globalmente
window.gestionarLogout = gestionarLogout;


// Exponer funciones globalmente para que login.js pueda accederlas
window.gestionarPostLogin = gestionarPostLogin;
window.gestionarLogout = gestionarLogout;

// // document.addEventListener("DOMContentLoaded", () => {
// //     actualizarCarrito();
// //     actualizarCantidadCarrito();
// // src/main/resources/static/js/carrito.js

// // Variable global para el estado de autenticación (simplificado)
// // Idealmente, esto se establecería de forma más robusta (ej. desde el backend al cargar la página)
// let IS_USER_AUTHENTICATED = false; // Se actualizará dinámicamente

// // Función para verificar el estado de autenticación (marcador de posición)
// // Deberías reemplazar esto con tu lógica real para verificar la autenticación.
// // Por ejemplo, verificar la existencia de un token, o un endpoint del backend.
// async function checkAuthenticationStatus() {
//     try {
//         // Intenta obtener información del usuario o un indicador de sesión
//         // Esto es un EJEMPLO. Necesitas un endpoint real o un método fiable.
//         const response = await fetch('/api/usuario/actual'); // Endpoint hipotético
//         if (response.ok) {
//             // const usuario = await response.json(); // Si devuelve datos del usuario
//             // IS_USER_AUTHENTICATED = !!usuario; // O alguna otra lógica basada en la respuesta
//             IS_USER_AUTHENTICATED = true; // Asumimos que si responde OK, está autenticado
//         } else {
//             IS_USER_AUTHENTICATED = false;
//         }
//     } catch (error) {
//         console.warn('Error al verificar estado de autenticación, asumiendo no autenticado:', error);
//         IS_USER_AUTHENTICATED = false;
//     }
// }

// document.addEventListener("DOMContentLoaded", async () => {
//     // Primero, verifica el estado de autenticación
//     await checkAuthenticationStatus(); // Asegúrate que esto se complete

//     // Luego carga el carrito basado en el estado
//     await cargarCarrito(); // Cambiado a cargarCarrito

//     // Event listeners existentes
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

//     // document.getElementById("vaciar-carrito").addEventListener("click", () => {
//     //     carrito = [];
//     //     actualizarCarrito();
//     //     actualizarCantidadCarrito();
//     // });

//     // document.getElementById("mostrar-carrito").addEventListener("click", () => {
//     //     document.getElementById("carrito").classList.toggle("mostrar");
//     const vaciarCarritoBtn = document.getElementById("vaciar-carrito");
//     if (vaciarCarritoBtn) {
//         vaciarCarritoBtn.addEventListener("click", () => {
//             vaciarCarrito();
//         });
//     }

//     const mostrarCarritoBtn = document.getElementById("mostrar-carrito");
//     if (mostrarCarritoBtn) {
//         mostrarCarritoBtn.addEventListener("click", () => {
//             const carritoElement = document.getElementById("carrito");
//             if (carritoElement) {
//                 carritoElement.classList.toggle("mostrar");
//             }
//         });
//     }
// });

// // let carrito = JSON.parse(localStorage.getItem("carrito")) || [];

// // function agregarAlCarrito(productoId, nombre, precio, imagen) {
// let carrito = []; // Ya no se carga directamente de localStorage aquí

// async function cargarCarrito() {
//     console.log("Cargando carrito. Autenticado:", IS_USER_AUTHENTICATED);
//     if (IS_USER_AUTHENTICATED) {
//         await migrarCarritoLocalStorageAlBackend(); // Intenta migrar primero
//         await obtenerCarritoDelBackend();
//     } else {
//         // Carga desde localStorage para usuarios no autenticados
//         const carritoLocal = localStorage.getItem("carrito");
//         carrito = carritoLocal ? JSON.parse(carritoLocal) : [];
//         actualizarVisualizacionCarrito();
//         actualizarCantidadCarritoIcono();
//     }
// }

// async function obtenerCarritoDelBackend() {
//     try {
//         const response = await fetch('/api/carrito');
//         if (!response.ok) {
//             if (response.status === 401) { // No autorizado / No logueado
//                 console.warn("Usuario no autenticado o sesión expirada al obtener carrito.");
//                 IS_USER_AUTHENTICATED = false; // Actualizar estado
//                 carrito = []; // Limpiar carrito local si la sesión expiró
//                 localStorage.removeItem("carrito"); // Limpiar localStorage también
//             } else {
//                 throw new Error(`Error del servidor al obtener carrito: ${response.status}`);
//             }
//         } else {
//             const contentType = response.headers.get("content-type");
//             if (contentType && contentType.indexOf("application/json") !== -1) {
//                 carrito = await response.json();
//             } else {
//                 console.warn("Respuesta de obtenerCarritoDelBackend no es JSON. Contenido:", await response.text());
//                 carrito = []; // Asumir carrito vacío si la respuesta no es JSON
//             }
//         }
//     } catch (error) {
//         console.error("Error en obtenerCarritoDelBackend:", error);
//         carrito = []; // En caso de error, carrito vacío
//     }
//     actualizarVisualizacionCarrito();
//     actualizarCantidadCarritoIcono();
// }

// async function agregarAlCarrito(productoId, nombre, precio, imagen) {
//     if (IS_USER_AUTHENTICATED) {
//         try {
//             const response = await fetch(`/api/carrito/agregar?productoId=${productoId}&cantidad=1`, {
//                 method: 'POST',
//             });
//             if (!response.ok) {
//                 if (response.status === 401) {
//                     mostrarAlerta("Debes iniciar sesión para agregar productos al carrito.");
//                     IS_USER_AUTHENTICATED = false;
//                     return;
//                 }
//                 throw new Error(`Error al agregar producto al carrito del backend: ${response.status}`);
//             }
//             await obtenerCarritoDelBackend();
//             mostrarAlerta(`"${nombre}" se agregó al carrito.`);
//         } catch (error) {
//             console.error("Error en agregarAlCarrito (backend):", error);
//             mostrarAlerta(`Error al agregar "${nombre}". Intenta de nuevo.`);
//         }
//     } else {
//         let existe = carrito.find(p => p.id === productoId);
//         if (existe) {
//             existe.cantidad++;
//         } else {
//             carrito.push({
//                 id: productoId,
//                 nombre: nombre,
//                 precio: parseFloat(precio),
//                 cantidad: 1,
//                 imagen: imagen
//             });
//         }
//         // actualizarCarrito();
//         // actualizarCantidadCarrito();
//         // mostrarAlerta(`"${nombre}" se agregó al carrito.`);
//         localStorage.setItem("carrito", JSON.stringify(carrito));
//         actualizarVisualizacionCarrito();
//         actualizarCantidadCarritoIcono();
//         mostrarAlerta(`"${nombre}" se agregó al carrito local. Inicia sesión para guardar.`);
//     }

//     // function actualizarCarrito() {

// }

// async function eliminarProducto(productoId) {
//     if (IS_USER_AUTHENTICATED) {
//         try {
//             const response = await fetch(`/api/carrito/eliminar?productoId=${productoId}`, {
//                 method: 'DELETE',
//             });
//             if (!response.ok) {
//                 if (response.status === 401) {
//                     mostrarAlerta("Debes iniciar sesión para modificar el carrito.");
//                     IS_USER_AUTHENTICATED = false;
//                     return;
//                 }
//                 throw new Error(`Error al eliminar producto del backend: ${response.status}`);
//             }
//             await obtenerCarritoDelBackend();
//             mostrarAlerta(`Producto eliminado del carrito.`);
//         } catch (error) {
//             console.error("Error en eliminarProducto (backend):", error);
//             mostrarAlerta(`Error al eliminar producto.`);
//         }
//     } else {
//         const index = carrito.findIndex(p => p.id === productoId);
//         if (index > -1) {
//             carrito.splice(index, 1);
//             localStorage.setItem("carrito", JSON.stringify(carrito));
//             actualizarVisualizacionCarrito();
//             actualizarCantidadCarritoIcono();
//             mostrarAlerta(`Producto eliminado del carrito local.`);
//         }
//     }
// }

// async function actualizarCantidadEnCarrito(productoId, nuevaCantidad) {
//     nuevaCantidad = parseInt(nuevaCantidad);
//     if (isNaN(nuevaCantidad) || nuevaCantidad < 0) {
//         if (nuevaCantidad === 0) {
//             await eliminarProducto(productoId);
//         }
//         return;
//     }

//     if (IS_USER_AUTHENTICATED) {
//         try {
//             const response = await fetch(`/api/carrito/agregar?productoId=${productoId}&cantidad=${nuevaCantidad}`, {
//                 method: 'POST',
//             });

//             if (!response.ok) {
//                 if (response.status === 401) {
//                     mostrarAlerta("Debes iniciar sesión para modificar el carrito.");
//                     IS_USER_AUTHENTICATED = false;
//                     return;
//                 }
//                 throw new Error(`Error al actualizar cantidad en backend: ${response.status}`);
//             }
//             await obtenerCarritoDelBackend();
//             mostrarAlerta(`Cantidad actualizada.`);

//         } catch (error) {
//             console.error("Error actualizando cantidad (backend):", error);
//             mostrarAlerta(`Error al actualizar cantidad.`);
//         }
//     } else {
//         const productoEnCarrito = carrito.find(p => p.id === productoId);
//         if (productoEnCarrito) {
//             if (nuevaCantidad > 0) {
//                 productoEnCarrito.cantidad = nuevaCantidad;
//             } else {
//                 const index = carrito.findIndex(p => p.id === productoId);
//                 carrito.splice(index, 1);
//             }
//             localStorage.setItem("carrito", JSON.stringify(carrito));
//             actualizarVisualizacionCarrito();
//             actualizarCantidadCarritoIcono();
//         }
//     }
// }


// async function vaciarCarrito() {
//     if (IS_USER_AUTHENTICATED) {
//         try {
//             const response = await fetch('/api/carrito/vaciar', { method: 'DELETE' });
//             if (!response.ok) {
//                 if (response.status === 401) {
//                     mostrarAlerta("Debes iniciar sesión para vaciar el carrito.");
//                     IS_USER_AUTHENTICATED = false;
//                     return;
//                 }
//                 throw new Error(`Error al vaciar carrito en backend: ${response.status}`);
//             }
//             await obtenerCarritoDelBackend();
//             mostrarAlerta("Carrito vaciado.");
//         } catch (error) {
//             console.error("Error vaciando carrito (backend):", error);
//             mostrarAlerta("Error al vaciar el carrito.");
//         }
//     } else {
//         carrito = [];
//         localStorage.removeItem("carrito");
//         actualizarVisualizacionCarrito();
//         actualizarCantidadCarritoIcono();
//         mostrarAlerta("Carrito local vaciado.");
//     }
// }

// async function migrarCarritoLocalStorageAlBackend() {
//     const carritoLocalSerializado = localStorage.getItem("carrito");
//     if (!carritoLocalSerializado) return;

//     const carritoLocal = JSON.parse(carritoLocalSerializado);
//     if (carritoLocal && carritoLocal.length > 0) {
//         console.log("Migrando carrito de localStorage al backend:", carritoLocal);
//         try {
//             const promesasDeMigracion = carritoLocal.map(producto => {
//                 return fetch(`/api/carrito/agregar?productoId=${producto.id}&cantidad=${producto.cantidad}`, {
//                     method: 'POST',
//                 }).then(response => {
//                     if (!response.ok) {
//                         console.warn(`Error migrando producto ${producto.id}: ${response.status}, ${response.statusText}`);
//                     }
//                     return response;
//                 });
//             });

//             await Promise.all(promesasDeMigracion);
//             console.log("Migración completada.");
//             // mostrarAlerta("Carrito local sincronizado con el servidor."); // Se muestra después de obtenerCarritoDelBackend
//         } catch (error) {
//             console.error("Error durante la migración del carrito:", error);
//             mostrarAlerta("Hubo un problema al sincronizar tu carrito local.");
//         } finally {
//             localStorage.removeItem("carrito");
//             console.log("localStorage limpiado después de la migración.");
//         }
//     }
// }


// function actualizarVisualizacionCarrito() {
//     let tablaCarrito = document.querySelector("#lista-carrito tbody");
//     // let totalCarrito = document.getElementById("totalCarrito");
//     // if (!tablaCarrito || !totalCarrito) return;

//     let totalCarritoEl = document.getElementById("totalCarrito");

//     if (!tablaCarrito || !totalCarritoEl) {
//         return;
//     }

//     tablaCarrito.innerHTML = "";
//     // let total = 0;

//     // carrito.forEach((producto, index) => {
//     let totalGeneral = 0;

//     if (carrito && carrito.length > 0) {
//         carrito.forEach(item => {
//             const esProductoDeBackend = item.hasOwnProperty('producto') && item.producto !== null;

//             const idProductoParaAcciones = esProductoDeBackend ? item.producto.id : item.id;
//             const nombreProducto = esProductoDeBackend ? item.producto.nombre : item.nombre;
//             // Asegúrate de que el precio exista y sea un número
//             const precioProductoBruto = esProductoDeBackend ? item.producto.precioVenta : item.precio;
//             const precioProducto = parseFloat(precioProductoBruto);

//             const imagenProducto = esProductoDeBackend ? (item.producto.rutaImagen || 'default.jpg') : (item.imagen || 'default.jpg');
//             const cantidadProducto = parseInt(item.cantidad);

//             if (isNaN(precioProducto) || isNaN(cantidadProducto)) {
//                 console.error("Producto con datos inválidos en el carrito:", item);
//                 return; // Saltar este producto si los datos son inválidos
//             }
//             let fila = document.createElement("tr");
//             fila.innerHTML = `
//             // <td><img src="${producto.imagen}" width="50"></td>
//             // <td>${producto.nombre}</td>
//             // <td>$${producto.precio.toFixed(2)}</td>
//             td><img src="/images/${imagenProducto}" width="50" alt="${nombreProducto}"></td>
//                 <td>${nombreProducto}</td>
//                 <td>$${precioProducto.toFixed(2)}</td>
//             <td>
//             //     <input type="number" class="cantidad" value="${producto.cantidad}" min="1" data-index="${index}">
//             // </td>
//             <input type="number" class="cantidad form-control form-control-sm" style="width: 70px;" value="${cantidadProducto}" min="1" data-product-id="${idProductoParaAcciones}">
//                 </td>
//             <td><button class="bi bi-trash-fill border-0 bg-transparent" onclick="eliminarProducto(${index})"></button></td>
//         `;
//             tablaCarrito.appendChild(fila);
//             // total += producto.precio * producto.cantidad;
//         });

//         // totalCarrito.textContent = `Total: $${total.toFixed(2)}`;
//         // totalCarrito.style.marginTop = "20px"; // Añadir margen superior de 20px
//         // localStorage.setItem("carrito", JSON.stringify(carrito));

//     } else {
//         let filaVacia = document.createElement("tr");
//         filaVacia.innerHTML = `<td colspan="6" class="text-center">El carrito está vacío.</td>`;
//         tablaCarrito.appendChild(filaVacia);
//     }

//     totalCarritoEl.textContent = `Total: $${totalGeneral.toFixed(2)}`;

//     // document.querySelectorAll(".cantidad").forEach(input => {
//     //     input.addEventListener("change", (e) => {
//     //         let index = input.getAttribute("data-index");
//     //         let nuevaCantidad = parseInt(e.target.value);
//     //         if (nuevaCantidad > 0) {
//     //             carrito[index].cantidad = nuevaCantidad;
//     //             actualizarCarrito();
//     //             actualizarCantidadCarrito();
//     //         }
//     //     });
//     // });
//     document.querySelectorAll("#lista-carrito .cantidad").forEach(input => {
//         input.removeEventListener('change', handleCantidadChange); // Remover listener anterior para evitar duplicados
//         input.addEventListener('change', handleCantidadChange);
//     });
// }

// // function eliminarProducto(index) {
// //     carrito.splice(index, 1);
// //     actualizarCarrito();
// //     actualizarCantidadCarrito();

// function handleCantidadChange(e) {
//     const productoId = e.target.getAttribute("data-product-id");
//     const nuevaCantidad = parseInt(e.target.value);
//     actualizarCantidadEnCarrito(productoId, nuevaCantidad);
// }

// // function actualizarCantidadCarrito() {
// //     const cantidadTotal = carrito.reduce((acc, producto) => acc + producto.cantidad, 0);
// //     document.getElementById('carrito-cantidad').textContent = cantidadTotal;
// function actualizarCantidadCarritoIcono() {
//     const cantidadTotal = carrito.reduce((acc, item) => acc + parseInt(item.cantidad), 0);
//     const carritoCantidadEl = document.getElementById('carrito-cantidad');
//     if (carritoCantidadEl) {
//         carritoCantidadEl.textContent = isNaN(cantidadTotal) ? 0 : cantidadTotal;
//     }
// }

// function mostrarAlerta(mensaje) {
//     const alertaContainer = document.getElementById('alertas-carrito-container');
//     if (!alertaContainer) {
//         console.warn("Contenedor de alertas 'alertas-carrito-container' no encontrado. Creando uno temporal.");
//         const tempContainer = document.createElement('div');
//         tempContainer.id = 'alertas-carrito-container';
//         tempContainer.style.position = 'fixed';
//         tempContainer.style.top = '20px';
//         tempContainer.style.right = '20px';
//         tempContainer.style.zIndex = '2000';
//         document.body.appendChild(tempContainer);
//         // Reintentar mostrar la alerta con el contenedor recién creado
//         mostrarAlerta(mensaje);
//         return;
//     }
//     const alerta = document.createElement('div');
//     // alerta.className = 'alerta-carrito';
//     // alerta.textContent = mensaje;
//     // document.body.appendChild(alerta);
//     // Usar clases de Bootstrap para alertas si Bootstrap está disponible
//     alerta.className = 'alert alert-success alert-dismissible fade show';
//     alerta.setAttribute('role', 'alert');
//     alerta.innerHTML = `
//         ${mensaje}
//         <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
//     `;

//     alertaContainer.appendChild(alerta);
//     // Bootstrap maneja el cierre. Si no usas Bootstrap, necesitas tu propia lógica de timeout.
//     // Este timeout es para quitarlo del DOM después de que se desvanece, si no se cierra manualmente

//     // setTimeout(() => {
//     //     alerta.classList.add('mostrar');
//     // }, 100);

//     setTimeout(() => {
//         //alerta.classList.remove('mostrar');
//         if (alerta.parentNode) {
//             // Bootstrap 5 podría ya haberlo quitado si se usó el botón de cerrar.
//             // Para desvanecerlo suavemente si aún existe:
//             alerta.classList.remove('show');
//             setTimeout(() => {
//                 //document.body.removeChild(alerta);
//                 //}, 300);
//                 if (alerta.parentNode) {
//                     alerta.parentNode.removeChild(alerta);
//                 }
//             }, 150); // Esperar la transición de desvanecimiento de Bootstrap
//         }
//     }, 3000);
// }

// async function gestionarPostLogin() {
//     console.log("Gestionando post-login...");
//     await checkAuthenticationStatus();
//     if (IS_USER_AUTHENTICATED) {
//         console.log("Usuario autenticado después de login, procediendo a cargar carrito y migrar si es necesario.");
//         await cargarCarrito();
//         mostrarAlerta("Sesión iniciada. Tu carrito ha sido cargado/sincronizado.");
//     } else {
//         console.warn("Usuario NO autenticado después de intentar login. No se migra carrito.");
//     }
// }

// async function gestionarLogout() {
//     console.log("Gestionando logout...");
//     IS_USER_AUTHENTICATED = false;
//     carrito = [];
//     localStorage.removeItem("carrito");
//     actualizarVisualizacionCarrito();
//     actualizarCantidadCarritoIcono();
//     mostrarAlerta("Has cerrado sesión. Tu carrito ha sido limpiado.");
// }

// window.gestionarPostLogin = gestionarPostLogin;
// window.gestionarLogout = gestionarLogout;

// //Funcion para gestionar el logout
// // Esta función limpia el carrito y actualiza la interfaz
// async function gestionarLogout() {
//     console.log("Gestionando logout...");
//     IS_USER_AUTHENTICATED = false;

//     //Mantener el carrito local durante el logout
//     const carritoLocal = localStorage.getItem("carrito");
//     carrito = carritoLocal ? JSON.parse(carritoLocal) : [];

//     actualizarVisualizacionCarrito();
//     actualizarCantidadCarritoIcono();
// '}'
