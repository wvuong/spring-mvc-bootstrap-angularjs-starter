package com.willvuong.bootstrapper.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created with IntelliJ IDEA.
 * User: will
 * Date: 11/23/13
 * Time: 3:14 PM
 */
@Controller
public class ErrorHandlerController {

    @RequestMapping("/error")
    public String errorView() {
        return "/WEB-INF/views/error.jsp";
    }

    @RequestMapping("/debug/ThrowException")
    @ResponseBody
    public String throwException(@RequestParam String really) {
        if ("true".equals(really)) {
            throw new RuntimeException("intentional exception throw to invoke error handler", new RuntimeException("causal exception"));
        }

        return "really=" + really;
    }
}
