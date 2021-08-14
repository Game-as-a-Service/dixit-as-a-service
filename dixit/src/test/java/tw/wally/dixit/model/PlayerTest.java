package tw.wally.dixit.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Random;

import static java.lang.Integer.MIN_VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static tw.wally.dixit.model.Game.NUMBER_OF_PLAYER_HAND_CARDS;

public class PlayerTest extends AbstractDixitTest {

    private Player player;

    @BeforeEach
    public void beforeTest() {
        this.player = new Player(MIN_VALUE, DIXIT_PLAYER);
    }

    @Test
    public void WhenDealSixCardsToPlayer_ThenPlayerShouldHaveSixCards() {
        var cards = generateCards(6);

        player.addHandCards(cards);

        assertEquals(6, player.getHandCards().size());
    }

    @Test
    public void WhenDealOneCardToPlayer_ThenPlayerShouldHaveOneCard() {
        var cards = generateCards(1);

        player.addHandCards(cards);

        assertEquals(1, player.getHandCards().size());
    }

    @Test
    public void WhenDealTenCardsToPlayer_ThenShouldFail() {
        var cards = generateCards(10);

        assertThrows(IllegalArgumentException.class, () -> player.addHandCards(cards));
    }

    @Test
    public void GivenPlayerHasSixCards_WhenPlayOneHandCard_ThenPlayerShouldHaveFiveCards() {
        dealSixCards();

        playRandomCard();

        assertEquals(5, player.getHandCards().size());
    }

    @Test
    public void GivenPlayerHasSixCards_WhenPlayOneCardNotInHisHandCards_ThenShouldFail() {
        dealSixCards();

        assertThrows(IllegalArgumentException.class, () -> player.playCard(-1));
    }

    @Test
    public void WhenAddInvalidScores_ThenShouldFail() {
        assertThrows(IllegalArgumentException.class, () -> player.addScore(0));
        assertThrows(IllegalArgumentException.class, () -> player.addScore(4));
    }

    private void dealSixCards() {
        dealCards(generateCards(NUMBER_OF_PLAYER_HAND_CARDS));
    }

    private void dealCards(Collection<Card> cards) {
        player.addHandCards(cards);
    }

    private void playRandomCard() {
        var handCards = player.getHandCards();
        var card = handCards.get(new Random().nextInt(player.getHandCards().size()));
        player.playCard(card.getId());
    }
}