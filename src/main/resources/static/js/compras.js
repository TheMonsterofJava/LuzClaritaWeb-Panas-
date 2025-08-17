// Agregar CSRF token a las peticiones AJAX
$(function() {
    const token = $("meta[name='_csrf']").attr("content");
    const header = $("meta[name='_csrf_header']").attr("content");
    
    if (token && header) {
        $(document).ajaxSend(function(e, xhr, options) {
            if (!/^(GET|HEAD|OPTIONS)$/i.test(options.type) && 
                options.url && 
                !options.url.startsWith('http')) {
                xhr.setRequestHeader(header, token);
            }
        });
    } else {
        console.error("CSRF token or header not found. AJAX POST requests might fail.");
    }
});

// Inicializa select2 para un select de ingrediente
function initSelect2Ingrediente($select, proveedorId) {
    $select.select2({
        placeholder: "Buscar o agregar ingrediente...",
        allowClear: true,
        tags: true,
        ajax: {
            url: '/inventario/buscar',
            dataType: 'json',
            delay: 250,
            data: function(params) {
                return { q: params.term || '' };
            },
            processResults: function(data) {
                return { results: data };
            },
            cache: true
        },
        createTag: function(params) {
            const term = $.trim(params.term);
            if (term === '') return null;
            return { id: term, text: term, newTag: true };
        }
    }).on('select2:select', function(e) {
        const data = e.params.data;
        const $select = $(this);

        if (data.newTag) {
            const currentProveedorId = $('#proveedor').val();

            if (!currentProveedorId) {
                Swal.fire({
                    icon: 'error',
                    title: 'Error',
                    text: 'Debe seleccionar un proveedor antes de agregar un nuevo ingrediente.'
                });
                $select.val(null).trigger('change');
                return;
            }

            Swal.fire({
                title: `Nuevo Ingrediente: ${data.text}`,
                html: '<input id="swal-unidad" class="swal2-input" placeholder="Unidad de medida (kg, gr, etc)">',
                focusConfirm: false,
                showCancelButton: true,
                confirmButtonText: 'Crear',
                cancelButtonText: 'Cancelar',
                preConfirm: () => {
                    const unidad = $('#swal-unidad').val();
                    if (!unidad) {
                        Swal.showValidationMessage('Por favor, ingrese la unidad de medida');
                        return false;
                    }
                    return unidad;
                }
            }).then((result) => {
                if (result.isConfirmed && result.value) {
                    const unidadMedida = result.value;
                    $.post('/inventario/ajax/crear-rapido', {
                        nombre: data.text,
                        unidad: unidadMedida,
                        proveedorId: currentProveedorId
                    }).done(function(resp) {
                        if (resp?.id && resp?.text) {
                            const option = new Option(resp.text, resp.id, true, true);
                            $select.empty().append(option).trigger('change');
                        } else {
                            showError('La respuesta del servidor no fue la esperada al crear el ingrediente.');
                            $select.val(null).trigger('change');
                        }
                    }).fail(function(jqXHR) {
                        console.error("Error AJAX en crear-rapido:", jqXHR.responseText);
                        showError(jqXHR.responseJSON?.message || jqXHR.statusText);
                        $select.val(null).trigger('change');
                    });
                } else {
                    $select.val(null).trigger('change');
                }
            });
        }
    });
}

// Función para mostrar errores
function showError(message) {
    Swal.fire({
        icon: 'error',
        title: 'Error al crear ingrediente',
        text: `Hubo un problema al intentar guardar el nuevo ingrediente. ${message}`
    });
}

$(document).ready(function() {
    // Función para agregar nueva fila
    function agregarFila(index) {
        return `
        <tr>
            <td>
                <select class="form-select select2-ingrediente" 
                        name="detalles[${index}].ingrediente.id" 
                        required style="width:100%"></select>
            </td>
            <td>
                <input type="number" class="form-control cantidad" 
                        name="detalles[${index}].cantidad" min="1" required>
            </td>
            <td>
                <input type="number" class="form-control precio" 
                        name="detalles[${index}].precioUnitario" 
                        min="0.01" step="0.01" required>
            </td>
            <td class="subtotal">0.00</td>
            <td class="text-center">
                <button type="button" class="btn btn-danger btn-sm btn-eliminar">
                    <i class="bi bi-trash"></i>
                </button>
            </td>
        </tr>`;
    }

    // Inicializa Select2 en los selects existentes
    $('.select2-ingrediente').each(function() {
        initSelect2Ingrediente($(this), $('#proveedor').val());
    });

    // Evento para agregar detalle
    $('#agregarDetalleBtn').click(function() {
        const index = $('#tablaDetalles tbody tr').length;
        $('#tablaDetalles tbody').append(agregarFila(index));

        // Inicializar Select2 en la nueva fila
        setTimeout(() => {
            const $newSelect = $('#tablaDetalles tbody tr:last .select2-ingrediente');
            initSelect2Ingrediente($newSelect, $('#proveedor').val());
        }, 100);

        // Inicializar eventos en la nueva fila
        const $newRow = $('#tablaDetalles tbody tr').last();
        $newRow.find('.cantidad, .precio').on('input', calcularSubtotal);
        $newRow.find('.btn-eliminar').click(eliminarDetalle);
    });

    // Actualizar proveedorId en los selects cuando cambia
    $('#proveedor').on('change', function() {
        $('.select2-ingrediente').each(function() {
            $(this).data('proveedorId', $(this).val());
        });
    });

    // Cálculo de subtotal
    function calcularSubtotal() {
        const $row = $(this).closest("tr");
        const cantidad = parseFloat($row.find('.cantidad').val()) || 0;
        const precio = parseFloat($row.find('.precio').val()) || 0;
        const subtotal = cantidad * precio;
        $row.find('.subtotal').text(subtotal.toFixed(2));
        calcularTotal();
    }

    // Eliminar fila
    function eliminarDetalle() {
        Swal.fire({
            title: '¿Eliminar este item?',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'Sí, eliminar',
            cancelButtonText: 'No'
        }).then((result) => {
            if (result.isConfirmed) {
                $(this).closest("tr").remove();
                calcularTotal();
                reindexarFilas();
            }
        });
    }

    // Cálculo de total
    function calcularTotal() {
        let total = 0;
        $('.subtotal').each(function() {
            total += parseFloat($(this).text()) || 0;
        });
        $("#totalTabla").text(total.toFixed(2));
    }

    // Reindexar filas
    function reindexarFilas() {
        $('#tablaDetalles tbody tr').each(function(newIndex) {
            $(this).find('[name]').each(function() {
                const newName = $(this).attr('name')
                    .replace(/detalles\[\d+\]/g, `detalles[${newIndex}]`);
                $(this).attr('name', newName);
            });
        });
    }

    // Eventos iniciales
    $(document).on('input', '.cantidad, .precio', calcularSubtotal);
    $(document).on('click', '.btn-eliminar', eliminarDetalle);

    // Validación del formulario
    $('#formCompra').on('submit', function(e) {
        let hasErrors = false;
        
        $('.select2-ingrediente').each(function() {
            if (!$(this).val() || isNaN(Number($(this).val()))) {
                hasErrors = true;
                return false; // Salir del each
            }
        });
        
        if (hasErrors) {
            e.preventDefault();
            Swal.fire("Hay ingredientes no válidos", "Corrige los ingredientes antes de guardar", "error");
            return false;
        }
        
        if ($('#tablaDetalles tbody tr').length === 0) {
            e.preventDefault();
            Swal.fire("Debes agregar al menos un detalle", "", "warning");
        }
    });
});