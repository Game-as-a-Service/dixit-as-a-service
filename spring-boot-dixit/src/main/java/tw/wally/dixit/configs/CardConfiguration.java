package tw.wally.dixit.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tw.wally.dixit.repositories.CardRepository;
import tw.wally.dixit.repositories.ResourceCardRepository;

/**
 * @author - wally55077@gmail.com
 */
@Configuration
public class CardConfiguration {

    @Bean
    public CardRepository cardRepository() {
        return new ResourceCardRepository();
    }
}
