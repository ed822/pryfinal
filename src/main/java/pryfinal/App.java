// Paquete
package pryfinal;

// Imports
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

// Clase App (main)
public class App extends Application {
	// Variables
	private static Stage primaryStage;

	// Start
	@Override
	public void start(Stage stage) throws IOException {
		primaryStage = stage;
		mostrarPantallaLogin();
	}

	// Pantalla login (InicioSesion)
	public static void mostrarPantallaLogin() {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("vista/InicioSesion.fxml"));
			Scene scene = new Scene(fxmlLoader.load(), 800, 450);
			primaryStage.setTitle("Inicio de Sesión - Veterinaria");
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (IOException e) {
			System.err.println("Error al cargar la pantalla de inicio de sesión: " + e.getMessage());
			e.printStackTrace();
		}
	}

	// Reiniciar login (para cerrar sesion)
	public static void reiniciarALogin() {
		if (primaryStage != null) {
			mostrarPantallaLogin();
		} else {
			System.err.println("Error: El primaryStage no está inicializado. No se puede reiniciar a login.");
		}
	}

	// Main
	public static void main(String[] args) {
		launch();
	}
}
