package Controllers.Student;

import java.awt.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
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


public class Student_feeController implements Initializable {

    @FXML
    private Button btn_back;
    @FXML
    private TextField studentID;
    @FXML
    private TextField studentFee;
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


        ObservableList<String> months = FXCollections.observableArrayList("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December");
        month.setItems(months);

        ObservableList<String> years = FXCollections.observableArrayList("2020", "2021", "2022", "2023", "2024", "2025", "2026", "2027", "2028", "2029", "2030");
        year.setItems(years);
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

    @FXML
    private void submitButtonAction(MouseEvent event) {
        String id = studentID.getText();
        String fee = studentFee.getText();
        String selectedMonth = month.getValue();
        String selectedYear = year.getValue();


        if (id == null || id.trim().isEmpty() || fee == null || fee.trim().isEmpty() || selectedMonth == null || selectedYear == null) {
            showAlert("Validation Error", "All Fields Are Required!", Alert.AlertType.ERROR);
            return;
        }


        try {
            Double.parseDouble(fee);
        } catch (NumberFormatException e) {
            showAlert("Warning", "Fee must be a numeric value", Alert.AlertType.WARNING);
            return;
        }


        connection = handler.connectDB();
        String checkStudentQuery = "SELECT email FROM register_students WHERE nsbmId = ?";
        String studentEmail = null;
        try {
            pst = connection.prepareStatement(checkStudentQuery);
            pst.setString(1, id);
            ResultSet rs = pst.executeQuery();
            if (!rs.next()) {
                showAlert("Error Code 404", "Student ID doesn't exist", Alert.AlertType.ERROR);
                return;
            }
            studentEmail = rs.getString("email");
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            showAlert("Database Error", ex.getMessage(), Alert.AlertType.ERROR);
            return;
        }


        String insertQuery = "INSERT INTO student_fee(studentid, year, fee, month) VALUES(?, ?, ?, ?)";
        try {
            pst = connection.prepareStatement(insertQuery);
            pst.setString(1, id);
            pst.setString(2, selectedYear);
            pst.setString(3, fee);
            pst.setString(4, selectedMonth);

            pst.executeUpdate();
            showAlert("Message", "Fee Record Added Successfully", Alert.AlertType.INFORMATION);


            sendEmail(studentEmail, selectedMonth, selectedYear, fee);

            setTExtRefresh();
        } catch (SQLException ex) {
            Logger.getLogger(Student_feeController.class.getName()).log(Level.SEVERE, null, ex);
            showAlert("Error", "Error Saving Fee Record", Alert.AlertType.ERROR);
        } finally {
            try {
                if (pst != null) pst.close();
                if (connection != null) connection.close();
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
                showAlert("Database Error", "An error occurred while connecting to the database", Alert.AlertType.ERROR);
            }
        }
    }

    private void sendEmail(String studentEmail, String month, String year, String fee) {
        try {
            String subject = "Fee Payment Pending for " + month + " " + year;
            String body = "Dear Student,\n\nYour fee of " + fee + " for the month of " + month + " " + year + " is still pending.\n\nPlease make the payment at the earliest.\n\nBest regards,\nYour p2University";


            String encodedSubject = URLEncoder.encode(subject, "UTF-8");
            String encodedBody = URLEncoder.encode(body, "UTF-8");


            String mailto = "https://mail.google.com/mail/?view=cm&fs=1&to=" + studentEmail + "&su=" + encodedSubject + "&body=" + encodedBody;


            if (Desktop.isDesktopSupported()) {
                URI mailtoURI = new URI(mailto);
                Desktop.getDesktop().browse(mailtoURI);
            } else {
                showAlert("Error", "Desktop email client not supported", Alert.AlertType.ERROR);
            }
        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
            showAlert("Encoding Error", "Failed to encode the email content", Alert.AlertType.ERROR);
        } catch (java.net.URISyntaxException e) {
            System.out.println(e.getMessage());
            showAlert("URI Error", "Invalid URI format for mailto link", Alert.AlertType.ERROR);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            showAlert("Error", "Failed to open web browser", Alert.AlertType.ERROR);
        }
    }

    private void setTExtRefresh() {
        studentID.clear();
        studentFee.clear();
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
