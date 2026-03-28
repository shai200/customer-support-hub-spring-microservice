package net.shailev.customer_service_hub;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public class AliveController {

    @RequestMapping(method = RequestMethod.GET, path = "/alive")
    public String alive() {
        return "alive";
    }
}
