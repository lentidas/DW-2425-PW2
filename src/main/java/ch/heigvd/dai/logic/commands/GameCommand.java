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

package ch.heigvd.dai.logic.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Abstract class representing a command that can be sent to/from the server.
 *
 * <p>It provides a method to convert the command to a string that can be sent over the network.
 *
 * @author Pedro Alves da Silva
 * @author Gonçalo Carvalheiro Heleno
 */
public abstract class GameCommand {

  /** String of vowels in the alphabet. */
  public static final String VOWELS = "AEIOU"; // The original game didn't consider Y to be a vowel

  /** The type of the command. */
  protected final GameCommandType type;

  /** The arguments of the command. */
  protected List<Object> args;

  /** The handler function for generating a command from a TCP message. */
  private static final Map<GameCommandType, CommandFactoryFunction> _factoryHandlers =
      new HashMap<>();

  // TODO Improve this debugging by using a proper Java logging framework.
  /** Boolean to enable/disable debug messages. */
  private static final boolean DEBUG_MODE = false;

  /**
   * Default constructor.
   *
   * @param type the type of the command
   */
  public GameCommand(GameCommandType type) {
    this.type = type;
    this.args = new LinkedList<>();
  }

  /**
   * Gets the type of the command.
   *
   * @return the {@link GameCommandType} of the command
   */
  public GameCommandType getType() {
    return type;
  }

  /**
   * Parses the arguments of the command from a TCP message.
   *
   * @param body a {@link String} with the body of the TCP message
   * @return a {@link GameCommand} with the parsed arguments
   * @throws InvalidPropertiesFormatException if the arguments are invalid for this command
   */
  public static GameCommand fromTcpBody(String body) throws InvalidPropertiesFormatException {

    String[] commandNames = body.split(" ");
    if (commandNames.length == 0) {
      throw new InvalidPropertiesFormatException("Command name is missing");
    }
    String commandName = commandNames[0];

    GameCommandType commandType;
    try {
      commandType = GameCommandType.valueOf(commandName.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new InvalidPropertiesFormatException("Invalid command name: " + commandName);
    }

    if (!_factoryHandlers.containsKey(commandType)) {
      throw new InvalidPropertiesFormatException("No handler for command " + commandName);
    }

    // Split word by word, unless quoted
    String argsSubstr = body.substring(commandName.length()).stripLeading();
    Pattern regexPatt = Pattern.compile("([^\"]\\S*|.+?\")\\s*");
    Matcher matcher = regexPatt.matcher(argsSubstr);

    String[] commandArgs = null;
    List<String> allMatches = new ArrayList<>();
    while (matcher.find()) {
      allMatches.add(matcher.group());
    }

    if (!allMatches.isEmpty()) {
      commandArgs = new String[allMatches.size()];

      int i = 0;
      for (String arg : allMatches) {

        // Remove start and end quotes
        if (arg.startsWith("\"") && arg.endsWith("\"")) {
          arg = arg.substring(1, arg.length() - 1);
        }
        commandArgs[i] = arg.trim(); // Trim any leading and trailing whitespaces.
        i++;
      }
    }

    return _factoryHandlers.get(commandType).apply(commandArgs);
  }

  /**
   * Adds a handler for each command type.
   *
   * @param type the {@link GameCommandType} of the command
   * @param handler the {@link CommandFactoryFunction} to handle the command
   */
  protected static void addFactoryHandler(GameCommandType type, CommandFactoryFunction handler) {
    _factoryHandlers.put(type, handler);
    if (DEBUG_MODE) {
      System.out.println("[DEBUG] Added handler for " + type);
    }
  }

  /** Registers all the handlers for the commands. */
  public static void registerHandlers() {
    GameCommand.addFactoryHandler(GameCommandType.END, EndCommand::fromTcpBody);
    GameCommand.addFactoryHandler(GameCommandType.FILL, FillCommand::fromTcpBody);
    GameCommand.addFactoryHandler(GameCommandType.GO, GoCommand::fromTcpBody);
    GameCommand.addFactoryHandler(GameCommandType.GUESS, GuessCommand::fromTcpBody);
    GameCommand.addFactoryHandler(GameCommandType.INFO, InfoCommand::fromTcpBody);
    GameCommand.addFactoryHandler(GameCommandType.JOIN, JoinCommand::fromTcpBody);
    GameCommand.addFactoryHandler(GameCommandType.LAST, LastCommand::fromTcpBody);
    GameCommand.addFactoryHandler(GameCommandType.LOBBY, LobbyCommand::fromTcpBody);
    GameCommand.addFactoryHandler(GameCommandType.QUIT, QuitCommand::fromTcpBody);
    GameCommand.addFactoryHandler(GameCommandType.ROUND, RoundCommand::fromTcpBody);
    GameCommand.addFactoryHandler(GameCommandType.START, StartCommand::fromTcpBody);
    GameCommand.addFactoryHandler(GameCommandType.STATUS, StatusCommand::fromTcpBody);
    GameCommand.addFactoryHandler(GameCommandType.TURN, TurnCommand::fromTcpBody);
    GameCommand.addFactoryHandler(GameCommandType.VOWEL, VowelCommand::fromTcpBody);
    GameCommand.addFactoryHandler(GameCommandType.WINNER, WinnerCommand::fromTcpBody);
    GameCommand.addFactoryHandler(GameCommandType.SKIP, SkipCommand::fromTcpBody);
    GameCommand.addFactoryHandler(GameCommandType.LETTERS, LettersCommand::fromTcpBody);
    GameCommand.addFactoryHandler(GameCommandType.HELP, HelpCommand::fromTcpBody);
    GameCommand.addFactoryHandler(GameCommandType.HOST, HostCommand::fromTcpBody);
  }

  /**
   * Converts an argument to a string.
   *
   * @param arg the argument to convert
   * @return a {@link String} with the argument converted to a string
   */
  private String argToString(Object arg) {
    StringBuilder sb = new StringBuilder();
    if (arg instanceof String && ((String) arg).contains(" ")) {
      sb.append('"').append(arg).append('"');
    } else {
      sb.append(arg);
    }
    return sb.toString();
  }

  /**
   * Converts the command to a string that can be sent over the network.
   *
   * @return a {@link String} with the command converted to a string
   */
  public String toTcpBody() {
    StringBuilder sb = new StringBuilder();
    sb.append(type.name());

    if (null != args) {
      for (Object arg : args) {
        if (arg instanceof Object[]) {
          for (int i = 0; i < ((Object[]) arg).length; i++) {
            sb.append(' ').append(argToString(((Object[]) arg)[i]));
          }
        } else {
          sb.append(' ');
          sb.append(argToString(arg));
        }
      }
    }

    return sb.toString();
  }

  /**
   * Gets the arguments of the command.
   *
   * @return a {@link List} with the arguments of the command
   */
  public List<Object> getArgs() {
    if (null != args) {
      return List.copyOf(args);
    } else {
      return new LinkedList<>();
    }
  }

  /**
   * Checks if a character is a vowel.
   *
   * @param c the character to check
   * @return {@code true} if the character is a vowel, {@code false} otherwise
   */
  protected static boolean isCharAVowel(char c) {
    char lower = Character.toUpperCase(c);
    for (char vowel : VOWELS.toCharArray()) {
      if (lower == vowel) {
        return true;
      }
    }
    return false;
  }
}
