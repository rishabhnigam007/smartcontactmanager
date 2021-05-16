package com.smartcontactmanager.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartcontactmanager.entities.Contact;

public interface ContactRepository extends JpaRepository<Contact, Integer>
{
	// Pagination
	
	@Query(value = "select c from Contact c where c.user.id =:userId")
	public List<Contact> findContactsByUser(@Param("userId")int userId);
}