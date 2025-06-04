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
import pryfinal.modelo.HistoriaClinica;
import pryfinal.modelo.Persona;
import pryfinal.modelo.Usuario;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// Clase DetalleHistoriaClinica
public class DetalleHistoriaClinica {

	@FXML private Label lblTituloDetalleHC;
	@FXML private TextField txtCedulaDuenoHC;
	@FXML private TextField txtNombreMascotaHC;
	@FXML private TextField txtFechaVisitaHCDisplay;
	@FXML private DatePicker dateFechaVisitaHCEdit;
	@FXML private ComboBox<String> cmbVeterinarioEncargadoHC;
	@FXML private TextArea areaMotivoConsultaHC;
	@FXML private TextArea areaDiagnosticoHC;
	@FXML private TextArea areaTratamientoIndicadoHC;
	@FXML private TextArea areaObservacionesHC;

	@FXML private HBox botonesAccionBoxHC;
	@FXML private Button btnModificarHC;
	@FXML private Button btnEliminarHC;
	@FXML private HBox botonesEdicionBoxHC;
	@FXML private Button btnGuardarCambiosHC;
	@FXML private Button btnDescartarCambiosHC;
	@FXML private Button btnCerrarDetalleHistoria;

	private HistoriaClinica historiaSeleccionada;
	private Usuario usuarioActual;
	private ConsultaHistoriaClinica consultaHistoriaClinicaController;
	private boolean enModoEdicion = false;

	private ObjectMapper objectMapper;
	private final String RUTA_HISTORIAS_JSON = "data/historias_clinicas.json";
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

	public void setConsultaHistoriaClinicaController(ConsultaHistoriaClinica controller) {
		this.consultaHistoriaClinicaController = controller;
	}

	private void configurarDatePickerEdit() {
		dateFechaVisitaHCEdit.setConverter(new StringConverter<LocalDate>() {
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
		cmbVeterinarioEncargadoHC.setItems(listaNombresVeterinarios);
		if(historiaSeleccionada != null) cmbVeterinarioEncargadoHC.setValue(historiaSeleccionada.getVeterinario());
	}

	public void cargarDatos(HistoriaClinica historia, Usuario usuarioLogueado) {
		this.historiaSeleccionada = historia;
		this.usuarioActual = usuarioLogueado;

		if (historia != null) {
			txtCedulaDuenoHC.setText(String.valueOf(historia.getCedula()));
			txtNombreMascotaHC.setText(historia.getNombre());
			txtFechaVisitaHCDisplay.setText(historia.getFecha());
			try { dateFechaVisitaHCEdit.setValue(LocalDate.parse(historia.getFecha(), FORMATO_FECHA_ISO)); }
			catch (Exception e) { dateFechaVisitaHCEdit.setValue(null); }

			// Configurar ComboBox Veterinario para modo solo lectura
			if (historia.getVeterinario() != null && !historia.getVeterinario().isEmpty()) {
				cmbVeterinarioEncargadoHC.setItems(FXCollections.observableArrayList(historia.getVeterinario()));
				cmbVeterinarioEncargadoHC.setValue(historia.getVeterinario());
			} else {
				cmbVeterinarioEncargadoHC.setItems(FXCollections.observableArrayList()); // Lista vacía
				cmbVeterinarioEncargadoHC.setPromptText("No asignado");
			}

			areaMotivoConsultaHC.setText(historia.getMotivo());
			areaDiagnosticoHC.setText(historia.getDiagnostico());
			areaTratamientoIndicadoHC.setText(historia.getTratamiento());
			areaObservacionesHC.setText(historia.getObservaciones());
		}
		configurarSegunRolUsuario();
		salirModoEdicion();
	}

	private void configurarSegunRolUsuario() {
		boolean esAdmin = usuarioActual != null && ADMIN_USER_TYPE.equals(usuarioActual.getTipo());
		btnModificarHC.setDisable(!esAdmin);
		btnEliminarHC.setDisable(!esAdmin);
	}

	private void entrarModoEdicion() {
		enModoEdicion = true;
		lblTituloDetalleHC.setText("Modificar Historia Clínica");
		txtNombreMascotaHC.setEditable(true);
		txtFechaVisitaHCDisplay.setVisible(false); txtFechaVisitaHCDisplay.setManaged(false);
		dateFechaVisitaHCEdit.setVisible(true); dateFechaVisitaHCEdit.setManaged(true);

		cargarVeterinariosParaEdicion();
		cmbVeterinarioEncargadoHC.setDisable(false);
		if(historiaSeleccionada != null) cmbVeterinarioEncargadoHC.setValue(historiaSeleccionada.getVeterinario());

		areaMotivoConsultaHC.setEditable(true);
		areaDiagnosticoHC.setEditable(true);
		areaTratamientoIndicadoHC.setEditable(true);
		areaObservacionesHC.setEditable(true);
		actualizarVisibilidadBotonesEdicion();
	}

	private void salirModoEdicion() {
		enModoEdicion = false;
		lblTituloDetalleHC.setText("Detalle de Historia Clínica");
		txtNombreMascotaHC.setEditable(false);
		txtFechaVisitaHCDisplay.setVisible(true); txtFechaVisitaHCDisplay.setManaged(true);
		dateFechaVisitaHCEdit.setVisible(false); dateFechaVisitaHCEdit.setManaged(false);
		cmbVeterinarioEncargadoHC.setDisable(true);
		areaMotivoConsultaHC.setEditable(false);
		areaDiagnosticoHC.setEditable(false);
		areaTratamientoIndicadoHC.setEditable(false);
		areaObservacionesHC.setEditable(false);
		if (historiaSeleccionada != null) {
			cargarValoresOriginales();
		}
		actualizarVisibilidadBotonesEdicion();
	}

	private void cargarValoresOriginales() {
		txtNombreMascotaHC.setText(historiaSeleccionada.getNombre());
		txtFechaVisitaHCDisplay.setText(historiaSeleccionada.getFecha());
		try { dateFechaVisitaHCEdit.setValue(LocalDate.parse(historiaSeleccionada.getFecha(), FORMATO_FECHA_ISO)); }
		catch (Exception e) { dateFechaVisitaHCEdit.setValue(null); }

		if (historiaSeleccionada.getVeterinario() != null && !historiaSeleccionada.getVeterinario().isEmpty()) {
			cmbVeterinarioEncargadoHC.setItems(FXCollections.observableArrayList(historiaSeleccionada.getVeterinario()));
			cmbVeterinarioEncargadoHC.setValue(historiaSeleccionada.getVeterinario());
		} else {
			cmbVeterinarioEncargadoHC.setItems(FXCollections.observableArrayList());
			cmbVeterinarioEncargadoHC.setPromptText("No asignado");
		}

		areaMotivoConsultaHC.setText(historiaSeleccionada.getMotivo());
		areaDiagnosticoHC.setText(historiaSeleccionada.getDiagnostico());
		areaTratamientoIndicadoHC.setText(historiaSeleccionada.getTratamiento());
		areaObservacionesHC.setText(historiaSeleccionada.getObservaciones());
	}

	private void actualizarVisibilidadBotonesEdicion() {
		botonesAccionBoxHC.setVisible(!enModoEdicion);
		botonesAccionBoxHC.setManaged(!enModoEdicion);
		botonesEdicionBoxHC.setVisible(enModoEdicion);
		botonesEdicionBoxHC.setManaged(enModoEdicion);
		btnCerrarDetalleHistoria.setVisible(!enModoEdicion);
	}

	@FXML private void handleModificarHC(ActionEvent event) { entrarModoEdicion(); }
	@FXML private void handleDescartarCambiosHC(ActionEvent event) { salirModoEdicion(); }

	@FXML
	private void handleGuardarCambiosHC(ActionEvent event) {
		if (historiaSeleccionada == null) return;

		List<String> errores = validarCamposParaModificacion();
		if (!errores.isEmpty()) {
			mostrarAlertaValidacion("Errores de Validación", errores);
			return;
		}

		List<HistoriaClinica> historias = cargarHistoriasDesdeJson();
		int indiceHistoria = -1;
		for (int i = 0; i < historias.size(); i++) {
			HistoriaClinica hc = historias.get(i);
			// Clave para identificar: Cédula dueño, Nombre mascota, Fecha original
			if (hc.getCedula() == historiaSeleccionada.getCedula() &&
					hc.getNombre().equals(historiaSeleccionada.getNombre()) &&
					hc.getFecha().equals(historiaSeleccionada.getFecha())) {
				indiceHistoria = i;
				break;
					}
		}

		if (indiceHistoria != -1) {
			HistoriaClinica historiaParaActualizar = historias.get(indiceHistoria);

			historiaParaActualizar.setNombre(txtNombreMascotaHC.getText().trim());
			historiaParaActualizar.setFecha(FORMATO_FECHA_ISO.format(dateFechaVisitaHCEdit.getValue()));
			historiaParaActualizar.setVeterinario(cmbVeterinarioEncargadoHC.getValue());
			historiaParaActualizar.setMotivo(areaMotivoConsultaHC.getText().trim());
			historiaParaActualizar.setDiagnostico(areaDiagnosticoHC.getText().trim());
			historiaParaActualizar.setTratamiento(areaTratamientoIndicadoHC.getText().trim());
			historiaParaActualizar.setObservaciones(areaObservacionesHC.getText().trim());

			if (guardarHistoriasEnJson(historias)) {
				mostrarAlerta("Éxito", "Historia clínica actualizada correctamente.");
				this.historiaSeleccionada = historiaParaActualizar; // Actualizar referencia local
				if (consultaHistoriaClinicaController != null) {
					consultaHistoriaClinicaController.refrescarListaHistorias();
				}
			} else {
				mostrarAlertaError("Error", "No se pudo guardar los cambios.");
			}
		} else {
			mostrarAlertaError("Error", "No se encontró la historia clínica para actualizar. Pudo haber sido modificada o eliminada.");
		}
		salirModoEdicion();
	}

	@FXML
	private void handleEliminarHC(ActionEvent event) {
		if (historiaSeleccionada == null) return;

		Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
		confirmacion.setTitle("Confirmar Eliminación");
		confirmacion.setHeaderText("Eliminar entrada de Historia Clínica para: " + historiaSeleccionada.getNombre());
		confirmacion.setContentText("¿Está seguro? Esta acción no se puede deshacer.");

		Optional<ButtonType> resultado = confirmacion.showAndWait();
		if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
			List<HistoriaClinica> historias = cargarHistoriasDesdeJson();
			boolean eliminada = historias.removeIf(hc ->
					hc.getCedula() == historiaSeleccionada.getCedula() &&
					hc.getNombre().equals(historiaSeleccionada.getNombre()) &&
					hc.getFecha().equals(historiaSeleccionada.getFecha()) &&
					Objects.equals(hc.getVeterinario(), historiaSeleccionada.getVeterinario()) && // Añadir más campos para asegurar unicidad
					Objects.equals(hc.getMotivo(), historiaSeleccionada.getMotivo())
					);

			if (eliminada) {
				if (guardarHistoriasEnJson(historias)) {
					mostrarAlerta("Éxito", "Historia clínica eliminada correctamente.");
					if (consultaHistoriaClinicaController != null) {
						consultaHistoriaClinicaController.refrescarListaHistorias();
					}
					handleCerrar(null); // Cerrar ventana de detalle
				} else {
					mostrarAlertaError("Error", "No se pudo eliminar la historia clínica del archivo.");
				}
			} else {
				mostrarAlertaError("Error", "No se encontró la historia clínica para eliminar (puede que ya haya sido eliminada o sus datos clave cambiaron).");
			}
		}
	}

	private List<String> validarCamposParaModificacion() {
		List<String> errores = new ArrayList<>();
		String nombreMascota = txtNombreMascotaHC.getText().trim();
		if (nombreMascota.isEmpty()) errores.add("- Nombre de mascota vacío.");
		else if (nombreMascota.length() < 3 || nombreMascota.length() >= 40) errores.add("- Nombre mascota: 3-39 chars.");
		else if (!PATRON_NOMBRE_MASCOTA.matcher(nombreMascota).matches()) errores.add("- Nombre mascota: letras, números, y '.- permitidos.");

		if (dateFechaVisitaHCEdit.getValue() == null) errores.add("- Fecha de visita vacía.");
		String fechaEditor = dateFechaVisitaHCEdit.getEditor().getText();
		if (!fechaEditor.isEmpty()) {
			try { LocalDate.parse(fechaEditor, FORMATO_FECHA_ISO); }
			catch (DateTimeParseException e) { errores.add("- Fecha visita: formato AAAA-MM-DD inválido."); }
		} else if(dateFechaVisitaHCEdit.getValue() == null){
			errores.add("- Fecha de visita vacía.");
		}

		if (cmbVeterinarioEncargadoHC.getValue() == null || cmbVeterinarioEncargadoHC.getValue().isEmpty()) {
			errores.add("- Debe seleccionar un veterinario.");
		}

		String motivo = areaMotivoConsultaHC.getText().trim();
		if (motivo.isEmpty()) errores.add("- Motivo consulta vacío.");
		else if (motivo.length() < 3 || motivo.length() > 975) errores.add("- Motivo consulta: 3-975 chars.");

		String diagnostico = areaDiagnosticoHC.getText().trim();
		if (diagnostico.isEmpty()) errores.add("- Diagnóstico vacío.");
		else if (diagnostico.length() < 3 || diagnostico.length() > 975) errores.add("- Diagnóstico: 3-975 chars.");

		String tratamiento = areaTratamientoIndicadoHC.getText().trim();
		if (tratamiento.isEmpty()) errores.add("- Tratamiento indicado vacío.");
		else if (tratamiento.length() < 3 || tratamiento.length() > 975) errores.add("- Tratamiento: 3-975 chars.");

		String observaciones = areaObservacionesHC.getText().trim();
		if (!observaciones.isEmpty() && (observaciones.length() < 3 || observaciones.length() > 975)) {
			errores.add("- Observaciones (si se ingresa): 3-975 chars.");
		}
		return errores;
	}

	private List<HistoriaClinica> cargarHistoriasDesdeJson() {
		File archivo = new File(RUTA_HISTORIAS_JSON);
		if (archivo.exists() && archivo.length() > 0) {
			try { return objectMapper.readValue(archivo, new TypeReference<List<HistoriaClinica>>() {}); }
			catch (IOException e) { System.err.println("Error al leer historias: " + e.getMessage()); }
		}
		return new ArrayList<>();
	}

	private boolean guardarHistoriasEnJson(List<HistoriaClinica> historias) {
		try { objectMapper.writeValue(new File(RUTA_HISTORIAS_JSON), historias); return true; }
		catch (IOException e) { System.err.println("Error al guardar historias: " + e.getMessage()); return false; }
	}

	@FXML private void handleCerrar(ActionEvent event) {
	Stage stage = (Stage) btnCerrarDetalleHistoria.getScene().getWindow();
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
