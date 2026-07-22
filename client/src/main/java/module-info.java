module com.divarclone.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;

    opens com.divarclone.client to javafx.fxml;
    opens com.divarclone.client.controller to javafx.fxml;
    opens com.divarclone.client.model to com.fasterxml.jackson.databind, javafx.base;

    exports com.divarclone.client;
}