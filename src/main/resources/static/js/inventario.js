const tableBody = document.getElementById('table-body');
const addItemForm = document.getElementById('add-item-form');
const toggleFormBtn = document.getElementById('toggle-form-btn');
const overlay = document.getElementById('overlay');
let editingRow = null;

// Mostrar/ocultar el formulario y el fondo difuminado
toggleFormBtn.addEventListener('click', () => {
    if (addItemForm.style.display === 'none' || addItemForm.style.display === '') {
        addItemForm.style.display = 'block';
        overlay.style.display = 'block';
        toggleFormBtn.textContent = 'Ocultar Formulario';
    } else {
        addItemForm.style.display = 'none';
        overlay.style.display = 'none';
        toggleFormBtn.textContent = 'Mostrar Formulario';
    }
});

// Cerrar el formulario al hacer clic fuera de él
overlay.addEventListener('click', () => {
    addItemForm.style.display = 'none';
    overlay.style.display = 'none';
    toggleFormBtn.textContent = 'Mostrar Formulario';
});

// Manejo del formulario
addItemForm.addEventListener('submit', (e) => {
    e.preventDefault();

    const ingrediente = document.getElementById('ing').value;
    const unidad = document.getElementById('uni').value;
    const precio = document.getElementById('pre').value;
    const cantidad = document.getElementById('cant').value;
    const fechaIngreso = document.getElementById('fecha-ingreso').value;
    const fechaVencimiento = document.getElementById('fecha-vencimiento').value;
    const idProveedor = document.getElementById('id-proveedor').value;
    const idUsuario = document.getElementById('id-usuario').value;

    if (!ingrediente || !unidad || !precio || !cantidad || !fechaIngreso || !fechaVencimiento || !idProveedor || !idUsuario) {
        alert("Por favor, completa todos los campos.");
        return;
    }

    if (editingRow) {
        // Si estamos editando, reemplazamos la fila existente
        editingRow.innerHTML = `
            <td>${ingrediente}</td>
            <td>${unidad}</td>
            <td>$${precio}</td>
            <td>${cantidad}</td>
            <td>${fechaIngreso}</td>
            <td>${fechaVencimiento}</td>
            <td>${idProveedor}</td>
            <td>${idUsuario}</td>
            <td>
                <button class="editar">Editar</button>
                <button class="eliminar">Eliminar</button>
            </td>
        `;
        editingRow = null; // Resetear
    } else {
        // Agregar nueva fila
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${ingrediente}</td>
            <td>${unidad}</td>
            <td>$${precio}</td>
            <td>${cantidad}</td>
            <td>${fechaIngreso}</td>
            <td>${fechaVencimiento}</td>
            <td>${idProveedor}</td>
            <td>${idUsuario}</td>
            <td>
                <button class="editar">Editar</button>
                <button class="eliminar">Eliminar</button>
            </td>
        `;
        tableBody.appendChild(row);
    }

    addItemForm.reset();
    addItemForm.style.display = 'none';
    overlay.style.display = 'none';
    toggleFormBtn.textContent = 'Mostrar Formulario';
});

// Manejo de edición y eliminación
tableBody.addEventListener('click', (e) => {
    if (e.target.classList.contains('editar')) {
        const row = e.target.closest('tr');
        editingRow = row;
        const cells = row.getElementsByTagName('td');

        document.getElementById('ing').value = cells[0].textContent;
        document.getElementById('uni').value = cells[1].textContent;
        document.getElementById('pre').value = cells[2].textContent.replace('$', '');
        document.getElementById('cant').value = cells[3].textContent;
        document.getElementById('fecha-ingreso').value = cells[4].textContent;
        document.getElementById('fecha-vencimiento').value = cells[5].textContent;
        document.getElementById('id-proveedor').value = cells[6].textContent;
        document.getElementById('id-usuario').value = cells[7].textContent;

        // Mostrar el formulario si está oculto
        if (addItemForm.style.display === 'none' || addItemForm.style.display === '') {
            addItemForm.style.display = 'block';
            overlay.style.display = 'block';
            toggleFormBtn.textContent = 'Ocultar Formulario';
        }
    } else if (e.target.classList.contains('eliminar')) {
        e.target.closest('tr').remove();
    }
});