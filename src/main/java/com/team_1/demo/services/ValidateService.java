package com.team_1.demo.services;

public interface ValidateService {
    boolean userNameExists(String username);

    boolean hashtagExists(String tag);


    boolean userNameAvailable(String username);
}
