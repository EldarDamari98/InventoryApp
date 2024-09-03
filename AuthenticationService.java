package com.example.excelnew;

import okhttp3.*;

import java.io.IOException;

public class AuthenticationService {

    private static final String FIREBASE_AUTH_URL = "***";

    public boolean authenticateUser(String email, String password) {
        OkHttpClient client = new OkHttpClient();

        String json = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\",\"returnSecureToken\":true}";

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"), json);

        Request request = new Request.Builder()
                .url(FIREBASE_AUTH_URL)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return true;
            } else {
                System.out.println("Login failed. Response code: " + response.code());
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
