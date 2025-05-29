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
	@Override
	public void start(Stage stage) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("vista/InicioSesion.fxml"));
		Scene scene = new Scene(fxmlLoader.load(), 800, 450);
		stage.setTitle("Incio de sesion");
		stage.setScene(scene);
		stage.show();
	}

	public static void main(String[] args) {
		launch();
	}
}
