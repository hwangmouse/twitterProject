package main.java;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Alert {

    public static void show(String title, String message) {
        Stage popupStage = new Stage();
        popupStage.setTitle(title);

        // 레이아웃 설정
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #0078D7; -fx-border-width: 2px;");

        // 아이콘 추가
        ImageView icon = new ImageView(new Image(Alert.class.getResourceAsStream("/main/images/errorIcon.png")));
        icon.setFitWidth(50);
        icon.setFitHeight(50);

        // 메시지 표시
        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #333; -fx-font-weight: bold;");

        // 확인 버튼
        Button okButton = new Button("확인");
        okButton.setStyle(
            "-fx-background-color: #0078D7; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 10;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 5, 0.5, 0, 1);"
        );
        okButton.setOnMouseEntered(e -> okButton.setStyle(
            "-fx-background-color: #005BB5; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 10;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 8, 0.8, 0, 2);"
        ));
        okButton.setOnMouseExited(e -> okButton.setStyle(
            "-fx-background-color: #0078D7; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 10;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 5, 0.5, 0, 1);"
        ));
        okButton.setOnAction(e -> popupStage.close());

        layout.getChildren().addAll(icon, messageLabel, okButton);

        // Scene 및 Stage 설정
        Scene scene = new Scene(layout, 350, 200);
        popupStage.setScene(scene);
        popupStage.setAlwaysOnTop(true); // 팝업 항상 위에 표시
        popupStage.initModality(Modality.APPLICATION_MODAL); // 모달 창으로 설정
        popupStage.getIcons().add(new Image("/main/images/twitterIcon.png"));

        // 애니메이션 효과
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), layout);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        popupStage.showAndWait();
    }
}
