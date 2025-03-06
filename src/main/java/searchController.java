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
import javafx.scene.control.TextField;
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

public class searchController {

    @FXML
    private TextField searchField; // 검색 입력 필드

    @FXML
    private ListView<HBox> userListView; // 검색 결과 표시할 ListView

    private ObservableList<HBox> userList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 초기화 시 전체 사용자 로드
        loadUsers("");

        // 검색 필드에 입력값 변경 시 필터링
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            loadUsers(newValue);
        });
    }

    private void loadUsers(String searchQuery) {
        userList.clear(); // Clear existing data

        try {
            Connection con = Main.con;
            if (con == null) {
                System.out.println("Database connection is not available.");
                return;
            }

            // 로그인한 사용자를 제외하고 사용자 ID와 프로필 이미지를 가져옴
            String query = "SELECT user_id, profile_image FROM user WHERE user_id LIKE ? AND user_id != ?";
            try (PreparedStatement pstmt = con.prepareStatement(query)) {
                String searchPattern = "%" + searchQuery + "%"; // 검색 패턴
                pstmt.setString(1, searchPattern);
                pstmt.setString(2, Main.loggedInUserId); // 본인 제외를 위해

                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    String userId = rs.getString("user_id");
                    byte[] imageBytes = rs.getBytes("profile_image"); 
                    userList.add(createUserCell(userId, imageBytes));
                }
            }

            // Display results in ListView
            userListView.setItems(userList);

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error occurred while fetching users.");
        }
    }

    private HBox createUserCell(String userId, byte[] imageBytes) {
        Text userIdText = new Text(userId);
        userIdText.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        
        ImageView profileImageView = new ImageView();
        if (imageBytes != null) {
            Image img = new Image(new ByteArrayInputStream(imageBytes));
            profileImageView.setImage(img);
            profileImageView.setFitHeight(30); // Set the height of the image
            profileImageView.setFitWidth(30); // Set the width of the image
            profileImageView.setClip(new Circle(15, 15, 15)); // Clip as a circle
        }
        
        Button followButton = new Button();

        // 초기 상태 설정 (팔로우 여부 확인)
        checkFollowStatus(userId, followButton);
        followButton.setStyle("-fx-background-color: #FFC1C1; -fx-text-fill: white; -fx-background-radius: 10;");
        followButton.setOnAction(event -> handleFollowButton(userId, followButton));
        
     // Block 버튼
        Button blockButton = new Button();
        checkBlockStatus(userId, blockButton); // Block 상태 확인
        blockButton.setStyle("-fx-background-color: #A9A9A9; -fx-text-fill: white; -fx-background-radius: 10;");
        blockButton.setOnAction(event -> handleBlockButton(userId, blockButton));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox cell = new HBox(userIdText, profileImageView, spacer, blockButton, followButton);
        cell.setSpacing(20); // 간격 설정
        return cell;
    }
    
    private void checkBlockStatus(String userId, Button blockButton) {
        try {
            Connection con = Main.con;
            if (con == null) {
                System.out.println("Database connection is not available.");
                return;
            }

            // 차단 상태 확인 쿼리
            String query = "SELECT * FROM block WHERE blocker = ? AND blocked = ?";
            try (PreparedStatement pstmt = con.prepareStatement(query)) {
                pstmt.setString(1, Main.loggedInUserId); // 현재 로그인된 사용자
                pstmt.setString(2, userId); // 확인할 사용자

                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    blockButton.setText("Unblock"); // 이미 차단된 상태
                } else {
                    blockButton.setText("Block"); // 차단되지 않은 상태
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error occurred while checking block status.");
        }
    }
    
    private void handleBlockButton(String userId, Button blockButton) {
        if (blockButton.getText().equals("Block")) {
            blockUser(userId, blockButton);
        } else {
            unblockUser(userId, blockButton);
        }
    }
    
    private void blockUser(String userId, Button blockButton) {
        try {
            Connection con = Main.con;
            if (con == null) {
                System.out.println("Database connection is not available.");
                return;
            }

            // block 테이블에 데이터 삽입
            String blockQuery = "INSERT INTO block (blocker, blocked) VALUES (?, ?)";
            try (PreparedStatement pstmt = con.prepareStatement(blockQuery)) {
                pstmt.setString(1, Main.loggedInUserId); // 현재 로그인된 사용자
                pstmt.setString(2, userId); // 차단할 사용자
                pstmt.executeUpdate();
            }

            blockButton.setText("Unblock"); // 버튼 상태 변경
            System.out.println("Blocked user: " + userId);

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error occurred while blocking the user.");
        }
    }
    
    private void unblockUser(String userId, Button blockButton) {
        try {
            Connection con = Main.con;
            if (con == null) {
                System.out.println("Database connection is not available.");
                return;
            }

            // block 테이블에서 데이터 삭제
            String unblockQuery = "DELETE FROM block WHERE blocker = ? AND blocked = ?";
            try (PreparedStatement pstmt = con.prepareStatement(unblockQuery)) {
                pstmt.setString(1, Main.loggedInUserId); // 현재 로그인된 사용자
                pstmt.setString(2, userId); // 차단 해제할 사용자
                pstmt.executeUpdate();
            }

            blockButton.setText("Block"); // 버튼 상태 변경
            System.out.println("Unblocked user: " + userId);

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error occurred while unblocking the user.");
        }
    }
    
    

 

    private void checkFollowStatus(String userId, Button followButton) {
        try {
            Connection con = Main.con;
            if (con == null) {
                System.out.println("Database connection is not available.");
                return;
            }

            // 팔로우 상태 확인 쿼리
            String query = "SELECT * FROM follower WHERE user_id = ? AND follower_id = ?";
            try (PreparedStatement pstmt = con.prepareStatement(query)) {
                pstmt.setString(1, userId);
                pstmt.setString(2, Main.loggedInUserId);

                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    followButton.setText("Followed");
                } else {
                    followButton.setText("Follow");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error occurred while checking follow status.");
        }
    }

    private void handleFollowButton(String userId, Button followButton) {
        if (followButton.getText().equals("Follow")) {
            followUser(userId, followButton);
        } else {
            unfollowUser(userId, followButton);
        }
    }

    private void followUser(String userId, Button followButton) {
        try {
            Connection con = Main.con;
            if (con == null) {
                System.out.println("Database connection is not available.");
                return;
            }

            // follower 테이블에 데이터 삽입
            String insertFollowerQuery = "INSERT INTO follower (f_id, f_allow, user_id, follower_id) VALUES (?, 'Y', ?, ?)";
            try (PreparedStatement pstmt = con.prepareStatement(insertFollowerQuery)) {
                pstmt.setString(1, java.util.UUID.randomUUID().toString()); // f_id 생성
                pstmt.setString(2, userId); // user_id
                pstmt.setString(3, Main.loggedInUserId); // follower_id
                pstmt.executeUpdate();
            }

            // following 테이블에 데이터 삽입
            String insertFollowingQuery = "INSERT INTO following (f_id, f_allow, user_id, following_id) VALUES (?, 'Y', ?, ?)";
            try (PreparedStatement pstmt = con.prepareStatement(insertFollowingQuery)) {
                pstmt.setString(1, java.util.UUID.randomUUID().toString()); // f_id 생성
                pstmt.setString(2, Main.loggedInUserId); // user_id
                pstmt.setString(3, userId); // following_id
                pstmt.executeUpdate();
            }

            followButton.setText("Followed");
            System.out.println("Followed user: " + userId);

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error occurred while following the user.");
        }
    }

    private void unfollowUser(String userId, Button followButton) {
        try {
            Connection con = Main.con;
            if (con == null) {
                System.out.println("Database connection is not available.");
                return;
            }

            // follower 테이블에서 데이터 삭제
            String deleteFollowerQuery = "DELETE FROM follower WHERE user_id = ? AND follower_id = ?";
            try (PreparedStatement pstmt = con.prepareStatement(deleteFollowerQuery)) {
                pstmt.setString(1, userId);
                pstmt.setString(2, Main.loggedInUserId);
                pstmt.executeUpdate();
            }

            // following 테이블에서 데이터 삭제
            String deleteFollowingQuery = "DELETE FROM following WHERE user_id = ? AND following_id = ?";
            try (PreparedStatement pstmt = con.prepareStatement(deleteFollowingQuery)) {
                pstmt.setString(1, Main.loggedInUserId);
                pstmt.setString(2, userId);
                pstmt.executeUpdate();
            }

            followButton.setText("Follow");
            System.out.println("Unfollowed user: " + userId);

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error occurred while unfollowing the user.");
        }
    }
    
    @FXML
    private void handleBackButton(ActionEvent event) {
        try {
        	// 메인 화면으로 돌아가기
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/resources/mainfeedScreen.fxml"));
            Parent mainfeedRoot = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene mainfeedScene = new Scene(mainfeedRoot);
            stage.setScene(mainfeedScene);
            stage.setTitle("Welcome! '별명");  //별명 구현
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error occurred while navigating back.");
        }
    }
}
