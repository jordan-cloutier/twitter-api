package com.team_1.demo.services.impl;

import com.team_1.demo.repositories.HashtagRepository;
import com.team_1.demo.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.team_1.demo.services.ValidateService;
@Service
@RequiredArgsConstructor
public class ValidateServiceImpl implements ValidateService {

    private final UserRepository userRepository;
    private final HashtagRepository hashtagRepository;
    @Override
    public boolean userNameExists(String username) {
        if (userRepository.findByUsername(username) == null) {
            return false;
        }
        return true;
    }

    @Override
    public boolean hashtagExists(String tag) {
        if (hashtagRepository.findByLabel(tag) == null) {
            return false;
        }
        return true;
    }

    @Override
    public boolean userNameAvailable(String username) {
        if (userRepository.findByUsername(username) != null) {
            return false;
        }
        return true;
    }
}
