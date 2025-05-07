
public class Tuple {
    // The key of the tuple, declared as final to ensure immutability after initialization.
    // This enforces the protocol's requirement that keys must be unique in the tuple space.
    private final String key;
    // The value associated with the key. This field is mutable via the setValue method.
    // Note: The current protocol disallows updating existing keys via PUT operations.
    // This mutability may be reserved for future extensions (e.g., administrative overrides).
    private String value;

    public Tuple(String key, String value) {
        this.key = key;
        this.value = value;
    }
}

    