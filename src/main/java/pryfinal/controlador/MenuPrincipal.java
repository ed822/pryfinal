// Paquete
package pryfinal.controlador;

// Importaciones
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Alert; // Importación de Alert añadida

import java.io.IOException;
import java.util.Objects;

import pryfinal.modelo.Usuario;

// Clase MenuPrincipal
public class MenuPrincipal {

    private Usuario usuarioActual;

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
        // Esto asegura que si algo falla en la carga del usuario, la UI no sea usable incorrectamente.
        deshabilitarTodosLosBotonesFuncionales();
    }

    public void configurarParaUsuario(Usuario usuario) {
        this.usuarioActual = usuario;
        System.out.println("Usuario logueado en MenuPrincipal: " + usuario.getNombre() + " (" + usuario.getTipo() + ")");
        actualizarVisibilidadBotones();
    }

    private void actualizarVisibilidadBotones() {
        if (usuarioActual == null) {
            deshabilitarTodosLosBotonesFuncionales();
            return;
        }

        String tipoUsuario = usuarioActual.getTipo();

        // Por defecto, habilitamos todo para el admin, y luego restringimos para otros.
        // O, empezamos deshabilitando todo y habilitamos selectivamente. La segunda es más segura.
        deshabilitarTodosLosBotonesFuncionales(); // Empezar deshabilitando

        if (TIPO_USUARIO_ADMIN.equals(tipoUsuario)) {
            habilitarTodosLosBotones();
        } else if (TIPO_USUARIO_VETERINARIO.equals(tipoUsuario)) {
            // veterinario: ConsultaHistoriaClinica, ConsultaMascota, ConsultaOrdenMedica, ConsultaPersona,
            //              RegistroHistoriaClinica, RegistroMascota, RegistroOrdenMedica y RegistroPersona
            if (btnIrAConsultaHistoriaClinica != null) btnIrAConsultaHistoriaClinica.setDisable(false);
            if (btnIrAConsultaMascota != null) btnIrAConsultaMascota.setDisable(false);
            if (btnIrAConsultaOrdenMedica != null) btnIrAConsultaOrdenMedica.setDisable(false);
            if (btnIrAConsultaPersona != null) btnIrAConsultaPersona.setDisable(false);

            if (btnIrARegistroHistoriaClinica != null) btnIrARegistroHistoriaClinica.setDisable(false);
            if (btnIrARegistroMascota != null) btnIrARegistroMascota.setDisable(false);
            if (btnIrARegistroOrdenMedica != null) btnIrARegistroOrdenMedica.setDisable(false);
            if (btnIrARegistroPersona != null) btnIrARegistroPersona.setDisable(false); // Según especificación

        } else if (TIPO_USUARIO_VENDEDOR.equals(tipoUsuario)) {
            // vendedor: ConsultaFactura, ConsultaPersona, RegistroFactura y RegistroPersona
            if (btnIrAConsultaFactura != null) btnIrAConsultaFactura.setDisable(false);
            if (btnIrAConsultaPersona != null) btnIrAConsultaPersona.setDisable(false); // Común con veterinario

            if (btnIrARegistroFactura != null) btnIrARegistroFactura.setDisable(false);
            if (btnIrARegistroPersona != null) btnIrARegistroPersona.setDisable(false); // Según especificación
        }
        // Si hay otros tipos de usuario, permanecerán con todo deshabilitado a menos que se añadan reglas.
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

    private void cargarVista(ActionEvent event, String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/pryfinal/vista/" + fxmlFile));
            Parent root = loader.load();

            // Si la vista cargada tiene un controlador que necesita el usuario actual,
            // puedes pasárselo aquí, similar a como se hace con MenuPrincipal.
            // Object controladorCargado = loader.getController();
            // if (controladorCargado instanceof IControladorConUsuario) {
            //     ((IControladorConUsuario) controladorCargado).setUsuarioActual(this.usuarioActual);
            // }

            Stage nuevaVentana = new Stage();
            nuevaVentana.setTitle(title);
            nuevaVentana.setScene(new Scene(root));

            // Para la navegación de "ir hacia atrás", podrías hacer que el menú principal se oculte
            // y se muestre de nuevo cuando la nueva ventana se cierre.
            Stage menuPrincipalStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            // menuPrincipalStage.hide(); // Opcional: ocultar el menú principal

            // nuevaVentana.setOnHidden(e -> {
            //     if (menuPrincipalStage != null) {
            //         menuPrincipalStage.show(); // Mostrar el menú principal de nuevo
            //     }
            // });
            
            nuevaVentana.show();

        } catch (IOException e) {
            System.err.println("Error al cargar la vista: " + fxmlFile + " - " + e.getMessage());
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error de Navegación");
            alert.setHeaderText("No se pudo abrir la ventana: " + title);
            alert.setContentText("Detalle: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    @FXML private void irAConsultaMascota(ActionEvent event) { cargarVista(event, "ConsultaMascota.fxml", "Consultar Mascotas"); }
    @FXML private void irAConsultaPersona(ActionEvent event) { cargarVista(event, "ConsultaPersona.fxml", "Consultar Personas"); }
    @FXML private void irAConsultaFactura(ActionEvent event) { cargarVista(event, "ConsultaFactura.fxml", "Consultar Facturas"); }
    @FXML private void irAConsultaHistoriaClinica(ActionEvent event) { cargarVista(event, "ConsultaHistoriaClinica.fxml", "Consultar Historias Clínicas"); }
    @FXML private void irAConsultaOrdenMedica(ActionEvent event) { cargarVista(event, "ConsultaOrdenMedica.fxml", "Consultar Órdenes Médicas"); }

    @FXML private void irARegistroMascota(ActionEvent event) { cargarVista(event, "RegistroMascota.fxml", "Registrar Nueva Mascota"); }
    @FXML private void irARegistroPersona(ActionEvent event) { cargarVista(event, "RegistroPersona.fxml", "Registrar Nueva Persona"); }
    @FXML private void irARegistroFactura(ActionEvent event) { cargarVista(event, "RegistroFactura.fxml", "Registrar Nueva Factura"); }
    @FXML private void irARegistroHistoriaClinica(ActionEvent event) { cargarVista(event, "RegistroHistoriaClinica.fxml", "Registrar Entrada de Historia Clínica"); }
    @FXML private void irARegistroOrdenMedica(ActionEvent event) { cargarVista(event, "RegistroOrdenMedica.fxml", "Registrar Nueva Orden Médica"); }
}
