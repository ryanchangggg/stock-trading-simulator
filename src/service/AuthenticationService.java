package service;

import model.user.User;
import repository.UserRepository;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Handles user registration and authentication.
 * <p>
 * Passwords are hashed using SHA-256 before storage.
 * The service depends only on {@link UserRepository} — no direct
 * database access.
 */
public class AuthenticationService {

    private final UserRepository userRepository;
    private long nextUserId = 1;

    public AuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Result<Session> register(String username, String password) {
        if (username == null || username.isBlank())
            return Result.failure("Username is required");
        if (password == null || password.length() < 4)
            return Result.failure("Password must be at least 4 characters");
        if (userRepository.findByUsername(username).isPresent())
            return Result.failure("Username already exists");

        String hash = hashPassword(password);
        User user = new User(nextUserId++, username, hash);
        userRepository.save(user);
        return Result.success(new Session(user));
    }

    public Result<Session> login(String username, String password) {
        if (username == null || username.isBlank())
            return Result.failure("Username is required");
        if (password == null || password.isBlank())
            return Result.failure("Password is required");

        Optional<User> opt = userRepository.findByUsername(username);
        if (opt.isEmpty())
            return Result.failure("Invalid username or password");

        User user = opt.get();
        if (!user.getPasswordHash().equals(hashPassword(password)))
            return Result.failure("Invalid username or password");

        return Result.success(new Session(user));
    }

    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
