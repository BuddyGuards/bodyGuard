package org.buddyguard.bodyguard.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

@Slf4j
@AllArgsConstructor
@Controller
@RequestMapping("/group")
public class GroupController {

    @GetMapping("/create")
    public String createHandle(Model model) {

        // html에서 세팅해야하는 정보가 뭐에요?

        return "group/create";
    }







}
