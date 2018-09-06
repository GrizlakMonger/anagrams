package jca.anagrams;

import java.util.List;
import java.util.Set;

import jca.anagrams.AnagramSolvingService.AnagramFinder;

import static jca.anagrams.utils.WordUtils.sortString;

/* This class represents captured words. Perhaps I can rename it to reflect that. At an abstract level
this has some things in common with a letter collection. Maybe I can make this extend letterCollection

 */
public class LetterCombination {
  private String currentWord;
  private String sortedCharacters;  // this is the "key" used for lookups for this letter collection.
  private Set<String> possibleAnagrams;
  private List<String> wordHistory; // maybe used linked list since words grow on each other
  private AnagramFinder anagramFinder; // there's got to be a better way than putting the finder reference in each instance

  public LetterCombination(String word, AnagramFinder anagramFinder) {
    this.currentWord = word;
    this.sortedCharacters = sortString(word);
    this.anagramFinder = anagramFinder;
    this.possibleAnagrams = anagramFinder.findAnagrams(word);
  }


  boolean hasUnusedAnagrams() {
    return wordHistory.containsAll(possibleAnagrams);
  }

//  public boolean addToWordHistory(String word) {
//    char[] wordArray = word.toCharArray();
//    for ()
//  }
}
