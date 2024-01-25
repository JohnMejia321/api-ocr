package com.inner.tesseract;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({ "com.inner.*" })
public class OcrTesseractApplication {

	public static void main(String[] args) {
		SpringApplication.run(OcrTesseractApplication.class, args);
	}

}
