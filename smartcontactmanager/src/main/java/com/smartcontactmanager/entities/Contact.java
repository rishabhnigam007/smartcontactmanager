package com.smartcontactmanager.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "CONTACT")
public class Contact 
{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int cId;
	@NotBlank(message = "Name field is required !!")
    @Size(min = 2, max = 20 , message = "Name should be between 2 - 20 characters !!")
	private String name;
	@NotBlank(message = "Second name field is required !!")
    @Size(min = 2, max = 20 , message = "Second name should be between 2 - 20 characters !!")
	private String secondName;
    @Column(unique = true)
    @NotBlank(message = "Email field is required !!")
    @Email(regexp = "^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$")
	private String email;
    @NotBlank(message = "Work field is required !!")
	private String work;
	@NotBlank(message = "Phone field is required !!")
	private String phone;
//	@NotBlank(message = "ImageUrl field is required !!")
	private String image;
	@Column(length = 1600)
//	@NotBlank(message = "Description field is required !!")
	private String description;
	
	@ManyToOne
	@JsonIgnore
	private User user;
	
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public int getcId() {
		return cId;
	}
	public void setcId(int cId) {
		this.cId = cId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSecondName() {
		return secondName;
	}
	public void setSecondName(String secondName) {
		this.secondName = secondName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getWork() {
		return work;
	}
	public void setWork(String work) {
		this.work = work;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Contact(int cId, String name, String secondName, String email, String work, String phone, String image,
			String description) {
		super();
		this.cId = cId;
		this.name = name;
		this.secondName = secondName;
		this.email = email;
		this.work = work;
		this.phone = phone;
		this.image = image;
		this.description = description;
	}
	public Contact() {
		super();
	}
	
	@Override
	public String toString() {
		return "Contact [cId=" + cId + ", name=" + name + ", secondName=" + secondName + ", email=" + email + ", work="
				+ work + ", phone=" + phone + ", image=" + image + ", description=" + description + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + cId;
		return result;
	}
	
	@Override
	public boolean equals(Object obj) 
	{
		return this.cId==((Contact) obj).getcId();
	}
		
}