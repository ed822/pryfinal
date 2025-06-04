// Paquete
package pryfinal.controlador;

// Imports JavaFX

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import pryfinal.modelo.Persona;
import pryfinal.modelo.Usuario;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

// Clase DetallePersona
public class DetallePersona {

	@FXML private Label lblTituloDetallePersona;
	@FXML private TextField txtCedulaPersona;
	@FXML private TextField txtNombrePersona;
	@FXML private TextField txtApellido;
	@FXML private ComboBox<String> cmbTipoPersona;
	@FXML private TextField txtCelular;
	@FXML private TextField txtDireccion;
	@FXML private TextField txtEmail;

	@FXML private HBox botonesAccionBoxPersona;
	@FXML private Button btnModificarPersona;
	@FXML private Button btnEliminarPersona;

	@FXML private HBox botonesEdicionBoxPersona;
	@FXML private Button btnGuardarCambiosPersona;
	@FXML private Button btnDescartarCambiosPersona;

	@FXML private Button btnCerrarDetallePersona;

	private Persona personaSeleccionada;
	private Usuario usuarioActual;
	private ConsultaPersona consultaPersonaController; // CORREGIDO AQUÍ
	private boolean enModoEdicion = false;

	private ObjectMapper objectMapper;
	private final String RUTA_PERSONAS_JSON = "data/personas.json";
	private final String ADMIN_USER_TYPE = "admin";
	private final Pattern PATRON_NOMBRE_APELLIDO = Pattern.compile("^[\\p{L} .'-]+$");
	private final Pattern PATRON_EMAIL = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");

	@FXML
	public void initialize() {
		objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		// Items del ComboBox ya están en FXML
		cmbTipoPersona.setItems(FXCollections.observableArrayList("Dueño de Mascota", "Veterinario", "Encargado de bodega"));
		actualizarVisibilidadBotonesEdicion();
	}

	public void setConsultaPersonaController(ConsultaPersona controller) { // CORREGIDO AQUÍ
		this.consultaPersonaController = controller;
	}

	public void cargarDatos(Persona persona, Usuario usuarioLogueado) {
		this.personaSeleccionada = persona;
		this.usuarioActual = usuarioLogueado;

		if (persona != null) {
			txtCedulaPersona.setText(String.valueOf(persona.getCedula()));
			txtNombrePersona.setText(persona.getNombre());
			txtApellido.setText(persona.getApellido());
			cmbTipoPersona.setValue(persona.getTipo());
			txtCelular.setText(String.valueOf(persona.getCelular()));
			txtDireccion.setText(persona.getDireccion());
			txtEmail.setText(persona.getEmail());
		}
		configurarSegunRolUsuario();
		salirModoEdicion();
	}

	private void configurarSegunRolUsuario() {
		boolean esAdmin = usuarioActual != null && ADMIN_USER_TYPE.equals(usuarioActual.getTipo());
		btnModificarPersona.setDisable(!esAdmin);
		btnEliminarPersona.setDisable(!esAdmin);
	}

	private void entrarModoEdicion() {
		enModoEdicion = true;
		lblTituloDetallePersona.setText("Modificar Persona");
		txtNombrePersona.setEditable(true);
		txtApellido.setEditable(true);
		cmbTipoPersona.setDisable(false);
		txtCelular.setEditable(true);
		txtDireccion.setEditable(true);
		txtEmail.setEditable(true);
		actualizarVisibilidadBotonesEdicion();
	}

	private void salirModoEdicion() {
		enModoEdicion = false;
		lblTituloDetallePersona.setText("Detalle de Persona");
		txtNombrePersona.setEditable(false);
		txtApellido.setEditable(false);
		cmbTipoPersona.setDisable(true);
		txtCelular.setEditable(false);
		txtDireccion.setEditable(false);
		txtEmail.setEditable(false);
		if (personaSeleccionada != null) {
			cargarValoresOriginales();
		}
		actualizarVisibilidadBotonesEdicion();
	}

	private void cargarValoresOriginales() {
		txtNombrePersona.setText(personaSeleccionada.getNombre());
		txtApellido.setText(personaSeleccionada.getApellido());
		cmbTipoPersona.setValue(personaSeleccionada.getTipo());
		txtCelular.setText(String.valueOf(personaSeleccionada.getCelular()));
		txtDireccion.setText(personaSeleccionada.getDireccion());
		txtEmail.setText(personaSeleccionada.getEmail());
	}

	private void actualizarVisibilidadBotonesEdicion() {
		botonesAccionBoxPersona.setVisible(!enModoEdicion);
		botonesAccionBoxPersona.setManaged(!enModoEdicion);
		botonesEdicionBoxPersona.setVisible(enModoEdicion);
		botonesEdicionBoxPersona.setManaged(enModoEdicion);
		btnCerrarDetallePersona.setVisible(!enModoEdicion);
	}

	@FXML
	private void handleModificarPersona(ActionEvent event) {
		entrarModoEdicion();
	}

	@FXML
	private void handleGuardarCambiosPersona(ActionEvent event) {
		if (personaSeleccionada == null) return;

		List<String> errores = validarCamposParaModificacion();
		if (!errores.isEmpty()) {
			mostrarAlertaValidacion("Errores de Validación", errores);
			return;
		}

		List<Persona> personas = cargarPersonasDesdeJson();
		int indicePersona = -1;
		for (int i = 0; i < personas.size(); i++) {
			if (personas.get(i).getCedula() == personaSeleccionada.getCedula()) {
				indicePersona = i;
				break;
			}
		}

		if (indicePersona != -1) {
			Persona personaParaActualizar = personas.get(indicePersona);

			personaParaActualizar.setNombre(txtNombrePersona.getText().trim());
			personaParaActualizar.setApellido(txtApellido.getText().trim());
			personaParaActualizar.setTipo(cmbTipoPersona.getValue());
			personaParaActualizar.setCelular(Long.parseLong(txtCelular.getText().trim()));
			personaParaActualizar.setDireccion(txtDireccion.getText().trim());
			personaParaActualizar.setEmail(txtEmail.getText().trim());

			if (guardarPersonasEnJson(personas)) {
				mostrarAlerta("Éxito", "Persona actualizada correctamente.");
				this.personaSeleccionada = personaParaActualizar;
				if (consultaPersonaController != null) { // CORREGIDO AQUÍ
					consultaPersonaController.refrescarListaPersonas();
				}
			} else {
				mostrarAlertaError("Error", "No se pudo guardar los cambios de la persona.");
			}
		} else {
			mostrarAlertaError("Error", "No se encontró la persona para actualizar.");
		}
		salirModoEdicion();
	}

	@FXML
	private void handleDescartarCambiosPersona(ActionEvent event) {
		salirModoEdicion();
	}

	@FXML
	private void handleEliminarPersona(ActionEvent event) {
		if (personaSeleccionada == null) return;

		Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
		confirmacion.setTitle("Confirmar Eliminación");
		confirmacion.setHeaderText("Eliminar Persona: " + personaSeleccionada.getNombre() + " " + personaSeleccionada.getApellido());
		confirmacion.setContentText("¿Está seguro de que desea eliminar esta persona? Esta acción no se puede deshacer.");

		Optional<ButtonType> resultado = confirmacion.showAndWait();
		if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
			List<Persona> personas = cargarPersonasDesdeJson();
			boolean eliminada = personas.removeIf(p -> p.getCedula() == personaSeleccionada.getCedula());

			if (eliminada) {
				if (guardarPersonasEnJson(personas)) {
					mostrarAlerta("Éxito", "Persona eliminada correctamente.");
					if (consultaPersonaController != null) { // CORREGIDO AQUÍ
						consultaPersonaController.refrescarListaPersonas();
					}
					handleCerrar(null);
				} else {
					mostrarAlertaError("Error", "No se pudo eliminar la persona del archivo.");
				}
			} else {
				mostrarAlertaError("Error", "No se encontró la persona para eliminar.");
			}
		}
	}

	private List<String> validarCamposParaModificacion() {
		List<String> errores = new ArrayList<>();
		String nombre = txtNombrePersona.getText().trim();
		if (nombre.isEmpty()) errores.add("- Nombre no puede estar vacío.");
		else if (nombre.length() < 3 || nombre.length() >= 40) errores.add("- Nombre: 3-39 caracteres.");
		else if (!PATRON_NOMBRE_APELLIDO.matcher(nombre).matches()) errores.add("- Nombre: solo letras/caracteres permitidos.");

		String apellido = txtApellido.getText().trim();
		if (apellido.isEmpty()) errores.add("- Apellido no puede estar vacío.");
		else if (apellido.length() < 3 || apellido.length() >= 40) errores.add("- Apellido: 3-39 caracteres.");
		else if (!PATRON_NOMBRE_APELLIDO.matcher(apellido).matches()) errores.add("- Apellido: solo letras/caracteres permitidos.");

		if (cmbTipoPersona.getValue() == null || cmbTipoPersona.getValue().isEmpty()) errores.add("- Debe seleccionar un tipo de persona.");

		String celularStr = txtCelular.getText().trim();
		if (celularStr.isEmpty()) errores.add("- Celular no puede estar vacío.");
		else if (!celularStr.matches("\\d+")) errores.add("- Celular: solo números.");
		else if (celularStr.length() < 5 || celularStr.length() > 13) errores.add("- Celular: 5-13 dígitos.");

		String direccion = txtDireccion.getText().trim();
		if (direccion.isEmpty()) errores.add("- Dirección no puede estar vacía.");
		else if (direccion.length() < 3 || direccion.length() > 180) errores.add("- Dirección: 3-180 caracteres.");

		String email = txtEmail.getText().trim();
		if (email.isEmpty()) errores.add("- Email no puede estar vacío.");
		else if (!PATRON_EMAIL.matcher(email).matches()) errores.add("- Email no tiene un formato válido.");

		return errores;
	}

	private List<Persona> cargarPersonasDesdeJson() {
		File archivo = new File(RUTA_PERSONAS_JSON);
		if (archivo.exists() && archivo.length() > 0) {
			try { return objectMapper.readValue(archivo, new TypeReference<List<Persona>>() {}); }
			catch (IOException e) { System.err.println("Error al leer personas: " + e.getMessage()); }
		}
		return new ArrayList<>();
	}

	private boolean guardarPersonasEnJson(List<Persona> personas) {
		try { objectMapper.writeValue(new File(RUTA_PERSONAS_JSON), personas); return true; }
		catch (IOException e) { System.err.println("Error al guardar personas: " + e.getMessage()); return false; }
	}

	@FXML
	private void handleCerrar(ActionEvent event) {
		Stage stage = (Stage) btnCerrarDetallePersona.getScene().getWindow();
		stage.close();
	}

	private void mostrarAlerta(String titulo, String mensaje) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(titulo); alert.setHeaderText(null); alert.setContentText(mensaje);
		alert.showAndWait();
	}
	private void mostrarAlertaError(String titulo, String mensaje) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(titulo); alert.setHeaderText(null); alert.setContentText(mensaje);
		alert.showAndWait();
	}
	private void mostrarAlertaValidacion(String titulo, List<String> mensajes) {
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle(titulo);
		alert.setHeaderText("Por favor corrija los siguientes errores:");
		alert.setContentText(String.join("\n", mensajes));
		alert.showAndWait();
	}
}
