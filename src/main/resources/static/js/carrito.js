let carrito = [];

function agregarAlCarrito(producto) {
    const item = carrito.find(item => item.producto.id === producto.id);
    if (item) {
        item.cantidad++;
    } else {
        carrito.push({ producto, cantidad: 1 });
    }
    actualizarCarrito();
}

function actualizarCarrito() {
    const cantidadCarrito = document.getElementById('cantidad-carrito');
    const productosCarrito = document.getElementById('productos-carrito');
    cantidadCarrito.innerText = carrito.reduce((acc, item) => acc + item.cantidad, 0);
    productosCarrito.innerHTML = '';
    carrito.forEach(item => {
        const li = document.createElement('li');
        li.innerText = `${item.producto.nombre} - ${item.producto.precio} x ${item.cantidad}`;
        productosCarrito.appendChild(li);
    });
}

function mostrarCarrito() {
    const carritoContenido = document.getElementById('carrito-contenido');
    carritoContenido.style.display = carritoContenido.style.display === 'none' ? 'block' : 'none';
    actualizarCarrito(); // Asegurarse de actualizar el contenido del carrito al mostrarlo
}

function comprar() {
    // Lógica para procesar la compra
    alert('Compra realizada con éxito');
    carrito = [];
    actualizarCarrito();
}
