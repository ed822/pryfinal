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
import javafx.util.StringConverter;
import pryfinal.modelo.HistoriaClinica;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.function.Predicate;

// Clase ConsultaHistoriaClinica
public class ConsultaHistoriaClinica {
	// Variables
	/// FXML
	@FXML private TextField txtBuscarHistoria;
	@FXML private DatePicker dateDesdeHistoria;
	@FXML private DatePicker dateHastaHistoria;
	@FXML private Button btnBuscarHistoria;
	@FXML private Button btnRefrescarHistorias;
	@FXML private TableView<HistoriaClinica> tablaHistoriasClinicas;

	//// Columnas
	@FXML private TableColumn<HistoriaClinica, Long> colCedulaDuenoHC;
	@FXML private TableColumn<HistoriaClinica, String> colIdNombreMascotaHC;
	@FXML private TableColumn<HistoriaClinica, String> colFechaVisitaHC;
	@FXML private TableColumn<HistoriaClinica, String> colVeterinarioHC;
	@FXML private TableColumn<HistoriaClinica, String> colMotivoConsultaHC;

	/// Otros
	private ObjectMapper objectMapper;
	private final String RUTA_HISTORIAS_JSON = "data/historias_clinicas.json";
	private final DateTimeFormatter FORMATO_FECHA_TABLA = DateTimeFormatter.ISO_LOCAL_DATE;
	private ObservableList<HistoriaClinica> listaObservableHistorias = FXCollections.observableArrayList();
	private FilteredList<HistoriaClinica> historiasFiltradas;

	// Incializar
	@FXML
	public void initialize() {
		objectMapper = new ObjectMapper();
		configurarColumnasTabla();
		configurarDatePickers();
		cargarYMostrarHistorias();
		configurarFiltroDinamico();
		configurarDobleClicEnTabla();
	}

	// Configurar columnas
	private void configurarColumnasTabla() {
		colCedulaDuenoHC.setCellValueFactory(new PropertyValueFactory<>("cedula"));
		colIdNombreMascotaHC.setCellValueFactory(new PropertyValueFactory<>("nombre"));
		colFechaVisitaHC.setCellValueFactory(new PropertyValueFactory<>("fecha"));
		colVeterinarioHC.setCellValueFactory(new PropertyValueFactory<>("veterinario"));
		colMotivoConsultaHC.setCellValueFactory(new PropertyValueFactory<>("motivo"));
	}

	// Configurar date pickers
	private void configurarDatePickers() {
		StringConverter<LocalDate> converter = new StringConverter<LocalDate>() {
			@Override public String toString(LocalDate date) { return (date != null) ? FORMATO_FECHA_TABLA.format(date) : ""; }
			@Override public LocalDate fromString(String string) { return (string != null && !string.isEmpty()) ? LocalDate.parse(string, FORMATO_FECHA_TABLA) : null; }
			};
		dateDesdeHistoria.setConverter(converter);
		dateHastaHistoria.setConverter(converter);
	}

	// Cagar
	private void cargarYMostrarHistorias() {
		listaObservableHistorias.clear();
		File archivo = new File(RUTA_HISTORIAS_JSON);
		if (archivo.exists() && archivo.length() > 0) {
			try {
				List<HistoriaClinica> historiasDesdeJson = objectMapper.readValue(archivo, new TypeReference<List<HistoriaClinica>>() {});
				listaObservableHistorias.addAll(historiasDesdeJson);
			} catch (IOException e) {
				System.err.println("Error al cargar historias clínicas desde JSON: " + e.getMessage());
				mostrarAlertaError("Error de Carga", "No se pudieron cargar los datos de las historias clínicas.");
			}
		} else {
			System.out.println("Archivo historias_clinicas.json no encontrado o vacío.");
		}
	}

	// Filtro dinamico
	private void configurarFiltroDinamico() {
		historiasFiltradas = new FilteredList<>(listaObservableHistorias, p -> true);
		txtBuscarHistoria.textProperty().addListener((obs, old, val) -> aplicarFiltros());

		SortedList<HistoriaClinica> historiasOrdenadas = new SortedList<>(historiasFiltradas);
		historiasOrdenadas.comparatorProperty().bind(tablaHistoriasClinicas.comparatorProperty());
		tablaHistoriasClinicas.setItems(historiasOrdenadas);
	}

	// Doble click
	private void configurarDobleClicEnTabla() {
		tablaHistoriasClinicas.setOnMouseClicked((MouseEvent event) -> {
			if (event.getClickCount() == 2) {
				HistoriaClinica historiaSeleccionada = tablaHistoriasClinicas.getSelectionModel().getSelectedItem();
				if (historiaSeleccionada != null) {
					mostrarDetalleHistoria(historiaSeleccionada);
				}
			}
		});
	}

	// Ventana "Detalle"
	private void mostrarDetalleHistoria(HistoriaClinica historia) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/pryfinal/vista/DetalleHistoriaClinica.fxml"));
			Parent root = loader.load();

			DetalleHistoriaClinica controller = loader.getController(); // CAMBIADO AQUÍ
			controller.cargarDatos(historia);

			Stage detalleStage = new Stage();
			detalleStage.setTitle("Detalle de Historia Clínica");
			detalleStage.setScene(new Scene(root));

			detalleStage.initModality(Modality.WINDOW_MODAL);
			detalleStage.initOwner(tablaHistoriasClinicas.getScene().getWindow());

			detalleStage.showAndWait();

		} catch (IOException e) {
			System.err.println("Error al abrir detalle de historia clínica: " + e.getMessage());
			e.printStackTrace();
			mostrarAlertaError("Error", "No se pudo mostrar el detalle de la historia clínica.");
		}
	}

	// Boton Buscar
	@FXML
	private void handleBuscarHistoria(ActionEvent event) { aplicarFiltros(); }

	// Aplicar todos los filtros
	private void aplicarFiltros() {
		String textoBusqueda = txtBuscarHistoria.getText().toLowerCase().trim();
		LocalDate fechaDesde = dateDesdeHistoria.getValue();
		LocalDate fechaHasta = dateHastaHistoria.getValue();

		// Predicados (Predicate) para cada filtro
		Predicate<HistoriaClinica> predicadoTexto = historia -> {
			if (textoBusqueda.isEmpty()) return true;
			return String.valueOf(historia.getCedula()).contains(textoBusqueda) ||
				historia.getNombre().toLowerCase().contains(textoBusqueda) ||
				(historia.getVeterinario() != null && historia.getVeterinario().toLowerCase().contains(textoBusqueda));
		};

		Predicate<HistoriaClinica> predicadoFechaDesde = historia -> {
			if (fechaDesde == null) return true;
			try {
				LocalDate fechaHistoria = LocalDate.parse(historia.getFecha(), FORMATO_FECHA_TABLA);
				return !fechaHistoria.isBefore(fechaDesde);
			} catch (DateTimeParseException e) { return true; }
		};

		Predicate<HistoriaClinica> predicadoFechaHasta = historia -> {
			if (fechaHasta == null) return true;
			try {
				LocalDate fechaHistoria = LocalDate.parse(historia.getFecha(), FORMATO_FECHA_TABLA);
				return !fechaHistoria.isAfter(fechaHasta);
			} catch (DateTimeParseException e) { return true; }
		};

		historiasFiltradas.setPredicate(predicadoTexto.and(predicadoFechaDesde).and(predicadoFechaHasta));
	}

	// Boton refrescar
	@FXML
	private void handleRefrescarHistorias(ActionEvent event) {
		txtBuscarHistoria.clear();
		dateDesdeHistoria.setValue(null);
		dateHastaHistoria.setValue(null);
		cargarYMostrarHistorias();
		aplicarFiltros();
		mostrarAlertaInformacion("Datos Actualizados", "La lista de historias clínicas ha sido refrescada.");
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
