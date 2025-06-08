// Paquete
package pryfinal.controlador;

// Imports
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
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

// Clase DetalleUsuario
public class DetalleUsuario {
	// Variables
	/// FXML
	@FXML private Label lblTituloDetalleUsuario;
	@FXML private TextField txtNombreUsuarioDetalle;
	@FXML private ComboBox<String> cmbTipoUsuarioDetalle;
	@FXML private Label lblContrasena;
	@FXML private PasswordField txtContrasenaDetalle;
	@FXML private HBox botonesAccionBox;
	@FXML private Button btnModificarUsuario;
	@FXML private Button btnEliminarUsuario;
	@FXML private HBox botonesEdicionBox;
	@FXML private Button btnGuardarCambios;
	@FXML private Button btnDescartarCambios;
	@FXML private Button btnCerrarDetalle;

	// Otros
	private Usuario usuarioSeleccionado;
	private boolean enModoEdicion = false;
	private ConsultaUsuario consultaUsuarioController;
	private ObjectMapper objectMapper;
	private final String RUTA_USUARIOS_JSON = "data/usuarios.json";

	// Initialize (inicializar)
	@FXML
	public void initialize() {
		objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		cmbTipoUsuarioDetalle.setItems(FXCollections.observableArrayList("veterinario", "vendedor"));
		actualizarVisibilidadBotonesEdicion();
	}

	// Consulta controlador
	public void setConsultaUsuarioController(ConsultaUsuario controller) {
		this.consultaUsuarioController = controller;
	}

	// Cargar
	public void cargarDatos(Usuario usuario) {
		this.usuarioSeleccionado = usuario;
		if (usuario != null) {
			txtNombreUsuarioDetalle.setText(usuario.getNombre());
			cmbTipoUsuarioDetalle.setValue(usuario.getTipo());
			txtContrasenaDetalle.setText("●●●●");
			txtContrasenaDetalle.setPromptText("●●●●");
		}
		salirModoEdicion();
	}

	// Modificar
	@FXML
	private void handleModificarUsuario(ActionEvent event) {
		entrarModoEdicion();
	}

	// Modo edicion
	private void entrarModoEdicion() {
		enModoEdicion = true;
		lblTituloDetalleUsuario.setText("Modificar Usuario");
		cmbTipoUsuarioDetalle.setDisable(false);
		txtContrasenaDetalle.setEditable(true);
		txtContrasenaDetalle.clear();
		txtContrasenaDetalle.setPromptText("Nueva contraseña (dejar vacío para no cambiar)");
		actualizarVisibilidadBotonesEdicion();
	}

	// Salir de modo edicion
	private void salirModoEdicion() {
		enModoEdicion = false;
		lblTituloDetalleUsuario.setText("Detalle de Usuario");
		cmbTipoUsuarioDetalle.setDisable(true);
		txtContrasenaDetalle.setEditable(false);
		txtContrasenaDetalle.setText("●●●●");
		txtContrasenaDetalle.setPromptText("●●●●");
		if (usuarioSeleccionado != null) {
			cmbTipoUsuarioDetalle.setValue(usuarioSeleccionado.getTipo());
		}
		actualizarVisibilidadBotonesEdicion();
	}

	// Visivilidad de botones
	private void actualizarVisibilidadBotonesEdicion() {
		botonesAccionBox.setVisible(!enModoEdicion);
		botonesAccionBox.setManaged(!enModoEdicion);
		botonesEdicionBox.setVisible(enModoEdicion);
		botonesEdicionBox.setManaged(enModoEdicion);
		btnCerrarDetalle.setVisible(!enModoEdicion);
	}

	// Guardar
	@FXML
	private void handleGuardarCambios(ActionEvent event) {
		if (usuarioSeleccionado == null) return;

		String nuevoTipo = cmbTipoUsuarioDetalle.getValue();
		String nuevaContrasena = txtContrasenaDetalle.getText();

		List<Usuario> usuarios = cargarUsuarios();
		Optional<Usuario> usuarioParaActualizarOpt = usuarios.stream()
			.filter(u -> u.getNombre().equals(usuarioSeleccionado.getNombre()))
			.findFirst();

		if (usuarioParaActualizarOpt.isPresent()) {
			Usuario usuarioParaActualizar = usuarioParaActualizarOpt.get();
			boolean cambiosRealizados = false;

			if (nuevoTipo != null && !nuevoTipo.equals(usuarioParaActualizar.getTipo())) {
				usuarioParaActualizar.setTipo(nuevoTipo);
				cambiosRealizados = true;
			}

			if (nuevaContrasena != null && !nuevaContrasena.isEmpty()) {
				usuarioParaActualizar.setContrasena(hashearContrasena(nuevaContrasena));
				cambiosRealizados = true;
			}

			if (cambiosRealizados) {
				if (guardarUsuarios(usuarios)) {
					mostrarAlerta("Éxito", "Usuario '" + usuarioSeleccionado.getNombre() + "' actualizado correctamente.");
					this.usuarioSeleccionado = usuarioParaActualizar;
					if (consultaUsuarioController != null) {
						consultaUsuarioController.refrescarListaUsuarios();
					}
				} else {
					mostrarAlertaError("Error", "No se pudo guardar los cambios del usuario.");
				}
			} else {
				mostrarAlerta("Información", "No se realizaron cambios.");
			}
		} else {
			mostrarAlertaError("Error", "No se encontró el usuario para actualizar.");
		}
		salirModoEdicion();
		cargarDatos(this.usuarioSeleccionado);
	}

	// Descartar
	@FXML
	private void handleDescartarCambios(ActionEvent event) {
		salirModoEdicion();
		cargarDatos(this.usuarioSeleccionado);
	}

	// Eliminar
	@FXML
	private void handleEliminarUsuario(ActionEvent event) {
		if (usuarioSeleccionado == null) return;

		Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
		confirmacion.setTitle("Confirmar Eliminación");
		confirmacion.setHeaderText("Eliminar Usuario: " + usuarioSeleccionado.getNombre());
		confirmacion.setContentText("¿Está seguro de que desea eliminar este usuario? Esta acción no se puede deshacer.");

		Optional<ButtonType> resultado = confirmacion.showAndWait();
		if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
			List<Usuario> usuarios = cargarUsuarios();
			boolean eliminado = usuarios.removeIf(u -> u.getNombre().equals(usuarioSeleccionado.getNombre()));

			if (eliminado) {
				if (guardarUsuarios(usuarios)) {
					mostrarAlerta("Éxito", "Usuario '" + usuarioSeleccionado.getNombre() + "' eliminado correctamente.");
					if (consultaUsuarioController != null) {
						consultaUsuarioController.refrescarListaUsuarios();
					}
					handleCerrarVentana(null);
				} else {
					mostrarAlertaError("Error", "No se pudo eliminar el usuario del archivo.");
				}
			} else {
				mostrarAlertaError("Error", "No se encontró el usuario para eliminar.");
			}
		}
	}

	// Cerrar ventana
	@FXML
	private void handleCerrarVentana(ActionEvent event) {
		Stage stage = (Stage) btnCerrarDetalle.getScene().getWindow();
		stage.close();
	}

	// Cargar usuarios
	private List<Usuario> cargarUsuarios() {
		File archivo = new File(RUTA_USUARIOS_JSON);
		if (archivo.exists() && archivo.length() > 0) {
			try { return objectMapper.readValue(archivo, new TypeReference<List<Usuario>>() {}); }
			catch (IOException e) { System.err.println("Error al leer usuarios: " + e.getMessage()); }
		}
		return new ArrayList<>();
	}

	// Guardar usuarios
	private boolean guardarUsuarios(List<Usuario> usuarios) {
		try { objectMapper.writeValue(new File(RUTA_USUARIOS_JSON), usuarios); return true; }
		catch (IOException e) { System.err.println("Error al guardar usuarios: " + e.getMessage()); return false; }
	}

	// Hashear contrasena
	private String hashearContrasena(String contrasena) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashBytes = digest.digest(contrasena.getBytes(StandardCharsets.UTF_8));
			Formatter formatter = new Formatter();
			for (byte b : hashBytes) { formatter.format("%02x", b); }
			String hashHex = formatter.toString();
			formatter.close();
			return hashHex;
		} catch (NoSuchAlgorithmException e) {
			System.err.println("Error SHA-256: " + e.getMessage()); return "error_hash_" + contrasena;
		}
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
}
