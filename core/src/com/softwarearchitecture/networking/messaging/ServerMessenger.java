package com.softwarearchitecture.networking.messaging;

import java.util.List;
import java.util.UUID;

import com.softwarearchitecture.game_client.states.InGame;
import com.softwarearchitecture.game_server.PlayerInput;
import com.softwarearchitecture.game_server.ServerMessagingController;

public class ServerMessenger implements ServerMessagingController {

    @Override
    public UUID createGame() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createGame'");
    }

    @Override
    public void setNewGameState(InGame gameState) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setNewGameState'");
    }

    @Override
    public List<PlayerInput> getActions(UUID gameID) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getActions'");
    }

}
