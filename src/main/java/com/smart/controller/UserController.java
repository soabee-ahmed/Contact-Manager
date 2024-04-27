package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.persistence.criteria.Path;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;
	@ModelAttribute
	public void addCommonData(Model model,Principal principal) {
		String userName=principal.getName();
		System.out.println("USERNAME "+userName);
		User user=userRepository.getUserByUserName(userName);
		System.out.println(user);
		model.addAttribute("user",user);
	}
	
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal) {
		model.addAttribute("title","user dashboard");
		return "normal/user_dashboard";
	}
	//open add form controller
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title","Add Contact");
		model.addAttribute("contact",new Contact());
		return "normal/add_contact_form";
	}
	
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,Model model,@RequestParam("profileImage") MultipartFile file,Principal principal) {
		try {
		String name=principal.getName();
		User user=userRepository.getUserByUserName(name);
		//processing the uploading file..
		if(file.isEmpty()) {
			System.out.println("file is empty");
			contact.setImage("contact.jpg");
		}
		else {
			contact.setImage(file.getOriginalFilename());
			File saveFile=new ClassPathResource("static/image").getFile();
			java.nio.file.Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
			System.out.println("file is uploaded");
		}
		user.getContacts().add(contact);
		contact.setUser(user);
		
		this.userRepository.save(user);
		System.out.println("Data "+contact);
		System.out.println("Added to data base");
		//message success
		model.addAttribute("message",new Message("Contact Added Successfully!!","alert-success"));

		/*
		 * session.setAttribute("message",new
		 * Message("Your Contact is added!! Add more..","success"));
		 */
		}catch(Exception e) {
			System.out.println("Error "+e.getMessage());
			e.printStackTrace();
			model.addAttribute("message",new Message("Something Went Wrong!!","alert-danger"));
			//message error
			/*
			 * session.setAttribute("message", new
			 * Message("Something Went Wrong !! Try Again","danger"));
			 */
		}
		return "normal/add_contact_form";
	}
	//showing all contacts with the help of pagination
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page,Model m,Principal principal) {
		m.addAttribute("title","Show User Contacts");
		String userName=principal.getName();
		User user=this.userRepository.getUserByUserName(userName);
		PageRequest pageable=PageRequest.of(page, 3); 
		Page<Contact> contacts=this.contactRepository.findContactByUser(user.getId(),pageable);
		m.addAttribute("contacts",contacts);
		
		m.addAttribute("currentPage",page);
		m.addAttribute("totalPages",contacts.getTotalPages());
		return "normal/show_contacts";
	}
	//showing particular contact details
	@GetMapping("/contact/{cId}")
	public String showContactDetail(@PathVariable("cId") Integer cId,Model model,Principal principal) {
		System.out.println("cID"+cId);
		Optional<Contact> contactOptional=this.contactRepository.findById(cId);
		Contact contact=contactOptional.get();
		String username=principal.getName();
		User user=this.userRepository.getUserByUserName(username);
		
		if(user.getId()==contact.getUser().getId()) {
		model.addAttribute("contact",contact);
		model.addAttribute("titel",contact.getName());
		}
		return "normal/contact_detail";
	}
	//delete contact handler
	@GetMapping("/delete/{cId}")
	@Transactional
	public String deleteContact(@PathVariable("cId")Integer cId,Model m,Principal principal) {
		/*
		 * Optional<Contact> optionalContact=this.contactRepository.findById(cId).get();
		 */
		Contact contact=this.contactRepository.findById(cId).get();
//		contact.setUser(null); 
		//delete old pic
		User user=this.userRepository.getUserByUserName(principal.getName());
		user.getContacts().remove(contact);
		this.userRepository.save(user);
//		this.contactRepository.delete(contact);
		m.addAttribute("message",new Message("Contact deleted Successfully...","alert-success"));
		return "redirect:/user/show-contacts/0";
	}
	//open update form handler
	@PostMapping("/update-contact/{cId}")
	public String updateForm(@PathVariable("cId") Integer cid,Model m) {
		Contact contact=this.contactRepository.findById(cid).get();
		m.addAttribute("contact",contact);
		m.addAttribute("title","Update Contact");
		return "normal/update_form";
		
	}
	//update contact handler
	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,Model m,Principal principal) {
		try {
			//old contact details
			Contact oldcontactDetail=this.contactRepository.findById(contact.getcId()).get();			
			if(!file.isEmpty()) {
				//delete old pic
				File deleteFile=new ClassPathResource("static/image").getFile();
				File file1=new File(deleteFile,oldcontactDetail.getImage());
				file1.delete();
				//update new pic
				File saveFile=new ClassPathResource("static/image").getFile();
				java.nio.file.Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
			}
			User user=this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			m.addAttribute("message",new Message("Your contact is Updated...","success"));
		}catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("CONTACT NAME "+contact.getName());
		System.out.println("CONTACT ID "+contact.getcId());
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	//your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		model.addAttribute("title","Profile Page");
		return "normal/profile";
	}
	//open setting handler
	@GetMapping("/settings")
	public String openSettings() {
		return "normal/settings";
	}
	//change password.. handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,@RequestParam("newPassword") String newPassword,Principal principal,RedirectAttributes redirectAttributes){
		System.out.println("OLD PASSWORD "+oldPassword);
		System.out.println("NEW PASSWORD "+newPassword);
		String userName=principal.getName();
		User currentUser=this.userRepository.getUserByUserName(userName);
		System.out.println(currentUser.getPassword());
		if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
			//change the password
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			redirectAttributes.addFlashAttribute("message", new Message("Your password is changed successfully..","success"));
		}else {
			redirectAttributes.addFlashAttribute("message", new Message("Please enter correct old password.","danger"));
			return "redirect:/user/settings";
		}
		return "redirect:/user/index";
	} 
	
}
