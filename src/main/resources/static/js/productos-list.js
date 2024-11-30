$(document).ready(() => {
    $("#tabla-productos").DataTable({
        order:[[2, "asc"]],
        lengthMenu: [10, 25, 50, 100],
        columns: [
            null,
            {orderable: false},
            null,
            null,
            {bSearchable: false},
            {orderable: false},
            {orderable: false}
        ],
        language: {
            "search": "Buscar",
            "lengthMenu": "Mostrar _MENU_ productos",
            "info": "Mostrando de _START_ a _END_ de _TOTAL_ productos",
            "infoFiltered": " (Filtrado de _MAX_ productos) ",
            "zeroRecords": "No hay coincidencias...",
            "infoEmpty": "No hay productos disponibles", // Mensaje cuando no hay datos para mostrar
            "emptyTable": "No hay datos disponibles en la tabla" // Mensaje cuando la tabla está vacía
        }
    });
});