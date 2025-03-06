package main.java;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.Node;
import java.io.IOException;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class loginController {
	@FXML
    private TextField idField; // 사용자 ID 입력 필
	@FXML
    private TextField passwordField; // 사용자 비밀번호 입력 필드
	
    @FXML
    private void handleSignUpButton(ActionEvent event) {
        try {
            // FXML 로드
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/resources/signupScreen.fxml"));
            Parent signUpRoot = loader.load();

            // 현재 Stage 가져오기
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            
            // 기존 Stage 크기 저장
	        double currentWidth = stage.getWidth();
	        double currentHeight = stage.getHeight();

            // 새로운 Scene 설정
            Scene signUpScene = new Scene(signUpRoot);
            
            // 애니메이션 설정 (오른쪽으로 넘기기)
            signUpRoot.setTranslateX(stage.getWidth()); // 새로운 화면을 오른쪽에 배치
            stage.setScene(signUpScene);
            
            // Stage 크기 고정 (비율 변경 방지)
	        stage.setWidth(currentWidth);
	        stage.setHeight(currentHeight);
            
            // TranslateTransition을 사용해 오른쪽에서 왼쪽으로 이동
            TranslateTransition slideIn = new TranslateTransition(Duration.seconds(0.5), signUpRoot);
            slideIn.setFromX(stage.getWidth()); // 화면 밖 오른쪽에서 시작
            slideIn.setToX(0);                  // 화면 안으로 슬라이드
            slideIn.play();
            stage.setTitle("Sign Up");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleLogInButton(ActionEvent event) {
        try {
            String userId = idField.getText();
            String password = passwordField.getText();

            if (userId.isEmpty() || password.isEmpty()) {
                Alert.show("Error", "Fill in ID & Password.");
                System.out.println("ID and Password fields cannot be empty.");
                return;
            }

            // Main.con을 사용하여 데이터베이스 연결
            Connection con = Main.con;
            if (con == null) {
                System.out.println("Database connection is not available.");
                return;
            }

            String loginSql = "SELECT * FROM user WHERE user_id = ? AND pwd = ?";
            try (PreparedStatement pstmt = con.prepareStatement(loginSql)) {
                pstmt.setString(1, userId);
                pstmt.setString(2, password);

                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                	// 로그인 성공 - Main에 사용자 ID 저장
                    Main.loggedInUserId = userId;
                    System.out.println("Logged-in User ID: " + Main.loggedInUserId);
                    
                    // FXML 로드
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/resources/mainfeedScreen.fxml"));
                    Parent mainfeedRoot = loader.load();

                    // 현재 Stage 가져오기
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

                    // 현재 Scene의 Root
                    Parent currentRoot = stage.getScene().getRoot();

                    // 페이드 아웃 애니메이션 (현재 화면)
                    FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.3), currentRoot);
                    fadeOut.setFromValue(1.0);
                    fadeOut.setToValue(0.0);
                    fadeOut.setOnFinished(e -> {
                        // 새로운 Scene 설정 및 페이드 인 애니메이션 추가
                        Scene mainfeedScene = new Scene(mainfeedRoot);
                        stage.setScene(mainfeedScene);
                        stage.setTitle("Welcome!");

                        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.3), mainfeedRoot);
                        fadeIn.setFromValue(0.0);
                        fadeIn.setToValue(1.0);
                        fadeIn.play();
                    });

                    // 페이드 아웃 시작
                    fadeOut.play();
                } else {
                    Alert.show("Login Failed", "Invalid ID or Password.");
                    System.out.println("Invalid ID or Password.");
                }

                // 리소스 해제
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Error occurred while connecting to the database.");
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error occurred while loading the next screen.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}