package com.fhict.hololiveocgmanager.service;

import com.fhict.hololiveocgmanager.domain.User;
import com.fhict.hololiveocgmanager.dto.request.UserUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    User createUser(User user);

    User getUser(Integer id);

    Page<User> getUsersByUsername(String username, Pageable pageable);

    User updateUser(UserUpdateRequest updateRequest, Integer id);

    void deleteUser(Integer id);
}
