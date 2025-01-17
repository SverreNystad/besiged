package com.softwarearchitecture.ecs.systems;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.softwarearchitecture.ecs.ComponentManager;
import com.softwarearchitecture.ecs.ECSManager;
import com.softwarearchitecture.ecs.Entity;
import com.softwarearchitecture.ecs.GraphicsController;
import com.softwarearchitecture.ecs.System;
import com.softwarearchitecture.ecs.components.EnemyComponent;
import com.softwarearchitecture.ecs.components.HealthComponent;
import com.softwarearchitecture.ecs.components.MoneyComponent;
import com.softwarearchitecture.ecs.components.PathfindingComponent;
import com.softwarearchitecture.ecs.components.PlayerComponent;
import com.softwarearchitecture.ecs.components.PositionComponent;
import com.softwarearchitecture.ecs.components.SpriteComponent;
import com.softwarearchitecture.ecs.components.TextComponent;
import com.softwarearchitecture.ecs.components.TileComponent;
import com.softwarearchitecture.ecs.components.VelocityComponent;
import com.softwarearchitecture.ecs.components.VillageComponent;
import com.softwarearchitecture.ecs.components.WaveComponent;
import com.softwarearchitecture.game_client.states.GameOverObserver;
import com.softwarearchitecture.game_server.EnemyFactory;
import com.softwarearchitecture.game_server.EnemyFactory.EnemyType;
import com.softwarearchitecture.game_server.Tile;
import com.softwarearchitecture.game_server.TileType;
import com.softwarearchitecture.math.Vector2;
import com.softwarearchitecture.math.Vector3;

/**
 * The {@code EnemySystem} class is responsible for managing enemy behaviors within the game, 
 * including their movement along paths, health management, and interactions with other entities 
 * such as the player's village. It handles the spawning of enemies, their removal upon reaching 
 * the end of a path or being defeated, and updates the game state based on these events.
 *
 * <p>Enemies that reach the endpoint of a path will deal damage to the village, potentially
 * leading to game over conditions if the village's health reaches zero. The system also manages
 * wave timing, spawning enemies in waves, and increasing difficulty with each wave.</p>
 *
 * <p>This system also interacts with various other components and managers to update game state,
 * display health, manage money rewards for defeating enemies, and other gameplay elements.</p>
 */
public class EnemySystem implements System {
    private ComponentManager<PositionComponent> positionManager;
    private ComponentManager<VelocityComponent> velocityManager;
    private ComponentManager<PathfindingComponent> pathfindingManager;
    private ComponentManager<SpriteComponent> drawableManager;
    private ComponentManager<TileComponent> tileManager;
    private ComponentManager<HealthComponent> healthManager;
    private ComponentManager<TextComponent> textManager;
    private ComponentManager<MoneyComponent> moneyManager;
    private ComponentManager<EnemyComponent> enemyManager;
    private ComponentManager<PlayerComponent> playerManager;
    private ComponentManager<VillageComponent> villageManger;   
    private ComponentManager<WaveComponent> WaveManager;
    private GameOverObserver gameOverObserver;
    private GraphicsController graphicsController;
    private List<Tile> path = null;
    private Entity mob;
    private int villageDamage;
    private boolean firstUpdate = true;
    
    private Entity waveEntity = null;
    private Entity waveNumberEntity;

    public EnemySystem() {
        this.positionManager = ECSManager.getInstance().getOrDefaultComponentManager(PositionComponent.class);
        this.velocityManager = ECSManager.getInstance().getOrDefaultComponentManager(VelocityComponent.class);
        this.drawableManager = ECSManager.getInstance().getOrDefaultComponentManager(SpriteComponent.class);
        this.pathfindingManager = ECSManager.getInstance().getOrDefaultComponentManager(PathfindingComponent.class);
        this.tileManager = ECSManager.getInstance().getOrDefaultComponentManager(TileComponent.class);
        this.healthManager = ECSManager.getInstance().getOrDefaultComponentManager(HealthComponent.class);
        this.moneyManager = ECSManager.getInstance().getOrDefaultComponentManager(MoneyComponent.class);
        this.textManager = ECSManager.getInstance().getOrDefaultComponentManager(TextComponent.class);
        this.playerManager = ECSManager.getInstance().getOrDefaultComponentManager(PlayerComponent.class);
        this.enemyManager = ECSManager.getInstance().getOrDefaultComponentManager(EnemyComponent.class);
        this.villageManger = ECSManager.getInstance().getOrDefaultComponentManager(VillageComponent.class);
        this.WaveManager = ECSManager.getInstance().getOrDefaultComponentManager(WaveComponent.class);
    
        WaveComponent waveComponent = new WaveComponent(1, 10, 0, 20f, 5);
        Entity waveEntity = new Entity();
        waveEntity.addComponent(WaveComponent.class, waveComponent);
        ECSManager.getInstance().addLocalEntity(waveEntity);
        this.waveEntity = waveEntity;
    }

    public EnemySystem(GameOverObserver gameOverObserver) {
        this();
        this.gameOverObserver = gameOverObserver;
    }

    /**
     * Updates the enemy system, processing each enemy entity to manage movements, health, 
     * and interactions with the game world, such as reaching the village or being defeated.
     * Also manages spawning of new enemies based on timing and wave conditions.
     *
     * @param entities   the set of all entities to be processed this update cycle
     * @param deltaTime  the time elapsed since the last update, used for timing events like spawning
     */
    @Override
    public void update(Set<Entity> entities, float deltaTime) {
        
        // Get the village entity
        Entity village = null;
        for (Entity entity : entities) {
            Optional<HealthComponent> healthComponent = healthManager.getComponent(entity);
            Optional<VillageComponent> villageComponent = villageManger.getComponent(entity);
            if (villageComponent.isPresent() && healthComponent.isPresent()) {  
                village = entity;
                break;
            }
        }
        // Get the wave entity
        WaveComponent wave = WaveManager.getComponent(this.waveEntity).get();

        // Set this.village to the village entity, but only once
        if (firstUpdate == true) {
            initializeWaveNumberDisplay(wave.waveNumber);
            firstUpdate = false;
        }

        // Get tile size so that enemies follow the path correctly
        Vector2 tileSize = new Vector2(0, 0);
        for (Entity entity : entities) {
            if (path == null) {
                Optional<PathfindingComponent> possiblePath = pathfindingManager.getComponent(entity);
                if (possiblePath.isPresent()) {
                    path = possiblePath.get().path;
                }
            }

            Optional<SpriteComponent> sprite = drawableManager.getComponent(entity);
            Optional<TileComponent> tile = tileManager.getComponent(entity);
            if (sprite.isPresent() && tile.isPresent()) {
                tileSize = sprite.get().size_uv;
                continue;
            }
        }

        /* If the path is not initialized correctly escape early */
        if (path == null) {
            return;
        }
        // Check if any enemies have reached the end of the path
        for (Entity entity : entities) {
            Optional<PositionComponent> position = positionManager.getComponent(entity);
            Optional<VelocityComponent> velocity = velocityManager.getComponent(entity);
            Optional<PathfindingComponent> pathfinding = pathfindingManager.getComponent(entity);
            Optional<HealthComponent> health = healthManager.getComponent(entity);
            Optional<EnemyComponent> enemy = enemyManager.getComponent(entity);
            
            if (!position.isPresent() || !velocity.isPresent() || !pathfinding.isPresent() || !health.isPresent()) {
                continue;
            }

            Vector2 pos = position.get().position;
            List<Tile> find = pathfinding.get().path;
            Tile nextTile = pathfinding.get().targetTile;
            int hp = health.get().getHealth();

            // If the enemy has reached the end of the path, move it to the start to be
            // spawned again and set villageDamage != 0 to damage the village
            if (nextTile.getType() == TileType.END) {
                pathfinding.get().targetTile = find.get(0);
                float startPosition_x = find.get(0).getX() * tileSize.x;
                float startPosition_y = find.get(0).getY() * tileSize.y;
                position.get().position = new Vector2(startPosition_x, startPosition_y);
                health.get().setHealth(health.get().getMaxHealth());
                wave.monsterCounter++;
                int remainingEnemyHealth = health.get().getHealth();
                this.villageDamage += remainingEnemyHealth;
            }
            // If the enemy is dead, set its velocity to 0
            else if (hp <= 0) {
                position.get().position = new Vector2(-1, -1);
                velocity.get().velocity = 0f;
                wave.liveMonsterCounter--;
                boolean claimedReward = enemy.get().claimedReward;
                if (!claimedReward) {
                    awardPlayerMoney(village, entity);
                    enemy.get().claimedReward = true;
                }
            }
        }

        wave.spawnTimer -= deltaTime;

        if (wave.spawnTimer <= 0 && wave.monsterCounter < wave.waveSize) {

            // Keep creating enemies under max-limit is met
            if (wave.liveMonsterCounter < wave.maxLiveMonsters) {

                // random enemy
                EnemyType[] enemyTypes = EnemyType.values();
                EnemyType randomEnemy = enemyTypes[(int) (Math.random() * enemyTypes.length)];

                mob = EnemyFactory.createEnemy(randomEnemy, path, tileSize);
                ECSManager.getInstance().addLocalEntity(mob);
                wave.monsterCounter++;
                wave.liveMonsterCounter++;
                wave.spawnTimer = wave.spawnDuration;
            }
            // If the max number of enemies has been met, check if any of them are dead
            else {

                for (Entity entity : entities) {
                    Optional<PositionComponent> position = positionManager.getComponent(entity);
                    Optional<VelocityComponent> velocity = velocityManager.getComponent(entity);
                    Optional<PathfindingComponent> pathfinding = pathfindingManager.getComponent(entity);
                    Optional<HealthComponent> health = healthManager.getComponent(entity);
                    Optional<EnemyComponent> enemy = enemyManager.getComponent(entity);

                    if (!position.isPresent() || !velocity.isPresent() || !pathfinding.isPresent() || !enemy.isPresent()
                            || !health.isPresent()) {
                        continue;
                    }

                    // Spawn a new enemy
                    List<Tile> find = pathfinding.get().path;
                    if (velocity.get().velocity == 0f) {
                        float startPosition_x = find.get(0).getX() * tileSize.x;
                        float startPosition_y = find.get(0).getY() * tileSize.y;
                        position.get().position = new Vector2(startPosition_x, startPosition_y);
                        pathfinding.get().targetTile = find.get(0);
                        velocity.get().velocity = velocity.get().baseVelocity;
                        health.get().setHealth(health.get().getMaxHealth());
                        wave.liveMonsterCounter++;
                    }
                }
            }
        }

        // Decrement the wave timer
        if (wave.waveTimer > 0) {
            wave.waveTimer -= deltaTime;
        }

        // If any enemies have gotten through, damage the village (actually applies the
        // damage here)
        if (villageDamage > 0) {
            
            Optional<HealthComponent> healthComponent = healthManager.getComponent(village);
            Optional<VillageComponent> villageComponent = villageManger.getComponent(village);
            if (villageComponent.isPresent() && healthComponent.isPresent()) {
                int villageHealth = healthComponent.get().getHealth();

                villageHealth -= villageDamage;;
                // If the village health is 0, the game is over
                if (villageHealth <= 0) {
                    villageHealth = 0;
                }
                
                healthComponent.get().setHealth(villageHealth);
                
                updateTopRightCornerText(village);
                villageDamage = 0;
            }

        }

        // Start the next wave
        if (wave.waveTimer <= 0) {
            wave.waveNumber++;
            wave.monsterCounter = 0;
            wave.waveSize += wave.waveNumber * 2 - 2;
            wave.waveTimer = wave.waveDuration;
            wave.spawnTimer = 0f;
            wave.spawnDuration -= 0;
            wave.maxLiveMonsters++;
        }

        updateWaveNumberDisplay();
    }

    private void awardPlayerMoney(Entity village, Entity enemy) {
        MoneyComponent balance = moneyManager.getComponent(village).get();
        MoneyComponent reward = moneyManager.getComponent(enemy).get();
        balance.amount += reward.amount;
        updateTopRightCornerText(village);
    }

    private void updateTopRightCornerText(Entity village) {
        // Get the text-component of the village and update the health
        ComponentManager<TextComponent> textManager = ECSManager.getInstance()
                .getOrDefaultComponentManager(TextComponent.class);
        ComponentManager<MoneyComponent> moneyManager = ECSManager.getInstance()
                .getOrDefaultComponentManager(MoneyComponent.class);
        ComponentManager<HealthComponent> healthManager = ECSManager.getInstance()
                .getOrDefaultComponentManager(HealthComponent.class);
        Optional<TextComponent> textComponent = textManager.getComponent(village);
        Optional<MoneyComponent> moneyComponent = moneyManager.getComponent(village);
        Optional<HealthComponent> healthComponent = healthManager.getComponent(village);

        if (textComponent.isPresent() && moneyComponent.isPresent() && healthComponent.isPresent()) {
            int villageHealth = healthComponent.get().getHealth();
            int money = moneyComponent.get().amount;
            String textToDisplay = "Health: " + villageHealth + "\n Money: " + money;
            textComponent.get().text = textToDisplay;
        }
        
    }

    private void initializeWaveNumberDisplay(int waveNumber) {
        // Create a new Entity for the wave number and add it to the ECS
        this.waveNumberEntity = new Entity();
        TextComponent waveNumberText = new TextComponent("Wave: " + waveNumber, new Vector2(0.05f, 0.05f));
        waveNumberText.setColor(new Vector3(0, 0, 0));
        PositionComponent waveNumberPosition = new PositionComponent(new Vector2(0.02f, 0.90f), 10);
        waveNumberEntity.addComponent(TextComponent.class, waveNumberText);
        waveNumberEntity.addComponent(PositionComponent.class, waveNumberPosition);
        ECSManager.getInstance().addLocalEntity(waveNumberEntity);
    }

    private void updateWaveNumberDisplay() {
        ComponentManager<TextComponent> textManager = ECSManager.getInstance()
                .getOrDefaultComponentManager(TextComponent.class);
        Optional<TextComponent> waveNumberText = textManager.getComponent(waveNumberEntity);

        WaveComponent wave = WaveManager.getComponent(this.waveEntity).get();
        if (waveNumberText.isPresent()) {
            waveNumberText.get().text = "Wave: " + wave.waveNumber;
        }
    }
}
