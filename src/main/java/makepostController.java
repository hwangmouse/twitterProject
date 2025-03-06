package main.java;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.scene.Node;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class makepostController {

    @FXML
    private TextField titleField; // 제목 입력 필드

    @FXML
    private TextArea contentField; // 내용 입력 필드

    @FXML
    private Label uploadStatusLabel; // 업로드 상태를 표시할 레이블

    @FXML
    private Button deleteImageButton; // 이미지 삭제 버튼

    private File selectedImageFile; // 선택된 이미지 파일

    private String currentPostTitle; // 현재 편집 중인 게시물 제목

    @FXML
    private void initialize() {
        // 초기 상태에서 삭제 버튼 비활성화
        deleteImageButton.setDisable(true);
    }

    public void loadPostDetails(String title) {
        currentPostTitle = title;
        try {
            Connection con = Main.con;
            if (con == null) {
                System.out.println("Database connection is not available.");
                return;
            }

            String query = "SELECT title, content, image FROM posts WHERE title = ? AND writer_id = ?";
            try (PreparedStatement pstmt = con.prepareStatement(query)) {
                pstmt.setString(1, title);
                pstmt.setString(2, Main.loggedInUserId); // 현재 로그인된 사용자 ID
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    titleField.setText(rs.getString("title"));
                    contentField.setText(rs.getString("content"));
                    
                    // 이미지 데이터 확인
                    InputStream imageStream = rs.getBinaryStream("image");
                    if (imageStream != null) {
                        selectedImageFile = null; // 새 이미지 로드 후 기존 파일 참조 제거
                        uploadStatusLabel.setText("사진 추가됨"); // 상태 업데이트
                        uploadStatusLabel.setStyle("-fx-text-fill: black;"); // 텍스트 색상 변경
                        deleteImageButton.setDisable(false); // 삭제 버튼 활성화
                    } else {
                        System.out.println("No image found for this post.");
                        uploadStatusLabel.setText("사진을 추가하세요");
                        uploadStatusLabel.setStyle("-fx-text-fill: #8A8A8A;");
                        deleteImageButton.setDisable(true); // 삭제 버튼 비활성화
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error occurred while loading post details.");
        }
    }

    @FXML
    private void handleSaveButton() {
        try {
            Connection con = Main.con;
            if (con == null) {
                System.out.println("Database connection is not available.");
                return;
            }

            String query = "UPDATE posts SET title = ?, content = ? WHERE title = ? AND writer_id = ?";
            try (PreparedStatement pstmt = con.prepareStatement(query)) {
                pstmt.setString(1, titleField.getText());
                pstmt.setString(2, contentField.getText());
                pstmt.setString(3, currentPostTitle);
                pstmt.setString(4, Main.loggedInUserId);

                pstmt.executeUpdate();
                System.out.println("Post updated successfully.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error occurred while updating the post.");
        }
    }

    @FXML
    private void handleuploadButton(ActionEvent event) {
        // 파일 선택 대화상자 열기
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        selectedImageFile = fileChooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());

        // 선택된 파일 경로 출력 및 레이블 업데이트
        if (selectedImageFile != null) {
            System.out.println("Selected file: " + selectedImageFile.getAbsolutePath());
            uploadStatusLabel.setText("사진 추가됨"); // 레이블을 업데이트
            uploadStatusLabel.setStyle("-fx-text-fill: black;"); // 텍스트 색상을 검정으로 설정
            deleteImageButton.setDisable(false); // 삭제 버튼 활성화
        } else {
            System.out.println("No file selected.");
            uploadStatusLabel.setText("사진을 추가하세요"); // 기본 메시지로 복원
            deleteImageButton.setDisable(true); // 삭제 버튼 비활성화
        }
    }

    @FXML
    private void handleDeleteImageButton(ActionEvent event) {
        // 선택된 이미지 파일 제거
        selectedImageFile = null;
        uploadStatusLabel.setText("사진을 추가하세요"); // 기본 메시지로 복원
        uploadStatusLabel.setStyle("-fx-text-fill: #8A8A8A;"); // 텍스트 색상을 회색으로 복원
        deleteImageButton.setDisable(true); // 삭제 버튼 비활성화
        System.out.println("Image removed.");
    }

    @FXML
    private void handleSubmitButton(ActionEvent event) {
        try {
            String title = titleField.getText();
            String content = contentField.getText();

            // 제목 또는 내용이 비어 있는지 확인
            if (title.isEmpty() || content.isEmpty()) {
                Alert.show("Error", "Title and Content cannot be empty.");
                System.out.println("title and content fields cannot be empty.");
                return;
            }

            // Main.con을 사용하여 데이터베이스 연결
            Connection con = Main.con;
            if (con == null) {
                System.out.println("Database connection is not available.");
                return;
            }

            if (currentPostTitle != null) {
                // 기존 게시물 업데이트
                String updateSql = "UPDATE posts SET title = ?, content = ?, image = ? WHERE title = ? AND writer_id = ?";
                try (PreparedStatement pstmt = con.prepareStatement(updateSql)) {
                    pstmt.setString(1, title); // 새 제목
                    pstmt.setString(2, content); // 새 내용

                    // 이미지가 선택되었는지 확인
                    if (selectedImageFile != null) {
                        FileInputStream fis = new FileInputStream(selectedImageFile);
                        pstmt.setBinaryStream(3, fis, (int) selectedImageFile.length());
                    } else {
                        pstmt.setNull(3, java.sql.Types.BLOB);
                    }

                    pstmt.setString(4, currentPostTitle); // 기존 제목
                    pstmt.setString(5, Main.loggedInUserId); // 작성자 ID

                    pstmt.executeUpdate();
                    System.out.println("Post updated successfully.");
                }
            } else {
                // 새 게시물 저장
                String insertSql = "INSERT INTO posts (post_id, title, content, num_of_likes, writer_id, image) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = con.prepareStatement(insertSql)) {
                    pstmt.setString(1, java.util.UUID.randomUUID().toString()); // 고유 post_id 생성
                    pstmt.setString(2, title);
                    pstmt.setString(3, content);
                    pstmt.setInt(4, 0); // 좋아요 초기값 0
                    pstmt.setString(5, Main.loggedInUserId); // 작성자 ID

                    // 이미지가 선택되었는지 확인
                    if (selectedImageFile != null) {
                        FileInputStream fis = new FileInputStream(selectedImageFile);
                        pstmt.setBinaryStream(6, fis, (int) selectedImageFile.length());
                    } else {
                        pstmt.setNull(6, java.sql.Types.BLOB);
                    }

                    pstmt.executeUpdate();
                    System.out.println("New post saved successfully.");
                }
            }

                // FXML 로드하여 화면 전환
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/resources/mainfeedScreen.fxml"));
                Parent mainfeedRoot = loader.load();

                // 현재 Stage 가져오기
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                
                // 기존 Stage 크기 저장
    	        double currentWidth = stage.getWidth();
    	        double currentHeight = stage.getHeight();
    	        
                // 새로운 Scene 설정
                Scene mainfeedScene = new Scene(mainfeedRoot);
                
                // 애니메이션 추가: 아래에서 위로 이동
                mainfeedRoot.setTranslateY(-stage.getHeight()); // 시작 위치 (아래쪽 화면 밖)
    	        stage.setScene(mainfeedScene);
    	        
    	        // Stage 크기 고정 (비율 변경 방지)
    	        stage.setWidth(currentWidth);
    	        stage.setHeight(currentHeight);

    	        // TranslateTransition 생성
    	        TranslateTransition slideIn = new TranslateTransition();
    	        slideIn.setDuration(javafx.util.Duration.seconds(0.3)); // 애니메이션 지속 시간
    	        slideIn.setNode(mainfeedRoot); // 애니메이션 대상
    	        slideIn.setFromY(-stage.getHeight()); // 아래쪽 화면 밖에서 시작
    	        slideIn.setToY(0); // 화면 안으로 이동
    	        slideIn.play();
                
                stage.setTitle("Welcome!");
                stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error occurred while loading the main feed screen.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error occurred while saving the post.");
        }
    }
}