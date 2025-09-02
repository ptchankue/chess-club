package za.co.tangentsolutions.chessclub.controllers;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping(value = {"/test", "/home", "index"})
    public String homePage(Model model) {
        return "Hello there!";
    }

}