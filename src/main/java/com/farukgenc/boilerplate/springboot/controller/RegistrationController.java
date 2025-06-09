package com.farukgenc.boilerplate.springboot.controller;

import com.farukgenc.boilerplate.springboot.events.service.KafkaEventProducerService;
import com.farukgenc.boilerplate.springboot.model.User;
import com.farukgenc.boilerplate.springboot.repository.UserRepository;
import com.farukgenc.boilerplate.springboot.security.dto.RegistrationRequest;
import com.farukgenc.boilerplate.springboot.security.dto.RegistrationResponse;
import com.farukgenc.boilerplate.springboot.security.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Created on Ağustos, 2020
 *
 * @author Faruk
 */
@CrossOrigin
@RestController
@RequestMapping("/register")
public class RegistrationController {

	@Autowired
	private UserService userService;

	@Autowired
	private KafkaEventProducerService kafkaEventProducerService;

	@Autowired
	private UserRepository userRepository; // To re-save user with hash

	// CryptographyUtils is used by KafkaEventProducerService, not directly here for previousMessageHash.
	// However, keeping it if other direct crypto ops were intended by the prompt's shell script.
	// @Autowired
	// private CryptographyUtils cryptographyUtils;

	@PostMapping
	public ResponseEntity<RegistrationResponse> registrationRequest(@Valid @RequestBody RegistrationRequest registrationRequest) {

		final RegistrationResponse registrationResponse = userService.registration(registrationRequest);

		// Assuming registrationResponse contains the ID of the created user or enough info to fetch the user
		// Or, ideally, userService.registration would return the User object.
		// For this example, let's fetch the user by username from the request,
		// assuming registration was successful if no exception was thrown.
		User user = userRepository.findByUsername(registrationRequest.getUsername()).orElse(null);

		if (user != null) {
			// Define a session ID - this should come from actual session management or context
			String sessionIdForEvent = "session-placeholder-" + UUID.randomUUID().toString();
			String userIdForEvent = user.getId().toString();

			String currentMessageHash = kafkaEventProducerService.sendUserEvent(
					user,
					"USER_CREATED",
					KafkaEventProducerService.INITIAL_HASH, // Previous hash for a new user
					userIdForEvent,
					sessionIdForEvent
			);

			if (currentMessageHash != null) {
				user.setPreviousMessageHash(currentMessageHash);
				userRepository.save(user); // Save again to update the hash
				System.out.println("User " + user.getUsername() + " updated with previousMessageHash: " + currentMessageHash);
			} else {
				// Log failure to send event or get hash
				System.err.println("Failed to send Kafka event for USER_CREATED or obtain message hash for user: " + user.getUsername());
				// Potentially, this could be a critical failure if DLT linking is mandatory.
				// Application might choose to roll back user creation or mark user for reconciliation.
			}
		} else {
			System.err.println("Could not find user " + registrationRequest.getUsername() + " after registration to send Kafka event.");
			// This case implies an issue with user creation or retrieval logic.
		}

		return ResponseEntity.status(HttpStatus.CREATED).body(registrationResponse);
	}

}
