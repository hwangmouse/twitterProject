package main.java;

import java.io.IOException;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;


public class mainfeedController {
	
	@FXML
	private Button defaultProfilePicture; // 프로필 이미지 버튼
	
	public class Post {
	    private String postId;
	    private String title;
	    private String content;
	    private int numOfLikes;
	    private String writerId;
	    private byte[] image; // 이미지 데이터를 저장
	    private String createdAt;

	    public Post(String postId, String title, String content, int numOfLikes, String writerId, byte[] image, String createdAt) {
	        this.postId = postId;
	        this.title = title;
	        this.content = content;
	        this.numOfLikes = numOfLikes;
	        this.writerId = writerId;
	        this.image = image;
	        this.createdAt = createdAt;	
	    }

	    // Getters
	    public String getPostId() { return postId; }
	    public String getTitle() { return title; }
	    public String getContent() { return content; }
	    public int getNumOfLikes() { return numOfLikes; }
	    public String getWriterId() { return writerId; }
	    public byte[] getImage() { return image; }
	    public String getCreatedAt() { return createdAt; }
	}
	
	
	@FXML
    private ListView<Post> postListView; // 게시물 리스트뷰
	
	@FXML
    private void initialize() {
		try {
	        loadProfileImage();
	        loadPosts();
	        setupPostSelection();
	        System.out.println("Mainfeed initialized successfully.");
	    } catch (Exception e) {
	        e.printStackTrace();
	        System.out.println("Error occurred during mainfeed initialization.");
	    }
    }
	
	private void loadProfileImage() {
		ensureDatabaseConnection();
	    try (Connection con = Main.con) {
	        if (con == null || con.isClosed()) {
	            System.out.println("Database connection is not available.");
	            return;
	        }

	        // 데이터베이스에서 프로필 이미지 가져오기
	        String query = "SELECT profile_image FROM user WHERE user_id = ?";
	        try (PreparedStatement pstmt = con.prepareStatement(query)) {
	            pstmt.setString(1, Main.loggedInUserId);
	            ResultSet rs = pstmt.executeQuery();

	            if (rs.next()) {
	                byte[] profileImageBytes = rs.getBytes("profile_image");
	                if (profileImageBytes != null) {
	                    // 프로필 이미지가 있으면 설정
	                    Image profileImage = new Image(new ByteArrayInputStream(profileImageBytes));
	                    ImageView imageView = new ImageView(profileImage);

	                    // 이미지 뷰 속성 설정
	                    double imageSize = 50; // 이미지 크기
	                    imageView.setFitWidth(imageSize);
	                    imageView.setFitHeight(imageSize);
	                    imageView.setPreserveRatio(true);

	                    // 동그라미 모양의 Clip 생성
	                    double radius = 25; // 원 반지름
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
	
	// 포스트 셀이 선택되었을 때 처리
	private void setupPostSelection() {
		ensureDatabaseConnection();
	    postListView.setOnMouseClicked(event -> {
	        Post selectedPost = postListView.getSelectionModel().getSelectedItem();
	        if (selectedPost != null) {
	            handlePostSelection(selectedPost);
	        }
	    });
	}
	
	// 선택된 포스트로 postScreen 이동
	private void handlePostSelection(Post selectedPost) {
	    try {
	        // FXML 파일 로드
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/resources/postScreen.fxml"));
	        Parent postRoot = loader.load();
	        
	        // 컨트롤러 가져오기
	        postController postController = loader.getController();

	        // 선택된 포스트의 ID를 전달
	        postController.setPostId(selectedPost.getPostId());

	        // 현재 Stage 가져오기
	        Stage stage = (Stage) postListView.getScene().getWindow();
	        
	        // 기존 Stage 크기 저장
	        double currentWidth = stage.getWidth();
	        double currentHeight = stage.getHeight();

	        // 새로운 Scene 설정
	        Scene postScene = new Scene(postRoot);
	        postRoot.setTranslateY(stage.getHeight()); // 시작 위치 (아래쪽 화면 밖)
	        stage.setScene(postScene);
	        stage.setWidth(currentWidth);
	        stage.setHeight(currentHeight);
	        TranslateTransition slideIn = new TranslateTransition();
	        slideIn.setDuration(javafx.util.Duration.seconds(0.3)); // 애니메이션 지속 시간
	        slideIn.setNode(postRoot); // 애니메이션 대상
	        slideIn.setFromY(stage.getHeight()); // 아래쪽 화면 밖에서 시작
	        slideIn.setToY(0); // 화면 안으로 이동
	        slideIn.play();
	        stage.setTitle("Comments");
	        stage.show();
	    } catch (IOException e) {
	        e.printStackTrace();
	        System.out.println("Error occurred while loading the post screen.");
	    }
	}
	
	private void loadPosts() {
	    ensureDatabaseConnection();
	    List<Post> posts = new ArrayList<>();
	    try {
	        Connection con = Main.con; // 데이터베이스 연결
	        if (con == null) {
	            System.out.println("Database connection is not available.");
	            return;
	        }

	        // 팔로우한 사용자와 본인의 게시물 가져오는 쿼리
	        String query = """
	                SELECT p.post_id, p.title, p.content, p.num_of_likes, p.writer_id, p.image, p.created_at
	                FROM posts p
	                LEFT JOIN following f ON p.writer_id = f.following_id AND f.user_id = ? AND f.f_allow = 'Y'
	                LEFT JOIN block b ON p.writer_id = b.blocked AND b.blocker = ?
	                WHERE (f.following_id IS NOT NULL OR p.writer_id = ?) AND b.blocked IS NULL
	                ORDER BY p.created_at DESC
	                """;

	        try (PreparedStatement pstmt = con.prepareStatement(query)) {
	            pstmt.setString(1, Main.loggedInUserId); // 팔로우 조건
	            pstmt.setString(2, Main.loggedInUserId); // 차단 조건
	            pstmt.setString(3, Main.loggedInUserId); // 본인 게시물 포함 조건
	            ResultSet rs = pstmt.executeQuery();

	            while (rs.next()) {
	                String postId = rs.getString("post_id");
	                String title = rs.getString("title");
	                String content = rs.getString("content");
	                int numOfLikes = rs.getInt("num_of_likes");
	                String writerId = rs.getString("writer_id");
	                byte[] image = rs.getBytes("image");
	                String createdAt = rs.getString("created_at");

	                posts.add(new Post(postId, title, content, numOfLikes, writerId, image, createdAt));
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    // 게시물을 ListView에 설정
	    postListView.getItems().setAll(posts);
	    postListView.setCellFactory(listView -> new PostListCell());
	}
	
    // Custom ListCell
	static class PostListCell extends ListCell<Post> {
	    private VBox content;  // 전체 컨테이너
	    private HBox header;   // 제목과 작성 시간을 포함하는 컨테이너
	    private VBox body;     // 내용과 이미지를 포함하는 컨테이너
	    private HBox footer;   // 좋아요 버튼을 포함하는 컨테이너
	    private Text title;        // 제목
	    private Text createdAt;    // 작성 시간
	    private Text postContent;  // 내용
	    private Text writerIdText; //작성자 id
	    private ImageView imageView; // 이미지
	    private Button likeButton; // 좋아요 버튼
	    
	    

	    public PostListCell() {
	        super();
	        // 제목과 작성 시간을 위한 HBox
	        title = new Text();
	        title.setStyle("-fx-font-weight: bold;");
	        createdAt = new Text();
	        Region spacer = new Region();
	        HBox.setHgrow(spacer, Priority.ALWAYS); // spacer가 가능한 공간을 차지하도록 설정
	        header = new HBox(title, spacer, createdAt);
	        header.setStyle("-fx-padding: 5;"); // 간격 설정
	        // 이미지와 내용을 위한 VBox
	        postContent = new Text();
	        postContent.setWrappingWidth(250); // 텍스트 줄바꿈	        
	        postContent.setStyle("-fx-background-color: transparent;"); // 흰색 배경 제거
	        imageView = new ImageView();
	        imageView.setFitWidth(100); // 이미지 너비 설정
	        imageView.setPreserveRatio(true);
	        body = new VBox(imageView, postContent); // 이미지와 내용을 세로로 배치
	        body.setSpacing(10);
	        body.setStyle("-fx-padding: 5;");
	        writerIdText = new Text();
	        writerIdText.setStyle("-fx-font-style: italic; -fx-padding: 5;");
	        // 좋아요 버튼을 위한 HBox
	        likeButton = new Button("♥");
	        likeButton.setStyle(
	        	    "-fx-background-color: #F7C6CC; " +
	        	    "-fx-text-fill: #D93664; " +
	        	    "-fx-font-weight: bold; " +
	        	    "-fx-background-radius: 15; " +
	        	    "-fx-padding: 5 10 5 10; "
	        	);
	        footer = new HBox(writerIdText, likeButton);
	        footer.setSpacing(10);
	        footer.setStyle("-fx-alignment: bottom-right; -fx-padding: 5;");
	        // 전체를 포함하는 VBox
	        content = new VBox(header, body, footer);
	        content.setSpacing(10);
	        content.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 10; -fx-background-radius: 5;");
	    }
	    
	    @Override
	    protected void updateItem(Post post, boolean empty) {
	        super.updateItem(post, empty);
	        if (empty || post == null) {
	            setGraphic(null);
	        } else {
	            title.setText(post.getTitle());
	            createdAt.setText(post.getCreatedAt());
	            postContent.setText(post.getContent());
	            likeButton.setText("♥ " + post.getNumOfLikes());
	            writerIdText.setText("Written by: " + post.getWriterId());
	            // 이미지 설정
	            if (post.getImage() != null) {
	                Image image = new Image(new ByteArrayInputStream(post.getImage()));
	                imageView.setImage(image);
	                imageView.setVisible(true); // 이미지가 있을 경우 표시
	            } else {
	                imageView.setImage(null);
	                imageView.setVisible(false); // 이미지가 없을 경우 숨기기
	            }
	            
	            // 좋아요 버튼 함수
	            likeButton.setOnAction(event -> {
	                try {
	                    Connection con = Main.con;
	                    if (con == null) {
	                        System.out.println("Database connection is not available.");
	                        return;
	                    }

	                    // 이미 좋아요 했는지 확인
	                    String checkLikeQuery = "SELECT * FROM post_like WHERE post_id = ? AND liker_id = ?";
	                    try (PreparedStatement checkStmt = con.prepareStatement(checkLikeQuery)) {
	                        checkStmt.setString(1, post.getPostId());
	                        checkStmt.setString(2, Main.loggedInUserId);
	                        ResultSet rs = checkStmt.executeQuery();

	                        if (rs.next()) {
	                            // 이미 좋아요 했으면 좋아요 취소
	                            String deleteLikeQuery = "DELETE FROM post_like WHERE post_id = ? AND liker_id = ?";
	                            try (PreparedStatement deleteStmt = con.prepareStatement(deleteLikeQuery)) {
	                                deleteStmt.setString(1, post.getPostId());
	                                deleteStmt.setString(2, Main.loggedInUserId);
	                                deleteStmt.executeUpdate();

	                                String decrementLikesQuery = "UPDATE posts SET num_of_likes = num_of_likes - 1 WHERE post_id = ?";
	                                try (PreparedStatement decrementStmt = con.prepareStatement(decrementLikesQuery)) {
	                                    decrementStmt.setString(1, post.getPostId());
	                                    decrementStmt.executeUpdate();
	                                }

	                                // Update the UI and post object
	                                post.numOfLikes--;
	                                likeButton.setText("♥ " + post.getNumOfLikes());
	                            }
	                        } else {
	                            // 좋아요 안했으면 좋아요++
	                            String insertLikeQuery = "INSERT INTO post_like (l_id, post_id, liker_id) VALUES (?, ?, ?)";
	                            try (PreparedStatement insertStmt = con.prepareStatement(insertLikeQuery)) {
	                                insertStmt.setString(1, java.util.UUID.randomUUID().toString()); // Unique ID for `post_like`
	                                insertStmt.setString(2, post.getPostId());
	                                insertStmt.setString(3, Main.loggedInUserId);
	                                insertStmt.executeUpdate();

	                                String incrementLikesQuery = "UPDATE posts SET num_of_likes = num_of_likes + 1 WHERE post_id = ?";
	                                try (PreparedStatement incrementStmt = con.prepareStatement(incrementLikesQuery)) {
	                                    incrementStmt.setString(1, post.getPostId());
	                                    incrementStmt.executeUpdate();
	                                }

	                                // Update the UI and post object
	                                post.numOfLikes++;
	                                likeButton.setText("♥ " + post.getNumOfLikes());
	                            }
	                        }
	                    }
	                } catch (SQLException e) {
	                    e.printStackTrace();
	                    System.out.println("Error occurred while toggling like.");
	                }
	            });
	            
	            setGraphic(content);
	        }
	    }
	}
	
	@FXML
    private void handleProfileButton(ActionEvent event) {
        try {
            // FXML 파일 로드
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/resources/profileScreen.fxml"));
            Parent profileRoot = loader.load();

            // 현재 Stage 가져오기
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // 새로운 Scene 설정
            Scene profileScene = new Scene(profileRoot);
            stage.setScene(profileScene);
            stage.setTitle("Profile");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error occurred while loading the profile screen.");
        }
    }
	
	@FXML
    private void handleSearchButton(ActionEvent event) {
        try {
            // FXML 파일 로드
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/resources/searchScreen.fxml"));
            Parent searchRoot = loader.load();

            // 현재 Stage 가져오기
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // 새로운 Scene 설정
            Scene searchScene = new Scene(searchRoot);
            stage.setScene(searchScene);
            stage.setTitle("Search");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error occurred while loading the search screen.");
        }
    }
	
	@FXML
    private void handleTwitterButton(ActionEvent event) {
        try {
            // FXML 파일 로드
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/resources/rankingScreen.fxml"));
            Parent rankingRoot = loader.load();

            // 현재 Stage 가져오기
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // 새로운 Scene 설정
            Scene rankingScene = new Scene(rankingRoot);
            stage.setScene(rankingScene);
            stage.setTitle("Top10");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error occurred while loading the ranking screen.");
        }
    }

	@FXML
	private void handlePlusButton(ActionEvent event) {
	    try {
	        // FXML 파일 로드
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/resources/makepostScreen.fxml"));
	        Parent makePostRoot = loader.load();

	        // 현재 Stage 가져오기
	        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
	        
	        // 기존 Stage 크기 저장
	        double currentWidth = stage.getWidth();
	        double currentHeight = stage.getHeight();
	        
	        // 새로운 Scene 설정
	        Scene makePostScene = new Scene(makePostRoot);

	        // 애니메이션 추가: 아래에서 위로 이동
	        makePostRoot.setTranslateY(stage.getHeight()); // 시작 위치 (아래쪽 화면 밖)
	        stage.setScene(makePostScene);
	        
	        // Stage 크기 고정 (비율 변경 방지)
	        stage.setWidth(currentWidth);
	        stage.setHeight(currentHeight);

	        // TranslateTransition 생성
	        TranslateTransition slideIn = new TranslateTransition();
	        slideIn.setDuration(javafx.util.Duration.seconds(0.3)); // 애니메이션 지속 시간
	        slideIn.setNode(makePostRoot); // 애니메이션 대상
	        slideIn.setFromY(stage.getHeight()); // 아래쪽 화면 밖에서 시작
	        slideIn.setToY(0); // 화면 안으로 이동
	        slideIn.play();

	        stage.setTitle("Make Post");
	        stage.show();
	    } catch (Exception e) {
	        e.printStackTrace();
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