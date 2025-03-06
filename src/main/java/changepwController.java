package main.java;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class changepwController {

    @FXML
    private PasswordField currentPasswordField; // 현재 비밀번호 입력 필드

    @FXML
    private PasswordField newPasswordField; // 새 비밀번호 입력 필드

    @FXML
    private PasswordField confirmPasswordField; // 새 비밀번호 확인 필드

    @FXML
    private void handleChangeButton(ActionEvent event) {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // 입력 값 검증
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
        	Alert.show("Error", "Fill in all fields.");
            System.out.println("Fields cannot be empty.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
        	Alert.show("Error", "New passwords do not match.");
            System.out.println("New passwords do not match.");
            return;
        }

        try {
            Connection con = Main.con;
            if (con == null) {
            	 System.out.println("Database connection is not available.");
                return;
            }

            // 현재 비밀번호 확인
            String query = "SELECT pwd FROM user WHERE user_id = ?";
            try (PreparedStatement pstmt = con.prepareStatement(query)) {
                pstmt.setString(1, Main.loggedInUserId); // 현재 로그인된 사용자 ID
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    String storedPassword = rs.getString("pwd");
                    if (!storedPassword.equals(currentPassword)) {
                    	Alert.show("Error", "Wrong current password");
                        System.out.println("Wrong current password");
                        return;
                    }
                } else {
                	Alert.show("Error", "사용자를 찾을 수 없습니다.");
                	System.out.println("사용자를 찾을 수 없습니다.");
                    return;
                }
            }

            // 새 비밀번호 업데이트
            String updateQuery = "UPDATE user SET pwd = ? WHERE user_id = ?";
            try (PreparedStatement pstmt = con.prepareStatement(updateQuery)) {
                pstmt.setString(1, newPassword);
                pstmt.setString(2, Main.loggedInUserId); // 현재 로그인된 사용자 ID
                pstmt.executeUpdate();
                System.out.println("pw changed");
            }

            // 설정 화면으로 돌아가기
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/resources/settingScreen.fxml"));
            Parent settingRoot = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene settingScene = new Scene(settingRoot);
            stage.setScene(settingScene);
            stage.setTitle("Settings");
            stage.show();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("비밀번호 변경 중 오류가 발생했습니다.");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("화면 전환 중 오류가 발생했습니다.");
        }
    }
    
    @FXML
    private void handleBackButton(ActionEvent event) {
        try {
        	// 설정 화면으로 돌아가기
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/resources/settingScreen.fxml"));
            Parent settingRoot = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene settingScene = new Scene(settingRoot);
            stage.setScene(settingScene);
            stage.setTitle("Settings");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error occurred while navigating back.");
        }
    }
}
