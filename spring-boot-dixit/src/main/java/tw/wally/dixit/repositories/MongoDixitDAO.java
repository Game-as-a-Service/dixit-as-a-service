package tw.wally.dixit.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tw.wally.dixit.repositories.entities.DixitData;

/**
 * @author - wally55077@gmail.com
 */
@Repository
public interface MongoDixitDAO extends MongoRepository<DixitData, String> {
    String DIXIT = "dixit";
}
