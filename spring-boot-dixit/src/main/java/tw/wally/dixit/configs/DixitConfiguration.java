package tw.wally.dixit.configs;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import tw.wally.dixit.model.Dixit;
import tw.wally.dixit.repositories.CardRepository;
import tw.wally.dixit.repositories.DixitRepository;
import tw.wally.dixit.repositories.MongoDixitDAO;
import tw.wally.dixit.repositories.MongoDixitRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static tw.wally.dixit.consts.Profiles.PROD;

/**
 * @author - wally55077@gmail.com
 */
@Configuration
public class DixitConfiguration {

    @Bean
    @Profile(PROD)
    public DixitRepository mongoDixitRepository(MongoDixitDAO mongoDixitDAO,
                                                CardRepository cardRepository) {
        return new MongoDixitRepository(mongoDixitDAO, cardRepository);
    }

    @Bean
    @ConditionalOnMissingBean(DixitRepository.class)
    public DixitRepository fakeDixitRepository() {
        return new DixitRepository() {

            private final Map<String, Dixit> dixitRepository = new HashMap<>();

            @Override

            public Optional<Dixit> findDixitById(String id) {
                return ofNullable(dixitRepository.get(id));
            }

            @Override
            public Dixit save(Dixit dixit) {
                return dixitRepository.put(dixit.getId(), dixit);
            }

            @Override
            public void deleteAll() {
                dixitRepository.clear();
            }
        };
    }
}
