package main.java;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class postController {
	private String currentPostId; // 현재 포스트 ID를 저장
	
    @FXML
    private TextField commentTextField;

    @FXML
    private ListView<Comment> commentListView; // 댓글을 표시할 ListView

    // Comment 클래스 정의
    public static class Comment {
    	private String commentId;
        private String content;
        private String writerId;
        private String createdAt;
        private int numOfLikes;

        public Comment(String commentId,String content, String writerId, String createdAt, int numOfLikes) {
        	this.commentId = commentId;
        	this.content = content;
            this.writerId = writerId;
            this.createdAt = createdAt;
            this.numOfLikes = numOfLikes;
        }
        
        public String getCommentId() { return commentId; }

        public String getContent() {
            return content;
        }

        public String getWriterId() {
            return writerId;
        }

        public String getCreatedAt() {
            return createdAt;
        }
        
        public int getNumOfLikes() { return numOfLikes; }
    }
    
    public void setPostId(String postId) {
        this.currentPostId = postId;
        loadComments(postId); // 전달받은 ID로 게시글 및 댓글 로드
    }

    

    // 댓글을 ListView에 로드하는 메서드
    private void loadComments(String postId) {
        List<Comment> comments = new ArrayList<>();
        try (Connection con = Main.con; // 데이터베이스 연결
             PreparedStatement pstmt = con.prepareStatement(
                 "SELECT c.comment_id, c.content, c.writer_id, c.created_at, c.num_of_likes " +
                 "FROM comment c " +
                 "WHERE c.post_id = ? AND NOT EXISTS (" +
                 "    SELECT 1 FROM block b " +
                 "    WHERE b.blocker = ? AND b.blocked = c.writer_id" +
                 ") " +
                 "ORDER BY c.created_at DESC"
             )) {
            pstmt.setString(1, postId);
            pstmt.setString(2, Main.loggedInUserId); // 현재 로그인한 사용자 ID
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String commentId = rs.getString("comment_id");
                String content = rs.getString("content");
                String writerId = rs.getString("writer_id");
                String createdAt = rs.getString("created_at");
                int numOfLikes = rs.getInt("num_of_likes");

                comments.add(new Comment(commentId, content, writerId, createdAt, numOfLikes));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to load comments.");
        }

        // ListView에 댓글 설정
        commentListView.getItems().setAll(comments);
        commentListView.setCellFactory(listView -> new CommentListCell());
    }   

    // Custom ListCell 클래스
    static class CommentListCell extends ListCell<Comment> {
        private VBox content;
        private HBox header;
        private HBox footer; // 댓글 본문과 좋아요 버튼을 포함하는 HBox
        private Text commentText;
        private Text writerText;
        private Text createdAtText;
        private Button likeButton; // 좋아요 버튼

        public CommentListCell() {
            super();
            
            // 작성자와 작성일 텍스트
            writerText = new Text();
            writerText.setStyle("-fx-font-weight: bold;");
            createdAtText = new Text();
            createdAtText.setStyle("-fx-font-size: 10px; -fx-text-fill: gray;");
            
            // 좋아요 버튼 스타일 지정
            likeButton = new Button("♥");    
            likeButton.setStyle(
                "-fx-background-color: #F7C6CC; " +
                "-fx-text-fill: #D93664; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 15; "
            );

            // 작성자와 작성일을 포함하는 HBox
            header = new HBox();
            header.getChildren().addAll(writerText, new Region(), createdAtText);
            HBox.setHgrow(header.getChildren().get(1), Priority.ALWAYS); // 빈 공간 확장

            // 댓글 본문
            commentText = new Text();
            commentText.setWrappingWidth(200); // 텍스트 줄바꿈 설정
            commentText.setStyle("-fx-padding: 5px 0; -fx-font-size: 13px;");

            // 댓글 본문과 좋아요 버튼을 포함하는 HBox
            footer = new HBox();
            Region spacer = new Region(); // 빈 공간 확장을 위한 Region 추가
            HBox.setHgrow(spacer, Priority.ALWAYS); // 빈 공간 확장
            footer.getChildren().addAll(commentText, spacer, likeButton); // 좋아요 버튼을 오른쪽 끝에 배치
            footer.setSpacing(3); // 간격 설정

            // 전체 레이아웃을 포함하는 VBox
            content = new VBox();
            content.getChildren().addAll(header, footer); // Header와 Footer를 포함
            content.setSpacing(3); // 요소 간 간격 설정
            content.setStyle("-fx-padding: 5;");
        }

        @Override
        protected void updateItem(Comment comment, boolean empty) {
        	super.updateItem(comment, empty);
            if (empty || comment == null) {
                setGraphic(null);
            } else {
                writerText.setText("id: " + comment.getWriterId());
                createdAtText.setText("date: " + comment.getCreatedAt());
                commentText.setText(comment.getContent());
                likeButton.setText("♥ " + comment.getNumOfLikes());
                
             // Like button functionality
                likeButton.setOnAction(event -> {
                    try {
                        ensureDatabaseConnection(); // Ensure connection is available
                        Connection con = Main.con;

                        if (con == null || con.isClosed()) {
                            System.out.println("Database connection is not available.");
                            return;
                        }

                        System.out.println("Logged-in User ID: " + Main.loggedInUserId);
                        if (Main.loggedInUserId == null || Main.loggedInUserId.isEmpty()) {
                            System.out.println("Error: Logged-in user ID is not set.");
                            return;
                        }

                        // Check if the user has already liked the comment
                        String checkLikeQuery = "SELECT * FROM comment_like WHERE comment_id = ? AND liker_id = ?";
                        try (PreparedStatement checkStmt = con.prepareStatement(checkLikeQuery)) {
                            checkStmt.setString(1, comment.getCommentId());
                            checkStmt.setString(2, Main.loggedInUserId);
                            ResultSet rs = checkStmt.executeQuery();

                            if (rs.next()) {
                                // User already liked the comment - Remove the like
                                String deleteLikeQuery = "DELETE FROM comment_like WHERE comment_id = ? AND liker_id = ?";
                                try (PreparedStatement deleteStmt = con.prepareStatement(deleteLikeQuery)) {
                                    deleteStmt.setString(1, comment.getCommentId());
                                    deleteStmt.setString(2, Main.loggedInUserId);
                                    deleteStmt.executeUpdate();
                                }

                                // Decrease the like count in the `comment` table
                                String decrementLikesQuery = "UPDATE comment SET num_of_likes = num_of_likes - 1 WHERE comment_id = ?";
                                try (PreparedStatement decrementStmt = con.prepareStatement(decrementLikesQuery)) {
                                    decrementStmt.setString(1, comment.getCommentId());
                                    decrementStmt.executeUpdate();
                                }

                                // Update the UI and comment object
                                comment.numOfLikes--;
                                likeButton.setText("♥ " + comment.getNumOfLikes());
                            } else {
                                // User has not liked the comment - Add the like
                                String insertLikeQuery = "INSERT INTO comment_like (l_id, comment_id, liker_id) VALUES (?, ?, ?)";
                                try (PreparedStatement insertStmt = con.prepareStatement(insertLikeQuery)) {
                                    insertStmt.setString(1, UUID.randomUUID().toString());
                                    insertStmt.setString(2, comment.getCommentId());
                                    insertStmt.setString(3, Main.loggedInUserId);
                                    insertStmt.executeUpdate();
                                }

                                // Increase the like count in the `comment` table
                                String incrementLikesQuery = "UPDATE comment SET num_of_likes = num_of_likes + 1 WHERE comment_id = ?";
                                try (PreparedStatement incrementStmt = con.prepareStatement(incrementLikesQuery)) {
                                    incrementStmt.setString(1, comment.getCommentId());
                                    incrementStmt.executeUpdate();
                                }

                                // Update the UI and comment object
                                comment.numOfLikes++;
                                likeButton.setText("♥ " + comment.getNumOfLikes());
                            }
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        System.out.println("Error occurred while toggling like. Query failed: " + e.getMessage());
                    }
                });
                setGraphic(content);
            }
        }
    }
    
    // 댓글 저장 및 UI 업데이트
    @FXML
    private void handleAddComment() {
    	ensureDatabaseConnection(); // 연결 상태 확인 및 재연결
        String commentContent = commentTextField.getText().trim();
        if (commentContent.isEmpty()) {
            return; // 댓글 내용이 비어있으면 아무 작업도 하지 않음
        }

        try (Connection con = Main.con) {
            if (con == null) {
                System.out.println("Database connection is not available.");
                return;
            }

            String commentId = UUID.randomUUID().toString();
            String writerId = Main.loggedInUserId;
            
            // 데이터베이스에 댓글 삽입
            String insertQuery = "INSERT INTO comment (comment_id, content, writer_id, post_id) VALUES (?, ?, ?, ?)";
            String selectQuery = "SELECT created_at, num_of_likes FROM comment WHERE comment_id = ?";

            try (PreparedStatement pstmt = con.prepareStatement(insertQuery)) {
                pstmt.setString(1, commentId);
                pstmt.setString(2, commentContent);
                pstmt.setString(3, writerId);
                pstmt.setString(4, currentPostId);
                pstmt.executeUpdate();
            }

            // 생성된 created_at 값 가져오기
            String createdAt = null;
            int numOfLikes = 0; // 메서드 스코프에서 선언하여 접근 가능하도록 변경
            try (PreparedStatement pstmt = con.prepareStatement(selectQuery)) {
                pstmt.setString(1, commentId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        createdAt = rs.getString("created_at");
                        numOfLikes = rs.getInt("num_of_likes");
                    }
                }
            }

            // UI에 새 댓글 추가
            if (createdAt != null) {
                Comment newComment = new Comment(commentId, commentContent, writerId, createdAt, numOfLikes);
                commentListView.getItems().add(newComment);
            }

            // 댓글 입력 필드 초기화
            commentTextField.clear();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to add comment.");
        }
        
        ensureDatabaseConnection(); // 연결 상태 확인 및 재연결
    }
    
    
    @FXML
    private void handleBackButton(ActionEvent event) {
        try {
        	// 메인 화면으로 돌아가기
        	ensureDatabaseConnection(); // 연결 상태 확인 및 재연결
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/resources/mainfeedScreen.fxml"));
            Parent mainfeedRoot = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene mainfeedScene = new Scene(mainfeedRoot);
            stage.setScene(mainfeedScene);
            stage.setTitle("Welcome!");
            stage.show();
            ensureDatabaseConnection(); // 연결 상태 확인 및 재연결
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
