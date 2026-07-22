package com.divarclone.client.controller;

import com.divarclone.client.model.Ad;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class AdCardController {

    @FXML
    private ImageView imageView;

    @FXML
    private Label titleLabel;

    @FXML
    private Label categoryLabel;

    @FXML
    private Label priceLabel;

    @FXML
    private Label cityLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private HBox actionsBox;

    @FXML
    private Button deleteButton;

    @FXML
    private Button markSoldButton;

    private Runnable onDelete;
    private Runnable onMarkSold;

    // نسخه‌ی ساده: بدون status و بدون دکمه‌های عملیات (صفحه اصلی)
    public void setAd(Ad ad, String categoryName) {
        fillBasicInfo(ad, categoryName);
    }

    // نسخه‌ی با status، بدون دکمه‌های عملیات
    public void setAd(Ad ad, String categoryName, boolean showStatus) {
        fillBasicInfo(ad, categoryName);
        applyStatus(ad, showStatus);
    }

    // نسخه‌ی کامل: با status و دکمه‌های عملیات (My Ads و All Ads)
    public void setAd(Ad ad,
                      String categoryName,
                      boolean showStatus,
                      boolean showDelete,
                      boolean showMarkSold,
                      Runnable onDelete,
                      Runnable onMarkSold) {

        fillBasicInfo(ad, categoryName);
        applyStatus(ad, showStatus);

        this.onDelete = onDelete;
        this.onMarkSold = onMarkSold;

        deleteButton.setVisible(showDelete);
        deleteButton.setManaged(showDelete);

        markSoldButton.setVisible(showMarkSold);
        markSoldButton.setManaged(showMarkSold);

        boolean anyAction = showDelete || showMarkSold;
        actionsBox.setVisible(anyAction);
        actionsBox.setManaged(anyAction);
    }

    private void fillBasicInfo(Ad ad, String categoryName) {
        titleLabel.setText(ad.getTitle());
        categoryLabel.setText(categoryName != null ? categoryName : "");
        priceLabel.setText(formatPrice(ad.getPrice()) + " تومان");
        cityLabel.setText("City : " + ad.getCity());

        if (ad.getImageFileName() != null && !ad.getImageFileName().isBlank()) {
            String imageUrl = com.divarclone.client.network.ApiClient.BASE_URL + "/images/" + ad.getImageFileName();
            imageView.setImage(new javafx.scene.image.Image(imageUrl, 196, 120, true, true, true));
        } else {
            imageView.setImage(null);
        }
    }

    private String formatPrice(double price) {
        long rounded = Math.round(price);
        return String.format("%,d", rounded);
    }

    private void applyStatus(Ad ad, boolean showStatus) {
        if (showStatus) {
            statusLabel.setText("Status : " + ad.getStatus());
            statusLabel.setVisible(true);
            statusLabel.setManaged(true);
        } else {
            statusLabel.setVisible(false);
            statusLabel.setManaged(false);
        }
    }

    @FXML
    private void onDeleteClicked(javafx.event.ActionEvent event) {
        event.consume();
        if (onDelete != null) {
            onDelete.run();
        }
    }

    @FXML
    private void onMarkSoldClicked(javafx.event.ActionEvent event) {
        event.consume();
        if (onMarkSold != null) {
            onMarkSold.run();
        }
    }

}