import java.util.Objects;

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
    // Getter methods for the key and value fields.
    public String getKey() {
        return key;
    }
    // Getter method for the value field.
    public String getValue() {
        return value;
    }
    // Setter method for the value field.
    public void setValue(String value) {
        this.value = value;
    }
    
    // Override the equals and hashCode methods to ensure immutability and uniqueness of keys.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tuple tuple = (Tuple) o;
        return Objects.equals(key, tuple.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }
}

    