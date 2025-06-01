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
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import javafx.scene.input.MouseEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

// Imports para JSON (Jackson)
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

// Imports para archivos, listas y fecha
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

// Modelo
import pryfinal.modelo.OrdenMedica;

// Clase ConsultaOrdenMedica
public class ConsultaOrdenMedica {

	@FXML private TextField txtBuscarOrdenMedica;
	@FXML private DatePicker dateDesdeOrden;
	@FXML private DatePicker dateHastaOrden;
	@FXML private Button btnBuscarOrdenMedica;
	@FXML private Button btnRefrescarOrdenesMedicas;
	@FXML private TableView<OrdenMedica> tablaOrdenesMedicas;

	// Columnas
	@FXML private TableColumn<OrdenMedica, String> colNumeroOrden;
	@FXML private TableColumn<OrdenMedica, String> colFechaEmisionOrden;
	@FXML private TableColumn<OrdenMedica, Long> colCedulaDuenoOrden;
	@FXML private TableColumn<OrdenMedica, String> colIdNombreMascotaOrden;
	@FXML private TableColumn<OrdenMedica, String> colVeterinarioPrescribeOrden;
	@FXML private TableColumn<OrdenMedica, String> colMedicamentosBreveOrden;
	@FXML private TableColumn<OrdenMedica, String> colDuracionTratamientoOrden;

	private ObjectMapper objectMapper;
	private final String RUTA_ORDENES_JSON = "data/ordenes_medicas.json";
	private final DateTimeFormatter FORMATO_FECHA_TABLA = DateTimeFormatter.ISO_LOCAL_DATE;

	private ObservableList<OrdenMedica> listaObservableOrdenes = FXCollections.observableArrayList();
	private FilteredList<OrdenMedica> ordenesFiltradas;

	@FXML
	public void initialize() {
		objectMapper = new ObjectMapper();
		configurarColumnasTabla();
		configurarDatePickers();
		cargarYMostrarOrdenes();
		configurarFiltroDinamico();
		configurarDobleClicEnTabla();
	}

	private void configurarColumnasTabla() {
		colNumeroOrden.setCellValueFactory(new PropertyValueFactory<>("numero"));
		colFechaEmisionOrden.setCellValueFactory(new PropertyValueFactory<>("fecha"));
		colCedulaDuenoOrden.setCellValueFactory(new PropertyValueFactory<>("cedula"));
		colIdNombreMascotaOrden.setCellValueFactory(new PropertyValueFactory<>("nombre"));
		colVeterinarioPrescribeOrden.setCellValueFactory(new PropertyValueFactory<>("veterinario"));
		colMedicamentosBreveOrden.setCellValueFactory(new PropertyValueFactory<>("dosis"));
		colDuracionTratamientoOrden.setCellValueFactory(new PropertyValueFactory<>("duracion"));
	}

	private void configurarDatePickers() {
		StringConverter<LocalDate> converter = new StringConverter<LocalDate>() {
			@Override public String toString(LocalDate date) { return (date != null) ? FORMATO_FECHA_TABLA.format(date) : ""; }
			@Override public LocalDate fromString(String string) { return (string != null && !string.isEmpty()) ? LocalDate.parse(string, FORMATO_FECHA_TABLA) : null; }
			};
		dateDesdeOrden.setConverter(converter);
		dateHastaOrden.setConverter(converter);
	}

	private void cargarYMostrarOrdenes() {
		listaObservableOrdenes.clear();
		File archivo = new File(RUTA_ORDENES_JSON);
		if (archivo.exists() && archivo.length() > 0) {
			try {
				List<OrdenMedica> ordenesDesdeJson = objectMapper.readValue(archivo, new TypeReference<List<OrdenMedica>>() {});
				listaObservableOrdenes.addAll(ordenesDesdeJson);
			} catch (IOException e) {
				mostrarAlertaError("Error de Carga", "No se pudieron cargar las órdenes médicas: " + e.getMessage());
			}
		} else {
			System.out.println("Archivo ordenes_medicas.json no encontrado o vacío.");
		}
	}

	private void configurarFiltroDinamico() {
		ordenesFiltradas = new FilteredList<>(listaObservableOrdenes, p -> true);
		txtBuscarOrdenMedica.textProperty().addListener((obs, old, val) -> aplicarFiltros());

		SortedList<OrdenMedica> ordenesOrdenadas = new SortedList<>(ordenesFiltradas);
		ordenesOrdenadas.comparatorProperty().bind(tablaOrdenesMedicas.comparatorProperty());
		tablaOrdenesMedicas.setItems(ordenesOrdenadas);
	}

	private void configurarDobleClicEnTabla() {
		tablaOrdenesMedicas.setOnMouseClicked((MouseEvent event) -> {
			if (event.getClickCount() == 2) {
				OrdenMedica ordenSeleccionada = tablaOrdenesMedicas.getSelectionModel().getSelectedItem();
				if (ordenSeleccionada != null) {
					mostrarDetalleOrden(ordenSeleccionada);
				}
			}
		});
	}

	private void mostrarDetalleOrden(OrdenMedica orden) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/pryfinal/vista/DetalleOrdenMedica.fxml"));
			Parent root = loader.load();

			DetalleOrdenMedica controller = loader.getController(); // CAMBIADO AQUÍ
			controller.cargarDatos(orden);

			Stage detalleStage = new Stage();
			detalleStage.setTitle("Detalle de Orden Médica");
			detalleStage.setScene(new Scene(root));

			detalleStage.initModality(Modality.WINDOW_MODAL);
			detalleStage.initOwner(tablaOrdenesMedicas.getScene().getWindow()); 

			detalleStage.showAndWait();

		} catch (IOException e) {
			System.err.println("Error al abrir detalle de orden médica: " + e.getMessage());
			e.printStackTrace();
			mostrarAlertaError("Error", "No se pudo mostrar el detalle de la orden médica.");
		}
	}

	@FXML
	private void handleBuscarOrdenMedica(ActionEvent event) {
		aplicarFiltros();
	}

	private void aplicarFiltros() {
		String textoBusqueda = txtBuscarOrdenMedica.getText().toLowerCase().trim();
		LocalDate fechaDesde = dateDesdeOrden.getValue();
		LocalDate fechaHasta = dateHastaOrden.getValue();

		Predicate<OrdenMedica> predicadoTexto = orden -> {
			if (textoBusqueda.isEmpty()) return true;
			return orden.getNumero().toLowerCase().contains(textoBusqueda) ||
				String.valueOf(orden.getCedula()).contains(textoBusqueda) ||
				orden.getNombre().toLowerCase().contains(textoBusqueda) ||
				(orden.getVeterinario() != null && orden.getVeterinario().toLowerCase().contains(textoBusqueda));
		};

		Predicate<OrdenMedica> predicadoFechaDesde = orden -> {
			if (fechaDesde == null) return true;
			try {
				LocalDate fechaOrden = LocalDate.parse(orden.getFecha(), FORMATO_FECHA_TABLA);
				return !fechaOrden.isBefore(fechaDesde);
			} catch (DateTimeParseException e) { return true; }
		};

		Predicate<OrdenMedica> predicadoFechaHasta = orden -> {
			if (fechaHasta == null) return true;
			try {
				LocalDate fechaOrden = LocalDate.parse(orden.getFecha(), FORMATO_FECHA_TABLA);
				return !fechaOrden.isAfter(fechaHasta);
			} catch (DateTimeParseException e) { return true; }
		};

		ordenesFiltradas.setPredicate(predicadoTexto.and(predicadoFechaDesde).and(predicadoFechaHasta));
	}

	@FXML
	private void handleRefrescarOrdenesMedicas(ActionEvent event) {
		txtBuscarOrdenMedica.clear();
		dateDesdeOrden.setValue(null);
		dateHastaOrden.setValue(null);
		cargarYMostrarOrdenes();
		aplicarFiltros();
		mostrarAlertaInformacion("Datos Actualizados", "La lista de órdenes médicas ha sido refrescada.");
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
