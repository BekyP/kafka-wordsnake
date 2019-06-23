package sk.powerex.kafka.wordsnake;

import java.util.Arrays;
import java.util.stream.IntStream;
import lombok.AllArgsConstructor;
import lombok.Data;
import sk.powerex.kafka.wordsnake.utils.Coords;

@Data
@AllArgsConstructor
class SnakeMap {


  private Coords coords;
  private Character[][] map;
  private int mapSize;
  private boolean finished;

  String getStringMap() {
    StringBuilder sb = new StringBuilder();

    Arrays.stream(map)
        .forEach(y -> {
          IntStream.range(0, mapSize).forEach(x -> {
            sb.append(y[x]);
          });
          sb.append("\n");
        });

    return sb.toString();
  }
}
