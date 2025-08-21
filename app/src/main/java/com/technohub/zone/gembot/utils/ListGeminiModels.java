package com.technohub.zone.gembot.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ListGeminiModels {
    public static void main(String[] args) {
        try {
            // Use URL from Constants
            URL url = new URL(Constants.LIST_MODELS_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");

            int responseCode = conn.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );
            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            System.out.println("Available Models:\n" + response.toString());
        } catch (Exception e) {
            System.err.println("Error fetching models: " + e.getMessage());
        }
    }
}
