// Paquete
package pryfinal.controlador;

// Imports
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import pryfinal.modelo.Factura;
import pryfinal.modelo.Usuario;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

// Clase DetalleFactura
public class DetalleFactura {
	// Variables
	/// FXML
	@FXML private Label lblTituloDetalleFactura;
	@FXML private TextField txtNumeroFactura;
	@FXML private TextField txtFechaEmisionDisplay;
	@FXML private DatePicker dateFechaEmisionEdit;
	@FXML private TextField txtCedulaCliente;
	@FXML private TextField txtNombreCliente;
	@FXML private TextArea areaDescripcion;
	@FXML private TextField txtSubtotal;
	@FXML private TextField txtIVA;
	@FXML private TextField txtTotal;
	@FXML private ComboBox<String> cmbMetodoPago;
	@FXML private HBox botonesAccionBoxFactura;
	@FXML private Button btnModificarFactura;
	@FXML private Button btnEliminarFactura;
	@FXML private HBox botonesEdicionBoxFactura;
	@FXML private Button btnGuardarCambiosFactura;
	@FXML private Button btnDescartarCambiosFactura;
	@FXML private Button btnCerrarDetalleFactura;

	/// Otros
	private Factura facturaSeleccionada;
	private Usuario usuarioActual;
	private ConsultaFactura consultaFacturaController;
	private boolean enModoEdicion = false;
	private ObjectMapper objectMapper;
	private final String RUTA_FACTURAS_JSON = "data/facturas.json";
	private final String ADMIN_USER_TYPE = "admin";
	private final DecimalFormat FORMATO_MONEDA = new DecimalFormat("#,##0.00");
	private final DateTimeFormatter FORMATO_FECHA_ISO = DateTimeFormatter.ISO_LOCAL_DATE;
	private final Pattern PATRON_NOMBRE_CLIENTE = Pattern.compile("^[\\p{L} .'-]+$");

	// Initialize (inicializar)
	@FXML
	public void initialize() {
		objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		configurarDatePickerEdit();
		txtTotal.setEditable(false);
		txtTotal.setFocusTraversable(false);
		actualizarVisibilidadBotonesEdicion();
	}

	// Controlador consulta
	public void setConsultaFacturaController(ConsultaFactura controller) {
		this.consultaFacturaController = controller;
	}

	// Date picker
	private void configurarDatePickerEdit() {
		dateFechaEmisionEdit.setConverter(new StringConverter<LocalDate>() {
			@Override public String toString(LocalDate date) { return (date != null) ? FORMATO_FECHA_ISO.format(date) : ""; }
			@Override public LocalDate fromString(String string) {
			if (string != null && !string.isEmpty()) {
				try { return LocalDate.parse(string, FORMATO_FECHA_ISO); }
				catch (DateTimeParseException e) { return null; }
			} else { return null; }
			}
		});
		txtSubtotal.textProperty().addListener((obs, oldV, newV) -> { if(enModoEdicion) calcularYActualizarTotalVisual(); });
		txtIVA.textProperty().addListener((obs, oldV, newV) -> { if(enModoEdicion) calcularYActualizarTotalVisual(); });
	}

	// Actualizar total
	private void calcularYActualizarTotalVisual() {
		try {
			String subtotalStr = txtSubtotal.getText().trim();
			String ivaPorcentajeStr = txtIVA.getText().trim();
			if (subtotalStr.isEmpty() || ivaPorcentajeStr.isEmpty()) {
				txtTotal.setText(""); return;
			}
			double subtotal = Double.parseDouble(subtotalStr);
			int ivaPorcentaje = Integer.parseInt(ivaPorcentajeStr);
			if (subtotal < 0 || ivaPorcentaje < 0 || ivaPorcentaje > 800) {
				txtTotal.setText(""); return;
			}
			double valorIva = subtotal * (ivaPorcentaje / 100.0);
			txtTotal.setText(FORMATO_MONEDA.format(subtotal + valorIva));
		} catch (NumberFormatException e) {
			txtTotal.setText("");
		}
	}

	// Cargar
	public void cargarDatos(Factura factura, Usuario usuarioLogueado) {
		this.facturaSeleccionada = factura;
		this.usuarioActual = usuarioLogueado;

		if (factura != null) {
			txtNumeroFactura.setText(factura.getFactura());
			txtFechaEmisionDisplay.setText(factura.getFecha());
			try { dateFechaEmisionEdit.setValue(LocalDate.parse(factura.getFecha(), FORMATO_FECHA_ISO)); }
			catch (Exception e) { dateFechaEmisionEdit.setValue(null); }
			txtCedulaCliente.setText(String.valueOf(factura.getCedula()));
			txtNombreCliente.setText(factura.getNombre());
			areaDescripcion.setText(factura.getDescripcion());
			txtSubtotal.setText(FORMATO_MONEDA.format(factura.getSubtotal()));
			txtIVA.setText(String.valueOf(factura.getIva()));
			txtTotal.setText(FORMATO_MONEDA.format(factura.getTotal()));
			cmbMetodoPago.setValue(factura.getMetodo());
		}
		configurarSegunRolUsuario();
		salirModoEdicion();
	}

	// Rol de usuario
	private void configurarSegunRolUsuario() {
		boolean esAdmin = usuarioActual != null && ADMIN_USER_TYPE.equals(usuarioActual.getTipo());
		btnModificarFactura.setDisable(!esAdmin);
		btnEliminarFactura.setDisable(!esAdmin);
	}

	// Modo edicion
	private void entrarModoEdicion() {
		enModoEdicion = true;
		lblTituloDetalleFactura.setText("Modificar Factura");
		txtFechaEmisionDisplay.setVisible(false); txtFechaEmisionDisplay.setManaged(false);
		dateFechaEmisionEdit.setVisible(true); dateFechaEmisionEdit.setManaged(true);

		txtNombreCliente.setEditable(true);
		areaDescripcion.setEditable(true);
		txtSubtotal.setEditable(true);
		txtIVA.setEditable(true);
		cmbMetodoPago.setDisable(false);

		try { txtSubtotal.setText(String.valueOf(facturaSeleccionada.getSubtotal())); } catch (Exception e) { txtSubtotal.setText("0.0");}
		try { txtIVA.setText(String.valueOf(facturaSeleccionada.getIva())); } catch (Exception e) { txtIVA.setText("0");}

		actualizarVisibilidadBotonesEdicion();
	}

	// Salir de modo edicion
	private void salirModoEdicion() {
		enModoEdicion = false;
		lblTituloDetalleFactura.setText("Detalle de Factura");
		txtFechaEmisionDisplay.setVisible(true); txtFechaEmisionDisplay.setManaged(true);
		dateFechaEmisionEdit.setVisible(false); dateFechaEmisionEdit.setManaged(false);

		txtNombreCliente.setEditable(false);
		areaDescripcion.setEditable(false);
		txtSubtotal.setEditable(false);
		txtIVA.setEditable(false);
		cmbMetodoPago.setDisable(true);

		if (facturaSeleccionada != null) {
			cargarValoresOriginales();
		}
		actualizarVisibilidadBotonesEdicion();
	}

	// Cargar valores originales
	private void cargarValoresOriginales() {
		txtFechaEmisionDisplay.setText(facturaSeleccionada.getFecha());
		try { dateFechaEmisionEdit.setValue(LocalDate.parse(facturaSeleccionada.getFecha(), FORMATO_FECHA_ISO)); }
		catch (Exception e) { dateFechaEmisionEdit.setValue(null); }
		txtNombreCliente.setText(facturaSeleccionada.getNombre());
		areaDescripcion.setText(facturaSeleccionada.getDescripcion());
		txtSubtotal.setText(FORMATO_MONEDA.format(facturaSeleccionada.getSubtotal()));
		txtIVA.setText(String.valueOf(facturaSeleccionada.getIva()));
		txtTotal.setText(FORMATO_MONEDA.format(facturaSeleccionada.getTotal()));
		cmbMetodoPago.setValue(facturaSeleccionada.getMetodo());
	}

	// Visibilidad de botones
	private void actualizarVisibilidadBotonesEdicion() {
		botonesAccionBoxFactura.setVisible(!enModoEdicion);
		botonesAccionBoxFactura.setManaged(!enModoEdicion);
		botonesEdicionBoxFactura.setVisible(enModoEdicion);
		botonesEdicionBoxFactura.setManaged(enModoEdicion);
		btnCerrarDetalleFactura.setVisible(!enModoEdicion);
	}

	// Modificar
	@FXML private void handleModificarFactura(ActionEvent event) { entrarModoEdicion(); }

	// Descartar
	@FXML private void handleDescartarCambiosFactura(ActionEvent event) { salirModoEdicion(); }

	// Guardar cambios
	@FXML
	private void handleGuardarCambiosFactura(ActionEvent event) {
		if (facturaSeleccionada == null) return;

		List<String> errores = validarCamposParaModificacion();
		if (!errores.isEmpty()) {
			mostrarAlertaValidacion("Errores de Validación", errores);
			return;
		}

		List<Factura> facturas = cargarFacturasDesdeJson();
		int indiceFactura = -1;
		for (int i = 0; i < facturas.size(); i++) {
			if (facturas.get(i).getFactura().equals(facturaSeleccionada.getFactura())) {
				indiceFactura = i;
				break;
			}
		}

		if (indiceFactura != -1) {
			Factura facturaParaActualizar = facturas.get(indiceFactura);

			facturaParaActualizar.setFecha(FORMATO_FECHA_ISO.format(dateFechaEmisionEdit.getValue()));
			facturaParaActualizar.setNombre(txtNombreCliente.getText().trim());
			facturaParaActualizar.setDescripcion(areaDescripcion.getText().trim());
			double subtotalNuevo = Double.parseDouble(txtSubtotal.getText().trim());
			int ivaNuevo = Integer.parseInt(txtIVA.getText().trim());
			facturaParaActualizar.setSubtotal(subtotalNuevo);
			facturaParaActualizar.setIva(ivaNuevo);
			facturaParaActualizar.setTotal(subtotalNuevo * (1 + (ivaNuevo / 100.0)));
			facturaParaActualizar.setMetodo(cmbMetodoPago.getValue());

			if (guardarFacturasEnJson(facturas)) {
				mostrarAlerta("Éxito", "Factura actualizada correctamente.");
				this.facturaSeleccionada = facturaParaActualizar;
				if (consultaFacturaController != null) {
					consultaFacturaController.refrescarListaFacturas();
				}
			} else {
				mostrarAlertaError("Error", "No se pudo guardar los cambios de la factura.");
			}
		} else {
			mostrarAlertaError("Error", "No se encontró la factura para actualizar.");
		}
		salirModoEdicion();
	}

	// Eliminar
	@FXML
	private void handleEliminarFactura(ActionEvent event) {
		if (facturaSeleccionada == null) return;

		Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
		confirmacion.setTitle("Confirmar Eliminación");
		confirmacion.setHeaderText("Eliminar Factura N°: " + facturaSeleccionada.getFactura());
		confirmacion.setContentText("¿Está seguro? Esta acción no se puede deshacer.");

		Optional<ButtonType> resultado = confirmacion.showAndWait();
		if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
			List<Factura> facturas = cargarFacturasDesdeJson();
			boolean eliminada = facturas.removeIf(f -> f.getFactura().equals(facturaSeleccionada.getFactura()));

			if (eliminada) {
				if (guardarFacturasEnJson(facturas)) {
					mostrarAlerta("Éxito", "Factura eliminada correctamente.");
					if (consultaFacturaController != null) {
						consultaFacturaController.refrescarListaFacturas();
					}
					handleCerrar(null);
				} else {
					mostrarAlertaError("Error", "No se pudo eliminar la factura del archivo.");
				}
			} else {
				mostrarAlertaError("Error", "No se encontró la factura para eliminar.");
			}
		}
	}

	// Validacion
	private List<String> validarCamposParaModificacion() {
		List<String> errores = new ArrayList<>();
		if (dateFechaEmisionEdit.getValue() == null) errores.add("- Fecha de emisión no puede estar vacía.");
		String fechaEditor = dateFechaEmisionEdit.getEditor().getText();
		if (!fechaEditor.isEmpty()) {
			try { LocalDate.parse(fechaEditor, FORMATO_FECHA_ISO); }
			catch (DateTimeParseException e) { errores.add("- Fecha de emisión tiene un formato inválido. Use AAAA-MM-DD."); }
		} else if (dateFechaEmisionEdit.getValue() == null) {
			errores.add("- Fecha de emisión no puede estar vacía.");
		}

		String nombreCliente = txtNombreCliente.getText().trim();
		if (nombreCliente.isEmpty()) errores.add("- Nombre del cliente no puede estar vacío.");
		else if (nombreCliente.length() < 3 || nombreCliente.length() >= 40) errores.add("- Nombre del cliente debe tener entre 3 y 39 caracteres.");
		else if (!PATRON_NOMBRE_CLIENTE.matcher(nombreCliente).matches()) errores.add("- Nombre del cliente solo debe contener letras y caracteres permitidos.");

		String descripcion = areaDescripcion.getText().trim();
		if (descripcion.isEmpty()) errores.add("- Descripción no puede estar vacía.");
		else if (descripcion.length() < 3 || descripcion.length() > 800) errores.add("- Descripción debe tener entre 3 y 800 caracteres.");

		String subtotalStr = txtSubtotal.getText().trim();
		if (subtotalStr.isEmpty()) errores.add("- Subtotal no puede estar vacío.");
		else {
			try {
				double subtotal = Double.parseDouble(subtotalStr);
				if (subtotal < 0) errores.add("- Subtotal debe ser un número positivo o cero.");
			} catch (NumberFormatException e) { errores.add("- Subtotal debe ser un número válido."); }
		}

		String ivaStr = txtIVA.getText().trim();
		if (ivaStr.isEmpty()) errores.add("- IVA (%) no puede estar vacío.");
		else {
			try {
				int iva = Integer.parseInt(ivaStr);
				if (iva < 0 || iva > 800) errores.add("- IVA (%) debe ser un número entero entre 0 y 800.");
			} catch (NumberFormatException e) { errores.add("- IVA (%) debe ser un número entero válido."); }
		}

		if (cmbMetodoPago.getValue() == null || cmbMetodoPago.getValue().isEmpty()) errores.add("- Debe seleccionar un método de pago.");
		return errores;
	}

	// Cargar
	private List<Factura> cargarFacturasDesdeJson() {
		File archivo = new File(RUTA_FACTURAS_JSON);
		if (archivo.exists() && archivo.length() > 0) {
			try { return objectMapper.readValue(archivo, new TypeReference<List<Factura>>() {}); }
			catch (IOException e) { System.err.println("Error al leer facturas: " + e.getMessage()); }
		}
		return new ArrayList<>();
	}

	// Guardar
	private boolean guardarFacturasEnJson(List<Factura> facturas) {
		try { objectMapper.writeValue(new File(RUTA_FACTURAS_JSON), facturas); return true; }
		catch (IOException e) { System.err.println("Error al guardar facturas: " + e.getMessage()); return false; }
	}

	// Cerrar
	@FXML
	private void handleCerrar(ActionEvent event) {
		Stage stage = (Stage) btnCerrarDetalleFactura.getScene().getWindow();
		stage.close();
	}

	// Alerta
	private void mostrarAlerta(String titulo, String mensaje) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(titulo); alert.setHeaderText(null); alert.setContentText(mensaje);
		alert.showAndWait();
	}

	/// Error
	private void mostrarAlertaError(String titulo, String mensaje) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(titulo); alert.setHeaderText(null); alert.setContentText(mensaje);
		alert.showAndWait();
	}

	/// Validacion
	private void mostrarAlertaValidacion(String titulo, List<String> mensajes) {
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle(titulo);
		alert.setHeaderText("Por favor corrija los siguientes errores:");
		alert.setContentText(String.join("\n", mensajes));
		alert.showAndWait();
	}
}
