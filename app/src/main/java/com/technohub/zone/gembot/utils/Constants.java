package com.technohub.zone.gembot.utils;

public class Constants {

    // Gemini API base URL
    public static final String BASE_URL = "https://generativelanguage.googleapis.com/";

    // Gemini Model endpoint (default 2.0 Flash)
    public static final String MODEL = "v1beta/models/gemini-2.0-flash:generateContent";

    // ⚠️ Your Gemini API Key
    public static final String API_KEY = "AIzaSyAt5FIcAlt2RUqegjNLxXvUfijVMxUqhY8";

    // Model listing endpoint
    public static final String LIST_MODELS_URL =
            BASE_URL + "v1beta/models?key=" + API_KEY;

    // Chat roles
    public static final String SENDER_USER = "user";
    public static final String SENDER_BOT = "bot";
}
