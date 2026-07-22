package com.divarclone.client.controller;

import com.divarclone.client.model.Ad;
import com.divarclone.client.network.ApiClient;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Map;
import com.divarclone.client.util.Session;

public class AdDetailsController {

    @FXML
    private ImageView imageView;

    @FXML
    private Label titleLabel;

    @FXML
    private Label priceLabel;

    @FXML
    private Label cityLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Label descriptionLabel;

    @FXML
    private Label ownerLabel;

    @FXML
    private Label star1, star2, star3, star4, star5;

    @FXML
    private Label ratingSummaryLabel;

    private final ApiClient apiClient = new ApiClient();

    private Ad currentAd;
    private int currentRating = 0;

    public void setAd(Ad ad) {

        this.currentAd = ad;

        titleLabel.setText(ad.getTitle());

        priceLabel.setText("Price : " + ad.getPrice());

        cityLabel.setText("City : " + ad.getCity());

        statusLabel.setText("Status : " + ad.getStatus());

        descriptionLabel.setText(ad.getDescription());

        if (ad.getImageFileName() != null && !ad.getImageFileName().isBlank()) {
            String imageUrl = ApiClient.BASE_URL + "/images/" + ad.getImageFileName();
            imageView.setImage(new javafx.scene.image.Image(imageUrl, 300, 220, true, true, true));
        } else {
            imageView.setImage(null);
        }

        loadOwnerInfo();
    }

    private void loadOwnerInfo() {
        try {
            Map<String, Object> owner = apiClient.getUserPublic(currentAd.getOwnerId());
            ownerLabel.setText("Posted by: " + owner.get("username"));
        } catch (IOException | InterruptedException e) {
            ownerLabel.setText("");
        }

        refreshRatingSummary();

        boolean isOwnAd = currentAd.getOwnerId() == Session.getUserId();

        if (isOwnAd) {
            star1.setVisible(false);
            star1.setManaged(false);
            star2.setVisible(false);
            star2.setManaged(false);
            star3.setVisible(false);
            star3.setManaged(false);
            star4.setVisible(false);
            star4.setManaged(false);
            star5.setVisible(false);
            star5.setManaged(false);
            return;
        }

        try {
            currentRating = apiClient.getMyRating(currentAd.getOwnerId());
        } catch (IOException | InterruptedException e) {
            currentRating = 0;
        }

        updateStarsDisplay();
    }

    private void refreshRatingSummary() {
        try {
            Map<String, Object> summary = apiClient.getRatingSummary(currentAd.getOwnerId());
            double average = ((Number) summary.get("average")).doubleValue();
            int count = ((Number) summary.get("count")).intValue();
            ratingSummaryLabel.setText(String.format("Average rating: %.1f (%d votes)", average, count));
        } catch (IOException | InterruptedException e) {
            ratingSummaryLabel.setText("");
        }
    }

    private void updateStarsDisplay() {
        Label[] stars = {star1, star2, star3, star4, star5};
        for (int i = 0; i < stars.length; i++) {
            stars[i].setText(i < currentRating ? "★" : "☆");
        }
    }

    private void submitRating(int value) {
        try {
            apiClient.rateUser(currentAd.getOwnerId(), value);
            currentRating = value;
            updateStarsDisplay();
            refreshRatingSummary();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void rateStar1() {
        submitRating(1);
    }

    @FXML
    private void rateStar2() {
        submitRating(2);
    }

    @FXML
    private void rateStar3() {
        submitRating(3);
    }

    @FXML
    private void rateStar4() {
        submitRating(4);
    }

    @FXML
    private void rateStar5() {
        submitRating(5);
    }

    @FXML
    private void handleClose() {

        Stage stage = (Stage) titleLabel.getScene().getWindow();

        stage.close();
    }
}