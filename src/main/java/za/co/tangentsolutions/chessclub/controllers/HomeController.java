package za.co.tangentsolutions.chessclub.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping(value = {"/", "/home", "index"})
    public String homePage(Model model) {
        return "Hello there!";
    }
    @GetMapping("/home2")
    public String index() {
        return "Greetings from Spring Boot!";
    }

}