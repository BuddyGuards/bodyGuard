package org.buddyguard.bodyguard.controller;


import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.buddyguard.bodyguard.entity.User;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
@AllArgsConstructor
public class IndexController {

    // @GetMapping({"/index", "/"})
    //public String indexHandel() {

    //    return "index";
    // }

    @GetMapping({"/index", "/"})
    public String indexHandel(Model model,
                              @SessionAttribute(value = "user", required = false) User user) {
        model.addAttribute("user", user);
        return "auth/index"; // templates
    }

//    @GetMapping("/index")
//    public String indexPage(Model model, @SessionAttribute(name = "user", required = false) User user) {
//        model.addAttribute("user", user);
//        return "index";
//    }


    @GetMapping("/help")
    public String helpHandel() {

        return "help";
    }
}
