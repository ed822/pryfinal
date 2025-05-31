// Paquete
package pryfinal.controlador;

// Importaciones
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality; // Importación para ventanas modales
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;

import java.io.IOException;
import java.util.Objects;

import pryfinal.modelo.Usuario;

// Clase MenuPrincipal
public class MenuPrincipal {

	private Usuario usuarioActual;
	private Stage escenarioPrincipal; // Referencia al escenario del Menú Principal

	// --- Declaraciones @FXML para tus botones del MenuPrincipal ---
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
	// --- Fin declaraciones @FXML ---

	// Constantes para tipos de usuario para claridad y mantenimiento
	private final String TIPO_USUARIO_ADMIN = "admin";
	private final String TIPO_USUARIO_VETERINARIO = "veterinario";
	private final String TIPO_USUARIO_VENDEDOR = "vendedor";

	@FXML
	public void initialize() {
		// Deshabilitar todos los botones por defecto hasta que se configure el usuario.
		deshabilitarTodosLosBotonesFuncionales();
	}

	// Método para ser llamado desde InicioSesion después de un login exitoso
	public void configurarParaUsuario(Usuario usuario, Stage escenarioPrincipalActual) {
		this.usuarioActual = usuario;
		this.escenarioPrincipal = escenarioPrincipalActual; // Guardar la referencia al escenario
		System.out.println("Usuario logueado en MenuPrincipal: " + usuario.getNombre() + " (" + usuario.getTipo() + ")");
		actualizarVisibilidadBotones();
	}

	private void actualizarVisibilidadBotones() {
		if (usuarioActual == null) {
			deshabilitarTodosLosBotonesFuncionales();
			return;
		}

		String tipoUsuario = usuarioActual.getTipo();
		deshabilitarTodosLosBotonesFuncionales(); // Empezar deshabilitando

		if (TIPO_USUARIO_ADMIN.equals(tipoUsuario)) {
			habilitarTodosLosBotones();
		} else if (TIPO_USUARIO_VETERINARIO.equals(tipoUsuario)) {
			if (btnIrAConsultaHistoriaClinica != null) btnIrAConsultaHistoriaClinica.setDisable(false);
			if (btnIrAConsultaMascota != null) btnIrAConsultaMascota.setDisable(false);
			if (btnIrAConsultaOrdenMedica != null) btnIrAConsultaOrdenMedica.setDisable(false);
			if (btnIrAConsultaPersona != null) btnIrAConsultaPersona.setDisable(false);
			if (btnIrARegistroHistoriaClinica != null) btnIrARegistroHistoriaClinica.setDisable(false);
			if (btnIrARegistroMascota != null) btnIrARegistroMascota.setDisable(false);
			if (btnIrARegistroOrdenMedica != null) btnIrARegistroOrdenMedica.setDisable(false);
			if (btnIrARegistroPersona != null) btnIrARegistroPersona.setDisable(false);
		} else if (TIPO_USUARIO_VENDEDOR.equals(tipoUsuario)) {
			if (btnIrAConsultaFactura != null) btnIrAConsultaFactura.setDisable(false);
			if (btnIrAConsultaPersona != null) btnIrAConsultaPersona.setDisable(false);
			if (btnIrARegistroFactura != null) btnIrARegistroFactura.setDisable(false);
			if (btnIrARegistroPersona != null) btnIrARegistroPersona.setDisable(false);
		}
	}

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
	}

	private void cargarVistaModal(String fxmlFile, String title) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/pryfinal/vista/" + fxmlFile));
			Parent root = loader.load();

			Stage nuevaVentana = new Stage();
			nuevaVentana.setTitle(title);
			nuevaVentana.setScene(new Scene(root));

			// Configurar la ventana como MODAL
			nuevaVentana.initModality(Modality.WINDOW_MODAL); // O APPLICATION_MODAL si prefieres bloquear toda la app

			// Establecer el propietario de la ventana modal (la ventana del Menú Principal)
			if (this.escenarioPrincipal != null) {
				nuevaVentana.initOwner(this.escenarioPrincipal);
			} else {
				// Fallback si escenarioPrincipal no está seteado (no debería pasar en el flujo normal)
				// Obtener el escenario actual desde un botón si es necesario (aunque ya lo tenemos)
				System.err.println("Advertencia: escenarioPrincipal no estaba seteado al abrir ventana modal. Intentando obtener desde el evento.");
				// Esto podría ser redundante si el botón que llama a este método está en escenarioPrincipal
				// Node sourceNode = (Node) event.getSource(); // Necesitaríamos el event si usamos este fallback
				// nuevaVentana.initOwner(sourceNode.getScene().getWindow());
			}

			nuevaVentana.showAndWait(); // Mostrar la ventana y esperar a que se cierre

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

	// Los métodos de acción ahora llaman a cargarVistaModal
	@FXML private void irAConsultaMascota(ActionEvent event) { cargarVistaModal("ConsultaMascota.fxml", "Consultar Mascotas"); }
	@FXML private void irAConsultaPersona(ActionEvent event) { cargarVistaModal("ConsultaPersona.fxml", "Consultar Personas"); }
	@FXML private void irAConsultaFactura(ActionEvent event) { cargarVistaModal("ConsultaFactura.fxml", "Consultar Facturas"); }
	@FXML private void irAConsultaHistoriaClinica(ActionEvent event) { cargarVistaModal("ConsultaHistoriaClinica.fxml", "Consultar Historias Clínicas"); }
	@FXML private void irAConsultaOrdenMedica(ActionEvent event) { cargarVistaModal("ConsultaOrdenMedica.fxml", "Consultar Órdenes Médicas"); }

	@FXML private void irARegistroMascota(ActionEvent event) { cargarVistaModal("RegistroMascota.fxml", "Registrar Nueva Mascota"); }
	@FXML private void irARegistroPersona(ActionEvent event) { cargarVistaModal("RegistroPersona.fxml", "Registrar Nueva Persona"); }
	@FXML private void irARegistroFactura(ActionEvent event) { cargarVistaModal("RegistroFactura.fxml", "Registrar Nueva Factura"); }
	@FXML private void irARegistroHistoriaClinica(ActionEvent event) { cargarVistaModal("RegistroHistoriaClinica.fxml", "Registrar Entrada de Historia Clínica"); }
	@FXML private void irARegistroOrdenMedica(ActionEvent event) { cargarVistaModal("RegistroOrdenMedica.fxml", "Registrar Nueva Orden Médica"); }
}
