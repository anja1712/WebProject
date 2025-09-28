package com.bank.auth;

import com.bank.model.User;
import com.bank.model.enums.Role;
import com.bank.model.request.UserRequest;
import com.bank.repository.UserRepository;
import com.bank.utils.DtoMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
	private final UserRepository userRepository;
    private final DtoMapper dtoMapper;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


	@PostMapping("/signup")
	public ResponseEntity<?> signup(@RequestBody UserRequest userRequest,
										 @RequestParam(defaultValue = "USER") String role,
										 HttpSession session) {
		if (userRepository.findByEmail(userRequest.getEmail()) != null) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already registered");
        }

		User user = new User();
		user.setEmail(userRequest.getEmail());
		user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
		user.setRole(Role.valueOf(role.toUpperCase()));
		user.setUserName(userRequest.getUserName());
		user.setFirstName(userRequest.getFirstName());
		user.setLastName(userRequest.getLastName());
		user.setBirthDate(userRequest.getBirthDate());
		user.setCreated(Instant.now());
		user.setModified(Instant.now());
		user.setBlocked(false);
		user.setProfilePicture("http//picture.jpg");

		userRepository.save(user);

		session.setAttribute("USER_ID", user.getId());
		session.setAttribute("USER_NAME", user.getUserName());
		session.setAttribute("ROLE", user.getRole());

		return ResponseEntity.ok(dtoMapper.toDto(user));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email,
                                        @RequestParam String password,
                                        HttpSession session) {

        User user = userRepository.findByEmail(email);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Invalid email");
        }

        if(!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid password");
        }

        if (user.getBlocked()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is blocked");
        }

        session.setAttribute("USER_ID", user.getId());
        session.setAttribute("ROLE", user.getRole());
        session.setAttribute("USER_NAME", user.getUserName());

        return ResponseEntity.ok(dtoMapper.toDto(user));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        Object userId = session.getAttribute("USER_ID");

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not signed in");
        }

        User user = userRepository.getUserById((Long) userId);

        return ResponseEntity.ok(dtoMapper.toDto(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("Logged out");
    }
}
