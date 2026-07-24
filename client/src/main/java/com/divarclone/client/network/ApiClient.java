package com.divarclone.client.network;

import com.divarclone.client.model.Ad;
import com.divarclone.client.model.Category;
import com.divarclone.client.model.User;
import com.divarclone.client.util.Session;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiClient {

    public static final String BASE_URL = "http://localhost:8080";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();


    private HttpResponse<String> sendGet(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Authorization", "Bearer " + Session.getToken())
                .GET()
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // یک درخواست POST با بدنه‌ی JSON و هدر Authorization می‌سازد و می‌فرستد
    private HttpResponse<String> sendPost(String path, Object body) throws IOException, InterruptedException {
        String json = mapper.writeValueAsString(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + Session.getToken())
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // یک درخواست POST بدون بدنه (فقط هدر Authorization)
    private HttpResponse<String> sendPostNoBody(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Authorization", "Bearer " + Session.getToken())
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // یک درخواست DELETE با هدر Authorization می‌سازد و می‌فرستد
    private HttpResponse<String> sendDelete(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Authorization", "Bearer " + Session.getToken())
                .DELETE()
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // ---------- احراز هویت ----------

    public HttpResponse<String> register(String username, String password, String phone, String email)
            throws IOException, InterruptedException {
        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);
        body.put("phone", phone);
        body.put("email", email);

        String json = mapper.writeValueAsString(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // در صورت موفقیت، Session را خودش پر می‌کند و true برمی‌گرداند
    public boolean login(String username, String password) throws IOException, InterruptedException {
        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);

        String json = mapper.writeValueAsString(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            return false;
        }

        Map<String, Object> result = mapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
        String token = (String) result.get("token");
        String role = (String) result.get("role");
        int userId = (int) result.get("userId");

        Session.setSession(token, role, userId);
        return true;
    }

    // ---------- آگهی‌ها ----------

// ---------- آگهی‌ها ----------

    private String buildAdsQuery(String basePath, Integer categoryId, String city, Double minPrice, Double maxPrice, String sortBy) {
        StringBuilder query = new StringBuilder(basePath + "?");
        if (categoryId != null) query.append("categoryId=").append(categoryId).append("&");
        if (city != null && !city.isBlank()) query.append("city=").append(city).append("&");
        if (minPrice != null) query.append("minPrice=").append(minPrice).append("&");
        if (maxPrice != null) query.append("maxPrice=").append(maxPrice).append("&");
        if (sortBy != null && !sortBy.isBlank()) query.append("sortBy=").append(sortBy);
        return query.toString();
    }

    public List<Ad> getAds(Integer categoryId, String city, Double minPrice, Double maxPrice, String sortBy)
            throws IOException, InterruptedException {
        HttpResponse<String> response = sendGet(buildAdsQuery("/ads", categoryId, city, minPrice, maxPrice, sortBy));
        return mapper.readValue(response.body(), new TypeReference<List<Ad>>() {});
    }

    public List<Ad> getMyAds(Integer categoryId, String city, Double minPrice, Double maxPrice, String sortBy)
            throws IOException, InterruptedException {
        HttpResponse<String> response = sendGet(buildAdsQuery("/ads/my", categoryId, city, minPrice, maxPrice, sortBy));
        return mapper.readValue(response.body(), new TypeReference<List<Ad>>() {});
    }

    public List<Ad> getAllAdsAdmin(Integer categoryId, String city, Double minPrice, Double maxPrice, String sortBy)
            throws IOException, InterruptedException {
        HttpResponse<String> response = sendGet(buildAdsQuery("/ads/all", categoryId, city, minPrice, maxPrice, sortBy));
        return mapper.readValue(response.body(), new TypeReference<List<Ad>>() {});
    }


    public Ad createAd(String title, String description, double price, String city, int categoryId)
            throws IOException, InterruptedException {
        Map<String, Object> body = new HashMap<>();
        body.put("title", title);
        body.put("description", description);
        body.put("price", price);
        body.put("city", city);
        body.put("categoryId", categoryId);

        HttpResponse<String> response = sendPost("/ads", body);
        return mapper.readValue(response.body(), Ad.class);
    }
    public HttpResponse<String> uploadAdImage(int adId, java.io.File imageFile)
            throws IOException, InterruptedException {

        String boundary = "----DivarBoundary" + System.currentTimeMillis();

        byte[] fileBytes = java.nio.file.Files.readAllBytes(imageFile.toPath());
        String fileName = imageFile.getName();

        String header = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n"
                + "Content-Type: application/octet-stream\r\n\r\n";

        String footer = "\r\n--" + boundary + "--\r\n";

        byte[] headerBytes = header.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] footerBytes = footer.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        byte[] requestBody = new byte[headerBytes.length + fileBytes.length + footerBytes.length];
        System.arraycopy(headerBytes, 0, requestBody, 0, headerBytes.length);
        System.arraycopy(fileBytes, 0, requestBody, headerBytes.length, fileBytes.length);
        System.arraycopy(footerBytes, 0, requestBody, headerBytes.length + fileBytes.length, footerBytes.length);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/ads/" + adId + "/image"))
                .header("Authorization", "Bearer " + Session.getToken())
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
    public HttpResponse<String> deleteAd(int id) throws IOException, InterruptedException {
        return sendDelete("/ads/" + id);
    }

    public HttpResponse<String> markAdAsSold(int id) throws IOException, InterruptedException {
        return sendPostNoBody("/ads/" + id + "/mark-sold");
    }
    // ---------- کاربران و امتیازدهی ----------

    public Map<String, Object> getUserPublic(int userId) throws IOException, InterruptedException {
        HttpResponse<String> response = sendGet("/users/" + userId + "/public");
        return mapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
    }

    public Map<String, Object> getRatingSummary(int userId) throws IOException, InterruptedException {
        HttpResponse<String> response = sendGet("/users/" + userId + "/rating-summary");
        return mapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
    }

    public int getMyRating(int userId) throws IOException, InterruptedException {
        HttpResponse<String> response = sendGet("/users/" + userId + "/my-rating");
        Map<String, Object> result = mapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
        return (int) result.get("value");
    }

    public HttpResponse<String> rateUser(int userId, int value) throws IOException, InterruptedException {
        Map<String, Object> body = new HashMap<>();
        body.put("value", value);
        return sendPost("/users/" + userId + "/rate", body);
    }

    // ---------- دسته‌بندی‌ها ----------

    public List<Category> getCategories() throws IOException, InterruptedException {
        HttpResponse<String> response = sendGet("/categories");
        return mapper.readValue(response.body(), new TypeReference<List<Category>>() {});
    }

    public HttpResponse<String> createCategory(String name) throws IOException, InterruptedException {
        Map<String, String> body = new HashMap<>();
        body.put("name", name);
        return sendPost("/categories", body);
    }

    // ---------- پنل ادمین ----------

    public List<Ad> getPendingAds() throws IOException, InterruptedException {
        HttpResponse<String> response = sendGet("/admin/ads/pending");
        return mapper.readValue(response.body(), new TypeReference<List<Ad>>() {});
    }

    public HttpResponse<String> approveAd(int id) throws IOException, InterruptedException {
        return sendPostNoBody("/admin/ads/" + id + "/approve");
    }

    public HttpResponse<String> rejectAd(int id) throws IOException, InterruptedException {
        return sendPostNoBody("/admin/ads/" + id + "/reject");
    }


    public List<User> getAllUsers() throws IOException, InterruptedException {

        HttpResponse<String> response = sendGet("/admin/users");

        System.out.println("Status = " + response.statusCode());
        System.out.println("Body = " + response.body());

        return mapper.readValue(response.body(),
                new TypeReference<List<User>>() {});
    }

    public HttpResponse<String> promoteUser(int id) throws IOException, InterruptedException {
        return sendPostNoBody("/admin/users/" + id + "/promote");
    }

    public HttpResponse<String> deleteUser(int id) throws IOException, InterruptedException {
        return sendDelete("/admin/users/" + id);
    }
    public HttpResponse<String> deleteCategory(int id)
            throws IOException, InterruptedException {

        return sendDelete("/categories/" + id);
    }
}