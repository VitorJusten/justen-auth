package com.justen.auth.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 
 * @Author GitHub - VitorJusten
 * @ProjectName justen-auth
 * @Year 2026
 *
 */
@Controller
public class AuthenticationController {

	@GetMapping("/login")
	public String login() {
		return "login";
	}

}
