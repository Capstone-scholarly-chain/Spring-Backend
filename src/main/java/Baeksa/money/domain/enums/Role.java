package Baeksa.money.domain.enums;

public enum Role {
    STUDENT("STUDENT"),
    COMMITTEE("COMMITTEE");

    private final String key;

    Role(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}

