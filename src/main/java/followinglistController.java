package main.java;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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

public class followinglistController {

    @FXML
    private ListView<HBox> followingListView; // 팔로잉을 표시할 ListView

    private ObservableList<HBox> followingItems = FXCollections.observableArrayList(); // 팔로잉 데이터 저장

    @FXML
    public void initialize() {
        loadFollowings(); // 팔로잉 데이터를 불러옵니다.
    }

    private void loadFollowings() {
        try {
            Connection con = Main.con;
            if (con == null) {
                System.out.println("Database connection is not available.");
                return;
            }

            // 팔로잉 목록 가져오는 SQL 쿼리
            String query = """
                    SELECT u.user_id, u.profile_image
                    FROM following f
                    JOIN user u ON f.following_id = u.user_id
                    WHERE f.user_id = ?
                      AND f.f_allow = 'Y'
                    ORDER BY u.user_id
                    """;

            try (PreparedStatement pstmt = con.prepareStatement(query)) {
                pstmt.setString(1, Main.loggedInUserId); // 현재 로그인된 사용자 ID
                ResultSet rs = pstmt.executeQuery();

                // 팔로잉 데이터를 HBox로 생성하여 추가
                while (rs.next()) {
                    String followingId = rs.getString("user_id");
                    byte[] imageBytes = rs.getBytes("profile_image");
                    HBox cell = createFollowingCell(followingId, imageBytes);
                    followingItems.add(cell);
                }

                // ListView에 팔로잉 데이터 설정
                followingListView.setItems(followingItems);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error occurred while loading followings.");
        }
    }

    private HBox createFollowingCell(String followingId, byte[] imageBytes) {
        // 팔로잉 ID 텍스트 생성
        Text followingText = new Text(followingId);
        followingText.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        ImageView profileImageView = new ImageView();
        if (imageBytes != null) {
            Image img = new Image(new ByteArrayInputStream(imageBytes));
            profileImageView.setImage(img);
            profileImageView.setFitHeight(30); // Set the height of the image
            profileImageView.setFitWidth(30); // Set the width of the image
            profileImageView.setClip(new Circle(15, 15, 15)); // Clip as a circle
        }

        Button unfollowButton = new Button();
        checkFollowingStatus(followingId, unfollowButton);
        unfollowButton.setStyle("-fx-background-color: #FFC1C1; -fx-text-fill: white; -fx-background-radius: 10;");
        unfollowButton.setOnAction(event -> handleUnfollowButton(followingId, unfollowButton));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox cell = new HBox(followingText, profileImageView, spacer, unfollowButton);
        cell.setSpacing(20); // 간격 설정
        cell.setUserData(followingId); // HBox에 사용자 ID 저장
        return cell;
    }

    private void checkFollowingStatus(String userId, Button unfollowButton) {
        try {
            Connection con = Main.con;
            if (con == null) {
                System.out.println("Database connection is not available.");
                return;
            }

            // 팔로잉 상태 확인 쿼리
            String query = "SELECT * FROM following WHERE user_id = ? AND following_id = ?";
            try (PreparedStatement pstmt = con.prepareStatement(query)) {
                pstmt.setString(1, Main.loggedInUserId);
                pstmt.setString(2, userId);

                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    unfollowButton.setText("Unfollow");
                } else {
                    unfollowButton.setText("Follow");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error occurred while checking following status.");
        }
    }

    private void handleUnfollowButton(String userId, Button unfollowButton) {
        if (unfollowButton.getText().equals("Unfollow")) {
            unfollowUser(userId, unfollowButton);
        }
    }

    private void unfollowUser(String userId, Button unfollowButton) {
        try {
            Connection con = Main.con;
            if (con == null) {
                System.out.println("Database connection is not available.");
                return;
            }

            // following 테이블에서 데이터 삭제
            String deleteFollowingQuery = "DELETE FROM following WHERE user_id = ? AND following_id = ?";
            try (PreparedStatement pstmt = con.prepareStatement(deleteFollowingQuery)) {
                pstmt.setString(1, Main.loggedInUserId);
                pstmt.setString(2, userId);
                pstmt.executeUpdate();
            }

            // follower 테이블에서 데이터 삭제
            String deleteFollowerQuery = "DELETE FROM follower WHERE user_id = ? AND follower_id = ?";
            try (PreparedStatement pstmt = con.prepareStatement(deleteFollowerQuery)) {
                pstmt.setString(1, userId);
                pstmt.setString(2, Main.loggedInUserId);
                pstmt.executeUpdate();
            }

            unfollowButton.setText("Follow");
            removeUserFromList(userId);
            System.out.println("Unfollowed user: " + userId);

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error occurred while unfollowing the user.");
        }
    }

    private void removeUserFromList(String userId) {
        HBox targetCell = null;
        for (HBox cell : followingItems) {
            if (cell.getUserData() != null && cell.getUserData().equals(userId)) {
                targetCell = cell;
                break;
            }
        }
        if (targetCell != null) {
            followingItems.remove(targetCell);
        }
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
