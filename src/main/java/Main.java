package main.java;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.sql.*;

public class Main extends Application {
	public static Connection con; // 공유할 Connection 객체
	public static String loggedInUserId; // 로그인한 사용자 ID
	
	@Override
    public void start(Stage primaryStage) {
        try {
            // MySQL 연결 설정
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://localhost/Twitter";
            String user = "root";
            String passwd = "0327";
            con = DriverManager.getConnection(url, user, passwd);
            System.out.println("Database connected successfully.");
            
            // 로그인 화면 FXML 로드
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/resources/loginScreen.fxml"));
            Parent root = loader.load();

            // Scene 설정
            Scene scene = new Scene(root, 320, 480);

            // Stage 설정
            primaryStage.setScene(scene);
            primaryStage.setTitle("LogIn");
            primaryStage.getIcons().add(new Image("/main/images/twitterIcon.png"));
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

