package jca.anagrams;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Created by janstett on 1/19/17.
 */
public class LetterSource {
  List<Character> characters;
  Map<Character, Integer> characterDistribution = new HashMap<>();
  Queue<Character> characterQueue;

  public LetterSource() {
    characterDistribution = setupCharacterDistribution();
    characters = populateList(characterDistribution);
    characterQueue = shuffleCharacters(characters);
  }

  public Character nextCharacter() {
    return characterQueue.poll();
  }

  public Queue<Character> shuffleCharacters (List<Character> characters) {
    Collections.shuffle(characters);
    Queue<Character> shuffledQueue = new ArrayDeque<>();
    for (Character character : characters) {
      shuffledQueue.add(character);
    }
    return shuffledQueue;
  }

  public List<Character> populateList(Map<Character, Integer> distribution) {
    List<Character> characters = new ArrayList<>();
    for (Character character : distribution.keySet()) {
      for (int i = 0; i < distribution.get(character); i += 1) {
        characters.add(character);
      }
    }
    return characters;
  }

  // default Bananagrams distribution
  // I could make a Bananagram bag object that manages its own Distribution and list and shuffles itself
  // and gives you a tile
  public Map<Character, Integer> setupCharacterDistribution() {
    Map<Character, Integer> distribution = new HashMap<>();
    distribution.put('A', 13);
    distribution.put('B', 3);
    distribution.put('C', 3);
    distribution.put('D', 6);
    distribution.put('E', 18);
    distribution.put('F', 3);
    distribution.put('G', 4);
    distribution.put('H', 3);
    distribution.put('I', 12);
    distribution.put('J', 2);
    distribution.put('K', 2);
    distribution.put('L', 5);
    distribution.put('M', 3);
    distribution.put('N', 8);
    distribution.put('O', 11);
    distribution.put('P', 3);
    distribution.put('Q', 2);
    distribution.put('R', 9);
    distribution.put('S', 6);
    distribution.put('T', 9);
    distribution.put('U', 6);
    distribution.put('V', 3);
    distribution.put('W', 3);
    distribution.put('X', 2);
    distribution.put('Y', 3);
    distribution.put('Z', 2);

    return distribution;
  }
}
