package Controllers;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class LoginController implements Initializable {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/hostel_management";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "zainyaqoob";

    @FXML
    private Button btn_signIn;
    @FXML
    private Button btn_signUp;
    @FXML
    private TextField txt_username;
    @FXML
    private PasswordField txt_password;
    @FXML
    private CheckBox pass_toggle;
    @FXML
    private TextField txt_pword;
    @FXML
    private Button btnClose;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.togglevisiblePassword(null);
    }

    @FXML
    private void signInButtonAction(MouseEvent event) {
        String username = txt_username.getText();
        String password = txt_password.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Input Error", "Username and password fields cannot be empty", Alert.AlertType.WARNING);
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?")) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                showAlert("Login Successful", "Welcome " + username, Alert.AlertType.INFORMATION);


                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/MenuComponent.fxml"));
                    Parent root = loader.load();

                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.show();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    showAlert("Error", "An error occurred while loading the next screen", Alert.AlertType.ERROR);
                }
            } else {
                showAlert("Login Failed", "Invalid username or password", Alert.AlertType.ERROR);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            showAlert("Database Error", "An error occurred while connecting to the database", Alert.AlertType.ERROR);
        }
    }


    @FXML
    private void signUpButtonAction(MouseEvent event) {
        String username = txt_username.getText();
        String password = txt_password.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Input Error", "Username and password fields cannot be empty", Alert.AlertType.WARNING);
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement checkStmt = conn.prepareStatement("SELECT * FROM users WHERE username = ?");
             PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)")) {

            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                showAlert("Signup Failed", "Username already exists", Alert.AlertType.ERROR);
            } else {
                insertStmt.setString(1, username);
                insertStmt.setString(2, password);
                insertStmt.executeUpdate();
                showAlert("Signup Successful", "User created successfully", Alert.AlertType.INFORMATION);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            showAlert("Database Error", "An error occurred while connecting to the database", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void togglevisiblePassword(ActionEvent event) {
        if (pass_toggle.isSelected()) {
            txt_pword.setText(txt_password.getText());
            txt_password.setVisible(false);
            txt_pword.setVisible(true);
        } else {
            txt_password.setText(txt_pword.getText());
            txt_password.setVisible(true);
            txt_pword.setVisible(false);
        }
    }

    @FXML
    public void closeLogin(ActionEvent event) {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
