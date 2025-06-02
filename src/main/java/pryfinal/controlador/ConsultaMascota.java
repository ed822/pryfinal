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
import pryfinal.modelo.Mascota;
import java.io.File;
import java.io.IOException;
import java.util.List;

// Clase ConsultaMascota
public class ConsultaMascota {
	// Variables
	/// FXML
	@FXML private TextField txtBuscarMascota;
	@FXML private Button btnBuscarMascota;
	@FXML private Button btnRefrescarMascotas;
	@FXML private TableView<Mascota> tablaMascotas;

	//// Columnas
	@FXML private TableColumn<Mascota, String> colCedulaDuenoMascota;
	@FXML private TableColumn<Mascota, String> colNombreMascota;
	@FXML private TableColumn<Mascota, String> colEspecieMascota;
	@FXML private TableColumn<Mascota, String> colRazaMascota;
	@FXML private TableColumn<Mascota, Float> colEdadMascota;
	@FXML private TableColumn<Mascota, String> colSexoMascota;
	@FXML private TableColumn<Mascota, Integer> colPesoMascota;

	/// Otros
	private ObjectMapper objectMapper;
	private final String RUTA_MASCOTAS_JSON = "data/mascotas.json";
	private ObservableList<Mascota> listaObservableMascotas = FXCollections.observableArrayList();

	// Inicializar
	@FXML
	public void initialize() {
		objectMapper = new ObjectMapper();
		configurarColumnasTabla();
		cargarYMostrarMascotas();
		configurarFiltroBusqueda();
	}

	// Configurar columnas
	private void configurarColumnasTabla() {
		colCedulaDuenoMascota.setCellValueFactory(new PropertyValueFactory<>("cedulaDueno"));
		colNombreMascota.setCellValueFactory(new PropertyValueFactory<>("nombreMascota"));
		colEspecieMascota.setCellValueFactory(new PropertyValueFactory<>("especie"));
		colRazaMascota.setCellValueFactory(new PropertyValueFactory<>("raza"));
		colEdadMascota.setCellValueFactory(new PropertyValueFactory<>("edad"));
		colSexoMascota.setCellValueFactory(new PropertyValueFactory<>("sexo"));
		colPesoMascota.setCellValueFactory(new PropertyValueFactory<>("peso"));
	}

	// Cargar
	private void cargarYMostrarMascotas() {
		listaObservableMascotas.clear();
		File archivo = new File(RUTA_MASCOTAS_JSON);
		if (archivo.exists() && archivo.length() > 0) {
			try {
				List<Mascota> mascotasDesdeJson = objectMapper.readValue(archivo, new TypeReference<List<Mascota>>() {});
				listaObservableMascotas.addAll(mascotasDesdeJson);
			} catch (IOException e) {
				System.err.println("Error al cargar mascotas desde JSON: " + e.getMessage());
				mostrarAlertaError("Error de Carga", "No se pudieron cargar los datos de las mascotas.");
			}
		} else {
			System.out.println("Archivo mascotas.json no encontrado o vacío. No se cargaron mascotas.");
		}
	}

	// Filtro de Busqueda
	private void configurarFiltroBusqueda() {
		FilteredList<Mascota> mascotasFiltradas = new FilteredList<>(listaObservableMascotas, p -> true);

		txtBuscarMascota.textProperty().addListener((observable, oldValue, newValue) -> {
			mascotasFiltradas.setPredicate(mascota -> {
				// Mostrar todas las mascotas si el filtro está vacío
				if (newValue == null || newValue.isEmpty()) { return true; }
				String textoBusquedaLower = newValue.toLowerCase();

				// Buscar por cédula del dueño o nombre de la mascota
				if (mascota.getCedulaDueno().toLowerCase().contains(textoBusquedaLower)) {
					return true;
				} else if (mascota.getNombreMascota().toLowerCase().contains(textoBusquedaLower)) {
					return true;
				} else if (mascota.getEspecie().toLowerCase().contains(textoBusquedaLower)) {
					return true;
				} else if (mascota.getRaza().toLowerCase().contains(textoBusquedaLower)) {
					return true;
				}
				return false;
			});
		});

		// FilteredList en SortedList (ordenar la tabla cuando se hace click en cabeceras de columna).
		SortedList<Mascota> mascotasOrdenadas = new SortedList<>(mascotasFiltradas);
		mascotasOrdenadas.comparatorProperty().bind(tablaMascotas.comparatorProperty());
		tablaMascotas.setItems(mascotasOrdenadas);
	}

	// Boton mostrar mascota
	@FXML
	private void handleBuscarMascota(ActionEvent event) { txtBuscarMascota.requestFocus(); }

	// Boton refrescar mascotas
	@FXML
	private void handleRefrescarMascotas(ActionEvent event) {
		txtBuscarMascota.clear();
		cargarYMostrarMascotas();
		mostrarAlertaInformacion("Datos Actualizados", "La lista de mascotas ha sido refrescada.");
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
