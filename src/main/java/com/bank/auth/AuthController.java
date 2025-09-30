package com.bank.auth;

import com.bank.model.User;
import com.bank.model.enums.Role;
import com.bank.model.request.UserRequest;
import com.bank.repository.UserRepository;
import com.bank.utils.DtoMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
	private final UserRepository userRepository;
    private final DtoMapper dtoMapper;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtUtil jwtUtil;


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
		user.setRole(Role.USER);
		user.setUserName(userRequest.getUserName());
		user.setFirstName(userRequest.getFirstName());
		user.setLastName(userRequest.getLastName());
		user.setBirthDate(userRequest.getBirthDate());
		user.setCreated(Instant.now());
		user.setModified(Instant.now());
		user.setBlocked(false);
		user.setProfilePicture("http//picture.jpg");

		userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getRole().toString(), user.getUserName());

        Map<String, Object> response = new HashMap<>();
        response.put("user", dtoMapper.toDto(user));
        response.put("token", token);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserRequest userRequest) {
        User user = userRepository.findByEmail(userRequest.getEmail());

        if (user == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Invalid email");
        }

        if(!passwordEncoder.matches(userRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid password");
        }

        if (user.getBlocked()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is blocked");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getRole().toString(), user.getUserName());

        Map<String, Object> response = new HashMap<>();
        response.put("user", dtoMapper.toDto(user));
        response.put("token", token);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest request) {
        Object userId = request.getAttribute("USER_ID");

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not signed in");
        }

        User user = userRepository.getUserById((Long) userId);
        return ResponseEntity.ok(dtoMapper.toDto(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        return ResponseEntity.ok("Logged out");
    }
}
