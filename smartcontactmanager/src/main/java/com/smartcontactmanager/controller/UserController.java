package com.smartcontactmanager.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
	
	@Autowired
	private BCryptPasswordEncoder bcrypt;
	
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
	@RequestMapping(value =  "/index" ,method = RequestMethod.GET)
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
	public String processContact(@ModelAttribute Contact contact,@RequestParam ("profileImage") MultipartFile file,Principal principle,HttpSession session,Model model)
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
		model.addAttribute("title", "Your Contact added !!");
		return "normal/add_contact_form";
	}
	
	
	// show contact handler
	// per page = 5[n]
	// current page = 0[page]
	
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page,Model model,Principal principal)
	{
		model.addAttribute("title", "Your Contacts");
		
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
		try
		{
			Optional<Contact> contactOptional=this.contactRepository.findById(cId);
			Contact contact=contactOptional.get();
			
			String userName=principal.getName();
			User user=this.userRepository.getUserByUserName(userName);
			
			if(user.getId()==contact.getUser().getId())
			{
				model.addAttribute("contact", contact);
			}
			model.addAttribute("title", "Your Contact : "+contact.getName());
		}
		catch (Exception e)
		{
			model.addAttribute("title", "Contact Not Found");
			return "normal/contact_not_found";
		}
		return "normal/contact_detail";
	}
	
	// Delete contact handler
	@GetMapping("/delete/{cId}")
	@Transactional
	public String deleteContact(@PathVariable("cId")Integer cId,Model model,HttpSession session,Principal principal)
	{
		System.out.println("CID "+cId);
		
		Contact contact=this.contactRepository.findById(cId).get();
		
		//check only authentic user can be deleted his contact
		System.out.println("Contact "+contact.getcId());
		
		// unlink the user and delete them
//		contact.setUser(null);
		
		User user=this.userRepository.getUserByUserName(principal.getName());
		user.getContacts().remove(contact);
		this.userRepository.save(user);
//		this.contactRepository.delete(contact);
		
		System.out.println("DELETED");
		
		session.setAttribute("message", new Message("Contact deleted successfully !!", "success"));
		
		return "redirect:/user/show-contacts/0";
	}
	
	// open Update form handler
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid,Model model)
	{
		model.addAttribute("title", "Update Contact");
		
		Contact contact=this.contactRepository.findById(cid).get();
		
		model.addAttribute("contact", contact);
		
		return "normal/update_form";
	}
	
	// update contact handler
	//PostMapping("/process-update")
	// or
	//same
	@RequestMapping(value = "/process-update",method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,Model model,HttpSession session,Principal principal)
	{
		try 
		{
			
			Contact oldcontactDetail = this.contactRepository.findById(contact.getcId()).get();
			if(!file.isEmpty())
			{
				//file rewrite
				// delete old photo
				File deleteFile=new ClassPathResource("static/image").getFile();
				File file1=new File(deleteFile, oldcontactDetail.getImage());
				file1.delete();
				
				// update new photo		
				File saveFile=new ClassPathResource("static/image").getFile();
				
				Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				contact.setImage(file.getOriginalFilename());
				
			}
			else
			{
				contact.setImage(oldcontactDetail.getImage());
			}
			
			User user=this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			
			this.contactRepository.save(contact);
			
			session.setAttribute("message", new Message("Contact Updated Successfully !!", "success"));
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		
		System.out.println("Contact Name "+contact.getName());
		System.out.println("Contact ID "+contact.getcId());
		
//		return "redirect:/user/show-contacts/0";
		model.addAttribute("title", "Update Contact");
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	
	// open your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model)
	{
		model.addAttribute("title", "Your Profile");
		return "normal/profile";
	}
	
	// open Setting handler
	@GetMapping("/settings")
	public String openSettings(Model model)
	{
		model.addAttribute("title", "Settings");
		return "normal/settings";
	}
	
	// Change password handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,@RequestParam("newPassword") String newPassword,HttpSession session,Principal principal)
	{
		System.out.println("Old Password "+oldPassword);
		System.out.println("New Password "+newPassword);
		
		String userName=principal.getName();
		User currentUser=this.userRepository.getUserByUserName(userName);
		System.out.println(currentUser.getPassword());
		
		if(this.bcrypt.matches(oldPassword, currentUser.getPassword()))
		{
			// change the password
			
			currentUser.setPassword(this.bcrypt.encode(newPassword));
			this.userRepository.save(currentUser);
			
		}
		else
		{
			// error password not match please enter correct old password
			session.setAttribute("message", new Message("Please Enter Correct Old Password !!","danger"));
			return "redirect:/user/settings";
		}
		
		session.setAttribute("message", new Message("Successfully Change Password !!", "success"));
		return "redirect:/user/index";
	}
	
	
	
}