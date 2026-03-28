package net.shailev.customer_service_hub.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AliveController {

    @GetMapping("/alive")
    public String alive() {
        return "alive";
    }
}
