package com.payinvariants;

import org.springframework.boot.SpringApplication;

public class TestPayInvariantsApplication {

	public static void main(String[] args) {
		SpringApplication.from(PayInvariantsApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
