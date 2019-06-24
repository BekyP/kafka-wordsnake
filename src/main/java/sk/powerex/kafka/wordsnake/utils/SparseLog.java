package sk.powerex.kafka.wordsnake.utils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;
import org.springframework.lang.Nullable;

@Data
@Slf4j
public class SparseLog {

  private SparseLog() {
    throw new IllegalStateException("Utility class");
  }

  public static void log(String name, @Nullable Object key, Object value, int modulo,
      Level level) {
    if (modulo > 0 && key != null && key.hashCode() % modulo == 0) {
      String format = "{}: {} = {}";
      switch (level) {
        case TRACE:
          log.trace(format, name, key, value);
          break;
        case DEBUG:
          log.debug(format, name, key, value);
          break;
        case WARN:
          log.warn(format, name, key, value);
          break;
        case ERROR:
          log.error(format, name, key, value);
          break;
        default:
          log.info(format, name, key, value);
          break;
      }
    }
  }
}
