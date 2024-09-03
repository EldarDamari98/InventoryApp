package com.example.excelnew;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        showLoginScreen(primaryStage);
    }

    private void showLoginScreen(Stage primaryStage) {
        // Title
        Label titleLabel = new Label("הזן פרטי משתמש כדי להתחבר");
        titleLabel.setFont(Font.font("Arial", 18)); // Font size 18 pixels

        // Input fields for email and password
        TextField emailField = new TextField();
        emailField.setPromptText("אימייל");
        emailField.setMaxWidth(300); // Reduce the input field size
        emailField.setFont(Font.font("Arial", 16)); // Change font
        emailField.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT); // Right to left orientation

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("סיסמא");
        passwordField.setMaxWidth(300); // Reduce the input field size
        passwordField.setFont(Font.font("Arial", 16)); // Change font
        passwordField.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT); // Right to left orientation

        // Login button
        Button loginButton = new Button("התחבר");
        loginButton.setFont(Font.font("Arial", 16)); // Change font

        // Login action
        loginButton.setOnAction(e -> {
            String email = emailField.getText();
            String password = passwordField.getText();
            AuthenticationService authService = new AuthenticationService();

            if (authService.authenticateUser(email, password)) {
                // If login is successful, proceed to the main application screen
                System.out.println("התחברת בהצלחה");
                showAlert("Success", "התחברת בהצלחה", Alert.AlertType.INFORMATION);

                // Open a new window for the inventory editor screen
                try {
                    showInventoryEditor(primaryStage);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }

            } else {
                // If login fails, display an error message
                System.out.println("התחברות נכשלה, שם משתמש או סיסמא לא מזוההים.");
                showAlert("Error", "התחברות נכשלה, שם משתמש או סיסמא לא מזוההים.", Alert.AlertType.ERROR);
            }
        });

        // Set up the UI for the login screen
        VBox vbox = new VBox(10, titleLabel, emailField, passwordField, loginButton);
        vbox.setAlignment(Pos.CENTER); // Center alignment on the screen
        Scene scene = new Scene(vbox, 800, 800);

        primaryStage.setTitle("Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showInventoryEditor(Stage primaryStage) throws IOException {
        // Load the UI for the inventory editor screen
        Parent root = FXMLLoader.load(getClass().getResource("/com/example/excelnew/hello-view.fxml"));
        primaryStage.setTitle("עריכת מלאי");
        primaryStage.setScene(new Scene(root, 800, 800));
        primaryStage.show();
    }

    // Display a message to the user
    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
