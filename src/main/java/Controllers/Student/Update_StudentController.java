/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controllers.Student;

import Model.StudentDetails;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import DBConnection.DBHandler;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javax.swing.JOptionPane;


public class Update_StudentController implements Initializable {

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
    @FXML
    private Button btn_update_student;

    @FXML
    private Button btn_refersh;
    private ObservableList<StudentDetails> data;

    @FXML
    private TableView<StudentDetails> tableStudent;
    @FXML
    private TableColumn<StudentDetails, String> col_name;
    @FXML
    private TableColumn<StudentDetails, String> col_nsbmid;
    @FXML
    private TableColumn<StudentDetails, String> col_email;
    @FXML
    private TableColumn<StudentDetails, String> col_phonenumber;
    @FXML
    private TableColumn<StudentDetails, String> col_nic;
    @FXML
    private TableColumn<StudentDetails, String> col_address;
    @FXML
    private TableColumn<StudentDetails, String> col_g_name;
    @FXML
    private TableColumn<StudentDetails, String> col_g_tel;

    private Connection connection;
    private DBHandler handler;
    private PreparedStatement pst;

    @FXML
    private TextField reg_txt_id;
    @FXML
    private TableColumn<StudentDetails, String> col_id;
    @FXML
    private Button btn_back;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        handler = new DBHandler();
    }

    private void autoRefresh() {
        connection = handler.connectDB();
        data = FXCollections.observableArrayList();

        try {
            // Execure query
            ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM register_students");

            while (rs.next()) {
                // get string from db
                data.add(new StudentDetails(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8), rs.getString(9)));

            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            showAlert("Database Error", "An error occurred while connecting to the database", Alert.AlertType.ERROR);
        }

        // set cell values
        col_id.setCellValueFactory(new PropertyValueFactory<>("id"));
        col_name.setCellValueFactory(new PropertyValueFactory<>("name"));
        col_nsbmid.setCellValueFactory(new PropertyValueFactory<>("nsbmId"));
        col_email.setCellValueFactory(new PropertyValueFactory<>("email"));
        col_phonenumber.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        col_nic.setCellValueFactory(new PropertyValueFactory<>("nic"));
        col_address.setCellValueFactory(new PropertyValueFactory<>("address"));
        col_g_name.setCellValueFactory(new PropertyValueFactory<>("guardName"));
        col_g_tel.setCellValueFactory(new PropertyValueFactory<>("guardTel"));

        tableStudent.setItems(null);
        tableStudent.setItems(data);
    }

    private boolean isNsbmIdExists(String nsbmID, String currentId) {
        String query = "SELECT id FROM register_students WHERE nsbmID = ? AND id != ?";
        try {
            pst = connection.prepareStatement(query);
            pst.setString(1, nsbmID);
            pst.setString(2, currentId);
            ResultSet rs = pst.executeQuery();
            return rs.next(); // If a record exists, nsbmID is already in use
        } catch (SQLException e) {
            showAlert("Database Error", "An error occurred while checking NSBM ID.", Alert.AlertType.ERROR);
            System.out.println(e.getMessage());
        }
        return false;
    }

    private boolean isNicExists(String nic, String currentId) {
        String query = "SELECT id FROM register_students WHERE nic = ? AND id != ?";
        try {
            pst = connection.prepareStatement(query);
            pst.setString(1, nic);
            pst.setString(2, currentId);
            ResultSet rs = pst.executeQuery();
            return rs.next(); // If a record exists, NIC is already in use
        } catch (SQLException e) {
            showAlert("Database Error", "An error occurred while checking NIC.", Alert.AlertType.ERROR);
            System.out.println(e.getMessage());
        }
        return false;
    }


    @FXML
    private void updateStudentButtonAction(MouseEvent event) {
        String id = reg_txt_id.getText();
        String userName = reg_txt_username.getText();
        String nsbmID = reg_txt_nsbmid.getText();
        String email = reg_txt_email.getText();
        String phoneNumber = reg_txt_phnmb.getText();
        String nic = reg_txt_nic.getText();
        String address = reg_txt_address.getText();
        String guardName = reg_txt_guardname.getText();
        String guardTel = reg_txt_guardtel.getText();

        // Validate inputs
        if (userName.isEmpty() || nsbmID.isEmpty() || email.isEmpty() || phoneNumber.isEmpty() ||
                nic.isEmpty() || address.isEmpty() || guardName.isEmpty() || guardTel.isEmpty()) {
            showAlert("Validation Error", "All fields are required!", Alert.AlertType.ERROR);
            return;
        }

        if (!nsbmID.matches("\\d+")) {
            showAlert("Validation Error", "NSBM ID must be numeric.", Alert.AlertType.ERROR);
            return;
        }

        if (!email.endsWith("@gmail.com")) {
            showAlert("Validation Error", "Email must end with '@gmail.com'.", Alert.AlertType.ERROR);
            return;
        }

        if (!phoneNumber.matches("\\+92\\d{10}")) {
            showAlert("Validation Error", "Phone number must start with +92 and contain exactly 10 digits.", Alert.AlertType.ERROR);
            return;
        }

        if (!guardTel.matches("\\+92\\d{10}")) {
            showAlert("Validation Error", "Guardian's contact must start with +92 and contain exactly 10 digits.", Alert.AlertType.ERROR);
            return;
        }

        if (!nic.matches("\\d{5}-\\d{7}-\\d")) {
            showAlert("Validation Error", "CNIC must be in the format 12345-1234567-1.", Alert.AlertType.ERROR);
            return;
        }

        if (isNsbmIdExists(nsbmID, id)) {
            showAlert("Duplicate Error", "The Roll No already exists.", Alert.AlertType.ERROR);
            return;
        }

        if (isNicExists(nic, id)) {
            showAlert("Duplicate Error", "The CNIC already exists.", Alert.AlertType.ERROR);
            return;
        }

        String updateQuery = "UPDATE register_students SET name = ?, nsbmID = ?, email = ?, phoneNumber = ?, nic = ?, address = ?, guardName = ?, guardTel = ? WHERE id = ?";
        connection = handler.connectDB();

        try {
            pst = connection.prepareStatement(updateQuery);
            pst.setString(1, userName);
            pst.setString(2, nsbmID);
            pst.setString(3, email);
            pst.setString(4, phoneNumber);
            pst.setString(5, nic);
            pst.setString(6, address);
            pst.setString(7, guardName);
            pst.setString(8, guardTel);
            pst.setString(9, id);

            int rowsUpdated = pst.executeUpdate();
            if (rowsUpdated > 0) {
                showAlert("Success", "Student record updated successfully!", Alert.AlertType.INFORMATION);
                autoRefresh();
            } else {
                showAlert("Update Failed", "No record found with the given ID.", Alert.AlertType.WARNING);
            }
        } catch (SQLException e) {
            showAlert("Database Error", "An error occurred while updating the student record.", Alert.AlertType.ERROR);
            System.out.println(e.getMessage());
        } finally {
            try {
                if (pst != null) pst.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }



    @FXML
    private void refreshButtionClickAction(MouseEvent event) {
        connection = handler.connectDB();
        data = FXCollections.observableArrayList();

        try {

            ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM register_students");

            while (rs.next()) {

                data.add(new StudentDetails(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8), rs.getString(9)));

            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }

        // set cell values
        col_id.setCellValueFactory(new PropertyValueFactory<>("id"));
        col_name.setCellValueFactory(new PropertyValueFactory<>("name"));
        col_nsbmid.setCellValueFactory(new PropertyValueFactory<>("nsbmId"));
        col_email.setCellValueFactory(new PropertyValueFactory<>("email"));
        col_phonenumber.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        col_nic.setCellValueFactory(new PropertyValueFactory<>("nic"));
        col_address.setCellValueFactory(new PropertyValueFactory<>("address"));
        col_g_name.setCellValueFactory(new PropertyValueFactory<>("guardName"));
        col_g_tel.setCellValueFactory(new PropertyValueFactory<>("guardTel"));

        tableStudent.setItems(null);
        tableStudent.setItems(data);
    }

    @FXML
    private void displaySelectedAction(MouseEvent event) {
        StudentDetails student = tableStudent.getSelectionModel().getSelectedItem();
        if (student == null) {
            showAlert("Warning", "Nothing Selected", Alert.AlertType.WARNING);
        } else {
            String id = student.getId();
            String name = student.getName();
            String nsbmid = student.getNsbmId();
            String email = student.getEmail();
            String phonenumber = student.getPhoneNumber();
            String nic = student.getNIC();
            String address = student.getAddress();
            String g_name = student.getGuardName();
            String g_tel = student.getGuardTel();

            reg_txt_id.setText(id);
            reg_txt_username.setText(name);
            reg_txt_nsbmid.setText(nsbmid);
            reg_txt_email.setText(email);
            reg_txt_phnmb.setText(phonenumber);
            reg_txt_nic.setText(nic);
            reg_txt_address.setText(address);
            reg_txt_guardname.setText(g_name);
            reg_txt_guardtel.setText(g_tel);
        }
    }

    @FXML
    private void back_btn_clicked(MouseEvent event) throws IOException {
        btn_back.getScene().getWindow().hide();

        Stage stu_Menu = new Stage();
        Parent root = FXMLLoader.load(getClass().getResource("/FXML/Student/Student_Menu.fxml"));
        Scene scene = new Scene(root);
        stu_Menu.setScene(scene);
        stu_Menu.initStyle(StageStyle.TRANSPARENT);
        stu_Menu.show();
        stu_Menu.setResizable(false);
        
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

}
