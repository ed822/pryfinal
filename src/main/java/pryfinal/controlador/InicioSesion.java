// Paquete
package pryfinal.controlador;

// Imports
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import pryfinal.modelo.Usuario;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Optional;

// Clase IncioSesion
public class InicioSesion {
	// Variables
	/// FXML
	@FXML private ComboBox<String> cmbTipoUsuario;
	@FXML private TextField txtNombreUsuario;
	@FXML private PasswordField txtContrasena;
	@FXML private Button btnRegistrarUsuario;
	@FXML private Button btnIniciarSesion;

	/// Otros
	private final String ADMIN_USERNAME = "admin";
	private final String ADMIN_USER_TYPE = "admin";
	private final String RUTA_DIRECTORIO_DATOS = "data";
	private final String RUTA_USUARIOS_JSON = RUTA_DIRECTORIO_DATOS + "/usuarios.json";
	private ObjectMapper objectMapper;

	// Initialize (inicializar)
	@FXML
	public void initialize() {
		objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

		File directorioDatos = new File(RUTA_DIRECTORIO_DATOS);
		if (!directorioDatos.exists()) { directorioDatos.mkdirs(); }

		cmbTipoUsuario.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
			if (ADMIN_USER_TYPE.equals(newValue)) {
				txtNombreUsuario.setText(ADMIN_USERNAME);
				txtNombreUsuario.setDisable(true);
				btnRegistrarUsuario.setDisable(verificarSiAdminExiste());
			} else {
				if (ADMIN_USERNAME.equals(txtNombreUsuario.getText()) && txtNombreUsuario.isDisabled()) {
					txtNombreUsuario.clear();
				}
				txtNombreUsuario.setDisable(false);
				btnRegistrarUsuario.setDisable(false);
			}
		});

		if (cmbTipoUsuario.getSelectionModel().isEmpty()) {
			txtNombreUsuario.setDisable(false);
			btnRegistrarUsuario.setDisable(false);
		} else if (ADMIN_USER_TYPE.equals(cmbTipoUsuario.getValue())) {
			btnRegistrarUsuario.setDisable(verificarSiAdminExiste());
		}
	}

	// Verificar si admin existe
	private boolean verificarSiAdminExiste() {
		List<Usuario> usuarios = cargarUsuarios();
		return usuarios.stream()
			.anyMatch(u -> ADMIN_USER_TYPE.equals(u.getTipo()) && ADMIN_USERNAME.equals(u.getNombre()));
	}

	// Registrar
	@FXML
	private void handleRegistrarUsuario(ActionEvent event) {
		String tipoUsuario = cmbTipoUsuario.getValue();
		String nombreUsuario = txtNombreUsuario.getText().trim();
		String contrasena = txtContrasena.getText();

		if (tipoUsuario == null || tipoUsuario.isEmpty()) {
			mostrarAlerta("Error de Registro", "Por favor, seleccione un tipo de usuario.");
			return;
		}
		if (nombreUsuario.isEmpty()) {
			mostrarAlerta("Error de Registro", "Por favor, ingrese un nombre de usuario.");
			return;
		}
		if (contrasena.isEmpty()) {
			mostrarAlerta("Error de Registro", "Por favor, ingrese una contraseña.");
			return;
		}

		List<Usuario> usuarios = cargarUsuarios();

		// Verificar si "admin" está siendo usado por un tipo no admin
		if (!ADMIN_USER_TYPE.equals(tipoUsuario) && ADMIN_USERNAME.equalsIgnoreCase(nombreUsuario)) {
			mostrarAlerta("Error de Registro", "El nombre de usuario '" + ADMIN_USERNAME + "' está reservado para el administrador.");
			return;
		}

		// Lógica para el tipo admin
		if (ADMIN_USER_TYPE.equals(tipoUsuario)) {
			if (verificarSiAdminExiste()) {
				mostrarAlerta("Error de Registro", "El usuario 'admin' ya ha sido registrado. No se pueden crear más administradores.");
				return;
			}
		} else {
			// Verificar si el nombre de usuario (no admin) ya existe
			boolean nombreUsuarioGlobalmenteExiste = usuarios.stream()
				.anyMatch(u -> u.getNombre().equalsIgnoreCase(nombreUsuario));
			if (nombreUsuarioGlobalmenteExiste) {
				mostrarAlerta("Error de Registro", "El nombre de usuario '" + nombreUsuario + "' ya está en uso. Por favor, elija otro.");
				return;
			}
		}

		String contrasenaHasheada = hashearContrasena(contrasena);
		Usuario nuevoUsuario = new Usuario(tipoUsuario, nombreUsuario, contrasenaHasheada);
		usuarios.add(nuevoUsuario);

		if (guardarUsuarios(usuarios)) {
			mostrarAlerta("Registro Exitoso", "Usuario '" + nombreUsuario + "' (" + tipoUsuario + ") registrado correctamente.");
			limpiarCampos();
			// Si se registró el admin, deshabilitar el botón de registro de admin
			if (ADMIN_USER_TYPE.equals(tipoUsuario)) {
				btnRegistrarUsuario.setDisable(true);
			}
		} else {
			mostrarAlerta("Error de Registro", "No se pudo guardar el usuario. Intente de nuevo.");
		}
	}

	// Inciar sesion
	@FXML
	private void handleIniciarSesion(ActionEvent event) {
		String tipoUsuarioSeleccionado = cmbTipoUsuario.getValue();
		String nombreUsuarioIngresado = txtNombreUsuario.getText().trim();
		String contrasenaIngresada = txtContrasena.getText();

		if (tipoUsuarioSeleccionado == null || tipoUsuarioSeleccionado.isEmpty()) {
			mostrarAlerta("Error de Inicio de Sesión", "Por favor, seleccione un tipo de usuario.");
			return;
		}
		if (nombreUsuarioIngresado.isEmpty()) {
			mostrarAlerta("Error de Inicio de Sesión", "Por favor, ingrese un nombre de usuario.");
			return;
		}
		if (contrasenaIngresada.isEmpty()) {
			mostrarAlerta("Error de Inicio de Sesión", "Por favor, ingrese una contraseña.");
			return;
		}

		List<Usuario> usuarios = cargarUsuarios();
		String contrasenaIngresadaHasheada = hashearContrasena(contrasenaIngresada);

		Optional<Usuario> usuarioAutenticado = usuarios.stream()
			.filter(u -> u.getTipo().equals(tipoUsuarioSeleccionado) &&
					u.getNombre().equalsIgnoreCase(nombreUsuarioIngresado) &&
					u.getContrasena().equals(contrasenaIngresadaHasheada))
			.findFirst();

		if (usuarioAutenticado.isPresent()) {
			mostrarAlerta("Inicio de Sesión Exitoso", "¡Bienvenido " + nombreUsuarioIngresado + "!");
			abrirMenuPrincipal(event, usuarioAutenticado.get());
		} else {
			mostrarAlerta("Error de Inicio de Sesión", "Tipo de usuario, nombre de usuario o contraseña incorrectos.");
		}
	}

	// Cargar
	private List<Usuario> cargarUsuarios() {
		File archivo = new File(RUTA_USUARIOS_JSON);
		if (archivo.exists() && archivo.length() > 0) {
			try {
				return objectMapper.readValue(archivo, new TypeReference<List<Usuario>>() {});
			} catch (IOException e) {
				System.err.println("Error al leer el archivo de usuarios: " + e.getMessage());
			}
		}
		return new ArrayList<>();
	}

	// Guardar
	private boolean guardarUsuarios(List<Usuario> usuarios) {
		try {
			File directorioDatos = new File(RUTA_DIRECTORIO_DATOS);
			if (!directorioDatos.exists()) {
				if (!directorioDatos.mkdirs()) {
					System.err.println("No se pudo crear el directorio de datos: " + RUTA_DIRECTORIO_DATOS);
					return false;
				}
			}
			objectMapper.writeValue(new File(RUTA_USUARIOS_JSON), usuarios);
			return true;
		} catch (IOException e) {
			System.err.println("Error al guardar el archivo de usuarios: " + e.getMessage());
			return false;
		}
	}

	// Hashear contresena
	private String hashearContrasena(String contrasena) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashBytes = digest.digest(contrasena.getBytes(StandardCharsets.UTF_8));
			Formatter formatter = new Formatter();
			for (byte b : hashBytes) {
				formatter.format("%02x", b);
			}
			String hashHex = formatter.toString();
			formatter.close();
			return hashHex;
		} catch (NoSuchAlgorithmException e) {
			System.err.println("Error: Algoritmo SHA-256 no encontrado. " + e.getMessage());
			return "error_hash_" + contrasena;
		}
	}

	// Menu principal
	private void abrirMenuPrincipal(ActionEvent event, Usuario usuarioLogueado) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/pryfinal/vista/MenuPrincipal.fxml"));
			Parent root = loader.load();

			Stage escenarioMenuPrincipal = new Stage();
			escenarioMenuPrincipal.setTitle("Menú Principal - Veterinaria");
			escenarioMenuPrincipal.setScene(new Scene(root, 800, 600));

			MenuPrincipal controladorMenuPrincipal = loader.getController();
			controladorMenuPrincipal.configurarParaUsuario(usuarioLogueado, escenarioMenuPrincipal);

			escenarioMenuPrincipal.show();

			((Stage) ((Node) event.getSource()).getScene().getWindow()).close();

		} catch (IOException e) {
			System.err.println("Error al cargar el Menú Principal: " + e.getMessage());
			e.printStackTrace();
			mostrarAlerta("Error Crítico", "No se pudo cargar la pantalla principal: " + e.getMessage());
		}
	}

	// Mostrar alerta
	private void mostrarAlerta(String titulo, String mensaje) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		if (titulo.toLowerCase().contains("error")) {
			alert.setAlertType(Alert.AlertType.ERROR);
		}
		alert.setTitle(titulo);
		alert.setHeaderText(null);
		alert.setContentText(mensaje);
		alert.showAndWait();
	}

	// Limpiar campos
	private void limpiarCampos() {
		if (cmbTipoUsuario.getValue() == null || !ADMIN_USER_TYPE.equals(cmbTipoUsuario.getValue())) {
			txtNombreUsuario.clear();
		}
		txtContrasena.clear();
	}
}
