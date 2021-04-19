package com.example.springsocial.repository;

import com.example.springsocial.model.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserInMemoryService implements UserRepository {

    //just for demo and easy to setup in local, in real word, you can use actual db table
    private static final List<User> IN_MEMORY_USERS = new ArrayList<>(10);
    private Long id = 1L;

    @Override
    public Optional<User> findByEmail(String email) {
        return IN_MEMORY_USERS.stream().filter(u -> u.getEmail().equals(email)).findAny();
    }

    @Override
    public Boolean existsByEmail(String email) {
        return IN_MEMORY_USERS.stream().anyMatch(u -> u.getEmail().equals(email));
    }

    @Override
    public User save(User user) {
        user.setId(id++);
        IN_MEMORY_USERS.add(user);
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        return IN_MEMORY_USERS.stream().filter(u -> u.getId().equals(id)).findAny();
    }
}
