package com.fhict.hololiveocgmanager.service;

import com.fhict.hololiveocgmanager.domain.User;

import java.util.List;

public interface UserService {
    User createUser(User user);

    User getUser(Integer id);

    List<User> getUsersByUsername(String username);

    User updateUser(User user, Integer id);

    void deleteUser(Integer id);
}
