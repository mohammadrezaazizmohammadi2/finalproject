package com.divarclone.client.controller;

import com.divarclone.client.network.ApiClient;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.http.HttpResponse;

public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField emailField;

    @FXML
    private Button registerButton;

    @FXML
    private Label messageLabel;

    private final ApiClient apiClient = new ApiClient();

    @FXML
    private void handleRegister() {

        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();
        String phone = phoneField.getText();
        String email = emailField.getText();

        if (username.isBlank() || password.isBlank()
                || confirm.isBlank()
                || phone.isBlank()
                || email.isBlank()) {

            messageLabel.setStyle("-fx-text-fill:red;");
            messageLabel.setText("Please fill all fields");
            return;
        }

        if (!password.equals(confirm)) {
            messageLabel.setStyle("-fx-text-fill:red;");
            messageLabel.setText("Passwords do not match");
            return;
        }

        try {

            HttpResponse<String> response =
                    apiClient.register(username,password,phone,email);

            if(response.statusCode()==200){

                messageLabel.setStyle("-fx-text-fill:green;");
                messageLabel.setText("Register successful");

                handleBackToLogin();

            }else{

                messageLabel.setStyle("-fx-text-fill:red;");
                messageLabel.setText(response.body());

            }

        } catch (IOException | InterruptedException e) {

            messageLabel.setStyle("-fx-text-fill:red;");
            messageLabel.setText("Cannot connect to server");

        }

    }

    @FXML
    private void handleBackToLogin() {

        try{

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/divarclone/client/login.fxml"));

            Parent root = loader.load();

            Stage stage = (Stage) registerButton.getScene().getWindow();

            stage.getScene().setRoot(root);

        }catch(IOException e){

            messageLabel.setStyle("-fx-text-fill:red;");
            messageLabel.setText("Cannot load login page");

        }

    }

}