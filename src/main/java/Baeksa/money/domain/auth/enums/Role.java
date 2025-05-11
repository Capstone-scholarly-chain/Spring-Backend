package Baeksa.money.domain.auth.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum Role {
    @JsonProperty("ROLE_STUDENT")
    ROLE_STUDENT,

    @JsonProperty("ROLE_COMMITTEE")
    ROLE_COMMITTEE,

    @JsonProperty("ROLE_ADMIN")
    ROLE_ADMIN;
}

