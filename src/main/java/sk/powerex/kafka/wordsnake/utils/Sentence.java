package sk.powerex.kafka.wordsnake.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Sentence {

  private Character lastCharacterOfValidWord;

  private String cleanUpRawSentence(String sentence) {
    return sentence.toUpperCase().replaceAll("[^a-zA-Z ]+", "").replaceAll("\\s+", " ");
  }

  private String removeInvalidWords(String sentence) {
    List<String> validWords = new ArrayList<>();
    List<String> words = Arrays.asList(sentence.split(" "));

    words.forEach(w -> {
      if (lastCharacterOfValidWord == null || lastCharacterOfValidWord == w.charAt(0)) {
        validWords.add(w);
        lastCharacterOfValidWord = w.charAt(w.length() - 1);
      }
    });

    return String.join(" ", validWords);
  }

  public String getProcessedSentence(String sentence) {
    return removeInvalidWords(cleanUpRawSentence(sentence));
  }
}
