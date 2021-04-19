package com.example.springsocial.repository;

import com.example.springsocial.model.User;

import java.util.Optional;

//@Repository
public interface UserRepository {

    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);

    User save(User user);

    Optional<User> findById(Long id);
}
