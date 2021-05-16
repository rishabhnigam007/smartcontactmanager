package com.smartcontactmanager.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smartcontactmanager.dao.UserRepository;
import com.smartcontactmanager.entities.Contact;
import com.smartcontactmanager.entities.User;

@Controller
@RequestMapping("/user")
public class UserController
{
	@Autowired
	private UserRepository userRepository;
	
	
	//Method For common for all response
	@ModelAttribute
	public void addCommonData(Model model,Principal principle)
	{
		String userName = principle.getName();
		System.out.println("USERNAME "+userName);
		//get the user using username from user repository
		User user=userRepository.getUserByUserName(userName);
		System.out.println("USER "+user);
		
		model.addAttribute("user", user);
	}
	
	// Dashboard home
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principle)
	{	
		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}
	
	
	//open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model)
	{
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		
		return "normal/add_contact_form";
	}
	
	// Processing add contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,@RequestParam ("profileImage") MultipartFile file,Principal principle)
	{
		try 
		{
			String name=principle.getName();
			User user = this.userRepository.getUserByUserName(name);
			
			// processing and uploading file
			if(file.isEmpty())
			{
				System.out.println("File is empty");
			}
			else
			{
				contact.setImage(file.getOriginalFilename());
				
				File saveFile=new ClassPathResource("static/image").getFile();
				
				Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				System.out.println("Image is uploaded");
			}
		
			// 	for mapping user id
			contact.setUser(user);
		
			user.getContacts().add(contact);
		
			this.userRepository.save(user);
		
			System.out.println("Data "+contact);
		
			System.out.println("Added to database");
		}
		catch (Exception e) 
		{
			System.out.println("Error "+e.getMessage());
			e.printStackTrace();
		}
		
		return "normal/add_contact_form";
	}
	
}