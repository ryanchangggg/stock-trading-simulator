package service;

import model.user.User;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents an authenticated user session.
 * <p>
 * Created when a user successfully logs in or registers,
 * and provides access to the authenticated user's identity
 * throughout the application.
 */
public class Session {
    private final User user;
    private final LocalDateTime loginTime;

    public Session(User user) {
        this.user = Objects.requireNonNull(user);
        this.loginTime = LocalDateTime.now();
    }

    public User getUser() { return user; }
    public long getUserId() { return user.getId(); }
    public String getUsername() { return user.getUsername(); }
    public LocalDateTime getLoginTime() { return loginTime; }
}
