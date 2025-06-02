// Paquete
package pryfinal.controlador;

// Imports
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import pryfinal.modelo.HistoriaClinica;

// Clase DetalleHistoriaClinica
public class DetalleHistoriaClinica {
	// FXML
	@FXML private TextField txtCedulaDuenoHC;
	@FXML private TextField txtNombreMascotaHC;
	@FXML private TextField txtFechaVisitaHC;
	@FXML private TextField txtVeterinarioEncargadoHC;
	@FXML private TextArea areaMotivoConsultaHC;
	@FXML private TextArea areaDiagnosticoHC;
	@FXML private TextArea areaTratamientoIndicadoHC;
	@FXML private TextArea areaObservacionesHC;
	@FXML private Button btnCerrarDetalleHistoria;

	// Cargar
	public void cargarDatos(HistoriaClinica historia) {
		if (historia != null) {
			txtCedulaDuenoHC.setText(String.valueOf(historia.getCedula()));
			txtNombreMascotaHC.setText(historia.getNombre());
			txtFechaVisitaHC.setText(historia.getFecha());
			txtVeterinarioEncargadoHC.setText(historia.getVeterinario());
			areaMotivoConsultaHC.setText(historia.getMotivo());
			areaDiagnosticoHC.setText(historia.getDiagnostico());
			areaTratamientoIndicadoHC.setText(historia.getTratamiento());
			areaObservacionesHC.setText(historia.getObservaciones());
		}
	}

	// Cerrar
	@FXML
	private void handleCerrar(ActionEvent event) {
		Stage stage = (Stage) btnCerrarDetalleHistoria.getScene().getWindow();
		stage.close();
	}
}
