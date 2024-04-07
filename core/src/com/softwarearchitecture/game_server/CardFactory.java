package com.softwarearchitecture.game_server;

import java.util.ArrayList;
import java.util.List;

import javax.swing.text.Position;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.softwarearchitecture.ecs.Entity;
import com.softwarearchitecture.ecs.components.MoneyComponent;
import com.softwarearchitecture.ecs.components.PlacedCardComponent;
import com.softwarearchitecture.ecs.components.PositionComponent;
import com.softwarearchitecture.ecs.components.SoundComponent;
import com.softwarearchitecture.ecs.components.SpriteComponent;
import com.softwarearchitecture.math.Vector2;

public class CardFactory {

    public enum CardType {
        ICE,
        FIRE,
        TECHNOLOGY,
        LIGHTNING,
        BOW,
        MAGIC
    }

    public static Entity createCard(CardType type, Vector2 position) {
        String texture = "texturePath";
        Vector2 size = new Vector2(0.02f, 0.02f);
        int cost = 0;
        String sound = "soundPath";

        switch (type) {
            case ICE:
                texture = TexturePack.CARD_ICE;
                cost = 100;
                sound = AudioPack.JENS; // TODO: Add the correct sound path
                break;

            case FIRE:
                texture = TexturePack.CARD_FIRE;
                cost = 100;
                sound = AudioPack.JENS; // TODO: Add the correct sound path
                break;

            case TECHNOLOGY:
                texture = TexturePack.CARD_TECHNOLOGY;
                cost = 100;
                sound = AudioPack.JENS; // TODO: Add the correct sound path
                break;

            case LIGHTNING:
                texture = TexturePack.CARD_LIGHTNING;
                cost = 500;
                sound = AudioPack.JENS; // TODO: Add the correct sound path
                break;

            case BOW:
                texture = TexturePack.CARD_BOW;
                cost = 100;
                sound = AudioPack.JENS; // TODO: Add the correct sound path
                break;

            case MAGIC:
                texture = TexturePack.CARD_MAGIC;
                cost = 100;
                sound = AudioPack.JENS; // TODO: Add the correct sound path
                break;

            default:
                throw new IllegalArgumentException("Invalid card type");
        }

        PlacedCardComponent placedCardComponent = new PlacedCardComponent((int) position.x, (int) position.y, type);
        PositionComponent positionComponent = new PositionComponent(position);
        SpriteComponent spriteComponent = new SpriteComponent(texture, size, 0);
        SoundComponent soundComponent = new SoundComponent(sound);
        MoneyComponent moneyComponent = new MoneyComponent(cost);

        Entity cardEntity = new Entity();
        cardEntity.addComponent(PlacedCardComponent.class, placedCardComponent);
        cardEntity.addComponent(PositionComponent.class, positionComponent);
        cardEntity.addComponent(SpriteComponent.class, spriteComponent);
        cardEntity.addComponent(SoundComponent.class, soundComponent);
        cardEntity.addComponent(MoneyComponent.class, moneyComponent);

        return cardEntity;
    }

}