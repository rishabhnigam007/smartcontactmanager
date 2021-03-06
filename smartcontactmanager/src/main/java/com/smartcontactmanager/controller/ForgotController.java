package com.smartcontactmanager.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smartcontactmanager.dao.UserRepository;
import com.smartcontactmanager.entities.User;
import com.smartcontactmanager.service.EmailService;

@Controller
public class ForgotController 
{
	
	Random random=new Random(1000);
	// Email id form open handler
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bcrypt;
	
	@GetMapping("/forgot")
	public String openEmailForm(Model model)
	{
		model.addAttribute("title", "Forgot Password");
		return "forgot_email_form";
	}
	
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email,HttpSession session,Model model)
	{
		System.out.println("Email "+email);
		
		// Generating otp of 6 digit
		int otp = random.nextInt(999999);
		
		System.out.println("OTP "+otp);
		
		// write code for send otp to email
		String subject="OTP from SCM";
		
		String message = ""
				+ "<div style='border:1px solid #e2e2e2; padding:20px'>"
				+ "<h1>"
				+ "OTP is "
				+ "<b>"+otp
				+ "</b>"
				+ "</h1>"
				+ "</div>";
		
		String to = email;
		
		boolean flag = this.emailService.sendEmail(subject, message, to);
		
		if(flag)
		{
			session.setAttribute("myotp", otp);
			session.setAttribute("email", email);
			model.addAttribute("title", "Verify OTP");
			return "verify_otp";
		}
		else
		{
			session.setAttribute("message", "Check your email id !!");
			model.addAttribute("title", "Forgot Email Form");
			return "forgot_email_form";
		}
		
	}
	
	//verify otp
	
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") int otp, HttpSession session,Model model)
	{
		int myOtp = (int)session.getAttribute("myotp");
		String email = (String)session.getAttribute("email");
		
		if(myOtp==otp)
		{
			//password change form
			
			User user = this.userRepository.getUserByUserName(email);
			
			if(user==null)
			{
				//send error message
				session.setAttribute("message", "User does not Exist with this Email !!");
				model.addAttribute("title", "User Does Not Exists");
				return "forgot_email_form";
			}
			else
			{
				//send change password form
				model.addAttribute("title", "Change Password");
				return "password_change_form";
			}
		}
		else
		{
			session.setAttribute("message", "You have Entered Wrong OTP !!");
			model.addAttribute("title", "You have Entered Wrong OTP !!");
			return "verify_otp";
		}		
	}
	
	//change password
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newpassword") String newpassword,HttpSession session)
	{
		String email=(String)session.getAttribute("email");
		User user= this.userRepository.getUserByUserName(email);
		user.setPassword(this.bcrypt.encode(newpassword));
		this.userRepository.save(user);
		
		return "redirect:/signin?change=Password Changed Successfully..";
	}
	
	
	
}