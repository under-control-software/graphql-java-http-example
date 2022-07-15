package com.graphql.example.http.utill;

public class Utility {

    public static String formatTime(long time) {
        double millis = (double) time / 1000000.0;
        return String.format("%.1f", millis);
    }

}
