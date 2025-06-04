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

	private static Stage primaryStage; // Guardar una referencia al stage primario

	@Override
	public void start(Stage stage) throws IOException {
		primaryStage = stage; // Guardar el stage primario
		mostrarPantallaLogin();
	}

	public static void mostrarPantallaLogin() {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("vista/InicioSesion.fxml"));
			Scene scene = new Scene(fxmlLoader.load(), 800, 450); // Ajusta el tamaño si es necesario
			primaryStage.setTitle("Inicio de Sesión - Veterinaria");
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (IOException e) {
			System.err.println("Error al cargar la pantalla de inicio de sesión: " + e.getMessage());
			e.printStackTrace();
			// Considerar mostrar una alerta de error crítico aquí si falla el inicio
		}
	}

	/**
	 * Método estático para ser llamado desde MenuPrincipal para "cerrar sesión"
	 * y volver a la pantalla de login.
	 */
	public static void reiniciarALogin() {
		if (primaryStage != null) {
			mostrarPantallaLogin();
		} else {
			System.err.println("Error: El primaryStage no está inicializado. No se puede reiniciar a login.");
		}
	}

	public static void main(String[] args) {
		launch();
	}
}
