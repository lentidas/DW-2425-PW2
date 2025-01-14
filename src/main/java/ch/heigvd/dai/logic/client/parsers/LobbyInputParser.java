/*
 * Wheel Of Fortune - a Java server/client CLI implementation of the television game
 * Copyright (C) 2024 Pedro Alves da Silva, Gonçalo Carvalheiro Heleno
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.heigvd.dai.logic.client.parsers;

import ch.heigvd.dai.logic.PlayerState;
import ch.heigvd.dai.logic.client.InteractiveConsole;
import ch.heigvd.dai.logic.commands.GameCommand;
import ch.heigvd.dai.logic.commands.GoCommand;
import ch.heigvd.dai.logic.commands.QuitCommand;

public class LobbyInputParser implements IInputParser {

  @Override
  public GameCommand parse(InteractiveConsole interactiveConsole, String input) {
    GameCommand command = null;

    if (input.equalsIgnoreCase("go")) {
      System.out.println("Asking server to start game...");
      command = new GoCommand();
    } else if (input.equalsIgnoreCase("quit")) {
      System.out.println("Saying goodbye the server...");
      command = new QuitCommand();
      interactiveConsole.setCurrentState(PlayerState.DISCONNECTED);
    } else {
      System.err.println("Invalid input!");
    }

    return command;
  }
}
