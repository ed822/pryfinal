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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import pryfinal.modelo.Persona;
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

	// Incializar
	@FXML
	public void initialize() {
		objectMapper = new ObjectMapper();
		configurarColumnasTabla();
		cargarYMostrarPersonas();
		configurarFiltroBusqueda();
	}

	// Columnas
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
		FilteredList<Persona> personasFiltradas = new FilteredList<>(listaObservablePersonas, p -> true);

		txtBuscarPersona.textProperty().addListener((observable, oldValue, newValue) -> {
			personasFiltradas.setPredicate(persona -> {
				if (newValue == null || newValue.isEmpty()) {
					return true;
				}
				String textoBusquedaLower = newValue.toLowerCase();

				// Buscar por cedula, nombre o apellido
				if (String.valueOf(persona.getCedula()).contains(textoBusquedaLower)) {
					return true;
				} else if (persona.getNombre().toLowerCase().contains(textoBusquedaLower)) {
					return true;
				} else if (persona.getApellido().toLowerCase().contains(textoBusquedaLower)) {
					return true;
				} else if (persona.getTipo().toLowerCase().contains(textoBusquedaLower)) {
					return true;
				}
				return false;
			});
		});

		SortedList<Persona> personasOrdenadas = new SortedList<>(personasFiltradas);
		personasOrdenadas.comparatorProperty().bind(tablaPersonas.comparatorProperty());
		tablaPersonas.setItems(personasOrdenadas);
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

	// Mostrar alerta
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
