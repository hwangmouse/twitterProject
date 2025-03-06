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
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class profileController {
	@FXML
    private Button defaultProfilePicture; // 프로필 사진 버튼
	
	@FXML
    private ListView<HBox> postListView; // 게시물 제목을 표시할 ListView
	@FXML
	private ListView<HBox> likedPostListView; //좋아요한 게시물을 표시할 ListView
	
	private ObservableList<HBox> postItems = FXCollections.observableArrayList(); // 게시물 제목 저장

	@FXML
    public void initialize() {
		loadProfileImageFromDatabase();// 프로필 이미지를 로드하여 초기화
        loadUserPosts(); // 사용자의 게시물을 불러옵니다.
        loadLikedPosts(); // 좋아요한 게시물을 불러옵니다.
    }
	
	private void loadProfileImageFromDatabase() {
	    try (Connection con = Main.con) {
	        if (con == null || con.isClosed()) {
	            System.out.println("Database connection is not available.");
	            return;
	        }

	        // 데이터베이스에서 프로필 이미지 로드
	        String query = "SELECT profile_image FROM user WHERE user_id = ?";
	        try (PreparedStatement pstmt = con.prepareStatement(query)) {
	            pstmt.setString(1, Main.loggedInUserId);
	            ResultSet rs = pstmt.executeQuery();

	            if (rs.next()) {
	                InputStream imageStream = rs.getBinaryStream("profile_image");
	                if (imageStream != null) {
	                    // 이미지가 있으면 Image 객체 생성
	                    Image profileImage = new Image(imageStream);

	                    // UI에 반영
	                    ImageView imageView = new ImageView(profileImage);
	                    double imageSize = 100; // 이미지 크기
	                    imageView.setFitWidth(imageSize);
	                    imageView.setFitHeight(imageSize);
	                    imageView.setPreserveRatio(true);

	                    // 원형 클립 생성
	                    double radius = 50; // 원 반지름
	                    Circle clip = new Circle(imageSize / 2, imageSize / 2, radius);
	                    imageView.setClip(clip);

	                    // 버튼에 설정
	                    defaultProfilePicture.setGraphic(imageView);
	                    System.out.println("Profile picture loaded from database.");
	                } else {
	                    System.out.println("No profile image found for user.");
	                }
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        System.out.println("Error occurred while loading profile image from database.");
	    }
	}
	
	private void loadUserPosts() {
		ensureDatabaseConnection();
		try {
			Connection con = Main.con;
			if (con == null) {
                System.out.println("Database connection is not available.");
                return;
            }
			//사용자가 작성한 게시글 제목 불러오기
			String query = "SELECT title, created_at FROM posts WHERE writer_id = ? ORDER BY created_at DESC";
	        try (PreparedStatement pstmt = con.prepareStatement(query)) {
	            pstmt.setString(1, Main.loggedInUserId); // 현재 로그인된 사용자 ID
	            ResultSet rs = pstmt.executeQuery();

	            // 게시물 제목과 날짜를 HBox에 추가
	            while (rs.next()) {
	                String title = rs.getString("title");
	                String createdAt = rs.getString("created_at");

	                // 제목(Text)과 날짜(Text)을 각각 생성
	                Text titleText = new Text(title);
	                titleText.setStyle("-fx-font-weight: bold;");

	                Text dateText = new Text(createdAt);
	                dateText.setStyle("-fx-alignment: center-right;");

	                // HBox에 제목과 날짜를 배치
	                HBox postItem = new HBox();
	                postItem.setSpacing(10);
	                postItem.setStyle("-fx-padding: 5; -fx-alignment: center-left;");
	                Region spacer = new Region();
	                HBox.setHgrow(spacer, Priority.ALWAYS); // spacer가 빈 공간을 차지하도록 설정

	                postItem.getChildren().addAll(titleText, spacer, dateText);
	                postItems.add(postItem);
	            }

	            postListView.setItems(postItems); // ListView에 게시물 설정
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	        System.out.println("Error occurred while fetching posts.");
	    }
	}
	
	private void loadLikedPosts() {
		ensureDatabaseConnection();
        try {
            Connection con = Main.con;
            if (con == null) {
                System.out.println("Database connection is not available.");
                return;
            }
            //사용자가 좋아요 한 게시글 제목 불러오기
            String query = """
                    SELECT p.title, p.num_of_likes, p.post_id
                    FROM posts p
                    INNER JOIN post_like pl ON p.post_id = pl.post_id
                    WHERE pl.liker_id = ?
                    ORDER BY p.created_at DESC
                    """;

            try (PreparedStatement pstmt = con.prepareStatement(query)) {
                pstmt.setString(1, Main.loggedInUserId);
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    String title = rs.getString("title");
                    int numOfLikes = rs.getInt("num_of_likes");
                    String postId = rs.getString("post_id");

                    HBox cell = createLikedPostCell(title, numOfLikes, postId);
                    likedPostListView.getItems().add(cell);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error occurred while loading liked posts.");
        }
    }

    private HBox createLikedPostCell(String title, int numOfLikes, String postId) {
        Text titleText = new Text(title);
        titleText.setWrappingWidth(200); // 제목 줄바꿈 설정
        titleText.setStyle("-fx-font-weight: bold;");

        Button likeButton = new Button("♥ " + numOfLikes);
        likeButton.setStyle("-fx-background-color: #FFC1C1; -fx-text-fill: white; -fx-background-radius: 10;");
        likeButton.setOnAction(event -> handleUnlike(postId));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox cell = new HBox(titleText, spacer, likeButton);
        cell.setSpacing(10);
        cell.setStyle("-fx-padding: 5; -fx-border-width: 1;");
        return cell;
    }

    private void handleUnlike(String postId) {
        try {
            Connection con = Main.con;
            if (con == null) {
                System.out.println("Database connection is not available.");
                return;
            }

            // 좋아요 취소
            String deleteQuery = "DELETE FROM post_like WHERE post_id = ? AND liker_id = ?";
            try (PreparedStatement pstmt = con.prepareStatement(deleteQuery)) {
                pstmt.setString(1, postId);
                pstmt.setString(2, Main.loggedInUserId);
                pstmt.executeUpdate();
                System.out.println("Like removed for post: " + postId);

                // ListView 갱신
                likedPostListView.getItems().clear();
                loadLikedPosts();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error occurred while unliking the post.");
        }
    }
	
	@FXML
    private void handlePostEdit(javafx.scene.input.MouseEvent event) {
		HBox selectedPost = postListView.getSelectionModel().getSelectedItem(); // 선택된 제목 가져오기

        if (selectedPost == null) {
            System.out.println("No post selected.");
            return;
        }

        try {
        	 // HBox에서 제목(Text) 추출
            Text titleText = (Text) selectedPost.getChildren().get(0); // HBox의 첫 번째 자식은 제목
            String selectedTitle = titleText.getText();
            // FXML 파일 로드
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/resources/makepostScreen.fxml"));
            Parent editPostRoot = loader.load();

            // makepostController에 선택된 제목 전달
            makepostController controller = loader.getController();
            controller.loadPostDetails(selectedTitle);

            // 현재 Stage 가져오기
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // 새로운 Scene 설정
            Scene editPostScene = new Scene(editPostRoot);
            stage.setScene(editPostScene);
            stage.setTitle("Edit Post");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error occurred while loading the post editing screen.");
        }
    }
	
	@FXML
    private void handleProfilePictureChange(ActionEvent event) {
        try {
            // FileChooser를 사용하여 로컬 파일 선택
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Profile Picture");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
            );
            File selectedFile = fileChooser.showOpenDialog((Stage) ((Node) event.getSource()).getScene().getWindow());

            if (selectedFile != null) {
                // 파일을 데이터베이스에 저장
                saveProfileImageToDatabase(selectedFile);
                loadProfileImageFromDatabase();
	            ensureDatabaseConnection();

                // UI에 업데이트된 이미지 표시
                updateProfileImageUI(selectedFile);
                loadProfileImageFromDatabase();
	            ensureDatabaseConnection();
            } else {
                System.out.println("No file selected.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error occurred while updating the profile picture.");
        }
    }

    private void saveProfileImageToDatabase(File file) throws IOException, SQLException {
        try (Connection con = Main.con; FileInputStream fis = new FileInputStream(file)) {
            if (con == null || con.isClosed()) {
                System.out.println("Database connection is not available.");
                return;
            }

            // 데이터베이스에 이미지 저장
            String updateQuery = "UPDATE user SET profile_image = ? WHERE user_id = ?";
            try (PreparedStatement pstmt = con.prepareStatement(updateQuery)) {
                pstmt.setBlob(1, fis, (int) file.length());
                pstmt.setString(2, Main.loggedInUserId);
                pstmt.executeUpdate();
                System.out.println("Profile picture updated successfully.");
            }
        }
    }

    private void updateProfileImageUI(File file) {
        try {
            // 이미지 로드
            Image newProfileImage = new Image(file.toURI().toString());
            ImageView imageView = new ImageView(newProfileImage);

            // 이미지 크기 설정
            double imageSize = 100; // 이미지의 전체 크기
            imageView.setFitWidth(imageSize);
            imageView.setFitHeight(imageSize);
            imageView.setPreserveRatio(true); // 비율 유지
            imageView.setSmooth(true); // 부드러운 렌더링

            // 동그라미 모양의 Clip 생성 (원의 크기를 줄임)
            double radius = 80; // 줄어든 원 반지름
            Circle clip = new Circle(imageSize / 2, imageSize / 2, radius); // 중심 좌표와 반지름 설정
            imageView.setClip(clip);

            // 버튼에 이미지 설정
            defaultProfilePicture.setGraphic(imageView);
            defaultProfilePicture.setStyle(
                "-fx-background-color: transparent; " + // 버튼 배경 투명
                "-fx-border-radius: 50%; " +           // 버튼 모서리 둥글게
                "-fx-padding: 0;"                      // 패딩 제거
            );

            System.out.println("Profile picture updated in the UI with smaller circle.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error occurred while updating profile picture in UI.");
        }
    }
	
    @FXML
    private void handleSettingButton(ActionEvent event) {
        try {
            // FXML 파일 로드
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/resources/settingScreen.fxml"));
            Parent settingRoot = loader.load();

            // 현재 Stage 가져오기
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // 새로운 Scene 설정
            Scene settingScene = new Scene(settingRoot);
            stage.setScene(settingScene);
            stage.setTitle("Settings");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error occurred while loading the setting screen.");
        }
    }
    
    @FXML
    private void handleFollowerButton(ActionEvent event) {
        try {
            // FXML 파일 로드
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/resources/followerlistScreen.fxml"));
            Parent followerListRoot = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene followerListScene = new Scene(followerListRoot);
            stage.setScene(followerListScene);
            stage.setTitle("Followers");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error occurred while loading the follower list screen.");
        }
    }

    @FXML
    private void handleFollowingButton(ActionEvent event) {
        try {
            // FXML 파일 로드
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/resources/followinglistScreen.fxml"));
            Parent followingListRoot = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene followingListScene = new Scene(followingListRoot);
            stage.setScene(followingListScene);
            stage.setTitle("Following");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error occurred while loading the following list screen.");
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
            stage.setTitle("Welcome!");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error occurred while navigating back.");
        }
    }
    
    private static void ensureDatabaseConnection() {
        try {
            if (Main.con == null || Main.con.isClosed()) {
                System.out.println("Database connection is not available. Reconnecting...");
                Main.con = DriverManager.getConnection(
                    "jdbc:mysql://localhost/Twitter", "root", "0327"
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to reconnect to the database.");
        }
    }
}
