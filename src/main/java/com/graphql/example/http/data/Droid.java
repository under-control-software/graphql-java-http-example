package com.graphql.example.http.data;

import java.util.Collections;
import java.util.List;

public class Droid implements FilmCharacter {
    final String id;
    final String name;
    final List<String> friends;
    final List<Integer> appearsIn;
    final String primaryFunction;
    final String queryTime;

    public Droid(String id, String name, List<String> friends, List<Integer> appearsIn, String primaryFunction,
            String queryTime) {
        this.id = id;
        this.name = name;
        this.primaryFunction = primaryFunction;
        this.queryTime = queryTime;

        if (friends == null && queryTime != "-10") {
            this.friends = Collections.<String>emptyList();
        } else {
            this.friends = friends;
        }

        if (appearsIn == null && queryTime != "-10") {
            this.appearsIn = Collections.<Integer>emptyList();
        } else {
            this.appearsIn = appearsIn;
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<String> getFriends() {
        return friends;
    }

    public List<Integer> getAppearsIn() {
        return appearsIn;
    }

    public String getPrimaryFunction() {
        return primaryFunction;
    }

    public String getQueryTime() {
        return queryTime;
    }

    @Override
    public String toString() {
        return "Droid{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
