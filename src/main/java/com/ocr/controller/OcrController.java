package com.ocr.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ocr.model.OcrModel;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import io.minio.MinioClient;
import io.minio.GetObjectArgs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@RestController
public class OcrController {

	@PostMapping("/api/ocr")
	public String DoOCR(@RequestParam("DestinationLanguage") String destinationLanguage,
			@RequestParam("PdfFile") String pdfFileName) throws TesseractException {
		ITesseract instance = new Tesseract();

		try {
			// Configurar cliente MinIO
			MinioClient minioClient = MinioClient.builder()
					.endpoint("http://localhost:9000") // Reemplazar con la URL de tu servidor MinIO
					.credentials("minioadmin", "minioadmin") // Reemplazar con tus credenciales
					.build();

			// Obtener el InputStream del archivo PDF desde MinIO
			InputStream pdfStream = minioClient.getObject(
					GetObjectArgs.builder()
							.bucket("my-bucket") // Reemplazar con el nombre de tu bucket
							.object(pdfFileName)
							.build());

			// Guardar temporalmente el PDF en el sistema de archivos local
			File pdfFile = File.createTempFile("temp-pdf", ".pdf");
			Files.copy(pdfStream, pdfFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

			// Realizar la operación de OCR en el archivo PDF local
			String result = instance.doOCR(pdfFile);

			// Eliminar el archivo PDF temporal
			pdfFile.delete();

			return result;
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return "Error while performing OCR on the PDF file from MinIO";
		}
	}
}
