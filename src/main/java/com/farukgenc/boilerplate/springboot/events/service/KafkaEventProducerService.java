package com.farukgenc.boilerplate.springboot.events.service;

import com.farukgenc.boilerplate.springboot.events.model.EventEnvelope;
import com.farukgenc.boilerplate.springboot.events.model.EventMetadata;
import com.farukgenc.boilerplate.springboot.events.model.EventPayload;
import com.farukgenc.boilerplate.springboot.events.model.KafkaEventMessage;
import com.farukgenc.boilerplate.springboot.model.User; // Assuming User is the entity
import com.farukgenc.boilerplate.springboot.utils.CryptographyUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.Base64;
import java.util.UUID;

@Service
public class KafkaEventProducerService {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private CryptographyUtils cryptographyUtils;

    @Autowired
    private ObjectMapper objectMapper; // For converting User object to JSON string

    @Value("${spring.kafka.template.default-topic}")
    private String defaultTopic;

    @Value("${app.service.id:transaction-service}") // Example service ID, configurable
    private String serviceId;


    // Defining a constant for an empty or initial hash
    public static final String INITIAL_HASH = "0000000000000000000000000000000000000000000000000000000000000000";


    public String sendUserEvent(User user, String eventType, String previousHashOverride, String userId, String sessionId) {
        try {
            // 1. Serialize the user data (payload)
            String userDataJson = objectMapper.writeValueAsString(user);
            String base64UserData = Base64.getEncoder().encodeToString(userDataJson.getBytes());

            // 2. Determine previousHash
            // For a new user (e.g. "CREATE" event), previousHash might be a special value (e.g. hash of user ID, or a constant)
            // For an updated user, it's the hash of the previous message for this user.
            // This will be passed from the calling service which has more context.
            String currentPreviousHash = (previousHashOverride != null) ? previousHashOverride : INITIAL_HASH;

            // 3. Construct the message components
            EventEnvelope envelope = EventEnvelope.builder()
                    .messageId(UUID.randomUUID().toString())
                    .timestamp(System.currentTimeMillis())
                    // sequenceNumber: This needs a strategy. For now, a timestamp derivative or a dedicated sequence generator.
                    // Let's use timestamp for simplicity for now, but this should be reviewed for uniqueness guarantee.
                    .sequenceNumber(System.currentTimeMillis() % 100000) // Simplified sequence
                    .userId(userId) // The user performing the action or related to the event
                    .serviceId(this.serviceId)
                    .sessionId(sessionId) // Session ID of the operation
                    .previousHash(currentPreviousHash)
                    // Signature will be calculated last, over the relevant parts of the message
                    .build();

            EventPayload<String> payload = EventPayload.<String>builder()
                    .encrypted(true) // As per requirement, though data is only Base64 encoded for now
                    .data(base64UserData)
                    .build();

            EventMetadata metadata = EventMetadata.builder()
                    .version("1.0")
                    .contentType("application/json")
                    .compressionAlgorithm("gzip") // Assuming gzip might be applied by Kafka client or broker, not explicitly here
                    .build();

            // Data to be signed: For DLT, typically you sign parts of envelope + payload hash
            // For simplicity here, let's sign: messageId + timestamp + payload.data + previousHash
            // A more robust approach would be to serialize a canonical representation of these fields.
            String dataToSign = envelope.getMessageId() + envelope.getTimestamp() + payload.getData() + envelope.getPreviousHash();
            String signature = cryptographyUtils.signWithED25519(dataToSign);
            envelope.setSignature(signature); // Set the signature back into the envelope

            KafkaEventMessage<String> eventMessage = KafkaEventMessage.<String>builder()
                    .envelope(envelope)
                    .payload(payload)
                    .metadata(metadata)
                    .build();

            // The key for the Kafka message could be the user ID or entity ID for partitioning
            String kafkaKey = user.getId().toString(); // Assuming User has getId() returning a suitable type

            // Calculate hash of the current message to be stored by the caller
            String dataToHash = envelope.getMessageId() + envelope.getTimestamp() + payload.getData() + envelope.getPreviousHash() + envelope.getSignature();
            String currentMessageHash = cryptographyUtils.generateSHA256Hash(dataToHash);

            kafkaTemplate.send(defaultTopic, kafkaKey, eventMessage).addCallback(
                result -> {
                    System.out.println("Sent " + eventType + " event for user " + user.getId() + " to Kafka topic " + defaultTopic);
                    // IMPORTANT: The logic to update user.setPreviousMessageHash() with the *new* hash
                    // (hash of *this* message) should be handled carefully, ideally in the calling service
                    // after this send is confirmed, within the same transaction as the User entity update.
                },
                ex -> {
                    System.err.println("Failed to send " + eventType + " event for user " + user.getId() + " to Kafka: " + ex.getMessage());
                    // Handle failure: logging, retry, dead-letter queue, etc.
                }
            );

            return currentMessageHash;

        } catch (JsonProcessingException e) {
            System.err.println("Error serializing user data to JSON for Kafka event: " + e.getMessage());
            // Handle error appropriately
            return null;
        } catch (Exception e) {
            System.err.println("An unexpected error occurred while sending Kafka event: " + e.getMessage());
            // Handle error appropriately
            return null;
        }
    }
}
