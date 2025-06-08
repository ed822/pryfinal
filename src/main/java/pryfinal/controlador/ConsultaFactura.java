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
import pryfinal.modelo.Factura;
import pryfinal.modelo.Usuario;
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
	private Usuario usuarioLogueado;

	// Initialize (inicializar)
	@FXML
	public void initialize() {
		objectMapper = new ObjectMapper();
		configurarColumnasTabla();
		configurarDatePickers();
		cargarYMostrarFacturas();
		configurarFiltroDinamico();
		configurarDobleClicEnTabla();
	}

	// Usuario actual (llamado por MenuPrincipal)
	public void setUsuarioActual(Usuario usuario) { this.usuarioLogueado = usuario; }

	// Columnas
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

	// Date picker
	private void configurarDatePickers() {
		StringConverter<LocalDate> converter = new StringConverter<LocalDate>() {
			@Override public String toString(LocalDate date) { return (date != null) ? FORMATO_FECHA_TABLA.format(date) : ""; }
			@Override public LocalDate fromString(String string) { return (string != null && !string.isEmpty()) ? LocalDate.parse(string, FORMATO_FECHA_TABLA) : null; }
			};
		dateDesdeFactura.setConverter(converter);
		dateHastaFactura.setConverter(converter);
	}

	// Cargar
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

	// Filtro
	private void configurarFiltroDinamico() {
		facturasFiltradas = new FilteredList<>(listaObservableFacturas, p -> true);
		txtBuscarFactura.textProperty().addListener((observable, oldValue, newValue) -> aplicarFiltros());
		SortedList<Factura> facturasOrdenadas = new SortedList<>(facturasFiltradas);
		facturasOrdenadas.comparatorProperty().bind(tablaFacturas.comparatorProperty());
		tablaFacturas.setItems(facturasOrdenadas);
	}

	// Doble click
	private void configurarDobleClicEnTabla() {
		tablaFacturas.setOnMouseClicked((MouseEvent event) -> {
			if (event.getClickCount() == 2) {
				Factura facturaSeleccionada = tablaFacturas.getSelectionModel().getSelectedItem();
				if (facturaSeleccionada != null) {
					mostrarDetalleFactura(facturaSeleccionada);
				}
			}
		});
	}

	// Detalle
	private void mostrarDetalleFactura(Factura factura) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/pryfinal/vista/DetalleFactura.fxml"));
			Parent root = loader.load();

			DetalleFactura controller = loader.getController();
			controller.setConsultaFacturaController(this);
			controller.cargarDatos(factura, this.usuarioLogueado);

			Stage detalleStage = new Stage();
			detalleStage.setTitle("Detalle de Factura");
			detalleStage.setScene(new Scene(root));

			detalleStage.initModality(Modality.WINDOW_MODAL);
			detalleStage.initOwner(tablaFacturas.getScene().getWindow());

			detalleStage.showAndWait();
		} catch (IOException e) {
			System.err.println("Error al abrir detalle de factura: " + e.getMessage());
			e.printStackTrace();
			mostrarAlertaError("Error", "No se pudo mostrar el detalle de la factura.");
		}
	}

	// Buscar
	@FXML
	private void handleBuscarFactura(ActionEvent event) { aplicarFiltros(); }

	// Aplicar filtros
	private void aplicarFiltros() {
		String textoBusqueda = txtBuscarFactura.getText().toLowerCase().trim();
		LocalDate fechaDesde = dateDesdeFactura.getValue();
		LocalDate fechaHasta = dateHastaFactura.getValue();

		Predicate<Factura> predicadoTexto = factura -> {
			if (textoBusqueda.isEmpty()) return true;
			return factura.getFactura().toLowerCase().contains(textoBusqueda) ||
				String.valueOf(factura.getCedula()).contains(textoBusqueda) ||
				factura.getNombre().toLowerCase().contains(textoBusqueda);
		};
		Predicate<Factura> predicadoFechaDesde = factura -> {
			if (fechaDesde == null) return true;
			try { LocalDate fechaFactura = LocalDate.parse(factura.getFecha(), FORMATO_FECHA_TABLA); return !fechaFactura.isBefore(fechaDesde); }
			catch (DateTimeParseException e) { return true; }
		};
		Predicate<Factura> predicadoFechaHasta = factura -> {
			if (fechaHasta == null) return true;
			try { LocalDate fechaFactura = LocalDate.parse(factura.getFecha(), FORMATO_FECHA_TABLA); return !fechaFactura.isAfter(fechaHasta); }
			catch (DateTimeParseException e) { return true; }
		};
		facturasFiltradas.setPredicate(predicadoTexto.and(predicadoFechaDesde).and(predicadoFechaHasta));
	}

	// Refrescar
	@FXML
	private void handleRefrescarFacturas(ActionEvent event) {
		txtBuscarFactura.clear();
		dateDesdeFactura.setValue(null);
		dateHastaFactura.setValue(null);
		cargarYMostrarFacturas();
		aplicarFiltros();
		mostrarAlertaInformacion("Datos Actualizados", "La lista de facturas ha sido refrescada.");
	}

	// Refrescar (llamado por DetalleFactura)
	public void refrescarListaFacturas() { cargarYMostrarFacturas(); }

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
