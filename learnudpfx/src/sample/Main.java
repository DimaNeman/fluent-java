package sample;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        primaryStage.setTitle("Learning UDP");
        int port = 8080;
        primaryStage.setMinWidth(200);
        primaryStage.setMinHeight(200);
        FlowPane pane = new FlowPane();
        Button button = new Button();
        button.setText("Run Server and Client");
        TextField textField = new TextField();
        textField.setPromptText("here you can write message for server");

        SimpleClientUDP clientUDP = new SimpleClientUDP("localhost", port);
        SimpleServerUDP serverUDP = new SimpleServerUDP(port);

        pane.getChildren().add(button);

        button.setOnAction(event -> {
            new Thread(serverUDP).start();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            new Thread(clientUDP).start();
            pane.getChildren().remove(button);
        });

        Button sendMessage = new Button("Send message");
        pane.getChildren().addAll(textField, sendMessage);

        sendMessage.setOnAction(event -> {
            String mes = textField.getText();
            textField.clear();
            clientUDP.push(mes.getBytes());
        });

        primaryStage.setScene(new Scene(pane));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
