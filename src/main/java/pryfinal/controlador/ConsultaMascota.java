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
import pryfinal.modelo.Mascota;
import pryfinal.modelo.Usuario;

import java.io.File;
import java.io.IOException;
import java.util.List;

// Clase ConsultaMascota
public class ConsultaMascota {

	@FXML private TextField txtBuscarMascota;
	@FXML private Button btnBuscarMascota;
	@FXML private Button btnRefrescarMascotas;
	@FXML private TableView<Mascota> tablaMascotas;

	// Columnas de la tabla
	@FXML private TableColumn<Mascota, String> colCedulaDuenoMascota;
	@FXML private TableColumn<Mascota, String> colNombreMascota;
	@FXML private TableColumn<Mascota, String> colEspecieMascota;
	@FXML private TableColumn<Mascota, String> colRazaMascota;
	@FXML private TableColumn<Mascota, Float> colEdadMascota;
	@FXML private TableColumn<Mascota, String> colSexoMascota;
	@FXML private TableColumn<Mascota, Integer> colPesoMascota;

	private ObjectMapper objectMapper;
	private final String RUTA_MASCOTAS_JSON = "data/mascotas.json";

	private ObservableList<Mascota> listaObservableMascotas = FXCollections.observableArrayList();
	private FilteredList<Mascota> mascotasFiltradas;

	private Usuario usuarioLogueado; // Para pasar al detalle

	@FXML
	public void initialize() {
		objectMapper = new ObjectMapper();
		configurarColumnasTabla();
		cargarYMostrarMascotas();
		configurarFiltroBusqueda();
		configurarDobleClicEnTabla();
	}

	/**
	 * Método para ser llamado desde MenuPrincipal para pasar el usuario logueado.
	 */
	public void setUsuarioActual(Usuario usuario) {
		this.usuarioLogueado = usuario;
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
			System.out.println("Archivo mascotas.json no encontrado o vacío.");
		}
	}

	private void configurarFiltroBusqueda() {
		mascotasFiltradas = new FilteredList<>(listaObservableMascotas, p -> true);

		txtBuscarMascota.textProperty().addListener((observable, oldValue, newValue) -> {
			mascotasFiltradas.setPredicate(mascota -> {
				if (newValue == null || newValue.isEmpty()) {
					return true;
				}
				String textoBusquedaLower = newValue.toLowerCase();
				return mascota.getCedulaDueno().toLowerCase().contains(textoBusquedaLower) ||
					mascota.getNombreMascota().toLowerCase().contains(textoBusquedaLower) ||
					mascota.getEspecie().toLowerCase().contains(textoBusquedaLower) ||
					mascota.getRaza().toLowerCase().contains(textoBusquedaLower);
			});
		});

		SortedList<Mascota> mascotasOrdenadas = new SortedList<>(mascotasFiltradas);
		mascotasOrdenadas.comparatorProperty().bind(tablaMascotas.comparatorProperty());
		tablaMascotas.setItems(mascotasOrdenadas);
	}

	private void configurarDobleClicEnTabla() {
		tablaMascotas.setOnMouseClicked((MouseEvent event) -> {
			if (event.getClickCount() == 2) {
				Mascota mascotaSeleccionada = tablaMascotas.getSelectionModel().getSelectedItem();
				if (mascotaSeleccionada != null) {
					mostrarDetalleMascota(mascotaSeleccionada);
				}
			}
		});
	}

	private void mostrarDetalleMascota(Mascota mascota) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/pryfinal/vista/DetalleMascota.fxml"));
			Parent root = loader.load();

			DetalleMascota controller = loader.getController();
			controller.setConsultaMascotaController(this); // Para refrescar
			controller.cargarDatos(mascota, this.usuarioLogueado); // PASAR USUARIO LOGUEADO

			Stage detalleStage = new Stage();
			detalleStage.setTitle("Detalle de Mascota");
			detalleStage.setScene(new Scene(root));

			detalleStage.initModality(Modality.WINDOW_MODAL);
			detalleStage.initOwner(tablaMascotas.getScene().getWindow());

			detalleStage.showAndWait();
			// refrescarListaMascotas(); // Opcional: refrescar siempre, o solo si DetalleMascota lo indica.
			// Actualmente DetalleMascota llama a refrescarListaMascotas si hay cambios.
		} catch (IOException e) {
			System.err.println("Error al abrir detalle de mascota: " + e.getMessage());
			e.printStackTrace();
			mostrarAlertaError("Error", "No se pudo mostrar el detalle de la mascota.");
		}
	}

	@FXML
	private void handleBuscarMascota(ActionEvent event) {
		// El filtro es en tiempo real, este botón podría usarse para forzar algo o quitarse.
		txtBuscarMascota.requestFocus();
	}

	@FXML
	private void handleRefrescarMascotas(ActionEvent event) {
		txtBuscarMascota.clear();
		cargarYMostrarMascotas(); // Recarga los datos y el filtro se aplica (o muestra todo si txtBuscar está vacío)
		mostrarAlertaInformacion("Datos Actualizados", "La lista de mascotas ha sido refrescada.");
	}

	/**
	 * Método público para ser llamado desde DetalleMascota después de una modificación/eliminación.
	 */
	public void refrescarListaMascotas() {
		cargarYMostrarMascotas();
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
