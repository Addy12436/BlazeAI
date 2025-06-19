package com.ai.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.ai.entities.User;
import com.ai.form.UserForm;
import com.ai.helper.Message;
import com.ai.helper.MessageType;
import com.ai.secservices.IService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;



@Controller
public class Regester {


        @Autowired
    private IService userService;

    @GetMapping("/register")
public String register(Model model) {

        UserForm userForm = new UserForm();
        // default data bhi daal sakte hai
        // userForm.setName("Adars");
        // userForm.setAbout("This is about : Write something about yourself");
        model.addAttribute("userForm", userForm);

        return "Signup";
    }

    // this is showing login page
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
public String showSignupPage(Model model) {
    model.addAttribute("userForm", new UserForm());  // important for form binding
    return "signup"; // must match signup.html
}



    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    public String processRegister(@Valid @ModelAttribute UserForm userForm, BindingResult rBindingResult,
            HttpSession session) {
        System.out.println("Processing registration");
        // fetch form data
        // UserForm
        System.out.println(userForm);

        // validate form data
        if (rBindingResult.hasErrors()) {
            return "signup";
        }

 

        User user = new User();
        user.setName(userForm.getName());
        user.setEmail(userForm.getEmail());
        user.setPassword(userForm.getPassword());
        user.setAbout(userForm.getAbout());
        user.setPhoneNumber(userForm.getPhoneNumber());
        user.setEnabled(false);
     user.setProfilePic("/images/profile.png");
        User savedUser = userService.saveUser(user);

        System.out.println("user saved :");

        // message = "Registration Successful"

        // add the message:

        Message message = Message.builder().content("Registration Successful").type(MessageType.green).build();

        session.setAttribute("message", message);

        // redirectto login page
        return "redirect:/login";
    }
}
