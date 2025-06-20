// Paquete
package pryfinal.controlador;

// Imports
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
import pryfinal.modelo.Persona;
import pryfinal.modelo.Usuario;
import java.io.File;
import java.io.IOException;
import java.util.List;

// Clase ConsultaPersona
public class ConsultaPersona {
	// Variables
	/// FXML
	@FXML private TextField txtBuscarPersona;
	@FXML private Button btnBuscarPersona;
	@FXML private Button btnRefrescarPersonas;
	@FXML private TableView<Persona> tablaPersonas;
	@FXML private TableColumn<Persona, Long> colCedulaPersona;
	@FXML private TableColumn<Persona, String> colNombrePersona;
	@FXML private TableColumn<Persona, String> colApellidoPersona;
	@FXML private TableColumn<Persona, String> colTipoPersona;
	@FXML private TableColumn<Persona, Long> colTelefonoPersona;
	@FXML private TableColumn<Persona, String> colDireccionPersona;
	@FXML private TableColumn<Persona, String> colEmailPersona;

	/// Otros
	private ObjectMapper objectMapper;
	private final String RUTA_PERSONAS_JSON = "data/personas.json";
	private ObservableList<Persona> listaObservablePersonas = FXCollections.observableArrayList();
	private FilteredList<Persona> personasFiltradas;
	private Usuario usuarioLogueado;

	// Initialize (inicializar)
	@FXML
	public void initialize() {
		objectMapper = new ObjectMapper();
		configurarColumnasTabla();
		cargarYMostrarPersonas();
		configurarFiltroBusqueda();
		configurarDobleClicEnTabla();
	}

	// Usuario actual
	public void setUsuarioActual(Usuario usuario) { this.usuarioLogueado = usuario; }

	// Tabla
	private void configurarColumnasTabla() {
		colCedulaPersona.setCellValueFactory(new PropertyValueFactory<>("cedula"));
		colNombrePersona.setCellValueFactory(new PropertyValueFactory<>("nombre"));
		colApellidoPersona.setCellValueFactory(new PropertyValueFactory<>("apellido"));
		colTipoPersona.setCellValueFactory(new PropertyValueFactory<>("tipo"));
		colTelefonoPersona.setCellValueFactory(new PropertyValueFactory<>("celular"));
		colDireccionPersona.setCellValueFactory(new PropertyValueFactory<>("direccion"));
		colEmailPersona.setCellValueFactory(new PropertyValueFactory<>("email"));
	}

	// Cargar
	private void cargarYMostrarPersonas() {
		listaObservablePersonas.clear();
		File archivo = new File(RUTA_PERSONAS_JSON);
		if (archivo.exists() && archivo.length() > 0) {
			try {
				List<Persona> personasDesdeJson = objectMapper.readValue(archivo, new TypeReference<List<Persona>>() {});
				listaObservablePersonas.addAll(personasDesdeJson);
			} catch (IOException e) {
				System.err.println("Error al cargar personas desde JSON: " + e.getMessage());
				mostrarAlertaError("Error de Carga", "No se pudieron cargar los datos de las personas.");
			}
		} else {
			System.out.println("Archivo personas.json no encontrado o vacío.");
		}
	}

	// Filtro
	private void configurarFiltroBusqueda() {
		personasFiltradas = new FilteredList<>(listaObservablePersonas, p -> true);

		txtBuscarPersona.textProperty().addListener((observable, oldValue, newValue) -> {
			personasFiltradas.setPredicate(persona -> {
				if (newValue == null || newValue.isEmpty()) {
					return true;
				}
				String textoBusquedaLower = newValue.toLowerCase();
				return String.valueOf(persona.getCedula()).contains(textoBusquedaLower) ||
					persona.getNombre().toLowerCase().contains(textoBusquedaLower) ||
					persona.getApellido().toLowerCase().contains(textoBusquedaLower) ||
					persona.getTipo().toLowerCase().contains(textoBusquedaLower);
			});
		});

		SortedList<Persona> personasOrdenadas = new SortedList<>(personasFiltradas);
		personasOrdenadas.comparatorProperty().bind(tablaPersonas.comparatorProperty());
		tablaPersonas.setItems(personasOrdenadas);
	}

	// Doble click
	private void configurarDobleClicEnTabla() {
		tablaPersonas.setOnMouseClicked((MouseEvent event) -> {
			if (event.getClickCount() == 2) {
				Persona personaSeleccionada = tablaPersonas.getSelectionModel().getSelectedItem();
				if (personaSeleccionada != null) {
					mostrarDetallePersona(personaSeleccionada);
				}
			}
		});
	}

	// Detalle
	private void mostrarDetallePersona(Persona persona) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/pryfinal/vista/DetallePersona.fxml"));
			Parent root = loader.load();

			DetallePersona controller = loader.getController();
			controller.setConsultaPersonaController(this);
			controller.cargarDatos(persona, this.usuarioLogueado);

			Stage detalleStage = new Stage();
			detalleStage.setTitle("Detalle de Persona");
			detalleStage.setScene(new Scene(root));

			detalleStage.initModality(Modality.WINDOW_MODAL);
			detalleStage.initOwner(tablaPersonas.getScene().getWindow());

			detalleStage.showAndWait();
		} catch (IOException e) {
			System.err.println("Error al abrir detalle de persona: " + e.getMessage());
			e.printStackTrace();
			mostrarAlertaError("Error", "No se pudo mostrar el detalle de la persona.");
		}
	}

	// Buscar
	@FXML
	private void handleBuscarPersona(ActionEvent event) { txtBuscarPersona.requestFocus(); }

	// Refrescar
	@FXML
	private void handleRefrescarPersonas(ActionEvent event) {
		txtBuscarPersona.clear();
		cargarYMostrarPersonas();
		mostrarAlertaInformacion("Datos Actualizados", "La lista de personas ha sido refrescada.");
	}

	// Lista personas (llamado por DetallePersona)
	public void refrescarListaPersonas() { cargarYMostrarPersonas(); }

	// Alerta
	/// Informacion
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
