package jca.anagrams;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jca.anagrams.AnagramSolvingService.AnagramFinder;
import jca.anagrams.utils.WordUtils;

public class MergeAttempt {

  private String baseWord;
  private String freeTiles;
  private List<String> fullWordMerges; // this should be a List, because you could potentially merge multiple of same word
  private String allLettersSorted;
  private Set<String> validWords;
  private boolean isValid;
  AnagramFinder anagramFinder; //should this have its own, or should the anagrams be sent in constructor at creation?
  // This seems like it could get malformed if I code it wrong though. I can use a builder that always applies the anagram finder.

  public MergeAttempt(String baseWord, String freeTiles, AnagramFinder anagramFinder) {
    this(baseWord, freeTiles, Collections.emptyList(), anagramFinder);
  }

  public MergeAttempt(String baseWord, String freeTiles, List<String> fullWordMerges, AnagramFinder anagramFinder) {
    this.baseWord = baseWord;
    this.freeTiles = freeTiles;
    this.fullWordMerges = fullWordMerges;
    this.anagramFinder = anagramFinder;
    String allLetters = baseWord + freeTiles + fullWordMerges.stream().collect(Collectors.joining());
    this.validWords = anagramFinder.findAnagrams(allLetters);
    this.allLettersSorted = WordUtils.sortString(allLetters);
    this.isValid = !validWords.isEmpty(); // with a builder, I could just reject a word up front that is not valid
  }

  public String getBaseWord() {
    return baseWord;
  }

  public int getNumberOfFreeTiles() {
    return freeTiles.length();
  }

  public String getFreeTiles() {
    return freeTiles;
  }

  public List<String> getFullWordMerges() {
    return fullWordMerges;
  }

  public Set<String> getValidWords() {
    return validWords;
  }

  public boolean isValid() {
    return isValid;
  }

  public String getSortedLetters() {
    return allLettersSorted;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(baseWord + " ");
    for (String mergedWord : fullWordMerges) {
      sb.append("+ " + mergedWord + " ");
    }
    if (!freeTiles.isEmpty()) {
      sb.append("+ ");
      for (char c : freeTiles.toCharArray()) {
        sb.append(c + " ");
      }
    }
    sb.append("-> ");
    sb.append(validWords.stream().collect(Collectors.joining(" ")));
    return sb.toString();
  }
}
