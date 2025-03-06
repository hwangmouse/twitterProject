package main.java;

import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.Node;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class signupController {
	
	@FXML
    private TextField idField; // 사용자 ID 입력 필
	@FXML
    private TextField passwordField; // 사용자 비밀번호 입력 필드
	@FXML
    private TextField confirmPasswordField;

    @FXML
    private void handleGoBackLogInButton(ActionEvent event) {
        try {
        	
        	String userId = idField.getText();
            String password = passwordField.getText();
            String checkpassword = confirmPasswordField.getText();
            
            
            if (userId.isEmpty() || password.isEmpty() || checkpassword.isEmpty()) {
            	Alert.show("Error", "ID and Password cannot be empty.");
                System.out.println("ID and Password fields cannot be empty.");
                return;
            }
            if (!password.equals(checkpassword)) {
            	Alert.show("Error", "Passwords do not match.");
                System.out.println("Passwords do not match.");
                return;
            }
            
            // Main.con을 사용하여 데이터베이스 연결
            Connection con = Main.con;
            if (con == null) {
                System.out.println("Database connection is not available.");
                return;
            }
            
            // 중복 확인 쿼리
            String checkDuplicateSql = "SELECT COUNT(*) FROM user WHERE user_id = ? OR pwd = ?";
            try (PreparedStatement checkStmt = con.prepareStatement(checkDuplicateSql)) {
                checkStmt.setString(1, userId);
                checkStmt.setString(2, password);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                	Alert.show("Error", " ID or PW is already in use.");
                    return;
                }
            }
            
            // 데이터 저장
            String insertUsersql = "INSERT INTO user (user_id, pwd) VALUES (?, ?)";
        	
            try (PreparedStatement pstmt = con.prepareStatement(insertUsersql)) {
                pstmt.setString(1, userId);
                pstmt.setString(2, password);

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("User registered successfully.");
                } else {
                    System.out.println("User registration failed.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                System.err.println("Error while inserting user into the database.");
            }
            
            // FXML 파일 로드
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/resources/loginScreen.fxml"));
            Parent makeLogInRoot = loader.load();

            // 현재 Stage 가져오기
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // 기존 Stage 크기 저장
	        double currentWidth = stage.getWidth();
	        double currentHeight = stage.getHeight();
	        
            // 새로운 Scene 설정
            Scene makeLogInScene = new Scene(makeLogInRoot);
            
            // 애니메이션 설정 (왼쪽으로 넘기기)
            makeLogInRoot.setTranslateX(-stage.getWidth()); // 새로운 화면을 오른쪽에 배치
            stage.setScene(makeLogInScene);
            
            // Stage 크기 고정 (비율 변경 방지)
	        stage.setWidth(currentWidth);
	        stage.setHeight(currentHeight);
	        	  
	        // TranslateTransition을 사용해 뢴쪽에서 오른쪽으로 이동
            TranslateTransition slideIn = new TranslateTransition(Duration.seconds(0.5), makeLogInRoot);
            slideIn.setFromX(-stage.getWidth()); // 화면 밖 왼쪽에서 시작
            slideIn.setToX(0);                  // 화면 안으로 슬라이드
            slideIn.play();
            stage.setTitle("LogIn");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}