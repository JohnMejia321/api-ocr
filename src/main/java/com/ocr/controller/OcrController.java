package com.ocr.controller;

import com.hazelcast.jet.Jet;
import com.hazelcast.jet.pipeline.BatchStage;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.test.TestSources;
import com.hazelcast.jet.json.JsonUtil;
import com.hazelcast.core.HazelcastJsonValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
import java.util.Properties;
import java.util.AbstractMap.SimpleEntry;

import com.hazelcast.jet.kafka.KafkaSinks;
import org.apache.kafka.common.serialization.StringSerializer;

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
			String ocrResult = instance.doOCR(pdfFile);

			// Eliminar el archivo PDF temporal
			pdfFile.delete();

			// Convertir el resultado a JSON usando Hazelcast Jet
			Pipeline p = Pipeline.create();

			BatchStage<String> source = p.readFrom(TestSources.items(ocrResult));

			BatchStage<SimpleEntry<Object, Object>> transformed = source.map(item -> {
				String jsonString = JsonUtil.toJson(item);
				HazelcastJsonValue jsonValue = new HazelcastJsonValue(jsonString);
				return new SimpleEntry<>(null, jsonValue);
			});

			// Configurar las propiedades de Kafka
			Properties props = new Properties();
			props.setProperty("bootstrap.servers", "localhost:9092"); // Reemplazar con la URL de tu servidor Kafka
			props.setProperty("key.serializer", StringSerializer.class.getCanonicalName());
			props.setProperty("value.serializer", StringSerializer.class.getCanonicalName());

			// Escribe el resultado en el tópico de Kafka
			transformed.writeTo(KafkaSinks.kafka(props, "topic1")); // Reemplazar con el nombre de tu tópico

			// Ejecutar el pipeline
			Jet.newJetInstance();

			// Devolver el resultado de OCR como cadena
			return ocrResult;
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return "Error while performing OCR on the PDF file from MinIO";
		}
	}
}
