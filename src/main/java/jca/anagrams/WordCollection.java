package jca.anagrams;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jca.anagrams.AnagramSolvingService.AnagramFinder;
import static java.util.stream.Collectors.*;

public class WordCollection {
  List<LetterCombination> words = new ArrayList<>();
  private AnagramFinder anagramFinder;

  public WordCollection(AnagramFinder anagramFinder) {
    this.anagramFinder = anagramFinder;
  }

  public boolean addWord(String word) {
    LetterCombination newWord = new LetterCombination(word, anagramFinder);
    return words.add(newWord);
  }

  public Map<String, Set<String>> getCurrentAnagrams() {
    return words.stream()
        .collect(toMap(lc -> lc.getCurrentWord(), lc -> lc.getUnusedAnagrams(), (lc1, lc2) -> lc1));
  }



  public boolean isEmpty() {
    return words.isEmpty();
  }

  // I might want to make nextWord all upper case for display, or make a private field for case (upper/lower)
  @Override
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();
    int toggle = 0;
    for (LetterCombination word : words) {
      String nextWord = word.toString();
      stringBuilder.append(nextWord);
      if (toggle == 2) {
        stringBuilder.append("\n");
        toggle = (toggle + 1) % 3;
      } else {
        stringBuilder.append("     ");
        toggle = (toggle + 1) % 3;
      }
    }
    return stringBuilder.toString();
  }


}
