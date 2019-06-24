package sk.powerex.kafka.wordsnake;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import sk.powerex.kafka.wordsnake.utils.Coords;

@Data
@Slf4j
class Wordsnake {

  private static final int RIGHT = 0;
  private static final int LEFT = 1;
  private static final int UP = 2;
  private static final int DOWN = 3;
  private static final Random random = new Random();
  private final List<String> words;
  private final Boolean allowJammedSnake;
  private SnakeMap snakeMap;

  Wordsnake(String sentence, Boolean allowJammedSnake) {
    this.words = Arrays.asList(sentence.split(" "));
    int sentenceLength = sentence.length();

    Character[][] map = new Character[sentenceLength][sentenceLength];

    // init empty map
    Arrays.stream(map).forEach(x -> Arrays.fill(x, ' '));

    this.snakeMap = new SnakeMap(new Coords(0, 0), map, sentenceLength, false);
    this.allowJammedSnake = allowJammedSnake;
  }

  SnakeMap createSnakeMap() {
    IntStream.range(0, words.size()).forEach(i -> {
          if (snakeMap.isFinished()) {
            return;
          }

          String w = words.get(i);
          int direction = getDirection(i);

          switch (direction) {
            case RIGHT:
            case LEFT:
              horizontalMove(w, direction);
              break;
            case UP:
            case DOWN:
              verticalMove(w, direction);
              break;
            default:
              log.error("Invalid direction {}", direction);
              this.snakeMap.setFinished(true);
              break;
          }
        }
    );

    return this.snakeMap;
  }

  // TODO movement to separate class

  private void verticalMove(String w, int generatedDirection) {
    if (!allowJammedSnake) {
      turnDown(w);
      return;
    }
    Map<Integer, Boolean> directions = freeVerticalDirections(w);

    // wants to go down and can go down - go down
    if (generatedDirection == DOWN && directions.getOrDefault(DOWN, false)) {
      turnDown(w);
    } //  wants to go down but can`t go down - go up
    else if (generatedDirection == DOWN && directions.getOrDefault(UP, false)) {
      turnUp(w);
    } // wants to go up and can go up - go up
    else if (generatedDirection == UP && directions.getOrDefault(UP, false)) {
      turnUp(w);
    } // wants to go up but can`t go up - go down
    else if (generatedDirection == UP && directions.getOrDefault(DOWN, false)) {
      turnDown(w);
    } // jammed snake, nowhere to move - finish
    else {
      this.snakeMap.setFinished(true);
    }
  }

  private void horizontalMove(String w, int generatedDirection) {
    Map<Integer, Boolean> directions = freeHorizontalDirections(w);

    // wants to go right and can go right - go right
    if (generatedDirection == RIGHT && directions.getOrDefault(RIGHT, false)) {
      turnRight(w);
    } // wants to go right but can`t go right - go left
    else if (generatedDirection == RIGHT && directions.getOrDefault(LEFT, false)) {
      turnLeft(w);
    } //wants to go left and can go left - go left
    else if (generatedDirection == LEFT && directions.getOrDefault(LEFT, false)) {
      turnLeft(w);
    } // wants to go left but can`t go left - go right
    else if (generatedDirection == LEFT && directions.getOrDefault(RIGHT, false)) {
      turnRight(w);
    } // jammed snake, nowhere to move - finish
    else {
      this.snakeMap.setFinished(true);
    }
  }

  private int getDirection(int i) {
    if (i % 2 == 0) { // horizontal - movement left or right
      return random.nextInt(2);
    } else {  // vertical - movement up or down
      return random.nextInt(2) + 2;
    }
  }

  private Map<Integer, Boolean> freeVerticalDirections(String word) {
    Coords coords = snakeMap.getCoords();
    Character[][] currentMap = snakeMap.getMap();

    Map<Integer, Boolean> freeDirections = new HashMap<>();

    // checking free space for word
    //down
    long freeSpaceDown = IntStream
        .range(coords.getY() + 1, coords.getY() + word.length())
        .filter(y -> currentMap[y][coords.getX()] == ' ').count();

    if (coords.getY() < snakeMap.getMapSize() && freeSpaceDown == word.length() - 1) {
      freeDirections.put(DOWN, true);
    }

    //up
    if (coords.getY() - (word.length() - 1) >= 0) {
      long freeSpaceUp = IntStream.iterate(coords.getY() - 1, i -> i - 1)
          .limit(word.length() - 1L)
          .filter(y -> currentMap[y][coords.getX()] == ' ')
          .count();

      if (coords.getY() < snakeMap.getMapSize() && freeSpaceUp == word.length() - 1) {
        freeDirections.put(UP, true);
      }
    }

    return freeDirections;
  }

  private Map<Integer, Boolean> freeHorizontalDirections(String word) {
    Coords coords = snakeMap.getCoords();
    Character[][] currentMap = snakeMap.getMap();

    Map<Integer, Boolean> freeDirections = new HashMap<>();

    // checking free space for word
    //right
    long freeSpaceRight = IntStream
        .range(coords.getX() + 1, coords.getX() + word.length())
        .filter(x -> currentMap[coords.getY()][x] == ' ').count();

    if (coords.getX() < snakeMap.getMapSize() && freeSpaceRight == word.length() - 1) {
      freeDirections.put(RIGHT, true);
    }

    //left
    if (coords.getX() - (word.length() - 1) >= 0) {
      long freeSpaceLeft = IntStream.iterate(coords.getX() - 1, i -> i - 1)
          .limit(word.length() - 1L)
          .filter(x -> currentMap[coords.getY()][x] == ' ')
          .count();

      if (coords.getX() < snakeMap.getMapSize() && freeSpaceLeft == word.length() - 1) {
        freeDirections.put(LEFT, true);
      }
    }

    return freeDirections;
  }

  private void putCharacterToMap(Character c, Coords coords) {
    Character[][] currentMap = snakeMap.getMap();
    currentMap[coords.getY()][coords.getX()] = c;
    snakeMap.setMap(currentMap);
  }

  private void turnRight(String word) {
    IntStream.range(0, word.length()).forEach(i -> {
      Coords coords = snakeMap.getCoords();

      putCharacterToMap(word.charAt(i), coords);
      if (word.length() - 1 != i) {
        snakeMap.getCoords().setX(coords.getX() + 1);
      }
    });
  }

  private void turnLeft(String word) {
    IntStream.range(0, word.length()).forEach(i -> {
      Coords coords = snakeMap.getCoords();

      putCharacterToMap(word.charAt(i), coords);
      if (word.length() - 1 != i) {
        snakeMap.getCoords().setX(coords.getX() - 1);
      }
    });
  }

  private void turnUp(String word) {
    IntStream.range(0, word.length()).forEach(i -> {
      Coords coords = snakeMap.getCoords();
      putCharacterToMap(word.charAt(i), coords);

      if (word.length() - 1 != i) {
        snakeMap.getCoords().setY(coords.getY() - 1);
      }
    });
  }

  private void turnDown(String word) {
    IntStream.range(0, word.length()).forEach(i -> {
      Coords coords = snakeMap.getCoords();
      putCharacterToMap(word.charAt(i), coords);

      if (word.length() - 1 != i) {
        snakeMap.getCoords().setY(coords.getY() + 1);
      }
    });
  }
}
