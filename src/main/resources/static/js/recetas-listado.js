$(document).ready(() => {
        $('#tabla-recetas').DataTable({
            order: [[0, 'asc']], // Ordenar por nombre de receta
            lengthMenu: [5, 10, 25, 100],
            paging: false,
            info: false,
            scrollCollapse: true,
            scrollY: '400px',
            columnDefs: [
                { orderable: false, targets: 3 }, // Desactivar orden en "Acciones"
                { searchable: false, targets: 3 }  // Desactivar búsqueda en "Acciones"
            ],
            language: {
                "search": "🔍 Buscar receta:",
                "lengthMenu": "Mostrar _MENU_ recetas por página",
                "info": "Mostrando _START_ a _END_ de _TOTAL_ recetas",
                "infoFiltered": "(filtrado de un total de _MAX_ recetas)",
                "infoEmpty": "No hay coincidencias",
                "zeroRecords": "No se encontraron recetas",
                "emptyTable": "No hay recetas cargadas aún",
            }
        });
    });
