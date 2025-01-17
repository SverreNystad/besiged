package com.softwarearchitecture.game_server.cards;

import java.io.Serializable;

import com.softwarearchitecture.ecs.Card;
import com.softwarearchitecture.ecs.components.CostComponent;
import com.softwarearchitecture.ecs.components.PlacedCardComponent;
import com.softwarearchitecture.ecs.components.SpriteComponent;

public class BowCard extends Card implements Serializable {

    public BowCard(PlacedCardComponent placedCardComponent, CostComponent costComponent,
            SpriteComponent spriteComponent) {
        super(placedCardComponent, costComponent, spriteComponent);
    }

}
