package com.web.flower.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.flower.domain.user.entity.UserEntity;
import com.web.flower.security.auth.UserEntityDetails;
import com.web.flower.security.domain.Message;
import com.web.flower.security.service.JwtService;
import com.web.flower.security.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

// 스프링 시큐리티에서 UsernamePasswordAuthenticationFilter 가 있다.
// /login 요청해서 username, password POST 전송하면 위 필터가 동작한다.
// 현재는 formLogin disable 했기 때문에 작동하지 않음.
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        System.out.println("JwtAuthenticationFilter: 로그인 요청 도착");

        try {
            ObjectMapper om = new ObjectMapper();
            UserEntity userEntity = om.readValue(request.getInputStream(), UserEntity.class);

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userEntity.getUsername(), userEntity.getPassword());

            // CustomUserDetailsService 의 loadUserByUsername() 함수 실행 - 인증객체에 사용 정보 담김
            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            return authentication;
        } catch (IOException e) {
            throw new AuthenticationServiceException("username, password 가 일치하지 않습니다.");
        }
    }

    // attemptAuthentication 실행 후, 인증이 정상적으로 되었다면 successfulAuthentication 함수가 실행된다.
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        System.out.println("successfulAuthentication 실행됨");
        UserEntityDetails userEntityDetails = (UserEntityDetails) authResult.getPrincipal();

        // Hash 암호화 방식
        String jwtToken = JwtService.createAccessToken(userEntityDetails);
        String refreshToken = JwtService.createRefreshToken(userEntityDetails);

        //refreshTokenService.save(refreshToken); // refresh Token DB에 저장

        response.addHeader(JwtProperties.JWT_HEADER,JwtProperties.TOKEN_PREFIX+jwtToken);
        response.addHeader(JwtProperties.REFRESH_HEADER, JwtProperties.TOKEN_PREFIX+refreshToken);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        System.out.println("unsuccessfulAuthentication 실행됨");

        Object exceptionType = failed.getClass();

        if(exceptionType.equals(BadCredentialsException.class) || exceptionType.equals(UsernameNotFoundException.class)){
            ObjectMapper om = new ObjectMapper();

            Message message = Message.builder()
                    .message("auth_fail")
                    .status(HttpStatus.UNAUTHORIZED)
                    .memo(failed.getLocalizedMessage())
                    .build();

            response.setContentType(MediaType.APPLICATION_JSON.toString());
            om.writeValue(response.getOutputStream(),message);
        }
    }
}
