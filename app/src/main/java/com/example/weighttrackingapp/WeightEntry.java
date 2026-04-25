package com.example.weighttrackingapp;

/**
 * Simple model object for one weight entry row.
 */
public class WeightEntry {
    private final int id;
    private final String date;
    private final double weight;

    public WeightEntry(int id, String date, double weight) {
        this.id = id;
        this.date = date;
        this.weight = weight;
    }

    public int getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public double getWeight() {
        return weight;
    }
}
