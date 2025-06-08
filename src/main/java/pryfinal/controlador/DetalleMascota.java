// Paquete
package pryfinal.controlador;

// Imports
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import pryfinal.modelo.Mascota;
import pryfinal.modelo.Usuario;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

// Clase DetalleMascota
public class DetalleMascota {
	// Variables
	/// FXML
	@FXML private Label lblTituloDetalleMascota;
	@FXML private TextField txtCedulaDueno;
	@FXML private TextField txtNombreMascota;
	@FXML private TextField txtEspecie;
	@FXML private TextField txtRaza;
	@FXML private TextField txtEdad;
	@FXML private ComboBox<String> cmbSexo;
	@FXML private TextField txtPeso;
	@FXML private HBox botonesAccionBoxMascota;
	@FXML private Button btnModificarMascota;
	@FXML private Button btnEliminarMascota;
	@FXML private HBox botonesEdicionBoxMascota;
	@FXML private Button btnGuardarCambiosMascota;
	@FXML private Button btnDescartarCambiosMascota;
	@FXML private Button btnCerrarDetalleMascota;

	/// Otros
	private Mascota mascotaSeleccionada;
	private Usuario usuarioActual;
	private ConsultaMascota consultaMascotaController;
	private boolean enModoEdicion = false;
	private ObjectMapper objectMapper;
	private final String RUTA_MASCOTAS_JSON = "data/mascotas.json";
	private final String ADMIN_USER_TYPE = "admin";
	private final DecimalFormat FORMATO_EDAD = new DecimalFormat("#0.0");
	private final Pattern PATRON_TEXTO_GENERAL = Pattern.compile("^[\\p{L} .'-]+$");

	// Initialize (incializer)
	@FXML
	public void initialize() {
		objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		actualizarVisibilidadBotonesEdicion();
	}

	// Controlador Consulta
	public void setConsultaMascotaController(ConsultaMascota controller) {
		this.consultaMascotaController = controller;
	}

	// Cargar
	public void cargarDatos(Mascota mascota, Usuario usuarioLogueado) {
		this.mascotaSeleccionada = mascota;
		this.usuarioActual = usuarioLogueado;

		if (mascota != null) {
			txtCedulaDueno.setText(mascota.getCedulaDueno());
			txtNombreMascota.setText(mascota.getNombreMascota());
			txtEspecie.setText(mascota.getEspecie());
			txtRaza.setText(mascota.getRaza());
			txtEdad.setText(FORMATO_EDAD.format(mascota.getEdad()));
			cmbSexo.setValue(mascota.getSexo());
			txtPeso.setText(String.valueOf(mascota.getPeso()));
		}
		configurarSegunRolUsuario();
		salirModoEdicion();
	}

	// Rol usuario
	private void configurarSegunRolUsuario() {
		boolean esAdmin = usuarioActual != null && ADMIN_USER_TYPE.equals(usuarioActual.getTipo());
		btnModificarMascota.setDisable(!esAdmin);
		btnEliminarMascota.setDisable(!esAdmin);
	}

	// Modo edicion
	private void entrarModoEdicion() {
		enModoEdicion = true;
		lblTituloDetalleMascota.setText("Modificar Mascota");
		txtNombreMascota.setEditable(true);
		txtEspecie.setEditable(true);
		txtRaza.setEditable(true);
		txtEdad.setEditable(true);
		cmbSexo.setDisable(false);
		txtPeso.setEditable(true);
		actualizarVisibilidadBotonesEdicion();
	}

	// Salir modo edicion
	private void salirModoEdicion() {
		enModoEdicion = false;
		lblTituloDetalleMascota.setText("Detalle de Mascota");
		txtNombreMascota.setEditable(false);
		txtEspecie.setEditable(false);
		txtRaza.setEditable(false);
		txtEdad.setEditable(false);
		cmbSexo.setDisable(true);
		txtPeso.setEditable(false);

		if (mascotaSeleccionada != null) {
			cargarValoresOriginales();
		}
		actualizarVisibilidadBotonesEdicion();
	}

	// Valores originales
	private void cargarValoresOriginales() {
		txtNombreMascota.setText(mascotaSeleccionada.getNombreMascota());
		txtEspecie.setText(mascotaSeleccionada.getEspecie());
		txtRaza.setText(mascotaSeleccionada.getRaza());
		txtEdad.setText(FORMATO_EDAD.format(mascotaSeleccionada.getEdad()));
		cmbSexo.setValue(mascotaSeleccionada.getSexo());
		txtPeso.setText(String.valueOf(mascotaSeleccionada.getPeso()));
	}

	// Visibilidad botones
	private void actualizarVisibilidadBotonesEdicion() {
		botonesAccionBoxMascota.setVisible(!enModoEdicion);
		botonesAccionBoxMascota.setManaged(!enModoEdicion);
		botonesEdicionBoxMascota.setVisible(enModoEdicion);
		botonesEdicionBoxMascota.setManaged(enModoEdicion);
		btnCerrarDetalleMascota.setVisible(!enModoEdicion);
	}

	// Modificar
	@FXML
	private void handleModificarMascota(ActionEvent event) { entrarModoEdicion(); }

	// Guardar cambios
	@FXML
	private void handleGuardarCambiosMascota(ActionEvent event) {
		if (mascotaSeleccionada == null) return;

		List<String> errores = validarCamposParaModificacion();
		if (!errores.isEmpty()) {
			mostrarAlertaValidacion("Errores de Validación", errores);
			return;
		}

		List<Mascota> mascotas = cargarMascotasDesdeJson();
		int indiceMascota = -1;
		for (int i = 0; i < mascotas.size(); i++) {
			if (mascotas.get(i).getCedulaDueno().equals(mascotaSeleccionada.getCedulaDueno()) &&
					mascotas.get(i).getNombreMascota().equals(mascotaSeleccionada.getNombreMascota()) ) {
				indiceMascota = i;
				break;
					}
		}

		if (indiceMascota != -1) {
			Mascota mascotaParaActualizar = mascotas.get(indiceMascota);

			mascotaParaActualizar.setNombreMascota(txtNombreMascota.getText().trim());
			mascotaParaActualizar.setEspecie(txtEspecie.getText().trim());
			mascotaParaActualizar.setRaza(txtRaza.getText().trim());
			mascotaParaActualizar.setEdad(Float.parseFloat(txtEdad.getText().trim()));
			mascotaParaActualizar.setSexo(cmbSexo.getValue());
			mascotaParaActualizar.setPeso(Integer.parseInt(txtPeso.getText().trim()));

			if (guardarMascotasEnJson(mascotas)) {
				mostrarAlerta("Éxito", "Mascota actualizada correctamente.");
				this.mascotaSeleccionada = mascotaParaActualizar;
				if (consultaMascotaController != null) {
					consultaMascotaController.refrescarListaMascotas();
				}
			} else {
				mostrarAlertaError("Error", "No se pudo guardar los cambios de la mascota.");
			}
		} else {
			mostrarAlertaError("Error", "No se encontró la mascota para actualizar. Pudo haber sido eliminada o modificada externamente.");
		}
		salirModoEdicion();
	}

	// Descartar
	@FXML
	private void handleDescartarCambiosMascota(ActionEvent event) {
		salirModoEdicion();
	}

	// Eliminar
	@FXML
	private void handleEliminarMascota(ActionEvent event) {
		if (mascotaSeleccionada == null) return;

		Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
		confirmacion.setTitle("Confirmar Eliminación");
		confirmacion.setHeaderText("Eliminar Mascota: " + mascotaSeleccionada.getNombreMascota());
		confirmacion.setContentText("¿Está seguro de que desea eliminar esta mascota? Esta acción no se puede deshacer.");

		Optional<ButtonType> resultado = confirmacion.showAndWait();
		if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
			List<Mascota> mascotas = cargarMascotasDesdeJson();
			boolean eliminada = mascotas.removeIf(m ->
					m.getCedulaDueno().equals(mascotaSeleccionada.getCedulaDueno()) &&
					m.getNombreMascota().equals(mascotaSeleccionada.getNombreMascota())
					);

			if (eliminada) {
				if (guardarMascotasEnJson(mascotas)) {
					mostrarAlerta("Éxito", "Mascota eliminada correctamente.");
					if (consultaMascotaController != null) {
						consultaMascotaController.refrescarListaMascotas();
					}
					handleCerrar(null);
				} else {
					mostrarAlertaError("Error", "No se pudo eliminar la mascota del archivo.");
				}
			} else {
				mostrarAlertaError("Error", "No se encontró la mascota para eliminar.");
			}
		}
	}

	// Validar
	private List<String> validarCamposParaModificacion() {
		List<String> errores = new ArrayList<>();
		String nombre = txtNombreMascota.getText().trim();
		if (nombre.isEmpty()) errores.add("- Nombre de la mascota no puede estar vacío.");
		else if (nombre.length() < 3 || nombre.length() >= 40) errores.add("- Nombre de la mascota debe tener entre 3 y 39 caracteres.");
		else if (!PATRON_TEXTO_GENERAL.matcher(nombre).matches()) errores.add("- Nombre de la mascota solo debe contener letras y caracteres permitidos (espacios, ', ., -).");

		String especie = txtEspecie.getText().trim();
		if (especie.isEmpty()) errores.add("- Especie no puede estar vacía.");
		else if (especie.length() < 3 || especie.length() >= 40) errores.add("- Especie debe tener entre 3 y 39 caracteres.");
		else if (!PATRON_TEXTO_GENERAL.matcher(especie).matches()) errores.add("- Especie solo debe contener letras y caracteres permitidos.");

		String raza = txtRaza.getText().trim();
		if (raza.isEmpty()) errores.add("- Raza no puede estar vacía.");
		else if (raza.length() < 3 || raza.length() >= 40) errores.add("- Raza debe tener entre 3 y 39 caracteres.");
		else if (!PATRON_TEXTO_GENERAL.matcher(raza).matches()) errores.add("- Raza solo debe contener letras y caracteres permitidos.");

		String edadStr = txtEdad.getText().trim();
		if (edadStr.isEmpty()) errores.add("- Edad no puede estar vacía.");
		else {
			try {
				float edad = Float.parseFloat(edadStr);
				if (edad <= 0 || edad > 40) errores.add("- Edad debe ser un número mayor que 0 y menor o igual a 40.");
			} catch (NumberFormatException e) { errores.add("- Edad debe ser un número válido (ej: 2.5)."); }
		}

		if (cmbSexo.getValue() == null || cmbSexo.getValue().isEmpty()) errores.add("- Debe seleccionar el sexo de la mascota.");

		String pesoStr = txtPeso.getText().trim();
		if (pesoStr.isEmpty()) errores.add("- Peso no puede estar vacío.");
		else {
			try {
				int peso = Integer.parseInt(pesoStr);
				if (peso <= 0 || peso >= 200) errores.add("- Peso debe ser un número entero mayor que 0 y menor que 200.");
			} catch (NumberFormatException e) { errores.add("- Peso debe ser un número entero válido."); }
		}
		return errores;
	}

	// Cargar
	private List<Mascota> cargarMascotasDesdeJson() {
		File archivo = new File(RUTA_MASCOTAS_JSON);
		if (archivo.exists() && archivo.length() > 0) {
			try { return objectMapper.readValue(archivo, new TypeReference<List<Mascota>>() {}); }
			catch (IOException e) { System.err.println("Error al leer mascotas: " + e.getMessage()); }
		}
		return new ArrayList<>();
	}

	// Guardar
	private boolean guardarMascotasEnJson(List<Mascota> mascotas) {
		try { objectMapper.writeValue(new File(RUTA_MASCOTAS_JSON), mascotas); return true; }
		catch (IOException e) { System.err.println("Error al guardar mascotas: " + e.getMessage()); return false; }
	}

	// Cerrar
	@FXML
	private void handleCerrar(ActionEvent event) {
		Stage stage = (Stage) btnCerrarDetalleMascota.getScene().getWindow();
		stage.close();
	}

	// Alerta
	private void mostrarAlerta(String titulo, String mensaje) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(titulo); alert.setHeaderText(null); alert.setContentText(mensaje);
		alert.showAndWait();
	}

	/// Error
	private void mostrarAlertaError(String titulo, String mensaje) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(titulo); alert.setHeaderText(null); alert.setContentText(mensaje);
		alert.showAndWait();
	}

	/// Validacion
	private void mostrarAlertaValidacion(String titulo, List<String> mensajes) {
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle(titulo);
		alert.setHeaderText("Por favor corrija los siguientes errores:");
		alert.setContentText(String.join("\n", mensajes));
		alert.showAndWait();
	}
}
