package jca.anagrams;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by janstett on 1/25/17.
 */
public class LetterUtilities {
  public static List<Character> stringToCharacterList(String word) {
    List<Character> characters = new ArrayList<>();
    for (int i = 0; i < word.length(); i += 1) {
      characters.add(word.charAt(i));
    }
    return characters;
  }
}
