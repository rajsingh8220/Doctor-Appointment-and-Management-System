/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dams.controller.client;

import com.dams.domain.Contact;
import com.dams.domain.Doctor;
import com.dams.domain.Patient;
import com.dams.domain.Slider;
import com.dams.service.ContactService;
import com.dams.service.DoctorService;

import com.dams.service.PatientService;
import com.dams.service.SliderService;
import com.dams.service.SpecialityService;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.codemonkey.simplejavamail.Mailer;
import org.codemonkey.simplejavamail.TransportStrategy;
import org.codemonkey.simplejavamail.email.Email;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author raj
 */
@Controller
public class ClientController {

	@Resource
	private DoctorService doctorService;

	@Resource
	private PatientService patientService;

	@Resource
	private SpecialityService specialityService;
        
        @Resource
	private SliderService sliderService;
        
	@Resource
	private ContactService contactService;

	@Autowired
	private HttpSession httpsession;

	@RequestMapping("/")
	public String start() {
		return "redirect:/client"; // How would it differ if it was forward
									// instead?
	}

	@RequestMapping("/client")
	public String welcome(@ModelAttribute("contact") Contact contact,Model model) {
		List<Slider> sliders =  sliderService.getAll();
		model.addAttribute("sliders", sliders);

		return "home";
	}

	@RequestMapping("/client/signup")
	public String register(Model model) {
		Patient user = new Patient();
		model.addAttribute("patient", user);
		return "signup";
	}

	@RequestMapping(value = "/client/signup", method = RequestMethod.POST)
	public String processRegister(@ModelAttribute("patient") Patient patient, RedirectAttributes redirectAttributes) {

  
		patientService.savePatient(patient);
		redirectAttributes.addFlashAttribute("message", "Registered Successfully");
		return "redirect:/client/doctors";
	}

	@RequestMapping(value = "/client/contactus", method = RequestMethod.POST)
	public @ResponseBody String postContactMsg(@RequestBody Contact contact, HttpServletRequest request) {
		contactService.saveContact(contact);

		System.out.println(contact);
                String msg = "Your Quiry has been received";
                String to = contact.getEmail();
                String name = contact.getName();
                
                sendSuccessAffirmation(to,name,msg,"Thank You: Inquiry");
		return "success";
                
	}
        
        private void sendSuccessAffirmation(String emailTo, String name, String msg, String subject) {
		Email email = new Email.Builder()
			    .from("Your Doctor", "doctor.a2789@gmail.com") 
			    .to(name, emailTo)
			    .subject(subject)
			    .text(msg)
			    .textHTML(msg)
			    .build();

		
		//new Mailer("smtp.gmail.com", 25, "hitsad12@gmail.com", "coder@id", TransportStrategy.SMTP_TLS).sendMail((org.codemonkey.simplejavamail.email.Email) email);
	    //new Mailer("smtp.gmail.com", 587, "hitsad12@gmail.com", "coder@id", TransportStrategy.SMTP_TLS).sendMail((org.codemonkey.simplejavamail.email.Email) email);
	    new Mailer("smtp.gmail.com", 465, "doctor.a2789@gmail.com", "coder@id", TransportStrategy.SMTP_SSL).sendMail((org.codemonkey.simplejavamail.email.Email) email);
		
		System.out.println("Email has been sent");
	}
        
	@RequestMapping(value = "/client/login", method = RequestMethod.POST)
	public @ResponseBody String processLogin(@RequestBody Patient patient, HttpServletRequest request) {
		JSONObject obj = new JSONObject();
		
		Patient dbPatient = patientService.findByUsername(patient.getUsername());

		// System.out.println(dbPatient.getPassword());
		if (dbPatient != null) {
			if (dbPatient.getPassword().equals(patient.getPassword())) {
				httpsession.setAttribute("patientId", dbPatient.getPatientId());
				obj.put("login", 1);
			} else {
				obj.put("login", 0);
			}
		} else {
			obj.put("login", 0);
		}

		return obj.toJSONString();
	}
	
	@RequestMapping(value = "/client/logout", method = RequestMethod.GET)
	public String processLogout(){
		httpsession.invalidate();
		return "redirect:/client";
	}
}
