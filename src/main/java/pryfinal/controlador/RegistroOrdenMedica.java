// Paquete
package pryfinal.controlador;

// Imports

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import pryfinal.modelo.OrdenMedica;
import pryfinal.modelo.Persona;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// Clase RegistroOrdenMedica
public class RegistroOrdenMedica {
	// Variables
	/// FXML
	@FXML private TextField txtNumeroOrden;
	@FXML private DatePicker dateFechaEmisionOrden;
	@FXML private TextField txtCedulaDuenoOrden;
	@FXML private TextField txtNombreMascotaOrden;
	@FXML private ComboBox<String> cmbVeterinarioPrescribe;
	@FXML private TextArea areaMedicamentosDosis;
	@FXML private TextArea areaInstruccionesAdmin;
	@FXML private TextField txtDuracionTratamiento;
	@FXML private TextArea areaNotasAdicionalesOrden;
	@FXML private Button btnRegistrarOrdenMedica;

	/// Patrones
	private final Pattern PATRON_NUMERO_ORDEN = Pattern.compile("^[a-zA-Z0-9-]{4,12}$");
	private final Pattern PATRON_NOMBRE_MASCOTA = Pattern.compile("^[\\p{L}0-9 .'-]+$");
	private final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ISO_LOCAL_DATE;

	/// Otros
	private ObjectMapper objectMapper;
	private final String RUTA_ORDENES_JSON = "data/ordenes_medicas.json";
	private final String RUTA_PERSONAS_JSON = "data/personas.json";
	private final String RUTA_DIRECTORIO_DATOS = "data";
	private ObservableList<String> listaNombresVeterinarios = FXCollections.observableArrayList();

	// Incializar
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

	// Configurar date picker
	private void configurarDatePicker() {
		dateFechaEmisionOrden.setValue(LocalDate.now());
		dateFechaEmisionOrden.setConverter(new StringConverter<LocalDate>() {
			@Override public String toString(LocalDate date) { return (date != null) ? FORMATO_FECHA.format(date) : ""; }
			@Override public LocalDate fromString(String string) {
			if (string != null && !string.isEmpty()) {
				try { return LocalDate.parse(string, FORMATO_FECHA); }
				catch (DateTimeParseException e) { return dateFechaEmisionOrden.getValue(); }
			} else { return null; }
			}
		});
	}

	// Cargar en combo box
	private void cargarVeterinariosEnComboBox() {
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
			} catch (IOException e) {
				mostrarAlertaError("Error Interno", "No se pudieron cargar los veterinarios: " + e.getMessage());
			}
		}

		cmbVeterinarioPrescribe.setItems(listaNombresVeterinarios);
		if (listaNombresVeterinarios.isEmpty()) {
			cmbVeterinarioPrescribe.setDisable(true);
			cmbVeterinarioPrescribe.setPromptText("No hay veterinarios registrados");
		} else if (listaNombresVeterinarios.size() == 1) {
			cmbVeterinarioPrescribe.getSelectionModel().selectFirst();
			cmbVeterinarioPrescribe.setDisable(true);
		} else {
			cmbVeterinarioPrescribe.setDisable(false);
			cmbVeterinarioPrescribe.setPromptText("Seleccione veterinario");
		}
	}

	// Boton registrar
	@FXML
	private void handleRegistrarOrdenMedica(ActionEvent event) {
		if (listaNombresVeterinarios.isEmpty()) {
			mostrarAlertaError("Operación no permitida", "No hay veterinarios registrados. No se puede crear una orden médica.");
			return;
		}

		List<String> errores = validarCampos();
		if (!errores.isEmpty()) {
			mostrarAlertaValidacion("Errores de Validación", errores);
			return;
		}

		String numeroOrden = txtNumeroOrden.getText().trim();
		String fechaEmision = FORMATO_FECHA.format(dateFechaEmisionOrden.getValue());
		long cedulaDueno = Long.parseLong(txtCedulaDuenoOrden.getText().trim());
		String nombreMascota = txtNombreMascotaOrden.getText().trim();
		String veterinario = cmbVeterinarioPrescribe.getValue();
		String dosis = areaMedicamentosDosis.getText().trim();
		String instrucciones = areaInstruccionesAdmin.getText().trim();
		String duracion = txtDuracionTratamiento.getText().trim();
		String notas = areaNotasAdicionalesOrden.getText().trim();

		OrdenMedica nuevaOrden = new OrdenMedica(numeroOrden, fechaEmision, cedulaDueno, nombreMascota,
				veterinario, dosis, instrucciones, duracion, notas);

		List<OrdenMedica> ordenes = cargarOrdenesMedicas();

		// Verificar si ya existe una orden con el mismo número
		boolean ordenYaExiste = ordenes.stream().anyMatch(o -> o.getNumero().equalsIgnoreCase(numeroOrden));
		if (ordenYaExiste) {
			mostrarAlertaError("Registro Duplicado", "Ya existe una orden médica registrada con el número: " + numeroOrden);
			return;
		}

		ordenes.add(nuevaOrden);

		if (guardarOrdenesMedicas(ordenes)) {
			mostrarAlertaInformacion("Registro Exitoso", "Orden Médica N°" + numeroOrden + " registrada correctamente.");
			limpiarCampos();
		} else {
			mostrarAlertaError("Error de Registro", "No se pudo guardar la orden médica.");
		}
	}

	// Validacion
	private List<String> validarCampos() {
		List<String> errores = new ArrayList<>();

		// Número de Orden
		String numOrden = txtNumeroOrden.getText().trim();
		if (numOrden.isEmpty()) {
			errores.add("- Número de orden no puede estar vacío.");
		} else if (!PATRON_NUMERO_ORDEN.matcher(numOrden).matches()) {
			errores.add("- Número de orden debe tener entre 4 y 12 caracteres (letras, números, guión '-').");
		}

		// Fecha de Emisión
		if (dateFechaEmisionOrden.getValue() == null) {
			errores.add("- Fecha de emisión no puede estar vacía.");
		}
		String fechaEditor = dateFechaEmisionOrden.getEditor().getText();
		if (fechaEditor.isEmpty() && dateFechaEmisionOrden.getValue() == null) {
			errores.add("- Fecha de emisión no puede estar vacía.");
		} else if (!fechaEditor.isEmpty()) {
			try { LocalDate.parse(fechaEditor, FORMATO_FECHA); }
			catch (DateTimeParseException e) { errores.add("- Fecha de emisión tiene un formato inválido. Use AAAA-MM-DD."); }
		}

		// Cédula del Dueño
		String cedula = txtCedulaDuenoOrden.getText().trim();
		if (cedula.isEmpty()) {
			errores.add("- Cédula del dueño no puede estar vacía.");
		} else if (!cedula.matches("\\d+")) {
			errores.add("- Cédula del dueño debe contener solo números.");
		} else if (cedula.length() < 5 || cedula.length() > 13) {
			errores.add("- Cédula del dueño debe tener entre 5 y 13 dígitos.");
		}

		// Nombre de Mascota
		String nombreMascota = txtNombreMascotaOrden.getText().trim();
		if (nombreMascota.isEmpty()) {
			errores.add("- Nombre de mascota no puede estar vacío.");
		} else if (nombreMascota.length() < 3 || nombreMascota.length() >= 40) {
			errores.add("- Nombre de mascota debe tener entre 3 y 39 caracteres.");
		} else if (!PATRON_NOMBRE_MASCOTA.matcher(nombreMascota).matches()) {
			errores.add("- Nombre de mascota solo debe contener letras, números y caracteres permitidos.");
		}

		// Veterinario que Prescribe
		if (cmbVeterinarioPrescribe.getValue() == null || cmbVeterinarioPrescribe.getValue().isEmpty()) {
			errores.add("- Debe seleccionar un veterinario que prescribe.");
		}

		// Medicamento(s) y Dosis
		String dosis = areaMedicamentosDosis.getText().trim();
		if (dosis.isEmpty()) {
			errores.add("- Medicamento(s) y Dosis no puede estar vacío.");
		} else if (dosis.length() < 3 || dosis.length() > 975) {
			errores.add("- Medicamento(s) y Dosis debe tener entre 3 y 975 caracteres.");
		}

		// Instrucciones de Administración
		String instrucciones = areaInstruccionesAdmin.getText().trim();
		if (instrucciones.isEmpty()) {
			errores.add("- Instrucciones de administración no puede estar vacío.");
		} else if (instrucciones.length() < 3 || instrucciones.length() > 975) {
			errores.add("- Instrucciones de administración debe tener entre 3 y 975 caracteres.");
		}

		// Duración del Tratamiento
		String duracion = txtDuracionTratamiento.getText().trim();
		if (duracion.isEmpty()) {
			errores.add("- Duración del tratamiento no puede estar vacío.");
		} else if (duracion.length() < 3 || duracion.length() > 975) {
			errores.add("- Duración del tratamiento debe tener entre 3 y 975 caracteres.");
		}

		// Notas Adicionales (opcional)
		String notas = areaNotasAdicionalesOrden.getText().trim();
		if (!notas.isEmpty() && notas.length() > 975) {
			errores.add("- Notas adicionales no debe exceder los 975 caracteres.");
		}

		return errores;
	}

	// Cargar
	private List<OrdenMedica> cargarOrdenesMedicas() {
		File archivo = new File(RUTA_ORDENES_JSON);
		if (archivo.exists() && archivo.length() > 0) {
			try {
				return objectMapper.readValue(archivo, new TypeReference<List<OrdenMedica>>() {});
			} catch (IOException e) { System.err.println("Error al leer órdenes médicas: " + e.getMessage()); }
		}
		return new ArrayList<>();
	}

	// Guardar
	private boolean guardarOrdenesMedicas(List<OrdenMedica> ordenes) {
		try {
			objectMapper.writeValue(new File(RUTA_ORDENES_JSON), ordenes);
			return true;
		} catch (IOException e) { System.err.println("Error al guardar órdenes médicas: " + e.getMessage()); return false; }
	}

	// Limpiar campos
	private void limpiarCampos() {
		txtNumeroOrden.clear();
		dateFechaEmisionOrden.setValue(LocalDate.now());
		txtCedulaDuenoOrden.clear();
		txtNombreMascotaOrden.clear();
		if (!cmbVeterinarioPrescribe.isDisabled()) {
			cmbVeterinarioPrescribe.getSelectionModel().clearSelection();
		}
		areaMedicamentosDosis.clear();
		areaInstruccionesAdmin.clear();
		txtDuracionTratamiento.clear();
		areaNotasAdicionalesOrden.clear();
		txtNumeroOrden.requestFocus();
	}

	// Mostrar alerta
	/// Validacion
	private void mostrarAlertaValidacion(String titulo, List<String> mensajes) {
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle(titulo);
		alert.setHeaderText("Por favor corrija los siguientes errores:");
		alert.setContentText(String.join("\n", mensajes));
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
