package com.backend;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RegisterReflectionForBinding({
		com.backend.codechef.CodechefApiResponse.class,
		com.backend.codechef.CodechefContest.class
})
public class MyspaceApplication {
	public static void main(String[] args) {
		SpringApplication.run(MyspaceApplication.class, args);
	}
}