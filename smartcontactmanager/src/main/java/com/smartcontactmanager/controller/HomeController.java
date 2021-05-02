package com.smartcontactmanager.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smartcontactmanager.dao.UserRepository;
import com.smartcontactmanager.entities.User;
import com.smartcontactmanager.helper.Message;

@Controller
public class HomeController 
{
	@Autowired
	private UserRepository userRepository;
	
	@RequestMapping("/")
	public String home(Model model)
	{
		model.addAttribute("title", "Home - Smart Contact Manager");
		return "home";
	}
	
	@RequestMapping("/about")
	public String about(Model model)
	{
		model.addAttribute("title", "About - Smart Contact Manager");
		return "about";
	}
	
	@RequestMapping("/signup")
	public String signup(Model model)
	{
		model.addAttribute("title", "SignUp - Smart Contact Manager");
		model.addAttribute("user",new User());
		return "signup";
	}
	
	// this handler is used for register user
	// PostMapping("/do_register") this is also same.
	@RequestMapping(value = "/do_register",method = RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("user") User user,BindingResult result1,@RequestParam(value = "agreement",defaultValue = "false")boolean agreement,Model model,HttpSession session)
	{
		// if Binding result is declare in the last after httpsession then changes not reflected in page so paste it in after user model attribute
		try 
		{
			if(result1.hasErrors())
			{
				System.out.println("Error "+result1.toString());
				model.addAttribute("user", user);
				return "signup";
			}
			
			if(!agreement)
			{
				System.out.println("you have not agreed the terms and condition !!");
				throw new Exception("You have not agreed the terms and condition !!");
			}			
			
			user.setRole("Role User");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			System.out.println("Agreement "+agreement);
			System.out.println("User "+user);
			
			//new user() for when register and hit submit button then form clean and ready for again register.
			model.addAttribute("user", new User());
			session.setAttribute("message", new Message("Successfully Registered !!", "alert-success"));
			userRepository.save(user);
			return "signup";
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			model.addAttribute("user",user);
			session.setAttribute("message", new  Message("Something went wrong !! "+e.getMessage(), "alert-danger"));
			return "signup";
		}
		
	}
	
}