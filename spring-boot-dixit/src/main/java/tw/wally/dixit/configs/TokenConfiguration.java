package tw.wally.dixit.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tw.wally.dixit.services.JwtTokenService;
import tw.wally.dixit.services.TokenService;

/**
 * @author - wally55077@gmail.com
 */
@Configuration
public class TokenConfiguration {

    @Bean
    public TokenService tokenService(@Value("${jwt.secret}") String secret) {
        return new JwtTokenService(secret);
    }

}
