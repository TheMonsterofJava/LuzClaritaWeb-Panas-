document.addEventListener('DOMContentLoaded', () => {
    const botonesAgregarCarrito = document.querySelectorAll('.agregar-carrito');

    botonesAgregarCarrito.forEach(boton => {
        boton.addEventListener('click', (event) => {
            event.preventDefault();

            const id = boton.getAttribute('data-id');
            const nombre = boton.getAttribute('data-nombre');
            const precio = boton.getAttribute('data-precio');

            const producto = {
                id: id,
                nombre: nombre,
                precio: parseFloat(precio)
            };

            fetch('/carrito/agregar', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(producto)
            })
            .then(response => response.json())
            .then(data => {
                console.log('Producto agregado:', data);
                // Aquí puedes actualizar el contador del carrito o mostrar un mensaje de éxito
            })
            .catch(error => {
                console.error('Error al agregar el producto:', error);
            });
        });
    });
});