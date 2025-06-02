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
import javafx.util.StringConverter;
import pryfinal.modelo.Factura;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.function.Predicate;

// Clase ConsultaFactura
public class ConsultaFactura {
	// Variables
	/// FXML
	@FXML private TextField txtBuscarFactura;
	@FXML private DatePicker dateDesdeFactura;
	@FXML private DatePicker dateHastaFactura;
	@FXML private Button btnBuscarFactura;
	@FXML private Button btnRefrescarFacturas;
	@FXML private TableView<Factura> tablaFacturas;

	//// Columnas
	@FXML private TableColumn<Factura, String> colNumeroFactura;
	@FXML private TableColumn<Factura, String> colFechaEmisionFactura;
	@FXML private TableColumn<Factura, Long> colCedulaClienteFactura;
	@FXML private TableColumn<Factura, String> colNombreClienteFactura;
	@FXML private TableColumn<Factura, String> colDescripcionFactura;
	@FXML private TableColumn<Factura, Double> colSubtotalFactura;
	@FXML private TableColumn<Factura, Integer> colIVAFactura;
	@FXML private TableColumn<Factura, Double> colTotalFactura;
	@FXML private TableColumn<Factura, String> colMetodoPagoFactura;

	/// Otros
	private ObjectMapper objectMapper;
	private final String RUTA_FACTURAS_JSON = "data/facturas.json";
	private final DateTimeFormatter FORMATO_FECHA_TABLA = DateTimeFormatter.ISO_LOCAL_DATE;
	private ObservableList<Factura> listaObservableFacturas = FXCollections.observableArrayList();
	private FilteredList<Factura> facturasFiltradas;

	// Incializar
	@FXML
	public void initialize() {
		objectMapper = new ObjectMapper();
		configurarColumnasTabla();
		configurarDatePickers();
		cargarYMostrarFacturas();
		configurarFiltroDinamico();
	}

	// Configurar columnas tabla
	private void configurarColumnasTabla() {
		colNumeroFactura.setCellValueFactory(new PropertyValueFactory<>("factura"));
		colFechaEmisionFactura.setCellValueFactory(new PropertyValueFactory<>("fecha"));
		colCedulaClienteFactura.setCellValueFactory(new PropertyValueFactory<>("cedula"));
		colNombreClienteFactura.setCellValueFactory(new PropertyValueFactory<>("nombre"));
		colDescripcionFactura.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
		colSubtotalFactura.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
		colIVAFactura.setCellValueFactory(new PropertyValueFactory<>("iva"));
		colTotalFactura.setCellValueFactory(new PropertyValueFactory<>("total"));
		colMetodoPagoFactura.setCellValueFactory(new PropertyValueFactory<>("metodo"));
	}

	// Configurar date pickers
	private void configurarDatePickers() {
		StringConverter<LocalDate> converter = new StringConverter<LocalDate>() {
			@Override
			public String toString(LocalDate date) {
				return (date != null) ? FORMATO_FECHA_TABLA.format(date) : "";
			}
			@Override
			public LocalDate fromString(String string) {
				return (string != null && !string.isEmpty()) ? LocalDate.parse(string, FORMATO_FECHA_TABLA) : null;
			}
		};
		dateDesdeFactura.setConverter(converter);
		dateHastaFactura.setConverter(converter);
	}

	// Cargar y mostrar
	private void cargarYMostrarFacturas() {
		listaObservableFacturas.clear();
		File archivo = new File(RUTA_FACTURAS_JSON);
		if (archivo.exists() && archivo.length() > 0) {
			try {
				List<Factura> facturasDesdeJson = objectMapper.readValue(archivo, new TypeReference<List<Factura>>() {});
				listaObservableFacturas.addAll(facturasDesdeJson);
			} catch (IOException e) {
				System.err.println("Error al cargar facturas desde JSON: " + e.getMessage());
				mostrarAlertaError("Error de Carga", "No se pudieron cargar los datos de las facturas.");
			}
		} else {
			System.out.println("Archivo facturas.json no encontrado o vacío.");
		}
	}

	// Filtro dinamico
	private void configurarFiltroDinamico() {
		facturasFiltradas = new FilteredList<>(listaObservableFacturas, p -> true);

		// Listener del TextField (se aplica en tiempo real)
		txtBuscarFactura.textProperty().addListener((observable, oldValue, newValue) -> aplicarFiltros());
		SortedList<Factura> facturasOrdenadas = new SortedList<>(facturasFiltradas);
		facturasOrdenadas.comparatorProperty().bind(tablaFacturas.comparatorProperty());
		tablaFacturas.setItems(facturasOrdenadas);
	}

	// Boton Buscar Factura
	@FXML
	private void handleBuscarFactura(ActionEvent event) { aplicarFiltros(); }

	// Aplicar todos los filtros
	private void aplicarFiltros() {
		String textoBusqueda = txtBuscarFactura.getText().toLowerCase().trim();
		LocalDate fechaDesde = dateDesdeFactura.getValue();
		LocalDate fechaHasta = dateHastaFactura.getValue();

		// Predicados (Predicate) para cada filtro
		Predicate<Factura> predicadoTexto = factura -> {
			if (textoBusqueda.isEmpty()) { return true; }
			return factura.getFactura().toLowerCase().contains(textoBusqueda) ||
				String.valueOf(factura.getCedula()).contains(textoBusqueda) ||
				factura.getNombre().toLowerCase().contains(textoBusqueda);
		};

		Predicate<Factura> predicadoFechaDesde = factura -> {
			if (fechaDesde == null) { return true; }
			try {
				LocalDate fechaFactura = LocalDate.parse(factura.getFecha(), FORMATO_FECHA_TABLA);
				return !fechaFactura.isBefore(fechaDesde);
			} catch (DateTimeParseException e) { return true; /* o false si se considera inválida */ }
		};

		Predicate<Factura> predicadoFechaHasta = factura -> {
			if (fechaHasta == null) { return true; }
			try {
				LocalDate fechaFactura = LocalDate.parse(factura.getFecha(), FORMATO_FECHA_TABLA);
				return !fechaFactura.isAfter(fechaHasta);
			} catch (DateTimeParseException e) { return true; /* o false si se considera inválida */ }
		};

		// Combinar todos los predicados
		facturasFiltradas.setPredicate(predicadoTexto.and(predicadoFechaDesde).and(predicadoFechaHasta));
	}


	// Boton refrescar facturas
	@FXML
	private void handleRefrescarFacturas(ActionEvent event) {
		txtBuscarFactura.clear();
		dateDesdeFactura.setValue(null);
		dateHastaFactura.setValue(null);
		cargarYMostrarFacturas();
		aplicarFiltros();
		mostrarAlertaInformacion("Datos Actualizados", "La lista de facturas ha sido refrescada.");
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
