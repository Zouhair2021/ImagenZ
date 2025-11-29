import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Si ton FXML s'appelle "main.fxml" à la racine du classpath :
        FXMLLoader fxml = new FXMLLoader(getClass().getResource("/MainLayout.fxml"));

        // Charge la scène
        Scene scene = new Scene(fxml.load(), 900, 600);

        stage.setTitle("ImagenZ");
        stage.getIcons().add(new Image(getClass().getResource("resources/icons/reves.png").toExternalForm()));
        stage.setMaximized(true);
        stage.setOnCloseRequest(e -> System.exit(0));
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
