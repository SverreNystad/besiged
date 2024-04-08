package com.softwarearchitecture.game_server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Optional;

import org.junit.Test;

import com.softwarearchitecture.ecs.Entity;
import com.softwarearchitecture.ecs.components.CostComponent;
import com.softwarearchitecture.ecs.components.MoneyComponent;
import com.softwarearchitecture.ecs.components.PositionComponent;
import com.softwarearchitecture.game_server.CardFactory;
import com.softwarearchitecture.game_server.CardFactory.CardType;
import com.softwarearchitecture.math.Vector2;

public class CardFactoryTest {
    Vector2 position = new Vector2(0, 0);

    @Test
    public void testCreateCardNotNull() {
        assertNotNull("Card should not be null", CardFactory.createCard(CardType.ICE, position));
        assertNotNull("Card should not be null", CardFactory.createCard(CardType.FIRE, position));
    }

    @Test
    public void testCardAttributes() {
        Entity magicCard = CardFactory.createCard(CardType.MAGIC, position);
        MoneyComponent moneyComponent = magicCard.getComponent(MoneyComponent.class).get();

        assertEquals(" cost should be correct for MagicCard", 100, moneyComponent.getAmount());

        Entity bowCard = CardFactory.createCard(CardType.BOW, position);
        PositionComponent positionComponent = bowCard.getComponent(PositionComponent.class).get();
        assertEquals("Attack value should be correct for BattleCard", position, positionComponent.getPosition());

    }

    @Test
    public void testInvalidCardType() {
        try {
            CardFactory.createCard(null, position); // Assuming 'position' is defined elsewhere in your test class
            fail("Should have thrown an IllegalArgumentException for unknown card type");
        } catch (IllegalArgumentException e) {
            assertNotNull("Exception message should not be null", e.getMessage());
        }
    }
}