package org.buddyguard.bodyguard.controller;

import lombok.AllArgsConstructor;
import org.codenova.moneylog.entity.User;
import org.codenova.moneylog.repository.ExpenseRepository;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.time.LocalDate;
import java.util.Optional;


@Controller
@AllArgsConstructor
public class IndexController {


    @GetMapping({"/index", "/"})
    public String indexHandel() {

        return "index";
    }



}
