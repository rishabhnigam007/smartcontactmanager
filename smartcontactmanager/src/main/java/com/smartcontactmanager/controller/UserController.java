package com.smartcontactmanager.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import com.smartcontactmanager.dao.ContactRepository;
import com.smartcontactmanager.dao.UserRepository;
import com.smartcontactmanager.entities.Contact;
import com.smartcontactmanager.entities.User;
import com.smartcontactmanager.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController
{
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
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
	public String processContact(@ModelAttribute Contact contact,@RequestParam ("profileImage") MultipartFile file,Principal principle,HttpSession session)
	{
		try 
		{
			String name=principle.getName();
			User user = this.userRepository.getUserByUserName(name);
			
			// processing and uploading file
			if(file.isEmpty())
			{
				System.out.println("File is empty");
				contact.setImage("contact.png");
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
			
			// message success
			session.setAttribute("message", new Message("Your Contact is added !! add more","success"));
			
		}
		catch (Exception e) 
		{
			System.out.println("Error "+e.getMessage());
			e.printStackTrace();
			// error message
			session.setAttribute("message", new Message("Something went wrong !!","danger"));
		}
		
		return "normal/add_contact_form";
	}
	
	
	// show contact handler
	// per page = 5[n]
	// current page = 0[page]
	
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page,Model model,Principal principal)
	{
		model.addAttribute("title", "Show User Contacts");
		
		// contact list find for logged in user
		String userName=principal.getName();
		
		User user=this.userRepository.getUserByUserName(userName);
		
		// bind only 5 item per page
		Pageable pageable = PageRequest.of(page, 4);
		
		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(),pageable);
		
		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());
		
		return "normal/show_contacts";
	}
	
	// Showing particular contact details
	@GetMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId,Model model,Principal principal)
	{
		System.out.println("CID "+cId);
		
		Optional<Contact> contactOptional=this.contactRepository.findById(cId);
		Contact contact=contactOptional.get();
		
		String userName=principal.getName();
		User user=this.userRepository.getUserByUserName(userName);
		
		if(user.getId()==contact.getUser().getId())
		{
			model.addAttribute("contact", contact);
		}
		else
		{
			return "normal/contact_not_found";
		}
		return "normal/contact_detail";
	}
	
	// Delete contact handler
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId")Integer cId,Model model,HttpSession session)
	{
		System.out.println("CID "+cId);
		
		Contact contact=this.contactRepository.findById(cId).get();
		
		//check only authentic user can be deleted his contact
		System.out.println("Contact "+contact.getcId());
		
		// unlink the user and delete them
		contact.setUser(null);
		
		this.contactRepository.delete(contact);
		
		System.out.println("DELETED");
		
		session.setAttribute("message", new Message("Contact deleted successfully !!", "success"));
		
		return "redirect:/user/show-contacts/0";
	}
	
}