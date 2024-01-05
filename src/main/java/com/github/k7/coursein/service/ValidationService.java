package com.github.k7.coursein.service;

import com.github.k7.coursein.entity.User;

public interface ValidationService {

    void validate(Object request);

    void validateAuth(User user);

}
