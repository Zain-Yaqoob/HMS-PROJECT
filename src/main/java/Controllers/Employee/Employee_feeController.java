package Controllers.Employee;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javax.swing.JOptionPane;
import DBConnection.DBHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Employee_feeController implements Initializable {

    @FXML
    private Button btn_back;
    @FXML
    private TextField employeeID;
    @FXML
    private TextField employeeFee;
    @FXML
    private Button submit;

    @FXML
    private ComboBox<String> month;

    @FXML
    private ComboBox<String> year;

    private Connection connection;
    private DBHandler handler;
    private PreparedStatement pst;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        handler = new DBHandler();

        ObservableList<String> months = FXCollections.observableArrayList(
                "January", "February", "March", "April", "May", "June", "July", "August",
                "September", "October", "November", "December"
        );
        month.setItems(months);

        ObservableList<String> years = FXCollections.observableArrayList(
                "2020", "2021", "2022", "2023", "2024", "2025", "2026", "2027", "2028",
                "2029", "2030"
        );
        year.setItems(years);
    }

    @FXML
    private void back_btn_clicked(MouseEvent event) throws IOException {
        btn_back.getScene().getWindow().hide();

        Stage emp_Menu = new Stage();
        Parent root = FXMLLoader.load(getClass().getResource("/FXML/Employee/Employee_Menu.fxml"));
        Scene scene = new Scene(root);
        emp_Menu.setScene(scene);
        emp_Menu.initStyle(StageStyle.TRANSPARENT);
        emp_Menu.show();
        emp_Menu.setResizable(false);
    }

    @FXML
    private void submitButtonAction(MouseEvent event) {
        String id = employeeID.getText();
        String fee = employeeFee.getText();
        String selectedMonth = month.getValue();
        String selectedYear = year.getValue();

        if (id == null || id.trim().isEmpty() || fee == null || fee.trim().isEmpty() || selectedMonth == null || selectedYear == null) {
            showAlert("Warning", "Nothing Selected", Alert.AlertType.WARNING);
            return;
        }

        try {
            Double.parseDouble(fee);
        } catch (NumberFormatException e) {
            showAlert("Warning", "Salary must be a numeric value.", Alert.AlertType.WARNING);
            return;
        }

        connection = handler.connectDB();
        String checkEmployeeQuery = "SELECT * FROM register_employee WHERE id = ?"; // Assuming a table named 'employees'
        try {
            pst = connection.prepareStatement(checkEmployeeQuery);
            pst.setString(1, id);
            ResultSet rs = pst.executeQuery();
            if (!rs.next()) {
                showAlert("Error 404", "Employee ID doesn't exist", Alert.AlertType.WARNING);
                return;
            }
        } catch (SQLException ex) {
            Logger.getLogger(Employee_feeController.class.getName()).log(Level.SEVERE, null, ex);
           showAlert("Database Error", "Couldn't connect to database", Alert.AlertType.ERROR);
            return;
        }

        String insertQuery = "INSERT INTO employee_fee(employeeid, year, salary, month) VALUES(?, ?, ?, ?)";
        try {
            pst = connection.prepareStatement(insertQuery);
            pst.setString(1, id);
            pst.setString(2, selectedYear);
            pst.setString(3, fee);
            pst.setString(4, selectedMonth);

            pst.executeUpdate();
            showAlert("Message", "Salary record added successfully", Alert.AlertType.INFORMATION);
            setTextRefresh();
        } catch (SQLException ex) {
            Logger.getLogger(Employee_feeController.class.getName()).log(Level.SEVERE, null, ex);
            showAlert("Error", "Error saving salary record", Alert.AlertType.ERROR);
        } finally {
            try {
                if (pst != null) pst.close();
                if (connection != null) connection.close();
            } catch (SQLException ex) {
                Logger.getLogger(Employee_feeController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void setTextRefresh() {
        employeeID.clear();
        employeeFee.clear();
        month.setValue(null);
        year.setValue(null);
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
