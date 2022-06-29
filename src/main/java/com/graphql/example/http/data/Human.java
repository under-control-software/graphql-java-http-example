package com.graphql.example.http.data;

import java.util.List;

public class Human implements FilmCharacter {
    final String id;
    final String name;
    final List<String> friends;
    final List<Integer> appearsIn;
    final String homePlanet;
    final String queryTime;

    public Human(String id, String name, List<String> friends, List<Integer> appearsIn, String homePlanet,
            String queryTime) {
        this.id = id;
        this.name = name;
        this.friends = friends;
        this.appearsIn = appearsIn;
        this.homePlanet = homePlanet;
        this.queryTime = queryTime;
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

    public String getHomePlanet() {
        return homePlanet;
    }

    public String getQueryTime() {
        return queryTime;
    }

    @Override
    public String toString() {
        return "Human{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
