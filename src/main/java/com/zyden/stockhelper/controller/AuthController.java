package com.zyden.stockhelper.controller;

import com.zyden.stockhelper.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.security.Principal;

/**
 * Created by Kenny on 5/16/2017.
 */

@RestController
@RequestMapping("/api/v1/session")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @RequestMapping(method = RequestMethod.GET)
    public User session(Principal user) {
        String name = user == null ? null : user.getName();
        User u = new User();
        u.setUsername(name);
        return u;
    }

    @RequestMapping(method = RequestMethod.DELETE)
    public void logout(HttpSession session) {
        session.invalidate();
    }
}
