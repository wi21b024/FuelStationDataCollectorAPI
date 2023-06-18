package com.example.invoicegeneratorapi;

import com.rabbitmq.client.Channel;
import javafx.scene.control.Alert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.rabbitmq.client.ConnectionFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@RestController
@RequestMapping("/invoices")
@Slf4j
public class InvoiceController {
    public static final String CUSTOMER_ID_CHANNEL = "CustomerIDChannel";
    private static Logger logger = LoggerFactory.getLogger(InvoiceController.class);

    @PostMapping("/{customerID}")
    public ResponseEntity<Void> generateInvoice(@PathVariable String customerID) {
        logger.info(customerID+" received!");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(30003);

        try {
            try (
                    com.rabbitmq.client.Connection connection = factory.newConnection();
                    Channel channel = connection.createChannel()
            ) {
                channel.queueDeclare(CUSTOMER_ID_CHANNEL, false, false, false, null);
                channel.basicPublish("", CUSTOMER_ID_CHANNEL, null, customerID.getBytes(StandardCharsets.UTF_8));
                logger.info("Published to queue " + CUSTOMER_ID_CHANNEL+ " message "+customerID);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{customerID}")
    public ResponseEntity<String> getInvoiceGenerationStatus(@PathVariable final String customerID) {
        String filePath = "invoice" + LocalDate.now() + ".pdf";
        File file = new File(filePath);

        if (file.exists() && !file.isDirectory()) {
            return ResponseEntity.ok(filePath);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("PDF nicht gefunden");
        }
    }

}

