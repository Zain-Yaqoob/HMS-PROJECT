/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controllers.Student;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import DBConnection.DBHandler;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


public class New_StudentController implements Initializable {

    @FXML
    private TextField reg_txt_username;
    @FXML
    private TextField reg_txt_nsbmid;
    @FXML
    private TextField reg_txt_email;
    @FXML
    private TextField reg_txt_phnmb;
    @FXML
    private TextField reg_txt_nic;
    @FXML
    private TextField reg_txt_address;
    @FXML
    private TextField reg_txt_guardname;
    @FXML
    private TextField reg_txt_guardtel;


    private Connection connection;
    private DBHandler handler;
    private PreparedStatement pst;
    @FXML
    private Button btn_back;
    @FXML
    private Button btn_reg_student;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        handler = new DBHandler();
    }
    @FXML
    private void registerButtonAction(MouseEvent event) {
        String userName = reg_txt_username.getText();
        String nsbmID = reg_txt_nsbmid.getText();
        String email = reg_txt_email.getText();
        String phoneNumber = reg_txt_phnmb.getText();
        String nic = reg_txt_nic.getText();
        String address = reg_txt_address.getText();
        String guardName = reg_txt_guardname.getText();
        String guardTel = reg_txt_guardtel.getText();

        if (userName.isEmpty() || nsbmID.isEmpty() || email.isEmpty() || phoneNumber.isEmpty() ||
                nic.isEmpty() || address.isEmpty() || guardName.isEmpty() || guardTel.isEmpty()) {
            showAlert("Validation Error", "All fields are required!", Alert.AlertType.ERROR);
            return;
        }

        if (!nsbmID.matches("\\d+")) {
            showAlert("Validation Error", "NSBM ID must be a number.", Alert.AlertType.ERROR);
            return;
        }

        if (!email.matches("^[a-zA-Z0-9._%+-]+@gmail\\.com$")) {
            showAlert("Validation Error", "Email must be a valid Gmail address.", Alert.AlertType.ERROR);
            return;
        }

        if (!phoneNumber.matches("\\+92\\d{10}")) {
            showAlert("Validation Error", "Phone number must begin with +92.", Alert.AlertType.ERROR);
            return;
        }

        if (!guardTel.matches("\\+92\\d{10}")) {
            showAlert("Validation Error", "Guardian's contact must must begin with +92", Alert.AlertType.ERROR);
            return;
        }

        if (!nic.matches("\\d{5}-\\d{7}-\\d")) {
            showAlert("Validation Error", "NIC must be in the format 12345-1234567-1.", Alert.AlertType.ERROR);
            return;
        }

        String checkQuery = "SELECT COUNT(*) FROM register_Students WHERE nsbmID = ? OR nic = ?";
        String insertQuery = "INSERT INTO register_Students(name, nsbmID, email, phoneNumber, nic, address, guardName, guardTel) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = handler.connectDB();
             PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
             PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {

            checkStmt.setString(1, nsbmID);
            checkStmt.setString(2, nic);
            try (java.sql.ResultSet resultSet = checkStmt.executeQuery()) {
                if (resultSet.next() && resultSet.getInt(1) > 0) {
                    showAlert("Validation Error", "Roll No or CNIC already exists.", Alert.AlertType.ERROR);
                    return;
                }
            }

            insertStmt.setString(1, userName);
            insertStmt.setString(2, nsbmID);
            insertStmt.setString(3, email);
            insertStmt.setString(4, phoneNumber);
            insertStmt.setString(5, nic);
            insertStmt.setString(6, address);
            insertStmt.setString(7, guardName);
            insertStmt.setString(8, guardTel);
            insertStmt.executeUpdate();

            showAlert("Success", "Student registered successfully!", Alert.AlertType.INFORMATION);
            setTExtRefresh();

        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            showAlert("Database Error", "Failed to register student. Please try again.", Alert.AlertType.ERROR);
        }
    }


    @FXML
    private void back_btn_clicked(MouseEvent event) throws IOException {
        btn_back.getScene().getWindow().hide();

        Stage stu_Menu = new Stage();
        Parent root = FXMLLoader.load(getClass().getResource("/FXML/Student/Student_Menu.fxml"));
        Scene scene = new Scene(root);
        stu_Menu.initStyle(StageStyle.TRANSPARENT);
        stu_Menu.setScene(scene);
        stu_Menu.show();
        stu_Menu.setResizable(false);
    }

    @FXML
    private void setTExtRefresh() {
        reg_txt_username.setText("");
        reg_txt_nsbmid.setText("");
        reg_txt_email.setText("");
        reg_txt_phnmb.setText("");
        reg_txt_nic.setText("");
        reg_txt_address.setText("");
        reg_txt_guardname.setText("");
        reg_txt_guardtel.setText("");
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }


}
