package tw.wally.dixit.repositories;

import tw.wally.dixit.model.Card;

import java.util.List;

/**
 * @author - wally55077@gmail.com
 */
public interface CardRepository {

    String EMPTY_CARD_IMAGE = "";

    List<Card> findAll();
}
