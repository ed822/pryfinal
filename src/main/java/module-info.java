module pryfinal {
	// Javafx
	requires javafx.controls;
	requires javafx.fxml;

	// Jackson
	requires com.fasterxml.jackson.databind;

	// Controlador
	opens pryfinal.controlador to javafx.fxml;

	// Modelo
	opens pryfinal.modelo to com.fasterxml.jackson.databind;

	exports pryfinal;
	exports pryfinal.controlador;
	exports pryfinal.modelo;
}
