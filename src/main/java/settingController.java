package main.java;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;

import java.io.IOException;

public class settingController {

    @FXML
    private void handleChangePasswordButton(ActionEvent event) {
        try {
            // changepwScreen.fxml 로드
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/resources/changepwScreen.fxml"));
            Parent changepwRoot = loader.load();

            // 현재 Stage 가져오기
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // 새로운 Scene 설정
            Scene changepwScene = new Scene(changepwRoot);
            stage.setScene(changepwScene);
            stage.setTitle("Change Password");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error occurred while loading Change Password screen.");
        }
    }

    @FXML
    private void handleLogoutButton(ActionEvent event) {
        try {
            // loginScreen.fxml 로드
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/resources/loginScreen.fxml"));
            Parent loginRoot = loader.load();

            // 현재 Stage 가져오기
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // 새로운 Scene 설정
            Scene loginScene = new Scene(loginRoot);
            stage.setScene(loginScene);
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error occurred while loading Login screen.");
        }
    }
    
    @FXML
    private void handleBackButton(ActionEvent event) {
        try {
        	// 프로필 화면으로 돌아가기
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/resources/profileScreen.fxml"));
            Parent profileRoot = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene profileScene = new Scene(profileRoot);
            stage.setScene(profileScene);
            stage.setTitle("Profile");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error occurred while navigating back.");
        }
    }

}
