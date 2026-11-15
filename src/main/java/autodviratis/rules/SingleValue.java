package autodviratis.rules;

import java.util.List;
import java.util.Optional;

final class SingleValue {

    private SingleValue() {
    }
    static <T> Optional<T> pick(List<T> values) {
        if (values.isEmpty()) {
            return Optional.empty();
        }
        T first = values.get(0);
        for (T value : values) {
            if (!value.equals(first)) {
                return Optional.empty();
            }
        }
        return Optional.of(first);
    }
}
