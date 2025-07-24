package com.expense.tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class ExpenseTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExpenseTrackerApplication.class, args);
		System.out.println(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("abc123"));
	}

	@GetMapping("/test")
	public String test(){
		return "Expense Tracker Application is running";
	}
}
