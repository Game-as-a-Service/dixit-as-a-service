package tw.wally.dixit.repositories;

import lombok.AllArgsConstructor;
import tw.wally.dixit.model.Dixit;
import tw.wally.dixit.repositories.entities.DixitData;

import javax.inject.Named;
import java.util.Optional;

import static tw.wally.dixit.repositories.entities.DixitData.toData;

/**
 * @author - wally55077@gmail.com
 */
@Named
@AllArgsConstructor
public class MongoDixitRepository implements DixitRepository {

    private final MongoDixitDAO mongoDixitDAO;

    @Override
    public Optional<Dixit> findDixitById(String id) {
        return mongoDixitDAO.findById(id)
                .map(DixitData::toEntity);
    }

    @Override
    public Dixit save(Dixit dixit) {
        return mongoDixitDAO.save(toData(dixit)).toEntity();
    }

    @Override
    public void deleteAll() {
        mongoDixitDAO.deleteAll();;
    }
}
