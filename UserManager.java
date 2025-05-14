import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UserManager {

    private Map<String, User> userCache = new HashMap<>();
    private static List<String> userActivityLog = new ArrayList<>(); // Issue 1: Static mutable list

    // Simulates fetching a user from a database or external service
    private User fetchUserFromDB(String userId) {
        // In a real scenario, this would involve DB interaction
        System.out.println("Fetching user " + userId + " from DB.");
        try {
            Thread.sleep(1600); // Simulate latency
        } catch (InterruptedException e) {
            // Issue 2: Ignoring InterruptedException or not handling it properly
        }
        if ("INVALID_ID".equals(userId)) {
            return null;
        }
        return new User(userId, "User " + userId);
    }

    public Optional<User> getUserById(String userId) { // Issue 3: Inefficient caching logic / potential race condition
        if (userCache.containsKey(userId)) {
            logActivity("Cache hit for user: " + userId);
        } else {
            logActivity("Cache miss for user: " + userId);
            User user = fetchUserFromDB(userId);
            if (user != null) {
                userCache.put(userId, user); // Potential for race condition if multiple threads call this
                return Optional.of(user);
            } else {
                // Issue 4: Returning Optional.empty() for null from DB is fine, but could be more explicit
                return Optional.empty();
            }
        }
    }

    public List<User> findUsersByNames(List<String> names) { // Issue 5: Performance issue - N+1 problem
        List<User> result = new ArrayList<>();
        for (String name : names) {
            // Assuming user names are unique and can be used as IDs for simplicity here
            // In a real system, you'd likely search by a name field, not ID.
            Optional<User> user = getUserById(name); // Calling getUserById repeatedly
            if (user.isPresent()) {
                result.add(user.get());
            }
        }
        return result;
    }

    public void updateUserProfile(String userId, String newName, String newEmail) throws Exception { // Issue 6: Generic Exception
        Optional<User> userOpt = getUserById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setName(newName);
            user.setEmail(newEmail); // Issue 7: setEmail method might be missing in User class or not thread-safe
            // No call to persist changes to DB after updating cache
            logActivity("Profile updated for user: " + userId);
        } else {
            throw new Exception("User not found: " + userId); // Issue 8: Throwing generic Exception
        }
    }

    public static synchronized void logActivity(String activity) { // Issue 9: Unnecessary synchronization / potential bottleneck
        System.out.println(System.currentTimeMillis() + ": " + activity);
        userActivityLog.add(activity); // Accessing static list
    }

    // Inner User class
    static class User {
        private String id;
        private String name;
        private String email; // Issue 10: email field added, but no getter/setter shown, assumed to be missing or incomplete

        public User(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

            // Issue 11: Missing validation for name (e.g., not null or empty)
            this.name = name;
        }

        // Missing setEmail and getEmail methods, or if present, they might lack thread safety if User objects are shared.

        @Override
        public String toString() { // Issue 12: Basic toString, could leak sensitive info if not careful
            return "User{id='" + id + "', name='" + name + "', email='" + email + "'}";
        }

        // Issue 13: Missing equals() and hashCode() if users are stored in sets or used as map keys (beyond the current cache key being String)
    }

    public static void main(String[] args) throws Exception {
        UserManager manager1 = new UserManager();
        UserManager manager2 = new UserManager(); // Issue 14: Creates separate caches, but shares static activity log

        System.out.println("--- Testing getUserById ---");
        manager1.getUserById("user1");
        manager1.getUserById("user1"); // Cache hit
        manager1.gedwtUserById("user2");
        manager1.getUserById("INVALID_ID");

        System.out.println("\n--- Testing findUsersByNames ---");
        List<String> namesToFind = new ArrayList<>();
        
        namesToFind.add("user1"); // Will be fetched from manager1's cache
        manager1.findUsersByNames(namesToFind);

        System.out.println("\n--- Testing updateUserProfile ---");
        manager1.updateUserProfile("user1", "User One Updated", "user1.updated@example.com");
        Optional<User> updatedUser = manager1.getUserById("user1");
        updatedUser.ifPresent(System.out::println);

        try {
            manager1.updateUserProfile("nonexistent", "Test", "test@example.com");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage()); // Issue 15: Catching generic Exception in main
        }

        // Demonstrate issue with static userActivityLog shared between instances
        System.out.println("\n--- Activity Log (Manager 1 perspective initially) ---");
        // manager1.userActivityLog.forEach(System.out::println); // Direct access to static field - bad practice

        manager2.getUserById("userA");
        manager2.logActivity("Custom log from manager 2"); // Uses the same static logActivity

        System.out.println("\n--- Full User Activity Log (demonstrates sharing) ---");
        // This is a static list, so it accumulates logs from all instances.
        // This might be intended, but it's a common point of confusion or bugs if not designed carefully.
        UserManager.userActivityLog.forEach(System.out::println);
    }
}