package com.graphql.example.http.data;

import java.util.Collections;
import java.util.List;

public class Human implements FilmCharacter {
    final String id;
    final String name;
    final List<String> friends;
    final List<Integer> appearsIn;
    final String homePlanet;
    final String queryTime;
    final String instant1;
    final String instant2;


    public Human(String id, String name, List<String> friends, List<Integer> appearsIn, String homePlanet,
                 String queryTime, String instant1, String instant2) {
        this.id = id;
        this.name = name;
        this.homePlanet = homePlanet;
        this.queryTime = queryTime;
        this.instant1 = instant1;
        this.instant2 = instant2;

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

    public String getHomePlanet() {
        return homePlanet;
    }

    public String getQueryTime() {
        return queryTime;
    }

    public String getInstant1() {
        return instant1;
    }

    public String getInstant2() {
        return instant2;
    }

    @Override
    public String toString() {
        return "Human{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            '}';
    }
}
