package tw.wally.dixit.repositories;

import tw.wally.dixit.model.Dixit;

import java.util.Optional;

/**
 * @author - wally55077@gmail.com
 */
public interface DixitRepository {

    Optional<Dixit> findDixitById(String id);

    Dixit save(Dixit dixit);

    void deleteAll();
}
