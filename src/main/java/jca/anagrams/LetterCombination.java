package jca.anagrams;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jca.anagrams.AnagramSolvingService.AnagramFinder;

import static jca.anagrams.utils.WordUtils.sortString;

/* This class represents captured words. Perhaps I can rename it to reflect that. At an abstract level
this has some things in common with a letter collection. Maybe I can make this extend letterCollection
However, unlike the letter collection used for the middle tiles, these tiles carry their history and "allegiance" with them
 */
public class LetterCombination {
  private String currentWord;
  private String sortedCharacters;  // this is the "key" used for lookups for this letter collection.
  private Set<String> allPossibleAnagrams;
  private List<String> wordHistory = new ArrayList<>(); // maybe used linked list since words grow on each other
  private AnagramFinder anagramFinder; // there's got to be a better way than putting the finder reference in each instance

  public LetterCombination(String word, AnagramFinder anagramFinder) {
    this.currentWord = word;
    this.sortedCharacters = sortString(word);
    this.anagramFinder = anagramFinder;
    this.allPossibleAnagrams = anagramFinder.findAnagrams(word);
    this.wordHistory.add(word);
  }

  public String getCurrentWord() {
    return currentWord;
  }

  public Set<String> getUnusedAnagrams () {
    Set<String> unusedAnagrams = allPossibleAnagrams.stream()
        .filter(s -> !wordHistory.contains(s))
        .collect(Collectors.toSet());
    return unusedAnagrams;
  }

  public Set<String> getAllPossibleAnagrams() { // maybe just give exhaustive list, current word included (just return the field)
    return allPossibleAnagrams.stream()
        .filter(s -> s.equals(currentWord))
        .collect(Collectors.toSet());
  }

  public boolean hasUnusedAnagrams() {
    return wordHistory.containsAll(allPossibleAnagrams);
  }

  @Override
  public String toString() {
    return currentWord;
  }

//  public boolean addToWordHistory(String word) {
//    char[] wordArray = word.toCharArray();
//    for ()
//  }
}
