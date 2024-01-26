package com.inner.controller;

import com.inner.service.EmpleadorService;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class EmpleadorController {

	@Autowired
	private EmpleadorService empleadorService;

	@PostMapping("/upload")
	public String procesarFormulario(@RequestParam("DestinationLanguage") String destinationLanguage,
			@RequestParam("PdfFile") String pdfFileName) {
		try {
			return empleadorService.procesarFormulario(destinationLanguage, pdfFileName);
		} catch (TesseractException e) {
			System.err.println(e.getMessage());
			return "Error while performing OCR on the PDF file from MinIO";
		}
	}

	@PostMapping("/upload-file")
	public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
		try {
			String result = empleadorService.uploadFile(file);
			return ResponseEntity.ok(result);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file.");
		}
	}
}
