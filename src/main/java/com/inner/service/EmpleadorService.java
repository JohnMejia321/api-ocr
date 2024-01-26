package com.inner.service;

import com.hazelcast.jet.Jet;
import com.hazelcast.jet.pipeline.BatchStage;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.test.TestSources;
import com.hazelcast.jet.json.JsonUtil;
import com.hazelcast.core.HazelcastJsonValue;
import com.hazelcast.jet.kafka.KafkaSinks;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.TesseractException;

import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.clients.producer.ProducerRecord;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.AbstractMap.SimpleEntry;
import java.util.Properties;

@Service
public class EmpleadorService {

    @Autowired
    private MinioClient minioClient;

    // @Autowired
    // private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ITesseract tesseract;

    public String procesarFormulario(String destinationLanguage, String pdfFileName) throws TesseractException {
        try {
            InputStream pdfStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket("my-bucket")
                            .object(pdfFileName)
                            .build());

            File pdfFile = File.createTempFile("temp-pdf", ".pdf");
            Files.copy(pdfStream, pdfFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            String ocrResult = tesseract.doOCR(pdfFile);

            pdfFile.delete();

            Pipeline p = Pipeline.create();

            BatchStage<String> source = p.readFrom(TestSources.items(ocrResult));

            BatchStage<SimpleEntry<Object, Object>> transformed = source.map(item -> {
                String jsonString = JsonUtil.toJson(item);
                HazelcastJsonValue jsonValue = new HazelcastJsonValue(jsonString);
                return new SimpleEntry<>(null, jsonValue);
            });

            Properties props = new Properties();
            props.setProperty("bootstrap.servers", "localhost:9092"); // Reemplazar con la URL de tu servidor Kafka
            props.setProperty("key.serializer", StringSerializer.class.getCanonicalName());
            props.setProperty("value.serializer", StringSerializer.class.getCanonicalName());

            transformed.writeTo(KafkaSinks.kafka(props, r -> new ProducerRecord<>("topic1", null, r.getValue())));

            Jet.newJetInstance();

            return ocrResult;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return "Error while performing OCR on the PDF file from MinIO";
        }
    }

    public String uploadFile(MultipartFile file) {
        try {
            InputStream inputStream = file.getInputStream();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket("my-bucket")
                    .object(file.getOriginalFilename())
                    .stream(inputStream, inputStream.available(), -1)
                    .build());
            return "File uploaded successfully.";
        } catch (Exception e) {
            return "Failed to upload file.";
        }
    }
}
