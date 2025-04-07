$(document).ready(() => {
    $("#tabla-proveedores").DataTable({
        // recordar que las matrices funcionan a partir de 0,1,2 por columna
        order:[[1, "asc"]],
        lengthMenu: [10, 100],
        paging: false,
        scrollCollapse: true,
        scrollY: '400px',
        columns: [
            null, //id
            null,//nombre
            {orderable: false},//contacto
            null,//telef
            null,//
            null,
            null,
            {orderable: false}
            ],
        
        language: {
            "search": "Buscar: ",
            "lengthMenu": "Mostrar _MENU_ registros",
            "info": "Mostrando de _START_ a _END_ de _TOTAL_ proveedores registrados",
            "infoFiltered": "(filtrado de _MAX_ proveedores)", 
            "infoEmpty": "No hay coincidencias...",
            "zeroRecords": "No se encontro ningún proveedor", 
            "emptyTable": "No se encontraron proveedor"
        }
    });
});