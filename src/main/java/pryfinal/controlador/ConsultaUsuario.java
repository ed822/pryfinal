// Paquete
package pryfinal.controlador;

// Imports JavaFX

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import pryfinal.modelo.Usuario;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

// Clase ConsultaUsuario (antes ConsultarUsuario)
public class ConsultaUsuario {

	@FXML private TextField txtBuscarUsuario;
	@FXML private Button btnRefrescarUsuarios;
	@FXML private TableView<Usuario> tablaUsuarios;
	@FXML private Button btnCerrarConsultaUsuarios;

	@FXML private TableColumn<Usuario, String> colNombreUsuario;
	@FXML private TableColumn<Usuario, String> colTipoUsuario;

	private ObjectMapper objectMapper;
	private final String RUTA_USUARIOS_JSON = "data/usuarios.json";
	private final String ADMIN_USER_TYPE = "admin";

	private ObservableList<Usuario> listaObservableUsuarios = FXCollections.observableArrayList();
	private FilteredList<Usuario> usuariosFiltrados;

	@FXML
	public void initialize() {
		objectMapper = new ObjectMapper();
		configurarColumnasTabla();
		cargarYMostrarUsuarios();
		configurarFiltroBusqueda();
		configurarDobleClicEnTabla();
	}

	private void configurarColumnasTabla() {
		colNombreUsuario.setCellValueFactory(new PropertyValueFactory<>("nombre"));
		colTipoUsuario.setCellValueFactory(new PropertyValueFactory<>("tipo"));
	}

	private void cargarYMostrarUsuarios() {
		listaObservableUsuarios.clear();
		File archivo = new File(RUTA_USUARIOS_JSON);
		if (archivo.exists() && archivo.length() > 0) {
			try {
				List<Usuario> todosLosUsuarios = objectMapper.readValue(archivo, new TypeReference<List<Usuario>>() {});
				List<Usuario> usuariosNoAdmin = todosLosUsuarios.stream()
					.filter(u -> !ADMIN_USER_TYPE.equals(u.getTipo()))
					.collect(Collectors.toList());
				listaObservableUsuarios.addAll(usuariosNoAdmin);
			} catch (IOException e) {
				mostrarAlertaError("Error de Carga", "No se pudieron cargar los datos de los usuarios: " + e.getMessage());
			}
		} else {
			System.out.println("Archivo usuarios.json no encontrado o vacío.");
		}
	}

	private void configurarFiltroBusqueda() {
		usuariosFiltrados = new FilteredList<>(listaObservableUsuarios, p -> true);

		txtBuscarUsuario.textProperty().addListener((observable, oldValue, newValue) -> {
			usuariosFiltrados.setPredicate(usuario -> {
				if (newValue == null || newValue.isEmpty()) {
					return true;
				}
				String textoBusquedaLower = newValue.toLowerCase();
				return usuario.getNombre().toLowerCase().contains(textoBusquedaLower) ||
					usuario.getTipo().toLowerCase().contains(textoBusquedaLower);
			});
		});

		SortedList<Usuario> usuariosOrdenados = new SortedList<>(usuariosFiltrados);
		usuariosOrdenados.comparatorProperty().bind(tablaUsuarios.comparatorProperty());
		tablaUsuarios.setItems(usuariosOrdenados);
	}

	private void configurarDobleClicEnTabla() {
		tablaUsuarios.setOnMouseClicked((MouseEvent event) -> {
			if (event.getClickCount() == 2) {
				Usuario usuarioSeleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
				if (usuarioSeleccionado != null) {
					mostrarDetalleUsuario(usuarioSeleccionado);
				}
			}
		});
	}

	private void mostrarDetalleUsuario(Usuario usuario) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/pryfinal/vista/DetalleUsuario.fxml"));
			Parent root = loader.load();

			DetalleUsuario controller = loader.getController();
			controller.setConsultaUsuarioController(this); // CORREGIDO AQUÍ
			controller.cargarDatos(usuario);


			Stage detalleStage = new Stage();
			detalleStage.setTitle("Detalle/Modificar Usuario");
			detalleStage.setScene(new Scene(root));

			detalleStage.initModality(Modality.WINDOW_MODAL);
			detalleStage.initOwner(tablaUsuarios.getScene().getWindow());

			detalleStage.showAndWait();

		} catch (IOException e) {
			System.err.println("Error al abrir detalle de usuario: " + e.getMessage());
			e.printStackTrace();
			mostrarAlertaError("Error", "No se pudo mostrar el detalle del usuario.");
		}
	}

	@FXML
	private void handleRefrescarUsuarios(ActionEvent event) {
		txtBuscarUsuario.clear();
		cargarYMostrarUsuarios();
		mostrarAlertaInformacion("Datos Actualizados", "La lista de usuarios ha sido refrescada.");
	}

	public void refrescarListaUsuarios() {
		cargarYMostrarUsuarios();
	}

	@FXML
	private void handleCerrarConsultaUsuarios(ActionEvent event) {
		Stage stage = (Stage) btnCerrarConsultaUsuarios.getScene().getWindow();
		stage.close();
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
