package jca.anagrams.AnagramSolvingService;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.math3.util.Combinations;

import jca.anagrams.MergeAttempt;

import static java.util.stream.Collectors.*;
import static java.util.function.Function.identity;
import static jca.anagrams.utils.WordUtils.sortString;

// https://stackoverflow.com/questions/5343689/java-reading-a-file-into-an-arraylist
// the word list to use should not be hardcoded. It should be changeable from the ui.
// Should this maintain state, or should the desired word list name be passed to the endpoint each call?
// That could be expensive to load each possible dictionary every api call (so we'd obviously want to cache each one).
// There aren't too many possible dictionaries, and maybe I can even use an enum corresponding to each dictionary.
// Same for letter distributions.

// I also want to make a bot that grabs words asap and run a simulation to see the most common words.
// I'll also do this with a simpler dictionary to avoid borderline/obscure words.

// THIS SERVICE SHOULD NOT HAVE THE DICTIONARY! It uses the dictionary but shouldn't have its own.
// For now, it can use its own, but eventually it should make use of the dictionary service when doing checks.
// Obviously I don't actually want to use network calls, but for this project I want to try to use modules well.
// Bob Martin is big on modules it seems, all the benefits of microservices without the hassle.

// So I don't want to just expose things between modules.

// 9/04/2018 - I decided to just look up permutation/anagram algorithms because I don't want to spend to much time on this.
// I was on completely wrong track. You shouldn't dynamically list every anagram and then check each entry.
// Since there are far more invalid permutations than valid, it makes more sense to start with the dictionary.
// Any anagram-group can be uniquely identified by a sorted version of itself. DUH! Have a set of
// valid character collections mapped to each of the possible words

// Then I remembered there's a stream method for this and found it:
// https://www.javacodegeeks.com/2015/11/java-8-streams-api-grouping-partitioning-stream.html
public class AnagramFinder {

  private static AnagramFinder INSTANCE;

  public static AnagramFinder buildAnagramFinder(Set<String> dictionary) {
    if (INSTANCE == null) {
      INSTANCE = new AnagramFinder(dictionary);
    }
    return INSTANCE;
  }

  public AnagramFinder getAnagramFinder() {
    return INSTANCE;
  }

  private AnagramFinder(Set<String> dictionary) {
    this.anagramMap = buildAnagramMap(dictionary);
  }

  final private Map<String, Set<String>> anagramMap;
  private int minimumWordLength = 4;

  public Map<String, Set<String>> buildAnagramMap(Set<String> dictionary) {
    Map<String, Set<String>> anagramMap =
        dictionary.stream() // parallel stream consistently takes longer on my macbook
            .collect(groupingBy(s -> sortString(s), mapping(identity(), toSet())));

    return anagramMap;
  }

  // must call this method with a valid word (meaning its confirmed in dictionary and word size)
  // since this is only called during construction of right sized word
  public Set<String> findAnagrams(String word) {
    if (word.length() < minimumWordLength) {
      return Collections.emptySet();
    }
    String sortedWord = sortString(word);
    Optional<Set<String>> possibleAnagrams = Optional.ofNullable(anagramMap.get(sortedWord));
    return possibleAnagrams.orElse(Collections.emptySet());
  }


  // I only need to find SORTED subanagrams, since I can check against the subanagram map if a potential new word is available (don't need to check actual dictionary)
  // (I don't need all permutations, only sorted combinations! This saves a lot of resources!)
  // So I start by finding all sorted letter combinations, then check if words exist for each of those combos
  //
  public Map<String, Set<String>> findSubAnagrams(String inputLetters) {
    if (inputLetters.length() < minimumWordLength) {
      return Collections.emptyMap();
    }
    int numberOfLetters = inputLetters.length();

    inputLetters = inputLetters.toLowerCase();
    char[] letters = inputLetters.toCharArray();
    Arrays.sort(letters);
    Set<String> sortedCombinations = new HashSet();
    
    for (int k = minimumWordLength;  k <= numberOfLetters; k += 1) {
      // extract this loop? I will end up with an array of indices, and grab the indices of the input letters to put in a set
      for (Iterator<int[]> combinationIterator = new Combinations(numberOfLetters, k).iterator(); combinationIterator.hasNext(); ) {
        sortedCombinations.add(getSubStringByIndices(letters, combinationIterator.next()));
      }
    }

    Set<String> validLetterCombos = sortedCombinations.stream()
        .filter(p -> anagramMap.containsKey(p))
        .collect(toSet());

    Map<String, Set<String>> combosAndAnagrams = validLetterCombos.stream()
        .collect(toMap(identity(), t -> anagramMap.get(t)));

    return combosAndAnagrams;
  }

  // the key to the outer-most map is the number of new center tiles used
  // the next map is keyed by the original "current" word that is being transformed
  // if we only want words that require one letter, we will get a map as the value of key "1", with each word and all of its derived extensions.
  // changed it to not return the map by added tiles. Eventually I can use a stream->filter to get the map of maps, keyed by number
  // of new tiles. That way you can ask "only 1 letter additions", for example, or sort the display results
  // under different headings, for number of added tiles
  public Map<String, Set<MergeAttempt>> findAllExtensionsWithoutWordMerges(String inPlay, List<String> capturedWords) {
    Map<String, Set<MergeAttempt>> mergesByBaseWord = new HashMap<>();
    for (String capturedWord : capturedWords) {
      Set<MergeAttempt> merges = getAllValidTileAdditionsForWord(capturedWord, inPlay); // I don't need this mapped yet, eventually I can group all the merge attempts by number of free tiles
      mergesByBaseWord.put(capturedWord, merges);
    }
    return mergesByBaseWord;
  }

  public Set<MergeAttempt> findAllExtensions(String inPlay, List<String> capturedWords) {
    Set<MergeAttempt> merges = new HashSet<>();
    for (String capturedWord : capturedWords) {
      List<String> otherCapturedWords = capturedWords.stream().collect(toList());
      otherCapturedWords.remove(capturedWord);
      merges.addAll(getAllValidMergesForWord(capturedWord, inPlay, otherCapturedWords)); // I don't need this mapped yet, eventually I can group all the merge attempts by number of free tiles
    }
    return merges;
  }

  private Set<MergeAttempt> getAllValidTileAdditionsForWord(String word, String inPlay) {
    Set<String> sortedTileCombos = getAllTileCombos(inPlay);
    Set<MergeAttempt> validMergeAttempts =
        sortedTileCombos.stream()
            .map(c -> new MergeAttempt(word, c, INSTANCE))
            .filter(MergeAttempt::isValid)
            .collect(toSet());
    return validMergeAttempts;
  }

  private Set<MergeAttempt> getAllValidMergesForWord(String word, String inPlay, List<String> otherWords) {
    Set<String> sortedTileCombos = getAllTileCombos(inPlay);
    Set<List<String>> sortedWordCombos = getAllWordCombos(otherWords);
    Set<MergeAttempt> mergeAttempts = new HashSet<>();
    for (String tileCombo : sortedTileCombos) {
      for (List<String> wordCombo : sortedWordCombos) {
        mergeAttempts.add(new MergeAttempt(word, tileCombo, wordCombo, INSTANCE));
      }
    }
    Set<MergeAttempt> validMergeAttempts = mergeAttempts.stream()
        .filter(MergeAttempt::isValid)
        .collect(toSet());
    return validMergeAttempts;
  }

  private Set<String> getAllTileCombosOfLengthK(String inPlay, int k) {
    Set<String> sortedCombos = new HashSet<>();
    int totalFreeTiles = inPlay.length();
    inPlay = inPlay.toLowerCase();
    char[] inPlayLetters = inPlay.toCharArray();
    Arrays.sort(inPlayLetters);
    Iterable<int[]> combinationIterator = new Combinations(totalFreeTiles, k);
    for (int[] indices : combinationIterator) {
      sortedCombos.add(getSubStringByIndices(inPlayLetters, indices));
    }
    return sortedCombos;
  }

  // I can make a more general version by taking tiles, a min, and max, then call it both from the
  // standard 'fresh' word finder (involving minimum word length) or the merging finder, which can take 1 or more tiles from mid
  // I've also made this to return a map keyed by number of letters used, rather than all the combos in one set.
  // It can easily be made into one set with a stream-> flat map.
  private Map<Integer, Set<String>> getAllTileCombosByLength(String inPlay) {
    HashMap<Integer, Set<String>> sortedCombos = new HashMap<>();
    int totalFreeTiles = inPlay.length();
    inPlay = inPlay.toLowerCase();
    char[] inPlayLetters = inPlay.toCharArray();
    Arrays.sort(inPlayLetters);

    for (int k = 1;  k <= totalFreeTiles; k += 1) {
      sortedCombos.put(k, new HashSet<>());
      Iterable<int[]> combinationIterator = new Combinations(totalFreeTiles, k);
      for (int[] indices : combinationIterator) {
        sortedCombos.get(k).add((getSubStringByIndices(inPlayLetters, indices)));
      }
    }
    return sortedCombos;
  }

  private Set<String> getAllTileCombos(String inPlay) {
    Set<String> sortedCombos = new HashSet<>();
    int totalFreeTiles = inPlay.length();
    inPlay = inPlay.toLowerCase();
    char[] inPlayLetters = inPlay.toCharArray();
    Arrays.sort(inPlayLetters);

    for (int k = 1;  k <= totalFreeTiles; k += 1) {
      Iterable<int[]> combinationIterator = new Combinations(totalFreeTiles, k);
      for (int[] indices : combinationIterator) {
        sortedCombos.add(getSubStringByIndices(inPlayLetters, indices));
      }
    }
    return sortedCombos;
  }

  private Set<List<String>> getAllWordCombos(List<String> words) {
    Set<List<String>> sortedCombos = new HashSet<>();
    int totalWords = words.size();
    String[] wordArray = words.toArray(new String[totalWords]);
    Arrays.sort(wordArray);

    for (int k = 0;  k <= 3; k += 1) { //start at 0, so we can get the empty set: to make mergeAttempt creation easier
      Iterable<int[]> combinationIterator = new Combinations(totalWords, k);
      for (int[] indices : combinationIterator) {
        sortedCombos.add(getSubListByIndices(wordArray, indices));
      }
    }
    return sortedCombos;
  }

  private String getSubStringByIndices(char[] letters, int[] indices) {
    char[] subarray = new char[indices.length];
    for (int i = 0; i < indices.length; i += 1) {
      subarray[i] = letters[indices[i]];
    }
    return new String(subarray);
  }

  private List<String> getSubListByIndices(String[] wordArray, int[] indices) {
    String[] subArray = new String[indices.length];
    for (int i = 0; i < indices.length; i += 1) {
      subArray[i] = wordArray[indices[i]];
    }
    return Arrays.asList(subArray);

  }

  public Map<String, Set<String>> buildMultiplicityFilteredAnagramMap() {
    int minimumAnagrams = 3;
    int wordLength = 3;
    Map<String, Set<String>> sortedAnagramMap =
        this.anagramMap
            .entrySet().stream()
            .filter(es -> es.getKey().length() == wordLength)
            .filter(es -> es.getValue().size() > minimumAnagrams)
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    return sortedAnagramMap;
  }

  public Map<String, Set<String>> buildLengthFilteredAnagramMap() {
    int minimumWordLength = 9;   //6,5 is a good combo, crazy long words, 9/3 is for really long words
    int minimumAnagrams = 3;
    Map<String, Set<String>> sortedAnagramMap =
        this.anagramMap
            .entrySet().stream()
            .filter(es -> es.getKey().length() >= minimumWordLength)
            .filter(es -> es.getValue().size() >= minimumAnagrams)
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    return sortedAnagramMap;
  }

  //TODO finish a brute force method of every permutation, even if it would perform bad, just as exercise. Would recursive method be ok, or should it be done iteratively?
  //TODO should I reuse the same string builder?
  //TODO I should not look up a solution for this. I ought to be able to find all permutations of a string on my own.
  //TODO This can be broken into two tasks. One is to find all inclusive anagrams (using all letters).
  //TODO The other is to break a group of letters into each possible proper subset of letters.

  //TODO once I have all permutations I can then filter based on if the dictionary contains that String (just use java stream, possibly concurrent?)

}
