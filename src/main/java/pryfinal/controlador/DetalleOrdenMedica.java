// Paquete
package pryfinal.controlador;

// Imports JavaFX
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

// Modelo
import pryfinal.modelo.OrdenMedica;

// Clase DetalleOrdenMedica (antes DetalleOrdenMedicaController)
public class DetalleOrdenMedica {

	@FXML private TextField txtNumeroOrden;
	@FXML private TextField txtFechaEmision;
	@FXML private TextField txtCedulaDueno;
	@FXML private TextField txtNombreMascota;
	@FXML private TextField txtVeterinarioPrescribe;
	@FXML private TextField txtDuracionTratamiento;
	@FXML private TextArea areaMedicamentosDosis;
	@FXML private TextArea areaInstruccionesAdmin;
	@FXML private TextArea areaNotasAdicionales;
	@FXML private Button btnCerrarDetalleOrden;

	public void cargarDatos(OrdenMedica orden) {
		if (orden != null) {
			txtNumeroOrden.setText(orden.getNumero());
			txtFechaEmision.setText(orden.getFecha());
			txtCedulaDueno.setText(String.valueOf(orden.getCedula()));
			txtNombreMascota.setText(orden.getNombre());
			txtVeterinarioPrescribe.setText(orden.getVeterinario());
			areaMedicamentosDosis.setText(orden.getDosis());
			areaInstruccionesAdmin.setText(orden.getIntrucciones());
			txtDuracionTratamiento.setText(orden.getDuracion());
			areaNotasAdicionales.setText(orden.getNotas());
		}
	}

	@FXML
	private void handleCerrar(ActionEvent event) {
		Stage stage = (Stage) btnCerrarDetalleOrden.getScene().getWindow();
		stage.close();
	}
}
