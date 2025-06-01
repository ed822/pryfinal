// Paquete
package pryfinal.controlador;

// Imports JavaFX
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

// Imports para JSON (Jackson)
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

// Imports para archivos y listas
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

// Modelos
import pryfinal.modelo.Persona;
import pryfinal.modelo.Usuario; // Para obtener el tipo de usuario actual

// Clase RegistroPersona
public class RegistroPersona {

	@FXML private TextField txtCedulaPersona;
	@FXML private TextField txtNombrePersona;
	@FXML private TextField txtApellido;
	@FXML private ComboBox<String> cmbTipoPersona;
	@FXML private TextField txtTelefono;
	@FXML private TextField txtDireccion;
	@FXML private TextField txtEmail;
	@FXML private Button btnRegistrarPersona;

	private ObjectMapper objectMapper;
	private final String RUTA_PERSONAS_JSON = "data/personas.json";
	private final String RUTA_DIRECTORIO_DATOS = "data";

	// Patrón para nombres, apellidos (letras, espacios, acentos, ñ)
	private final Pattern PATRON_NOMBRE_APELLIDO = Pattern.compile("^[\\p{L} .'-]+$");
	// Patrón básico para email (simplificado)
	private final Pattern PATRON_EMAIL = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");

	private Usuario usuarioActual; // Para determinar el rol del usuario logueado

	@FXML
	public void initialize() {
		objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

		File directorioDatos = new File(RUTA_DIRECTORIO_DATOS);
		if (!directorioDatos.exists()) {
			directorioDatos.mkdirs();
		}
		// La configuración del ComboBox basada en el rol se hará cuando se setee el usuarioActual
	}

	/**
	 * Método para ser llamado desde MenuPrincipal para pasar el usuario logueado.
	 * Esto es necesario para restringir el ComboBox cmbTipoPersona.
	 */
	public void configurarConUsuario(Usuario usuario) {
		this.usuarioActual = usuario;
		configurarTipoPersonaSegunRol();
	}

	private void configurarTipoPersonaSegunRol() {
		if (usuarioActual == null) {
			cmbTipoPersona.setDisable(true); // Deshabilitar si no hay usuario
			cmbTipoPersona.getItems().clear(); // Limpiar items para evitar selecciones inválidas
			return;
		}

		String tipoUsuarioLogueado = usuarioActual.getTipo();

		// Habilitar ComboBox por defecto, luego restringir si es necesario
		cmbTipoPersona.setDisable(false);

		if ("veterinario".equals(tipoUsuarioLogueado)) {
			cmbTipoPersona.setValue("Dueño de Mascota");
			cmbTipoPersona.setDisable(true);
		} else if ("vendedor".equals(tipoUsuarioLogueado)) {
			cmbTipoPersona.setValue("Encargado de bodega");
			cmbTipoPersona.setDisable(true);
		}
		// Para "admin", el ComboBox permanece habilitado con todas las opciones.
	}

	@FXML
	private void handleRegistrarPersona(ActionEvent event) {
		List<String> errores = validarCampos();

		if (!errores.isEmpty()) {
			mostrarAlertaValidacion("Errores de Validación", errores);
			return;
		}

		long cedula = Long.parseLong(txtCedulaPersona.getText().trim());
		String nombre = txtNombrePersona.getText().trim();
		String apellido = txtApellido.getText().trim();
		String tipoPersona = cmbTipoPersona.getValue();
		long celular = Long.parseLong(txtTelefono.getText().trim());
		String direccion = txtDireccion.getText().trim();
		String email = txtEmail.getText().trim();

		Persona nuevaPersona = new Persona(cedula, nombre, apellido, tipoPersona, celular, direccion, email);

		List<Persona> personas = cargarPersonas();

		// Opcional: Verificar si ya existe una persona con la misma cédula
		boolean personaYaExiste = personas.stream().anyMatch(p -> p.getCedula() == cedula);
		if (personaYaExiste) {
			mostrarAlertaError("Registro Duplicado", "Ya existe una persona registrada con la cédula: " + cedula);
			return;
		}

		personas.add(nuevaPersona);

		if (guardarPersonas(personas)) {
			mostrarAlertaInformacion("Registro Exitoso", "Persona '" + nombre + " " + apellido + "' registrada correctamente.");
			limpiarCampos();
		} else {
			mostrarAlertaError("Error de Registro", "No se pudo guardar la persona. Intente de nuevo.");
		}
	}

	private List<String> validarCampos() {
		List<String> errores = new ArrayList<>();

		// Cédula
		String cedulaStr = txtCedulaPersona.getText().trim();
		if (cedulaStr.isEmpty()) {
			errores.add("- Cédula no puede estar vacía.");
		} else if (!cedulaStr.matches("\\d+")) {
			errores.add("- Cédula debe contener solo números.");
		} else if (cedulaStr.length() < 5 || cedulaStr.length() > 13) {
			errores.add("- Cédula debe tener entre 5 y 13 dígitos.");
		}

		// Nombre
		String nombre = txtNombrePersona.getText().trim();
		if (nombre.isEmpty()) {
			errores.add("- Nombre no puede estar vacío.");
		} else if (nombre.length() < 3 || nombre.length() >= 40) {
			errores.add("- Nombre debe tener entre 3 y 39 caracteres.");
		} else if (!PATRON_NOMBRE_APELLIDO.matcher(nombre).matches()) {
			errores.add("- Nombre solo debe contener letras y caracteres permitidos (espacios, ', ., -).");
		}

		// Apellido
		String apellido = txtApellido.getText().trim();
		if (apellido.isEmpty()) {
			errores.add("- Apellido no puede estar vacío.");
		} else if (apellido.length() < 3 || apellido.length() >= 40) {
			errores.add("- Apellido debe tener entre 3 y 39 caracteres.");
		} else if (!PATRON_NOMBRE_APELLIDO.matcher(apellido).matches()) {
			errores.add("- Apellido solo debe contener letras y caracteres permitidos.");
		}

		// Tipo de Persona
		if (cmbTipoPersona.getValue() == null || cmbTipoPersona.getValue().isEmpty()) {
			errores.add("- Debe seleccionar un tipo de persona.");
		}

		// Celular
		String celularStr = txtTelefono.getText().trim();
		if (celularStr.isEmpty()) {
			errores.add("- Número de celular no puede estar vacío.");
		} else if (!celularStr.matches("\\d+")) {
			errores.add("- Número de celular debe contener solo números.");
		} else if (celularStr.length() < 5 || celularStr.length() > 13) { // Misma validación que cédula por simplicidad
			errores.add("- Número de celular debe tener entre 5 y 13 dígitos.");
		}

		// Dirección
		String direccion = txtDireccion.getText().trim();
		if (direccion.isEmpty()) {
			errores.add("- Dirección no puede estar vacía.");
		} else if (direccion.length() < 3 || direccion.length() > 180) {
			errores.add("- Dirección debe tener entre 3 y 180 caracteres.");
		}

		// Email
		String email = txtEmail.getText().trim();
		if (email.isEmpty()) {
			errores.add("- Email no puede estar vacío.");
		} else if (!PATRON_EMAIL.matcher(email).matches()) {
			errores.add("- Email no tiene un formato válido (ej: usuario@dominio.com).");
		}

		return errores;
	}

	private List<Persona> cargarPersonas() {
		File archivo = new File(RUTA_PERSONAS_JSON);
		if (archivo.exists() && archivo.length() > 0) {
			try {
				return objectMapper.readValue(archivo, new TypeReference<List<Persona>>() {});
			} catch (IOException e) {
				System.err.println("Error al leer el archivo de personas: " + e.getMessage());
			}
		}
		return new ArrayList<>();
	}

	private boolean guardarPersonas(List<Persona> personas) {
		try {
			objectMapper.writeValue(new File(RUTA_PERSONAS_JSON), personas);
			return true;
		} catch (IOException e) {
			System.err.println("Error al guardar el archivo de personas: " + e.getMessage());
			return false;
		}
	}

	private void limpiarCampos() {
		txtCedulaPersona.clear();
		txtNombrePersona.clear();
		txtApellido.clear();
		if (!cmbTipoPersona.isDisabled()) { // Solo limpiar si no está deshabilitado por rol
			cmbTipoPersona.getSelectionModel().clearSelection();
		}
		txtTelefono.clear();
		txtDireccion.clear();
		txtEmail.clear();
		txtCedulaPersona.requestFocus();
	}

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
