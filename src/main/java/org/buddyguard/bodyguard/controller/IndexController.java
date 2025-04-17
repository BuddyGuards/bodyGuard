package org.buddyguard.bodyguard.controller;


import lombok.AllArgsConstructor;
import org.buddyguard.bodyguard.entity.GroupMember;
import org.buddyguard.bodyguard.repository.GroupMemberRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.buddyguard.bodyguard.entity.User;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
@AllArgsConstructor
public class IndexController {

    private GroupMemberRepository groupMemberRepository;

    // @GetMapping({"/index", "/"})
    //public String indexHandel() {

    //    return "index";
    // }

    @GetMapping({"/index", "/"})
    public String indexHandel(Model model,
                              @SessionAttribute(value = "user", required = false) User user) {
        model.addAttribute("user", user);

        if (user != null) {
            groupMemberRepository.findByUserId(user.getId());
        }

        return "auth/index";
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


