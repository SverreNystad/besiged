package com.softwarearchitecture.game_client.states;

import java.util.UUID;

import com.softwarearchitecture.ecs.systems.InputSystem;
import com.softwarearchitecture.ecs.systems.RenderingSystem;
import com.softwarearchitecture.ecs.ECSManager;
import com.softwarearchitecture.ecs.Entity;
import com.softwarearchitecture.ecs.components.ButtonComponent.ButtonEnum;
import com.softwarearchitecture.ecs.components.PositionComponent;
import com.softwarearchitecture.ecs.components.SpriteComponent;
import com.softwarearchitecture.game_client.Controllers;
import com.softwarearchitecture.game_client.TexturePack;
import com.softwarearchitecture.math.Vector2;

public class Tutorial extends State implements Observer {

    protected Tutorial(Controllers defaultControllers, UUID yourId) {
        super(defaultControllers, yourId);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onAction(ButtonEnum type) {
        // TODO Auto-generated method stub
        switch (type) {
            case BACK:
                screenManager.nextState(new Menu(defaultControllers, yourId));
                break;
            default:
                break;
        }
    }

    @Override
    protected void activate() {

        // set background image
        String backgroundPath = TexturePack.BACKGROUND_TUTORIAL;
        SpriteComponent backgroundSprite = new SpriteComponent(backgroundPath, new Vector2(1, 1));
        PositionComponent backgroundPosition = new PositionComponent(new Vector2(0f, 0f), -1);
        Entity background = new Entity();
        background.addComponent(SpriteComponent.class, backgroundSprite);
        background.addComponent(PositionComponent.class, backgroundPosition);
        ECSManager.getInstance().addLocalEntity(background);

        // add buttons
        ButtonFactory.createAndAddButtonEntity(ButtonEnum.BACK, new Vector2(0.5f - 0.30f / 2f, 0.10f),
                new Vector2(0.30f, 0.10f), this, 0);

        // Add systems to the ECSManager
        RenderingSystem renderingSystem = new RenderingSystem(defaultControllers.graphicsController);
        InputSystem inputSystem = new InputSystem(defaultControllers.inputController);
        ECSManager.getInstance().addSystem(renderingSystem);
        ECSManager.getInstance().addSystem(inputSystem);
    }

}
