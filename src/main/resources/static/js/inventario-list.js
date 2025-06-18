$(document).ready(() => {
    $("#tabla-inventario").DataTable({
        // recordar que las matrices funcionan a partir de 0,1,2 por columna
        order:[[1, "asc"]],
        lengthMenu: [10, 100],
        paging: false,
        scrollCollapse: true,
        scrollY: '400px',
        columns: [
            {bSearchable: false}, //id
            null,//ingrediente
            {orderable: false},//unidad
            null,//precio
            null,//cantidad
            null,//FechIng
            null,
            null,
            null,
            {orderable: false}

        ],
        language: {
            "search": "Buscar: ",
            "lengthMenu": "Mostrar _MENU_ registros",
            "info": "Mostrando de _START_ a _END_ de _TOTAL_ ingredientes registrados",
            "infoFiltered": "(filtrado de _MAX_ ingredientes)", 
            "infoEmpty": "No hay coincidencias...",
            "zeroRecords": "No se encontro ningún ingrediente", 
            "emptyTable": "No se encontraron ingredientes"
        }
    });
});

// $(document).ready(function() {
//     $('#tabla-inventario').DataTable({
//         "ordering": true,
//         "columnDefs": [
//             { "orderable": false, "targets": [0, 8, 9] }, // Deshabilita orden en ID, ID Usuario y Acciones
//             { "type": "num-fmt", "targets": [3, 4] }, // Precio y Cantidad como números con formato
//             {
//                 "targets": [5, 6], // Fechas
//                 "type": "date",
//                 "render": function(data) {
//                     if (!data) return "";
//                     var parts = data.split("-"); // Suponiendo formato YYYY-MM-DD
//                     return parts[2] + "/" + parts[1] + "/" + parts[0]; // Convertir a DD/MM/YYYY
//                 }
//             }
//         ],
//         "language": {
//             "url": "//cdn.datatables.net/plug-ins/1.12.1/i18n/Spanish.json"
//         }
//     });
// });

