// Paquete
package pryfinal.controlador;

// Imports JavaFX
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

// Imports para JSON (Jackson)
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

// Imports para archivos, listas, fecha y formato
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// Modelos
import pryfinal.modelo.HistoriaClinica;
import pryfinal.modelo.Persona; // Necesario para cargar los veterinarios

// Clase RegistroHistoriaClinica
public class RegistroHistoriaClinica {

	@FXML private TextField txtCedulaDuenoHC;
	@FXML private TextField txtNombreMascotaHC;
	@FXML private DatePicker dateFechaVisitaHC;
	@FXML private ComboBox<String> cmbVeterinarioEncargado;
	@FXML private TextArea areaMotivoConsulta;
	@FXML private TextArea areaDiagnostico;
	@FXML private TextArea areaTratamientoIndicado;
	@FXML private TextArea areaObservacionesHC;
	@FXML private Button btnRegistrarHistoria;

	private ObjectMapper objectMapper;
	private final String RUTA_HISTORIAS_JSON = "data/historias_clinicas.json";
	private final String RUTA_PERSONAS_JSON = "data/personas.json"; // Para cargar veterinarios
	private final String RUTA_DIRECTORIO_DATOS = "data";

	// Patrones de validación
	private final Pattern PATRON_NOMBRE_MASCOTA = Pattern.compile("^[\\p{L}0-9 .'-]+$"); // Permite números también para IDs de mascota
	private final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ISO_LOCAL_DATE; // YYYY-MM-DD

	private ObservableList<String> listaNombresVeterinarios = FXCollections.observableArrayList();

	@FXML
	public void initialize() {
		objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

		File directorioDatos = new File(RUTA_DIRECTORIO_DATOS);
		if (!directorioDatos.exists()) {
			directorioDatos.mkdirs();
		}

		configurarDatePicker();
		cargarVeterinariosEnComboBox();
	}

	private void configurarDatePicker() {
		dateFechaVisitaHC.setValue(LocalDate.now()); // Fecha actual por defecto
		dateFechaVisitaHC.setConverter(new StringConverter<LocalDate>() {
			@Override
			public String toString(LocalDate date) {
				return (date != null) ? FORMATO_FECHA.format(date) : "";
			}
			@Override
			public LocalDate fromString(String string) {
				if (string != null && !string.isEmpty()) {
					try {
						return LocalDate.parse(string, FORMATO_FECHA);
					} catch (DateTimeParseException e) {
						System.err.println("Formato de fecha inválido ingresado: " + string);
						return dateFechaVisitaHC.getValue();
					}
				} else {
					return null;
				}
			}
		});
	}

	private void cargarVeterinariosEnComboBox() {
		listaNombresVeterinarios.clear();
		File archivoPersonas = new File(RUTA_PERSONAS_JSON);
		if (archivoPersonas.exists() && archivoPersonas.length() > 0) {
			try {
				List<Persona> todasLasPersonas = objectMapper.readValue(archivoPersonas, new TypeReference<List<Persona>>() {});
				List<String> veterinarios = todasLasPersonas.stream()
					.filter(p -> "Veterinario".equalsIgnoreCase(p.getTipo()))
					.map(p -> p.getNombre() + " " + p.getApellido()) // Nombre completo
					.collect(Collectors.toList());

				listaNombresVeterinarios.addAll(veterinarios);
			} catch (IOException e) {
				System.err.println("Error al cargar personas para veterinarios: " + e.getMessage());
				mostrarAlertaError("Error Interno", "No se pudieron cargar los veterinarios.");
			}
		}

		cmbVeterinarioEncargado.setItems(listaNombresVeterinarios);

		if (listaNombresVeterinarios.isEmpty()) {
			cmbVeterinarioEncargado.setDisable(true);
			cmbVeterinarioEncargado.setPromptText("No hay veterinarios registrados");
			// El botón de registrar se deshabilitará o la validación fallará si esto ocurre.
		} else if (listaNombresVeterinarios.size() == 1) {
			cmbVeterinarioEncargado.getSelectionModel().selectFirst();
			cmbVeterinarioEncargado.setDisable(true);
		} else {
			cmbVeterinarioEncargado.setDisable(false);
			cmbVeterinarioEncargado.setPromptText("Seleccione veterinario");
		}
	}


	@FXML
	private void handleRegistrarHistoria(ActionEvent event) {
		// Verificar si hay veterinarios antes de intentar validar y registrar
		if (listaNombresVeterinarios.isEmpty()) {
			mostrarAlertaError("Operación no permitida", "No hay veterinarios registrados en el sistema. Por favor, pida al administrador que agregue veterinarios para poder registrar una historia clínica.");
			return;
		}

		List<String> errores = validarCampos();

		if (!errores.isEmpty()) {
			mostrarAlertaValidacion("Errores de Validación", errores);
			return;
		}

		long cedulaDueno = Long.parseLong(txtCedulaDuenoHC.getText().trim());
		String nombreMascota = txtNombreMascotaHC.getText().trim();
		String fechaVisita = FORMATO_FECHA.format(dateFechaVisitaHC.getValue());
		String veterinarioEncargado = cmbVeterinarioEncargado.getValue(); // Ya validado que no es null
		String motivo = areaMotivoConsulta.getText().trim();
		String diagnostico = areaDiagnostico.getText().trim();
		String tratamiento = areaTratamientoIndicado.getText().trim();
		String observaciones = areaObservacionesHC.getText().trim();

		HistoriaClinica nuevaHistoria = new HistoriaClinica(cedulaDueno, nombreMascota, fechaVisita, motivo,
				diagnostico, tratamiento, observaciones, veterinarioEncargado);

		List<HistoriaClinica> historias = cargarHistorias();
		historias.add(nuevaHistoria);

		if (guardarHistorias(historias)) {
			mostrarAlertaInformacion("Registro Exitoso", "Entrada de historia clínica registrada correctamente.");
			limpiarCampos();
		} else {
			mostrarAlertaError("Error de Registro", "No se pudo guardar la historia clínica.");
		}
	}

	private List<String> validarCampos() {
		List<String> errores = new ArrayList<>();

		// Cédula del Dueño
		String cedula = txtCedulaDuenoHC.getText().trim();
		if (cedula.isEmpty()) {
			errores.add("- Cédula del dueño no puede estar vacía.");
		} else if (!cedula.matches("\\d+")) {
			errores.add("- Cédula del dueño debe contener solo números.");
		} else if (cedula.length() < 5 || cedula.length() > 13) {
			errores.add("- Cédula del dueño debe tener entre 5 y 13 dígitos.");
		}

		// Nombre de Mascota
		String nombreMascota = txtNombreMascotaHC.getText().trim();
		if (nombreMascota.isEmpty()) {
			errores.add("- Nombre de mascota no puede estar vacío.");
		} else if (nombreMascota.length() < 3 || nombreMascota.length() >= 40) {
			errores.add("- Nombre de mascota debe tener entre 3 y 39 caracteres.");
		} else if (!PATRON_NOMBRE_MASCOTA.matcher(nombreMascota).matches()) {
			errores.add("- Nombre de mascota solo debe contener letras, números y caracteres permitidos (espacios, ', ., -).");
		}

		// Fecha de Visita
		if (dateFechaVisitaHC.getValue() == null) {
			errores.add("- Fecha de visita no puede estar vacía.");
		}
		String fechaEditor = dateFechaVisitaHC.getEditor().getText();
		if (fechaEditor.isEmpty() && dateFechaVisitaHC.getValue() == null) {
			errores.add("- Fecha de visita no puede estar vacía.");
		} else if (!fechaEditor.isEmpty()) {
			try {
				LocalDate.parse(fechaEditor, FORMATO_FECHA);
			} catch (DateTimeParseException e) {
				errores.add("- Fecha de visita tiene un formato inválido. Use AAAA-MM-DD.");
			}
		}

		// Veterinario Encargado
		if (cmbVeterinarioEncargado.getValue() == null || cmbVeterinarioEncargado.getValue().isEmpty()) {
			errores.add("- Debe seleccionar un veterinario encargado.");
		}

		// Motivo de la Consulta
		String motivo = areaMotivoConsulta.getText().trim();
		if (motivo.isEmpty()) {
			errores.add("- Motivo de la consulta no puede estar vacío.");
		} else if (motivo.length() < 3 || motivo.length() > 975) {
			errores.add("- Motivo de la consulta debe tener entre 3 y 975 caracteres.");
		}

		// Diagnóstico
		String diagnostico = areaDiagnostico.getText().trim();
		if (diagnostico.isEmpty()) {
			errores.add("- Diagnóstico no puede estar vacío.");
		} else if (diagnostico.length() < 3 || diagnostico.length() > 975) {
			errores.add("- Diagnóstico debe tener entre 3 y 975 caracteres.");
		}

		// Tratamiento Indicado
		String tratamiento = areaTratamientoIndicado.getText().trim();
		if (tratamiento.isEmpty()) {
			errores.add("- Tratamiento indicado no puede estar vacío.");
		} else if (tratamiento.length() < 3 || tratamiento.length() > 975) {
			errores.add("- Tratamiento indicado debe tener entre 3 y 975 caracteres.");
		}

		// Observaciones (puede ser opcional, si es así, quitar la validación de isEmpty)
		String observaciones = areaObservacionesHC.getText().trim();
		if (observaciones.isEmpty()) {
			// Si es opcional, comentar la siguiente línea
			// errores.add("- Observaciones no puede estar vacío."); 
		} else if (observaciones.length() > 0 && (observaciones.length() < 3 || observaciones.length() > 975) ) { 
			// Solo validar longitud si no está vacío
			errores.add("- Observaciones debe tener entre 3 y 975 caracteres si se ingresa.");
		}

		return errores;
	}

	private List<HistoriaClinica> cargarHistorias() {
		File archivo = new File(RUTA_HISTORIAS_JSON);
		if (archivo.exists() && archivo.length() > 0) {
			try {
				return objectMapper.readValue(archivo, new TypeReference<List<HistoriaClinica>>() {});
			} catch (IOException e) {
				System.err.println("Error al leer el archivo de historias clínicas: " + e.getMessage());
			}
		}
		return new ArrayList<>();
	}

	private boolean guardarHistorias(List<HistoriaClinica> historias) {
		try {
			objectMapper.writeValue(new File(RUTA_HISTORIAS_JSON), historias);
			return true;
		} catch (IOException e) {
			System.err.println("Error al guardar el archivo de historias clínicas: " + e.getMessage());
			return false;
		}
	}

	private void limpiarCampos() {
		txtCedulaDuenoHC.clear();
		txtNombreMascotaHC.clear();
		dateFechaVisitaHC.setValue(LocalDate.now());
		if (!cmbVeterinarioEncargado.isDisabled()) { // Solo limpiar si no está deshabilitado
			cmbVeterinarioEncargado.getSelectionModel().clearSelection();
		}
		areaMotivoConsulta.clear();
		areaDiagnostico.clear();
		areaTratamientoIndicado.clear();
		areaObservacionesHC.clear();
		txtCedulaDuenoHC.requestFocus();
	}

	private void mostrarAlertaValidacion(String titulo, List<String> mensajes) {
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle(titulo);
		alert.setHeaderText("Por favor corrija los siguientes errores:");
		alert.setContentText(String.join("\n", mensajes));
		alert.showAndWait();
	}

	private void mostrarAlertaInformacion(String titulo, String mensaje) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(titulo);
		alert.setHeaderText(null);
		alert.setContentText(mensaje);
		alert.showAndWait();
	}

	private void mostrarAlertaError(String titulo, String mensaje) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(titulo);
		alert.setHeaderText(null);
		alert.setContentText(mensaje);
		alert.showAndWait();
	}
}
