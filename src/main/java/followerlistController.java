package main.java;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.io.ByteArrayInputStream;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class followerlistController {

    @FXML
    private ListView<HBox> followerListView; // 팔로워를 표시할 ListView

    private ObservableList<HBox> followerItems = FXCollections.observableArrayList(); // 팔로워 데이터 저장

    @FXML
    public void initialize() {
        loadFollowers(); // 팔로워 데이터를 불러옵니다.
    }

    private void loadFollowers() {
        try {
            Connection con = Main.con;
            if (con == null) {
                System.out.println("Database connection is not available.");
                return;
            }

            // 팔로워 목록 가져오는 SQL 쿼리
            String query = """
                    SELECT u.user_id, u.profile_image
                    FROM follower f
                    JOIN user u ON f.follower_id = u.user_id
                    WHERE f.user_id = ?
                      AND f.f_allow = 'Y'
                    ORDER BY u.user_id
                    """;

            try (PreparedStatement pstmt = con.prepareStatement(query)) {
                pstmt.setString(1, Main.loggedInUserId); // 현재 로그인된 사용자 ID
                ResultSet rs = pstmt.executeQuery();

                // 팔로워 데이터를 HBox로 생성하여 추가
                while (rs.next()) {
                    String followerId = rs.getString("user_id");
                    byte[] imageBytes = rs.getBytes("profile_image");
                    HBox cell = createFollowerCell(followerId, imageBytes);
                    followerItems.add(cell);
                }

                // ListView에 팔로워 데이터 설정
                followerListView.setItems(followerItems);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error occurred while loading followers.");
        }
    }

    private HBox createFollowerCell(String followerId, byte[] imageBytes) {
        // 팔로워 ID 텍스트 생성
        Text followerText = new Text(followerId);
        followerText.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        ImageView profileImageView = new ImageView();
        if (imageBytes != null) {
            Image img = new Image(new ByteArrayInputStream(imageBytes));
            profileImageView.setImage(img);
            profileImageView.setFitHeight(30); // 이미지 높이 설정
            profileImageView.setFitWidth(30); // 이미지 너비 설정
            profileImageView.setClip(new Circle(15, 15, 15)); // 원형 클립
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox cell = new HBox(followerText, profileImageView, spacer);
        cell.setSpacing(20); // 간격 설정
        cell.setUserData(followerId); // HBox에 사용자 ID 저장
        return cell;
    }
    
    @FXML
    private void handleBackButton(ActionEvent event) {
        try {
        	// 메인 화면으로 돌아가기
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/resources/profileScreen.fxml"));
            Parent profileRoot = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene profileScene = new Scene(profileRoot);
            stage.setScene(profileScene);
            stage.setTitle("Profile");  //별명 구현
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error occurred while navigating back.");
        }
    }
}
