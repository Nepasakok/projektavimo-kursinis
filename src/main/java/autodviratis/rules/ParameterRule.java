package autodviratis.rules;

import java.util.Optional;

public interface ParameterRule<T> {

    Optional<T> extract(String text);
}
