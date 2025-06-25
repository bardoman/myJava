
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene.*;
import javafx.scene.*;
import javafx.scene.shape.*;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
 
public class HelloWorld extends javafx.application.Application {
    
    @Override
    public void start(Stage primaryStage) {
        Button btn = new Button();
        btn.setText("Say 'Hello World'");
        btn.setOnAction(new EventHandler<ActionEvent>() {
 
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Hello World!");
            }
        });
        
        StackPane root = new StackPane();
        root.getChildren().add(btn);
        
          Rectangle rectangle = new Rectangle(150, 75, 300, 150);
          
          root.getChildren().add(rectangle);
                      
        // Create a Scene
        Scene scene = new Scene(root, 600, 300);

// Scene scene = new Scene(root, 300, 250);

        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        
      
        primaryStage.show();
    }
 public static void main(String[] args) {
        launch(args);
    }
}
