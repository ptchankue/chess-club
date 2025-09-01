package za.co.tangentsolutions.chessclub.controllers;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

public class HomeController {

    @GetMapping(value = {"/", "/home", "index"})
    public String homePage(Model model) {
        return "Hello there!";
    }
}