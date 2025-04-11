package org.buddyguard.bodyguard.controller;


import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@AllArgsConstructor
public class IndexController {


    @GetMapping({"/index", "/"})
    public String indexHandel() {

        return "index";
    }



}
