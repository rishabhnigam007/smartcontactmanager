package com.smartcontactmanager.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcontactmanager.entities.User;

public interface UserRepository extends JpaRepository<User, Integer> {

}
