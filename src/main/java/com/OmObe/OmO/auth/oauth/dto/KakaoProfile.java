package com.OmObe.OmO.auth.oauth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

// 카카오 사용자 정보 dto
@Data
public class KakaoProfile {
    public Long id;
    public String connected_at;
    public Properties properties;
    public KakaoAccount kakao_account;

    @Data
    public class Properties{
        public String nickname;
        public String profile_image;
        public String thumbnail_image;
    }

    @Data
    public class KakaoAccount {
        public Boolean profile_needs_agreement;
        public Profile profile;
        public Boolean has_email;
        public Boolean email_needs_agreement;
        public Boolean is_email_valid;
        public Boolean is_email_verified;
        public String email;

        @Data
        public class Profile{
            public String nickname;
            public String thumbnail_image_url;
            public String profile_image_url;
        }
    }
}
