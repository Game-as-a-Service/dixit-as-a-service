package tw.wally.dixit.repositories;

import tw.wally.dixit.model.Card;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author - wally55077@gmail.com
 */
public interface CardRepository {

    String EMPTY_CARD_IMAGE = "";

    default List<Card> findAll() {
        return new ArrayList<>(findAllAsMap().values());
    }

    Map<Integer, Card> findAllAsMap();
}
