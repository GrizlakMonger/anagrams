package jca.anagrams;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by janstett on 1/25/17.
 */
public class PrimitiveWordCollection {
  List<LetterCollection> words = new ArrayList<>();

  public boolean isEmpty() {
    return words.isEmpty();
  }

  public boolean addWord(String word) {
    List<Character> characters = LetterUtilities.stringToCharacterList(word);
    LetterCollection letterCollection = new LetterCollection(characters);
    return words.add(letterCollection);
  }

  @Override
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();
    int toggle = 0;
    for (LetterCollection word : words) {
      String nextWord = word.asWordString();
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
