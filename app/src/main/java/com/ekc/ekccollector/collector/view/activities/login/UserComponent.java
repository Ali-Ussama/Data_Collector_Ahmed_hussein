package com.ekc.ekccollector.collector.view.activities.login;

import com.ekc.ekccollector.collector.model.models.User;

import dagger.Component;

@Component
public interface UserComponent {

    User getUser();
}
