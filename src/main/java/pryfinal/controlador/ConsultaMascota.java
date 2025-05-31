// Paquete
package pryfinal.controlador;

// Imports JavaFX
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

// Imports para JSON (Jackson)
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

// Imports para archivos y listas
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Modelo
import pryfinal.modelo.Mascota;

// Clase ConsultaMascota
public class ConsultaMascota {

	@FXML private TextField txtBuscarMascota;
	@FXML private Button btnBuscarMascota; // Aunque el filtro es en tiempo real, el botón puede forzar la búsqueda
	@FXML private Button btnRefrescarMascotas;
	@FXML private TableView<Mascota> tablaMascotas;

	// Columnas de la tabla (deben coincidir con fx:id en FXML y nombres de propiedades en Mascota.java)
	@FXML private TableColumn<Mascota, String> colCedulaDuenoMascota;
	@FXML private TableColumn<Mascota, String> colNombreMascota;
	@FXML private TableColumn<Mascota, String> colEspecieMascota;
	@FXML private TableColumn<Mascota, String> colRazaMascota;
	@FXML private TableColumn<Mascota, Float> colEdadMascota; // Tipo Float para edad
	@FXML private TableColumn<Mascota, String> colSexoMascota;
	@FXML private TableColumn<Mascota, Integer> colPesoMascota; // Tipo Integer para peso

	private ObjectMapper objectMapper;
	private final String RUTA_MASCOTAS_JSON = "data/mascotas.json";

	private ObservableList<Mascota> listaObservableMascotas = FXCollections.observableArrayList();

	@FXML
	public void initialize() {
		objectMapper = new ObjectMapper();
		configurarColumnasTabla();
		cargarYMostrarMascotas();
		configurarFiltroBusqueda();
	}

	private void configurarColumnasTabla() {
		colCedulaDuenoMascota.setCellValueFactory(new PropertyValueFactory<>("cedulaDueno"));
		colNombreMascota.setCellValueFactory(new PropertyValueFactory<>("nombreMascota"));
		colEspecieMascota.setCellValueFactory(new PropertyValueFactory<>("especie"));
		colRazaMascota.setCellValueFactory(new PropertyValueFactory<>("raza"));
		colEdadMascota.setCellValueFactory(new PropertyValueFactory<>("edad"));
		colSexoMascota.setCellValueFactory(new PropertyValueFactory<>("sexo"));
		colPesoMascota.setCellValueFactory(new PropertyValueFactory<>("peso"));
	}

	private void cargarYMostrarMascotas() {
		listaObservableMascotas.clear(); // Limpiar lista antes de cargar
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
			// Podrías mostrar un mensaje en la UI o dejar la tabla vacía.
		}
		// tablaMascotas.setItems(listaObservableMascotas); // Se maneja a través de SortedList
	}

	private void configurarFiltroBusqueda() {
		FilteredList<Mascota> mascotasFiltradas = new FilteredList<>(listaObservableMascotas, p -> true);

		txtBuscarMascota.textProperty().addListener((observable, oldValue, newValue) -> {
			mascotasFiltradas.setPredicate(mascota -> {
				if (newValue == null || newValue.isEmpty()) {
					return true; // Mostrar todas las mascotas si el filtro está vacío
				}
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
				return false; // No coincide
			});
		});

		// Envolver la FilteredList en una SortedList.
		// La SortedList permite ordenar la tabla al hacer clic en las cabeceras de columna.
		SortedList<Mascota> mascotasOrdenadas = new SortedList<>(mascotasFiltradas);
		mascotasOrdenadas.comparatorProperty().bind(tablaMascotas.comparatorProperty()); // Enlazar el comparador de la tabla
		tablaMascotas.setItems(mascotasOrdenadas); // Establecer los items de la tabla
	}

	@FXML
	private void handleBuscarMascota(ActionEvent event) {
		// El filtro ya se aplica en tiempo real con el listener del TextField.
		// Este botón podría ser útil si el listener se quita o para forzar una actualización.
		// Por ahora, podemos dejarlo así o hacer que simplemente ponga el foco en el campo de búsqueda.
		txtBuscarMascota.requestFocus();
	}

	@FXML
	private void handleRefrescarMascotas(ActionEvent event) {
		txtBuscarMascota.clear(); // Limpiar el campo de búsqueda
		cargarYMostrarMascotas(); // Recargar los datos
															// El filtro se aplicará automáticamente porque txtBuscarMascota está vacío.
		mostrarAlertaInformacion("Datos Actualizados", "La lista de mascotas ha sido refrescada.");
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
