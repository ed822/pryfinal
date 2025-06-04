// Paquete
package pryfinal.controlador;

// Imports JavaFX

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import pryfinal.modelo.OrdenMedica;
import pryfinal.modelo.Persona;
import pryfinal.modelo.Usuario;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// Clase DetalleOrdenMedica
public class DetalleOrdenMedica {

	@FXML private Label lblTituloDetalleOrden;
	@FXML private TextField txtNumeroOrden; // No editable
	@FXML private TextField txtFechaEmisionDisplay;
	@FXML private DatePicker dateFechaEmisionEdit;
	@FXML private TextField txtCedulaDueno; // No editable
	@FXML private TextField txtNombreMascota;
	@FXML private ComboBox<String> cmbVeterinarioPrescribe;
	@FXML private TextField txtDuracionTratamiento;
	@FXML private TextArea areaMedicamentosDosis;
	@FXML private TextArea areaInstruccionesAdmin;
	@FXML private TextArea areaNotasAdicionales;

	@FXML private HBox botonesAccionBoxOrden;
	@FXML private Button btnModificarOrden;
	@FXML private Button btnEliminarOrden;
	@FXML private HBox botonesEdicionBoxOrden;
	@FXML private Button btnGuardarCambiosOrden;
	@FXML private Button btnDescartarCambiosOrden;
	@FXML private Button btnCerrarDetalleOrden;

	private OrdenMedica ordenSeleccionada;
	private Usuario usuarioActual;
	private ConsultaOrdenMedica consultaOrdenMedicaController;
	private boolean enModoEdicion = false;

	private ObjectMapper objectMapper;
	private final String RUTA_ORDENES_JSON = "data/ordenes_medicas.json";
	private final String RUTA_PERSONAS_JSON = "data/personas.json";
	private final String ADMIN_USER_TYPE = "admin";
	private final DateTimeFormatter FORMATO_FECHA_ISO = DateTimeFormatter.ISO_LOCAL_DATE;
	private final Pattern PATRON_NOMBRE_MASCOTA = Pattern.compile("^[\\p{L}0-9 .'-]+$");
	private ObservableList<String> listaNombresVeterinarios = FXCollections.observableArrayList();

	@FXML
	public void initialize() {
		objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		configurarDatePickerEdit();
		actualizarVisibilidadBotonesEdicion();
	}

	public void setConsultaOrdenMedicaController(ConsultaOrdenMedica controller) {
		this.consultaOrdenMedicaController = controller;
	}

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
	}

	private void cargarVeterinariosParaEdicion() {
		listaNombresVeterinarios.clear();
		File archivoPersonas = new File(RUTA_PERSONAS_JSON);
		if (archivoPersonas.exists() && archivoPersonas.length() > 0) {
			try {
				List<Persona> todasLasPersonas = objectMapper.readValue(archivoPersonas, new TypeReference<List<Persona>>() {});
				List<String> veterinarios = todasLasPersonas.stream()
					.filter(p -> "Veterinario".equalsIgnoreCase(p.getTipo()))
					.map(p -> p.getNombre() + " " + p.getApellido())
					.collect(Collectors.toList());
				listaNombresVeterinarios.addAll(veterinarios);
			} catch (IOException e) { mostrarAlertaError("Error Interno", "No se pudieron cargar los veterinarios."); }
		}
		cmbVeterinarioPrescribe.setItems(listaNombresVeterinarios);
		if(ordenSeleccionada != null) cmbVeterinarioPrescribe.setValue(ordenSeleccionada.getVeterinario());
	}

	public void cargarDatos(OrdenMedica orden, Usuario usuarioLogueado) {
		this.ordenSeleccionada = orden;
		this.usuarioActual = usuarioLogueado;

		if (orden != null) {
			txtNumeroOrden.setText(orden.getNumero());
			txtFechaEmisionDisplay.setText(orden.getFecha());
			try { dateFechaEmisionEdit.setValue(LocalDate.parse(orden.getFecha(), FORMATO_FECHA_ISO)); }
			catch (Exception e) { dateFechaEmisionEdit.setValue(null); }
			txtCedulaDueno.setText(String.valueOf(orden.getCedula()));
			txtNombreMascota.setText(orden.getNombre());

			if (orden.getVeterinario() != null && !orden.getVeterinario().isEmpty()) {
				cmbVeterinarioPrescribe.setItems(FXCollections.observableArrayList(orden.getVeterinario()));
				cmbVeterinarioPrescribe.setValue(orden.getVeterinario());
			} else {
				cmbVeterinarioPrescribe.setItems(FXCollections.observableArrayList());
				cmbVeterinarioPrescribe.setPromptText("No asignado");
			}

			txtDuracionTratamiento.setText(orden.getDuracion());
			areaMedicamentosDosis.setText(orden.getDosis());
			areaInstruccionesAdmin.setText(orden.getIntrucciones());
			areaNotasAdicionales.setText(orden.getNotas());
		}
		configurarSegunRolUsuario();
		salirModoEdicion();
	}

	private void configurarSegunRolUsuario() {
		boolean esAdmin = usuarioActual != null && ADMIN_USER_TYPE.equals(usuarioActual.getTipo());
		btnModificarOrden.setDisable(!esAdmin);
		btnEliminarOrden.setDisable(!esAdmin);
	}

	private void entrarModoEdicion() {
		enModoEdicion = true;
		lblTituloDetalleOrden.setText("Modificar Orden Médica");
		// Campos no editables: txtNumeroOrden, txtCedulaDueno
		txtFechaEmisionDisplay.setVisible(false); txtFechaEmisionDisplay.setManaged(false);
		dateFechaEmisionEdit.setVisible(true); dateFechaEmisionEdit.setManaged(true);

		txtNombreMascota.setEditable(true);
		cargarVeterinariosParaEdicion();
		cmbVeterinarioPrescribe.setDisable(false);
		if(ordenSeleccionada != null) cmbVeterinarioPrescribe.setValue(ordenSeleccionada.getVeterinario());

		txtDuracionTratamiento.setEditable(true);
		areaMedicamentosDosis.setEditable(true);
		areaInstruccionesAdmin.setEditable(true);
		areaNotasAdicionales.setEditable(true);
		actualizarVisibilidadBotonesEdicion();
	}

	private void salirModoEdicion() {
		enModoEdicion = false;
		lblTituloDetalleOrden.setText("Detalle de Orden Médica");
		txtFechaEmisionDisplay.setVisible(true); txtFechaEmisionDisplay.setManaged(true);
		dateFechaEmisionEdit.setVisible(false); dateFechaEmisionEdit.setManaged(false);

		txtNombreMascota.setEditable(false);
		cmbVeterinarioPrescribe.setDisable(true);
		txtDuracionTratamiento.setEditable(false);
		areaMedicamentosDosis.setEditable(false);
		areaInstruccionesAdmin.setEditable(false);
		areaNotasAdicionales.setEditable(false);

		if (ordenSeleccionada != null) {
			cargarValoresOriginales();
		}
		actualizarVisibilidadBotonesEdicion();
	}

	private void cargarValoresOriginales() {
		txtFechaEmisionDisplay.setText(ordenSeleccionada.getFecha());
		try { dateFechaEmisionEdit.setValue(LocalDate.parse(ordenSeleccionada.getFecha(), FORMATO_FECHA_ISO)); }
		catch (Exception e) { dateFechaEmisionEdit.setValue(null); }
		txtNombreMascota.setText(ordenSeleccionada.getNombre());

		if (ordenSeleccionada.getVeterinario() != null && !ordenSeleccionada.getVeterinario().isEmpty()) {
			cmbVeterinarioPrescribe.setItems(FXCollections.observableArrayList(ordenSeleccionada.getVeterinario()));
			cmbVeterinarioPrescribe.setValue(ordenSeleccionada.getVeterinario());
		} else {
			cmbVeterinarioPrescribe.setItems(FXCollections.observableArrayList());
			cmbVeterinarioPrescribe.setPromptText("No asignado");
		}

		txtDuracionTratamiento.setText(ordenSeleccionada.getDuracion());
		areaMedicamentosDosis.setText(ordenSeleccionada.getDosis());
		areaInstruccionesAdmin.setText(ordenSeleccionada.getIntrucciones());
		areaNotasAdicionales.setText(ordenSeleccionada.getNotas());
	}

	private void actualizarVisibilidadBotonesEdicion() {
		botonesAccionBoxOrden.setVisible(!enModoEdicion);
		botonesAccionBoxOrden.setManaged(!enModoEdicion);
		botonesEdicionBoxOrden.setVisible(enModoEdicion);
		botonesEdicionBoxOrden.setManaged(enModoEdicion);
		btnCerrarDetalleOrden.setVisible(!enModoEdicion);
	}

	@FXML private void handleModificarOrden(ActionEvent event) { entrarModoEdicion(); }
	@FXML private void handleDescartarCambiosOrden(ActionEvent event) { salirModoEdicion(); }

	@FXML
	private void handleGuardarCambiosOrden(ActionEvent event) {
		if (ordenSeleccionada == null) return;

		List<String> errores = validarCamposParaModificacion();
		if (!errores.isEmpty()) {
			mostrarAlertaValidacion("Errores de Validación", errores);
			return;
		}

		List<OrdenMedica> ordenes = cargarOrdenesMedicasDesdeJson();
		int indiceOrden = -1;
		for (int i = 0; i < ordenes.size(); i++) {
			// Número de orden es la clave no editable
			if (ordenes.get(i).getNumero().equals(ordenSeleccionada.getNumero())) {
				indiceOrden = i;
				break;
			}
		}

		if (indiceOrden != -1) {
			OrdenMedica ordenParaActualizar = ordenes.get(indiceOrden);

			// Cédula dueño no se modifica
			ordenParaActualizar.setFecha(FORMATO_FECHA_ISO.format(dateFechaEmisionEdit.getValue()));
			ordenParaActualizar.setNombre(txtNombreMascota.getText().trim());
			ordenParaActualizar.setVeterinario(cmbVeterinarioPrescribe.getValue());
			ordenParaActualizar.setDosis(areaMedicamentosDosis.getText().trim());
			ordenParaActualizar.setIntrucciones(areaInstruccionesAdmin.getText().trim());
			ordenParaActualizar.setDuracion(txtDuracionTratamiento.getText().trim());
			ordenParaActualizar.setNotas(areaNotasAdicionales.getText().trim());

			if (guardarOrdenesMedicasEnJson(ordenes)) {
				mostrarAlerta("Éxito", "Orden Médica actualizada correctamente.");
				this.ordenSeleccionada = ordenParaActualizar;
				if (consultaOrdenMedicaController != null) {
					consultaOrdenMedicaController.refrescarListaOrdenesMedicas();
				}
			} else {
				mostrarAlertaError("Error", "No se pudo guardar los cambios de la orden médica.");
			}
		} else {
			mostrarAlertaError("Error", "No se encontró la orden médica para actualizar.");
		}
		salirModoEdicion();
	}

	@FXML
	private void handleEliminarOrden(ActionEvent event) {
		if (ordenSeleccionada == null) return;

		Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
		confirmacion.setTitle("Confirmar Eliminación");
		confirmacion.setHeaderText("Eliminar Orden Médica N°: " + ordenSeleccionada.getNumero());
		confirmacion.setContentText("¿Está seguro? Esta acción no se puede deshacer.");

		Optional<ButtonType> resultado = confirmacion.showAndWait();
		if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
			List<OrdenMedica> ordenes = cargarOrdenesMedicasDesdeJson();
			boolean eliminada = ordenes.removeIf(o -> o.getNumero().equals(ordenSeleccionada.getNumero()));

			if (eliminada) {
				if (guardarOrdenesMedicasEnJson(ordenes)) {
					mostrarAlerta("Éxito", "Orden Médica eliminada correctamente.");
					if (consultaOrdenMedicaController != null) {
						consultaOrdenMedicaController.refrescarListaOrdenesMedicas();
					}
					handleCerrar(null);
				} else {
					mostrarAlertaError("Error", "No se pudo eliminar la orden médica del archivo.");
				}
			} else {
				mostrarAlertaError("Error", "No se encontró la orden médica para eliminar.");
			}
		}
	}

	private List<String> validarCamposParaModificacion() {
		List<String> errores = new ArrayList<>();
		// NumeroOrden y CedulaDueno no se validan para modificación

		if (dateFechaEmisionEdit.getValue() == null) errores.add("- Fecha de emisión no puede estar vacía.");
		String fechaEditor = dateFechaEmisionEdit.getEditor().getText();
		if (!fechaEditor.isEmpty()) {
			try { LocalDate.parse(fechaEditor, FORMATO_FECHA_ISO); }
			catch (DateTimeParseException e) { errores.add("- Fecha de emisión: formato AAAA-MM-DD inválido."); }
		} else if(dateFechaEmisionEdit.getValue() == null) {
			errores.add("- Fecha de emisión no puede estar vacía.");
		}

		String nombreMascota = txtNombreMascota.getText().trim();
		if (nombreMascota.isEmpty()) errores.add("- Nombre de mascota vacío.");
		else if (nombreMascota.length() < 3 || nombreMascota.length() >= 40) errores.add("- Nombre mascota: 3-39 chars.");
		else if (!PATRON_NOMBRE_MASCOTA.matcher(nombreMascota).matches()) errores.add("- Nombre mascota: letras, números y '.- permitidos.");

		if (cmbVeterinarioPrescribe.getValue() == null || cmbVeterinarioPrescribe.getValue().isEmpty()) {
			errores.add("- Debe seleccionar un veterinario.");
		}

		String dosis = areaMedicamentosDosis.getText().trim();
		if (dosis.isEmpty()) errores.add("- Medicamento(s) y Dosis vacío.");
		else if (dosis.length() < 3 || dosis.length() > 975) errores.add("- Dosis: 3-975 chars.");

		String instrucciones = areaInstruccionesAdmin.getText().trim();
		if (instrucciones.isEmpty()) errores.add("- Instrucciones de adm. vacío.");
		else if (instrucciones.length() < 3 || instrucciones.length() > 975) errores.add("- Instrucciones: 3-975 chars.");

		String duracion = txtDuracionTratamiento.getText().trim();
		if (duracion.isEmpty()) errores.add("- Duración del tratamiento vacío.");
		else if (duracion.length() < 3 || duracion.length() > 975) errores.add("- Duración: 3-975 chars.");

		String notas = areaNotasAdicionales.getText().trim();
		if (!notas.isEmpty() && notas.length() > 975) {
			errores.add("- Notas adicionales no debe exceder los 975 caracteres.");
		}
		return errores;
	}

	private List<OrdenMedica> cargarOrdenesMedicasDesdeJson() {
		File archivo = new File(RUTA_ORDENES_JSON);
		if (archivo.exists() && archivo.length() > 0) {
			try { return objectMapper.readValue(archivo, new TypeReference<List<OrdenMedica>>() {}); }
			catch (IOException e) { System.err.println("Error al leer órdenes: " + e.getMessage()); }
		}
		return new ArrayList<>();
	}

	private boolean guardarOrdenesMedicasEnJson(List<OrdenMedica> ordenes) {
		try { objectMapper.writeValue(new File(RUTA_ORDENES_JSON), ordenes); return true; }
		catch (IOException e) { System.err.println("Error al guardar órdenes: " + e.getMessage()); return false; }
	}

	@FXML private void handleCerrar(ActionEvent event) {
	Stage stage = (Stage) btnCerrarDetalleOrden.getScene().getWindow();
	stage.close();
	}

	private void mostrarAlerta(String titulo, String mensaje) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(titulo); alert.setHeaderText(null); alert.setContentText(mensaje);
		alert.showAndWait();
	}
	private void mostrarAlertaError(String titulo, String mensaje) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(titulo); alert.setHeaderText(null); alert.setContentText(mensaje);
		alert.showAndWait();
	}
	private void mostrarAlertaValidacion(String titulo, List<String> mensajes) {
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle(titulo);
		alert.setHeaderText("Por favor corrija los siguientes errores:");
		alert.setContentText(String.join("\n", mensajes));
		alert.showAndWait();
	}
}
