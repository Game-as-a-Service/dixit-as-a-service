package tw.wally.dixit.repositories;

import tw.wally.dixit.model.Card;

import java.util.List;

/**
 * @author - wally55077@gmail.com
 */
public interface CardRepository {
    List<Card> findAll();
}
