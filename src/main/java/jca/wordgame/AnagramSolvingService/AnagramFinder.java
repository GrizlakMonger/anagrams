package jca.wordgame.AnagramSolvingService;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AnagramFinder {


  // https://stackoverflow.com/questions/5343689/java-reading-a-file-into-an-arraylist
  // the word list to use should not be hardcoded. It should be changeable from the ui.
  // Should this maintain state, or should the desired word list name be passed to the endpoint each call?
  // That could be expesnive to load each possible dictionary every api call (so we'd obviously want to cache each one

  // THIS SERVICE SHOULD NOT HAVE THE DICTIONARY! It uses the dictionary but shouldn't have its own.
  // For now, it can use its own, but eventually it should make use of the dictionary service when doing checks.
  // Obviously I don't actually want to use network calls, but for this project I want to try to use modules well.
  // Bob Martin is big on modules it seems, all the benefits of microservices without the hassle.

  // So I don't want to just expose things between modules.

  private final Set<String> dictionary = buildDictionary();

  public List<String> findAnagrams(String inputLetters) {
    char[] letters = inputLetters.toCharArray();
    Set<String> anagrams = new HashSet();
    for (int i = 0; i < letters.length; i += 1) {
      StringBuilder anagramBuilder = new StringBuilder();

      //TODO finish this brute force method of every permutation. Would recursive method be ok, or should it be done iteratively?
      //TODO should I reuse the same string builder?
      //TODO I should not look up a solution for this. I ought to be able to find all permutations of a string on my own.
      //TODO This can be broken into two tasks. One is to find all inclusive anagrams (using all letters).
      //TODO The other is to break a group of letters into each possible proper subset of letters.

      //TODO once I have all permutations I can then filter based on if the dictionary contains that String (just use java stream, possibly concurrent?)

      //I should move the wordlists into a resource directory, and then figure out how to get the code to reliably get the file paths

    }
    return Collections.emptyList();
  }

  private Set<String> buildDictionary() {
    List<String> wordList;
    try {
      wordList = Files.readAllLines(new File("cleaned_word_list").toPath(), Charset.defaultCharset());
    } catch (IOException e) {
      wordList = Collections.emptyList();
    }
    return new HashSet(wordList);
  }
}
