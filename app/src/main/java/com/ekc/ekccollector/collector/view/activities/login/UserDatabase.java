package com.ekc.ekccollector.collector.view.activities.login;

import com.ekc.ekccollector.collector.model.models.User;

import javax.inject.Inject;

public class UserDatabase {

    @Inject
    User user;

    @Inject
    public UserDatabase() {
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
