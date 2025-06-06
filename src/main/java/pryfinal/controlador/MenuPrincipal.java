// Paquete
package pryfinal.controlador;

// Imports
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import pryfinal.App;
import pryfinal.modelo.Persona;
import pryfinal.modelo.Usuario;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Clase MenuPrincipal
public class MenuPrincipal {
	// Variables
	private Usuario usuarioActual;
	private Stage escenarioPrincipal;

	/// FXML
	@FXML private Button btnCerrarSesion;
	@FXML private Button btnIrAConsultaMascota;
	@FXML private Button btnIrAConsultaPersona;
	@FXML private Button btnIrAConsultaFactura;
	@FXML private Button btnIrAConsultaHistoriaClinica;
	@FXML private Button btnIrAConsultaOrdenMedica;
	@FXML private Button btnIrARegistroMascota;
	@FXML private Button btnIrARegistroPersona;
	@FXML private Button btnIrARegistroFactura;
	@FXML private Button btnIrARegistroHistoriaClinica;
	@FXML private Button btnIrARegistroOrdenMedica;
	@FXML private Button btnAdministrarUsuarios;

	/// Otros
	private final String TIPO_USUARIO_ADMIN = "admin";
	private final String TIPO_USUARIO_VETERINARIO = "veterinario";
	private final String TIPO_USUARIO_VENDEDOR = "vendedor";
	private ObjectMapper objectMapper = new ObjectMapper();
	private final String RUTA_PERSONAS_JSON = "data/personas.json";

	// Initialize (inicializar)
	@FXML
	public void initialize() { deshabilitarTodosLosBotonesFuncionales(); }

	// Para usuario
	public void configurarParaUsuario(Usuario usuario, Stage escenarioPrincipalActual) {
		this.usuarioActual = usuario;
		this.escenarioPrincipal = escenarioPrincipalActual;
		System.out.println("Usuario logueado en MenuPrincipal: " + usuario.getNombre() + " (" + usuario.getTipo() + ")");
		actualizarVisibilidadBotones();
	}

	// Botones
	private void actualizarVisibilidadBotones() {
		if (usuarioActual == null) {
			deshabilitarTodosLosBotonesFuncionales();
			if (btnCerrarSesion != null) btnCerrarSesion.setDisable(true);
			return;
		}

		String tipoUsuario = usuarioActual.getTipo();
		deshabilitarTodosLosBotonesFuncionales();

		if (TIPO_USUARIO_ADMIN.equals(tipoUsuario)) {
			habilitarTodosLosBotones();
			if (btnAdministrarUsuarios != null) btnAdministrarUsuarios.setDisable(false);
		} else if (TIPO_USUARIO_VETERINARIO.equals(tipoUsuario)) {
			if (btnIrAConsultaHistoriaClinica != null) btnIrAConsultaHistoriaClinica.setDisable(false);
			if (btnIrAConsultaMascota != null) btnIrAConsultaMascota.setDisable(false);
			if (btnIrAConsultaOrdenMedica != null) btnIrAConsultaOrdenMedica.setDisable(false);
			if (btnIrAConsultaPersona != null) btnIrAConsultaPersona.setDisable(false);
			if (btnIrARegistroHistoriaClinica != null) btnIrARegistroHistoriaClinica.setDisable(false);
			if (btnIrARegistroMascota != null) btnIrARegistroMascota.setDisable(false);
			if (btnIrARegistroOrdenMedica != null) btnIrARegistroOrdenMedica.setDisable(false);
			if (btnIrARegistroPersona != null) btnIrARegistroPersona.setDisable(false);
			if (btnAdministrarUsuarios != null) btnAdministrarUsuarios.setDisable(true);
		} else if (TIPO_USUARIO_VENDEDOR.equals(tipoUsuario)) {
			if (btnIrAConsultaFactura != null) btnIrAConsultaFactura.setDisable(false);
			if (btnIrAConsultaPersona != null) btnIrAConsultaPersona.setDisable(false);
			if (btnIrARegistroFactura != null) btnIrARegistroFactura.setDisable(false);
			if (btnIrARegistroPersona != null) btnIrARegistroPersona.setDisable(false);
			if (btnAdministrarUsuarios != null) btnAdministrarUsuarios.setDisable(true);
		} else {
			if (btnAdministrarUsuarios != null) btnAdministrarUsuarios.setDisable(true);
		}
		if (btnCerrarSesion != null) btnCerrarSesion.setDisable(false);
	}

	// Habilitar todos los botones
	private void habilitarTodosLosBotones() {
		if (btnIrAConsultaMascota != null) btnIrAConsultaMascota.setDisable(false);
		if (btnIrAConsultaPersona != null) btnIrAConsultaPersona.setDisable(false);
		if (btnIrAConsultaFactura != null) btnIrAConsultaFactura.setDisable(false);
		if (btnIrAConsultaHistoriaClinica != null) btnIrAConsultaHistoriaClinica.setDisable(false);
		if (btnIrAConsultaOrdenMedica != null) btnIrAConsultaOrdenMedica.setDisable(false);
		if (btnIrARegistroMascota != null) btnIrARegistroMascota.setDisable(false);
		if (btnIrARegistroPersona != null) btnIrARegistroPersona.setDisable(false);
		if (btnIrARegistroFactura != null) btnIrARegistroFactura.setDisable(false);
		if (btnIrARegistroHistoriaClinica != null) btnIrARegistroHistoriaClinica.setDisable(false);
		if (btnIrARegistroOrdenMedica != null) btnIrARegistroOrdenMedica.setDisable(false);
	}

	// Deshabilitar todos los botones
	private void deshabilitarTodosLosBotonesFuncionales() {
		if (btnIrAConsultaMascota != null) btnIrAConsultaMascota.setDisable(true);
		if (btnIrAConsultaPersona != null) btnIrAConsultaPersona.setDisable(true);
		if (btnIrAConsultaFactura != null) btnIrAConsultaFactura.setDisable(true);
		if (btnIrAConsultaHistoriaClinica != null) btnIrAConsultaHistoriaClinica.setDisable(true);
		if (btnIrAConsultaOrdenMedica != null) btnIrAConsultaOrdenMedica.setDisable(true);
		if (btnIrARegistroMascota != null) btnIrARegistroMascota.setDisable(true);
		if (btnIrARegistroPersona != null) btnIrARegistroPersona.setDisable(true);
		if (btnIrARegistroFactura != null) btnIrARegistroFactura.setDisable(true);
		if (btnIrARegistroHistoriaClinica != null) btnIrARegistroHistoriaClinica.setDisable(true);
		if (btnIrARegistroOrdenMedica != null) btnIrARegistroOrdenMedica.setDisable(true);
		if (btnAdministrarUsuarios != null) btnAdministrarUsuarios.setDisable(true);
	}

	// Cargar
	private List<Persona> cargarPersonasDesdeJson() {
		File archivoPersonas = new File(RUTA_PERSONAS_JSON);
		if (archivoPersonas.exists() && archivoPersonas.length() > 0) {
			try {
				return objectMapper.readValue(archivoPersonas, new TypeReference<List<Persona>>() {});
			} catch (IOException e) {
				System.err.println("Error al cargar personas desde JSON en MenuPrincipal: " + e.getMessage());
			}
		}
		return new ArrayList<>();
	}

	// Veterinario
	private boolean existenVeterinariosRegistrados() {
		List<Persona> personas = cargarPersonasDesdeJson();
		return personas.stream().anyMatch(p -> "Veterinario".equalsIgnoreCase(p.getTipo()));
	}

	// Vista modal
	private void cargarVistaModal(String fxmlFile, String title, boolean necesitaUsuarioDirecto, String tipoVista) {
		if ("RegistroHistoriaClinica".equals(tipoVista) || "RegistroOrdenMedica".equals(tipoVista)) {
			if (!existenVeterinariosRegistrados()) {
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle("Operación no permitida");
				alert.setHeaderText(null);
				String mensajeErrorVets;
				String tipoDeRegistro = "";
				if ("RegistroHistoriaClinica".equals(tipoVista)) tipoDeRegistro = "entrada de historia clínica";
				else if ("RegistroOrdenMedica".equals(tipoVista)) tipoDeRegistro = "orden médica";

				if (usuarioActual != null && TIPO_USUARIO_ADMIN.equals(usuarioActual.getTipo())) {
					mensajeErrorVets = "No hay veterinarios registrados en el sistema. Por favor, agregue veterinarios para poder crear una nueva " + tipoDeRegistro + ".";
				} else {
					mensajeErrorVets = "No hay veterinarios registrados en el sistema. Por favor, pida al administrador que agregue veterinarios para poder crear una nueva " + tipoDeRegistro + ".";
				}
				alert.setContentText(mensajeErrorVets);
				alert.showAndWait();
				return;
			}
		}

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/pryfinal/vista/" + fxmlFile));
			Parent root = loader.load();
			Object controladorCargado = loader.getController();

			if (necesitaUsuarioDirecto) {
				if (controladorCargado instanceof RegistroPersona) {
					((RegistroPersona) controladorCargado).configurarConUsuario(this.usuarioActual);
				}
			}

			// Pasar usuario a las consultas para que ellas lo pasen a sus detalles
			if (controladorCargado instanceof ConsultaMascota) {
				((ConsultaMascota) controladorCargado).setUsuarioActual(this.usuarioActual);
			} else if (controladorCargado instanceof ConsultaPersona) {
				((ConsultaPersona) controladorCargado).setUsuarioActual(this.usuarioActual);
			} else if (controladorCargado instanceof ConsultaFactura) {
				((ConsultaFactura) controladorCargado).setUsuarioActual(this.usuarioActual);
			} else if (controladorCargado instanceof ConsultaHistoriaClinica) {
				((ConsultaHistoriaClinica) controladorCargado).setUsuarioActual(this.usuarioActual);
			} else if (controladorCargado instanceof ConsultaOrdenMedica) {
				((ConsultaOrdenMedica) controladorCargado).setUsuarioActual(this.usuarioActual);
			}

			Stage nuevaVentana = new Stage();
			nuevaVentana.setTitle(title);
			nuevaVentana.setScene(new Scene(root));
			nuevaVentana.initModality(Modality.WINDOW_MODAL);
			if (this.escenarioPrincipal != null) nuevaVentana.initOwner(this.escenarioPrincipal);
			nuevaVentana.showAndWait();

		} catch (IOException e) {
			System.err.println("Error al cargar la vista modal: " + fxmlFile + " - " + e.getMessage());
			e.printStackTrace();
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Error de Navegación");
			alert.setHeaderText("No se pudo abrir la ventana: " + title);
			alert.setContentText("Detalle: " + e.getMessage());
			alert.showAndWait();
		}
	}

	// Cerrar sesion
	@FXML
	private void handleCerrarSesion(ActionEvent event) {
		Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
		confirmacion.setTitle("Confirmar Cierre de Sesión");
		confirmacion.setHeaderText("¿Está seguro de que desea cerrar la sesión?");
		confirmacion.setContentText("Será redirigido a la pantalla de inicio de sesión.");

		Optional<ButtonType> resultado = confirmacion.showAndWait();
		if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
			Stage stageActual = (Stage) btnCerrarSesion.getScene().getWindow();
			stageActual.close();
			App.reiniciarALogin();
		}
	}

	// CargarVistaModal
	@FXML private void irAConsultaMascota(ActionEvent event) { cargarVistaModal("ConsultaMascota.fxml", "Consultar Mascotas", false, null); }
	@FXML private void irAConsultaPersona(ActionEvent event) { cargarVistaModal("ConsultaPersona.fxml", "Consultar Personas", false, null); }
	@FXML private void irAConsultaFactura(ActionEvent event) { cargarVistaModal("ConsultaFactura.fxml", "Consultar Facturas", false, null); }
	@FXML private void irAConsultaHistoriaClinica(ActionEvent event) { cargarVistaModal("ConsultaHistoriaClinica.fxml", "Consultar Historias Clínicas", false, null); }
	@FXML private void irAConsultaOrdenMedica(ActionEvent event) { cargarVistaModal("ConsultaOrdenMedica.fxml", "Consultar Órdenes Médicas", false, null); }
	@FXML private void irAConsultarUsuarios(ActionEvent event) { cargarVistaModal("ConsultaUsuario.fxml", "Administrar Usuarios del Sistema", false, "ConsultaUsuario"); }

	@FXML private void irARegistroMascota(ActionEvent event) { cargarVistaModal("RegistroMascota.fxml", "Registrar Nueva Mascota", false, null); }
	@FXML private void irARegistroPersona(ActionEvent event) { cargarVistaModal("RegistroPersona.fxml", "Registrar Nueva Persona", true, "RegistroPersona"); }
	@FXML private void irARegistroFactura(ActionEvent event) { cargarVistaModal("RegistroFactura.fxml", "Registrar Nueva Factura", false, null); }
	@FXML private void irARegistroHistoriaClinica(ActionEvent event) { cargarVistaModal("RegistroHistoriaClinica.fxml", "Registrar Entrada de Historia Clínica", false, "RegistroHistoriaClinica"); }
	@FXML private void irARegistroOrdenMedica(ActionEvent event) { cargarVistaModal("RegistroOrdenMedica.fxml", "Registrar Nueva Orden Médica", false, "RegistroOrdenMedica"); }
}
