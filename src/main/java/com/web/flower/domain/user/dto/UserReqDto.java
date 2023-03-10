package com.web.flower.domain.user.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserReqDto {

    private UUID id;
    private String username;
    private String password;
    private String profileName;
    private int profileAge;

    @Builder
    @Data
    public static class ReqSignUp{
        private String username;
        private String password;
        private String profileName;
        private int profileAge;
    }

    @Builder
    @Data
    public static class ReqDeleteOne{
        private String username;
        private String password;
    }

}
