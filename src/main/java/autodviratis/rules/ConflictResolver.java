package autodviratis.rules;

import java.util.Optional;

public class ConflictResolver {
    public <T> Optional<T> resolve(Optional<T> fromTitle, Optional<T> fromDescription) {
        if (fromTitle.isEmpty()) {
            return fromDescription;
        }
        if (fromDescription.isEmpty()) {
            return fromTitle;
        }
        if (fromTitle.equals(fromDescription)) {
            return fromTitle;
        }
        return Optional.empty();
    }
}
