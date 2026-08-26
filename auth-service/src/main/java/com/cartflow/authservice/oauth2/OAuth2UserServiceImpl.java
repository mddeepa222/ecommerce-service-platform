package com.cartflow.authservice.oauth2;

import com.cartflow.authservice.entity.Provider;
import com.cartflow.authservice.entity.User;
import com.cartflow.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuth2UserServiceImpl extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        userRepository.findByEmail(email).ifPresentOrElse(
                existingUser -> {
                    if (existingUser.getAvatarUrl() == null && picture != null) {
                        existingUser.setAvatarUrl(picture);
                        userRepository.save(existingUser);
                    }
                },
                () -> {
                    User newUser = User.builder()
                            .name(name)
                            .email(email)
                            .avatarUrl(picture)
                            .provider(Provider.GOOGLE)
                            .build();
                    userRepository.save(newUser);
                }
        );

        return oAuth2User;
    }
}
