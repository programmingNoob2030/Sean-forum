package sim.forum.dto.post.content;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PostContentNodeType {
    TEXT("text"),
    IMAGE("image");

    private final String value;

    PostContentNodeType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static PostContentNodeType fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (PostContentNodeType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }
}
