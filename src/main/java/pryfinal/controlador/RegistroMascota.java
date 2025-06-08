// Paquete
package pryfinal.controlador;

// Imports
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import pryfinal.modelo.Mascota;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

// Clase RegistroMascota
public class RegistroMascota {
	// Variables
	/// FXML
	@FXML private TextField txtCedulaDueno;
	@FXML private TextField txtNombreMascota;
	@FXML private TextField txtEspecie;
	@FXML private TextField txtRaza;
	@FXML private TextField txtEdadMascota;
	@FXML private ComboBox<String> cmbSexoMascota;
	@FXML private TextField txtPesoMascota;
	@FXML private Button btnRegistrarMascota;

	/// Otros
	private ObjectMapper objectMapper;
	private final String RUTA_MASCOTAS_JSON = "data/mascotas.json";
	private final String RUTA_DIRECTORIO_DATOS = "data";
	private final Pattern PATRON_TEXTO_GENERAL = Pattern.compile("^[\\p{L} .'-]+$");

	// Inicializar
	@FXML
	public void initialize() {
		objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

		// Asegurar que el directorio de datos exista
		File directorioDatos = new File(RUTA_DIRECTORIO_DATOS);
		if (!directorioDatos.exists()) {
			directorioDatos.mkdirs();
		}
	}

	// Boton registrar
	@FXML
	private void handleRegistrarMascota(ActionEvent event) {
		List<String> errores = validarCampos();

		if (!errores.isEmpty()) {
			mostrarAlertaValidacion("Errores de Validación", errores);
			return;
		}

		// Si se valida, proceder a crear y guardar la mascota
		String cedulaDueno = txtCedulaDueno.getText().trim();
		String nombreMascota = txtNombreMascota.getText().trim();
		String especie = txtEspecie.getText().trim();
		String raza = txtRaza.getText().trim();
		float edad = Float.parseFloat(txtEdadMascota.getText().trim());
		String sexo = cmbSexoMascota.getValue();
		int peso = Integer.parseInt(txtPesoMascota.getText().trim());

		Mascota nuevaMascota = new Mascota(cedulaDueno, nombreMascota, especie, edad, sexo, raza, peso);

		List<Mascota> mascotas = cargarMascotas();
		mascotas.add(nuevaMascota);

		if (guardarMascotas(mascotas)) {
			mostrarAlertaInformacion("Registro Exitoso", "Mascota '" + nombreMascota + "' registrada correctamente.");
			limpiarCampos();
		} else {
			mostrarAlertaError("Error de Registro", "No se pudo guardar la mascota. Intente de nuevo.");
		}
	}

	// Validacion
	private List<String> validarCampos() {
		List<String> errores = new ArrayList<>();

		// Cédula del Dueño
		String cedula = txtCedulaDueno.getText().trim();
		if (cedula.isEmpty()) {
			errores.add("- Cédula del dueño no puede estar vacía.");
		} else if (!cedula.matches("\\d+")) {
			errores.add("- Cédula del dueño debe contener solo números.");
		} else if (cedula.length() < 5 || cedula.length() > 13) {
			errores.add("- Cédula del dueño debe tener entre 5 y 13 dígitos.");
		}

		// Nombre de la Mascota
		String nombre = txtNombreMascota.getText().trim();
		if (nombre.isEmpty()) {
			errores.add("- Nombre de la mascota no puede estar vacío.");
		} else if (nombre.length() < 3 || nombre.length() >= 40) {
			errores.add("- Nombre de la mascota debe tener entre 3 y 39 caracteres.");
		} else if (!PATRON_TEXTO_GENERAL.matcher(nombre).matches()) {
			errores.add("- Nombre de la mascota solo debe contener letras y caracteres permitidos (espacios, ', ., -).");
		}

		// Especie
		String especie = txtEspecie.getText().trim();
		if (especie.isEmpty()) {
			errores.add("- Especie no puede estar vacía.");
		} else if (especie.length() < 3 || especie.length() >= 40) {
			errores.add("- Especie debe tener entre 3 y 39 caracteres.");
		} else if (!PATRON_TEXTO_GENERAL.matcher(especie).matches()) {
			errores.add("- Especie solo debe contener letras y caracteres permitidos.");
		}

		// Raza
		String raza = txtRaza.getText().trim();
		if (raza.isEmpty()) {
			errores.add("- Raza no puede estar vacía.");
		} else if (raza.length() < 3 || raza.length() >= 40) {
			errores.add("- Raza debe tener entre 3 y 39 caracteres.");
		} else if (!PATRON_TEXTO_GENERAL.matcher(raza).matches()) {
			errores.add("- Raza solo debe contener letras y caracteres permitidos.");
		}

		// Edad
		String edadStr = txtEdadMascota.getText().trim();
		if (edadStr.isEmpty()) {
			errores.add("- Edad no puede estar vacía.");
		} else {
			try {
				float edad = Float.parseFloat(edadStr);
				if (edad <= 0 || edad > 40) {
					errores.add("- Edad debe ser un número mayor que 0 y menor o igual a 40.");
				}
			} catch (NumberFormatException e) {
				errores.add("- Edad debe ser un número válido (ej: 2.5).");
			}
		}

		// Sexo
		if (cmbSexoMascota.getValue() == null || cmbSexoMascota.getValue().isEmpty()) {
			errores.add("- Debe seleccionar el sexo de la mascota.");
		}

		// Peso
		String pesoStr = txtPesoMascota.getText().trim();
		if (pesoStr.isEmpty()) {
			errores.add("- Peso no puede estar vacío.");
		} else {
			try {
				int peso = Integer.parseInt(pesoStr);
				if (peso <= 0 || peso >= 200) {
					errores.add("- Peso debe ser un número entero mayor que 0 y menor que 200.");
				}
			} catch (NumberFormatException e) {
				errores.add("- Peso debe ser un número entero válido.");
			}
		}

		return errores;
	}

	// Cargar
	private List<Mascota> cargarMascotas() {
		File archivo = new File(RUTA_MASCOTAS_JSON);
		if (archivo.exists() && archivo.length() > 0) {
			try {
				return objectMapper.readValue(archivo, new TypeReference<List<Mascota>>() {});
			} catch (IOException e) {
				System.err.println("Error al leer el archivo de mascotas: " + e.getMessage());
			}
		}
		return new ArrayList<>();
	}

	// Guardar
	private boolean guardarMascotas(List<Mascota> mascotas) {
		try {
			objectMapper.writeValue(new File(RUTA_MASCOTAS_JSON), mascotas);
			return true;
		} catch (IOException e) {
			System.err.println("Error al guardar el archivo de mascotas: " + e.getMessage());
			return false;
		}
	}

	// Limpiar campos
	private void limpiarCampos() {
		txtCedulaDueno.clear();
		txtNombreMascota.clear();
		txtEspecie.clear();
		txtRaza.clear();
		txtEdadMascota.clear();
		cmbSexoMascota.getSelectionModel().clearSelection();
		txtPesoMascota.clear();
		txtCedulaDueno.requestFocus();
	}

	// Mostrar alerta
	/// Validacion
	private void mostrarAlertaValidacion(String titulo, List<String> mensajes) {
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle(titulo);
		alert.setHeaderText("Por favor corrija los siguientes errores:");

		StringBuilder contenido = new StringBuilder();
		for (String mensaje : mensajes) {
			contenido.append(mensaje).append("\n");
		}
		alert.setContentText(contenido.toString());
		alert.showAndWait();
	}

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
