document.addEventListener("DOMContentLoaded", () => {
    actualizarCarrito();
    actualizarCantidadCarrito();

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

    document.getElementById("vaciar-carrito").addEventListener("click", () => {
        carrito = [];
        actualizarCarrito();
        actualizarCantidadCarrito();
    });

    document.getElementById("mostrar-carrito").addEventListener("click", () => {
        document.getElementById("carrito").classList.toggle("mostrar");
    });
});

let carrito = JSON.parse(localStorage.getItem("carrito")) || [];

function agregarAlCarrito(productoId, nombre, precio, imagen) {
    let existe = carrito.find(p => p.id === productoId);
    if (existe) {
        existe.cantidad++;
    } else {
        carrito.push({
            id: productoId,
            nombre: nombre,
            precio: parseFloat(precio),
            cantidad: 1,
            imagen: imagen
        });
    }
    actualizarCarrito();
    actualizarCantidadCarrito();
    mostrarAlerta(`"${nombre}" se agregó al carrito.`);
}

function actualizarCarrito() {
    let tablaCarrito = document.querySelector("#lista-carrito tbody");
    let totalCarrito = document.getElementById("totalCarrito");
    if (!tablaCarrito || !totalCarrito) return;

    tablaCarrito.innerHTML = "";
    let total = 0;

    carrito.forEach((producto, index) => {
        let fila = document.createElement("tr");
        fila.innerHTML = `
            <td><img src="${producto.imagen}" width="50"></td>
            <td>${producto.nombre}</td>
            <td>$${producto.precio.toFixed(2)}</td>
            <td>
                <input type="number" class="cantidad" value="${producto.cantidad}" min="1" data-index="${index}">
            </td>
            <td><button class="bi bi-trash-fill border-0 bg-transparent" onclick="eliminarProducto(${index})"></button></td>
        `;
        tablaCarrito.appendChild(fila);
        total += producto.precio * producto.cantidad;
    });

    totalCarrito.textContent = `Total: $${total.toFixed(2)}`;
    totalCarrito.style.marginTop = "20px"; // Añadir margen superior de 20px
    localStorage.setItem("carrito", JSON.stringify(carrito));

    document.querySelectorAll(".cantidad").forEach(input => {
        input.addEventListener("change", (e) => {
            let index = input.getAttribute("data-index");
            let nuevaCantidad = parseInt(e.target.value);
            if (nuevaCantidad > 0) {
                carrito[index].cantidad = nuevaCantidad;
                actualizarCarrito();
                actualizarCantidadCarrito();
            }
        });
    });
}

function eliminarProducto(index) {
    carrito.splice(index, 1);
    actualizarCarrito();
    actualizarCantidadCarrito();
}

function actualizarCantidadCarrito() {
    const cantidadTotal = carrito.reduce((acc, producto) => acc + producto.cantidad, 0);
    document.getElementById('carrito-cantidad').textContent = cantidadTotal;
}

function mostrarAlerta(mensaje) {
    const alerta = document.createElement('div');
    alerta.className = 'alerta-carrito';
    alerta.textContent = mensaje;
    document.body.appendChild(alerta);

    setTimeout(() => {
        alerta.classList.add('mostrar');
    }, 100);

    setTimeout(() => {
        alerta.classList.remove('mostrar');
        setTimeout(() => {
            document.body.removeChild(alerta);
        }, 300);
    }, 3000);
}
