package com.willvuong.bootstrapper.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created with IntelliJ IDEA.
 * User: will
 * Date: 11/23/13
 * Time: 12:37 PM
 */
@Controller
@RequestMapping("/")
public class HomeController {

    @RequestMapping
    public String home() {
        return "/WEB-INF/views/angular-index.jsp";
    }

}
