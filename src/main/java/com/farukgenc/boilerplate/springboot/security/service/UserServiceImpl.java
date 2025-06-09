package com.farukgenc.boilerplate.springboot.security.service;

import com.farukgenc.boilerplate.springboot.service.UserValidationService;
import com.farukgenc.boilerplate.springboot.model.User;
import com.farukgenc.boilerplate.springboot.model.UserRole;
import com.farukgenc.boilerplate.springboot.security.dto.AuthenticatedUserDto;
import com.farukgenc.boilerplate.springboot.security.dto.RegistrationRequest;
import com.farukgenc.boilerplate.springboot.security.dto.RegistrationResponse;
import com.farukgenc.boilerplate.springboot.security.mapper.UserMapper;
import com.farukgenc.boilerplate.springboot.events.service.KafkaEventProducerService;
import com.farukgenc.boilerplate.springboot.repository.UserRepository;
import com.farukgenc.boilerplate.springboot.security.dto.UserUpdateRequest;
import com.farukgenc.boilerplate.springboot.utils.GeneralMessageAccessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Created on Ağustos, 2020
 *
 * @author Faruk
 */
import java.util.UUID; // For placeholder session ID

@Slf4j
@Service
public class UserServiceImpl implements UserService {

	private static final String REGISTRATION_SUCCESSFUL = "registration_successful";

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private UserValidationService userValidationService;

	@Autowired
	private GeneralMessageAccessor generalMessageAccessor;

	@Autowired
	private KafkaEventProducerService kafkaEventProducerService;

	@Override
	public User findByUsername(String username) {

		return userRepository.findByUsername(username);
	}

	@Override
	public RegistrationResponse registration(RegistrationRequest registrationRequest) {

		userValidationService.validateUser(registrationRequest);

		final User user = UserMapper.INSTANCE.convertToUser(registrationRequest);
		user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
		user.setUserRole(UserRole.USER);

		userRepository.save(user);

		final String username = registrationRequest.getUsername();
		final String registrationSuccessMessage = generalMessageAccessor.getMessage(null, REGISTRATION_SUCCESSFUL, username);

		log.info("{} registered successfully!", username);

		return new RegistrationResponse(registrationSuccessMessage);
	}

	@Override
	public AuthenticatedUserDto findAuthenticatedUserByUsername(String username) {

		final User user = findByUsername(username);

		return UserMapper.INSTANCE.convertToAuthenticatedUserDto(user);
	}

	@Override
	public User updateUser(String username, UserUpdateRequest userUpdateRequest) {
		User user = userRepository.findByUsername(username);
		if (user == null) {
			// Consider using a more specific exception, e.g., EntityNotFoundException
			throw new RuntimeException("User not found with username: " + username);
		}

		// Update user properties
		user.setEmail(userUpdateRequest.getEmail());
		// Add other updatable fields here if necessary

		User updatedUser = userRepository.save(user); // First save for the actual update
		log.info("User {}'s email updated to {}.", username, userUpdateRequest.getEmail());

		// --- Kafka Event Publishing ---
		// Assuming the user is performing this action on their own record.
		String actingUserId = updatedUser.getId().toString();
		// Session ID should ideally come from the current security context or request attributes.
		String sessionId = "session-placeholder-" + UUID.randomUUID().toString();

		// Use the current hash on the user record as the previous hash for the event.
		// If it's null (e.g. for a user created before DLT was implemented), use INITIAL_HASH.
		String previousMessageHashForEvent = updatedUser.getPreviousMessageHash() != null ?
				updatedUser.getPreviousMessageHash() : KafkaEventProducerService.INITIAL_HASH;

		String currentMessageHash = kafkaEventProducerService.sendUserEvent(
				updatedUser,
				"USER_UPDATED",
				previousMessageHashForEvent,
				actingUserId,
				sessionId
		);

		if (currentMessageHash != null) {
			updatedUser.setPreviousMessageHash(currentMessageHash);
			userRepository.save(updatedUser); // Second save to store the new DLT hash
			log.info("User {} updated and USER_UPDATED event sent. New previousMessageHash: {}", username, currentMessageHash);
		} else {
			log.error("User {}'s profile was updated, but failed to send USER_UPDATED Kafka event or retrieve the message hash. Manual reconciliation might be needed for DLT.", username);
			// Depending on business rules, you might throw an exception here,
			// trigger a compensating transaction, or add to a retry queue.
		}

		return updatedUser;
	}
}
