// Paquete
package pryfinal.controlador;

// Imports

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import pryfinal.modelo.Factura;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

// Clase RegistroFactura
public class RegistroFactura {
	// Variables
	/// FXML
	@FXML private TextField txtNumeroFactura;
	@FXML private DatePicker dateFechaEmision;
	@FXML private TextField txtCedulaClienteFactura;
	@FXML private TextField txtNombreClienteFactura;
	@FXML private TextArea areaDescripcionFactura;
	@FXML private TextField txtSubtotalFactura;
	@FXML private TextField txtIVAFactura;
	@FXML private TextField txtTotalFactura;
	@FXML private ComboBox<String> cmbMetodoPago;
	@FXML private Button btnRegistrarFactura;

	/// Patrones
	private final Pattern PATRON_NUMERO_FACTURA = Pattern.compile("^[a-zA-Z0-9-]{4,12}$");
	private final Pattern PATRON_NOMBRE_CLIENTE = Pattern.compile("^[\\p{L} .'-]+$");
	private final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ISO_LOCAL_DATE;
	private final DecimalFormat FORMATO_MONEDA = new DecimalFormat("#,##0.00");

	/// Otros
	private ObjectMapper objectMapper;
	private final String RUTA_FACTURAS_JSON = "data/facturas.json";
	private final String RUTA_DIRECTORIO_DATOS = "data";

	// Inicializar
	@FXML
	public void initialize() {
		objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

		File directorioDatos = new File(RUTA_DIRECTORIO_DATOS);
		if (!directorioDatos.exists()) {
			directorioDatos.mkdirs();
		}

		configurarDatePicker();
		configurarCalculoTotalDinamico();

		// Deshabilitar el campo Total
		txtTotalFactura.setEditable(false);
		txtTotalFactura.setFocusTraversable(false);
	}

	// Date picker
	private void configurarDatePicker() {
		// Fecha actual por defecto
		dateFechaEmision.setValue(LocalDate.now());

		// "Formateador" para la fecha en el DatePicker
		dateFechaEmision.setConverter(new StringConverter<LocalDate>() {
			@Override
			public String toString(LocalDate date) {
				if (date != null) {
					return FORMATO_FECHA.format(date);
				} else {
					return "";
				}
			}

			@Override
			public LocalDate fromString(String string) {
				if (string != null && !string.isEmpty()) {
					try {
						return LocalDate.parse(string, FORMATO_FECHA);
					} catch (DateTimeParseException e) {
						System.err.println("Formato de fecha inválido ingresado manualmente: " + string);
						return dateFechaEmision.getValue(); // Mantener valor anterior o null si es el primero
					}
				} else {
					return null;
				}
			}
		});
	}

	// Total dinamico
	private void configurarCalculoTotalDinamico() {
		// Listener para subtotal
		txtSubtotalFactura.textProperty().addListener((obs, oldVal, newVal) -> calcularYActualizarTotal());
		// Listener para IVA
		txtIVAFactura.textProperty().addListener((obs, oldVal, newVal) -> calcularYActualizarTotal());
	}

	// Actualizar total
	private void calcularYActualizarTotal() {
		try {
			String subtotalStr = txtSubtotalFactura.getText().trim();
			String ivaPorcentajeStr = txtIVAFactura.getText().trim();

			if (subtotalStr.isEmpty() || ivaPorcentajeStr.isEmpty()) {
				txtTotalFactura.setText(""); // Si alguno está vacío, el total también
				return;
			}

			double subtotal = Double.parseDouble(subtotalStr);
			int ivaPorcentaje = Integer.parseInt(ivaPorcentajeStr);

			// Validar que el subtotal sea positivo y el IVA esté en rango
			if (subtotal < 0 || ivaPorcentaje < 0 || ivaPorcentaje > 800) {
				txtTotalFactura.setText("");
				return;
			}

			double valorIva = subtotal * (ivaPorcentaje / 100.0);
			double totalCalculado = subtotal + valorIva;

			txtTotalFactura.setText(FORMATO_MONEDA.format(totalCalculado));

		} catch (NumberFormatException e) {
			txtTotalFactura.setText("");
		}
	}

	// Registar factura
	@FXML
	private void handleRegistrarFactura(ActionEvent event) {
		List<String> errores = validarCampos();

		if (!errores.isEmpty()) {
			mostrarAlertaValidacion("Errores de Validación", errores);
			return;
		}

		// Si la validación es exitosa
		String numeroFactura = txtNumeroFactura.getText().trim();
		String fechaEmision = FORMATO_FECHA.format(dateFechaEmision.getValue());
		long cedulaCliente = Long.parseLong(txtCedulaClienteFactura.getText().trim());
		String nombreCliente = txtNombreClienteFactura.getText().trim();
		String descripcion = areaDescripcionFactura.getText().trim();
		double subtotal = Double.parseDouble(txtSubtotalFactura.getText().trim());
		int iva = Integer.parseInt(txtIVAFactura.getText().trim());
		String metodoPago = cmbMetodoPago.getValue();

		// Calcular el total (aunque ya se muestre dinámicamente)
		double valorIvaCalculado = subtotal * (iva / 100.0);
		double totalFinal = subtotal + valorIvaCalculado;

		Factura nuevaFactura = new Factura(numeroFactura, fechaEmision, cedulaCliente, nombreCliente,
				descripcion, subtotal, iva, totalFinal, metodoPago);

		List<Factura> facturas = cargarFacturas();

		// Verificar si ya existe una factura con el mismo número
		boolean facturaYaExiste = facturas.stream().anyMatch(f -> f.getFactura().equalsIgnoreCase(numeroFactura));
		if (facturaYaExiste) {
			mostrarAlertaError("Registro Duplicado", "Ya existe una factura registrada con el número: " + numeroFactura);
			return;
		}

		facturas.add(nuevaFactura);

		if (guardarFacturas(facturas)) {
			mostrarAlertaInformacion("Registro Exitoso", "Factura N°" + numeroFactura + " registrada correctamente.");
			limpiarCampos();
		} else {
			mostrarAlertaError("Error de Registro", "No se pudo guardar la factura. Intente de nuevo.");
		}
	}

	// Validacion
	private List<String> validarCampos() {
		List<String> errores = new ArrayList<>();

		// Número de Factura
		String numFactura = txtNumeroFactura.getText().trim();
		if (numFactura.isEmpty()) {
			errores.add("- Número de factura no puede estar vacío.");
		} else if (!PATRON_NUMERO_FACTURA.matcher(numFactura).matches()) {
			errores.add("- Número de factura debe tener entre 4 y 12 caracteres (letras, números, guión '-').");
		}

		// Fecha de Emisión
		if (dateFechaEmision.getValue() == null) { errores.add("- Fecha de emisión no puede estar vacía."); }

		/// Validación explícita del formato de fecha
		String fechaEditor = dateFechaEmision.getEditor().getText();
		if (fechaEditor.isEmpty() && dateFechaEmision.getValue() == null) {
			errores.add("- Fecha de emisión no puede estar vacía.");
		} else if (!fechaEditor.isEmpty()) {
			try {
				LocalDate.parse(fechaEditor, FORMATO_FECHA);
			} catch (DateTimeParseException e) {
				errores.add("- Fecha de emisión tiene un formato inválido. Use AAAA-MM-DD.");
			}
		}

		// Cédula del Cliente
		String cedulaCliente = txtCedulaClienteFactura.getText().trim();
		if (cedulaCliente.isEmpty()) {
			errores.add("- Cédula del cliente no puede estar vacía.");
		} else if (!cedulaCliente.matches("\\d+")) {
			errores.add("- Cédula del cliente debe contener solo números.");
		} else if (cedulaCliente.length() < 5 || cedulaCliente.length() > 13) {
			errores.add("- Cédula del cliente debe tener entre 5 y 13 dígitos.");
		}

		// Nombre del Cliente
		String nombreCliente = txtNombreClienteFactura.getText().trim();
		if (nombreCliente.isEmpty()) {
			errores.add("- Nombre del cliente no puede estar vacío.");
		} else if (nombreCliente.length() < 3 || nombreCliente.length() >= 40) {
			errores.add("- Nombre del cliente debe tener entre 3 y 39 caracteres.");
		} else if (!PATRON_NOMBRE_CLIENTE.matcher(nombreCliente).matches()) {
			errores.add("- Nombre del cliente solo debe contener letras y caracteres permitidos.");
		}

		// Descripción
		String descripcion = areaDescripcionFactura.getText().trim();
		if (descripcion.isEmpty()) {
			errores.add("- Descripción no puede estar vacía.");
		} else if (descripcion.length() < 3 || descripcion.length() > 800) {
			errores.add("- Descripción debe tener entre 3 y 800 caracteres.");
		}

		// Subtotal
		String subtotalStr = txtSubtotalFactura.getText().trim();
		if (subtotalStr.isEmpty()) {
			errores.add("- Subtotal no puede estar vacío.");
		} else {
			try {
				double subtotal = Double.parseDouble(subtotalStr);
				if (subtotal < 0) {
					errores.add("- Subtotal debe ser un número positivo o cero.");
				}
			} catch (NumberFormatException e) {
				errores.add("- Subtotal debe ser un número válido.");
			}
		}

		// IVA
		String ivaStr = txtIVAFactura.getText().trim();
		if (ivaStr.isEmpty()) {
			errores.add("- IVA (%) no puede estar vacío.");
		} else {
			try {
				int iva = Integer.parseInt(ivaStr);
				if (iva < 0 || iva > 800) {
					errores.add("- IVA (%) debe ser un número entero entre 0 y 800.");
				}
			} catch (NumberFormatException e) {
				errores.add("- IVA (%) debe ser un número entero válido.");
			}
		}

		// Método de Pago
		if (cmbMetodoPago.getValue() == null || cmbMetodoPago.getValue().isEmpty()) {
			errores.add("- Debe seleccionar un método de pago.");
		}

		return errores;
	}

	// Cargar
	private List<Factura> cargarFacturas() {
		File archivo = new File(RUTA_FACTURAS_JSON);
		if (archivo.exists() && archivo.length() > 0) {
			try {
				return objectMapper.readValue(archivo, new TypeReference<List<Factura>>() {});
			} catch (IOException e) {
				System.err.println("Error al leer el archivo de facturas: " + e.getMessage());
			}
		}
		return new ArrayList<>();
	}

	// Guardar
	private boolean guardarFacturas(List<Factura> facturas) {
		try {
			objectMapper.writeValue(new File(RUTA_FACTURAS_JSON), facturas);
			return true;
		} catch (IOException e) {
			System.err.println("Error al guardar el archivo de facturas: " + e.getMessage());
			return false;
		}
	}

	// Limpiar campos
	private void limpiarCampos() {
		txtNumeroFactura.clear();
		dateFechaEmision.setValue(LocalDate.now());
		txtCedulaClienteFactura.clear();
		txtNombreClienteFactura.clear();
		areaDescripcionFactura.clear();
		txtSubtotalFactura.clear();
		txtIVAFactura.clear();
		cmbMetodoPago.getSelectionModel().clearSelection();
		txtNumeroFactura.requestFocus();
	}

	// Mostrar alerta
	/// Validacion
	private void mostrarAlertaValidacion(String titulo, List<String> mensajes) {
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle(titulo);
		alert.setHeaderText("Por favor corrija los siguientes errores:");
		StringBuilder contenido = new StringBuilder();
		for (String mensaje : mensajes) {
			contenido.append(mensaje).append("\n");
		}
		alert.setContentText(contenido.toString());
		alert.showAndWait();
	}

	/// Infomacion
	private void mostrarAlertaInformacion(String titulo, String mensaje) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(titulo);
		alert.setHeaderText(null);
		alert.setContentText(mensaje);
		alert.showAndWait();
	}

	/// Error
	private void mostrarAlertaError(String titulo, String mensaje) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(titulo);
		alert.setHeaderText(null);
		alert.setContentText(mensaje);
		alert.showAndWait();
	}
}
