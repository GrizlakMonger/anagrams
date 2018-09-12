package jca.anagrams;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import jca.anagrams.AnagramSolvingService.AnagramFinder;

// sandbox has a letter source, but just to keep track of available letters (letters are pulled out as desired)
// unless user wants to ignore letter constraints altogether and just explore words
// the next case I want to add is to go through each word and see what words can be made
// by adding every combination of middle tiles to it. Do not include "standing anagrams", or anagrams that take no extra tiles
// 'chances' command can show the probability of any of the tiles being next GIVEN the current tile usage
// this might not be as generally applicable because of the randomness of it. Giving an unweighted "potential"
// separates out concerns: here are possible English words-you sort out the likelihood (or I can give the likelihood based
// on the game distribution, but just exclude the existing letters for the word to be transformed)


public class Sandbox {

  private Set<String> dictionary;
  private AnagramFinder anagramFinder;
  private LetterSource letterSource;
  private LetterCollection inPlay;
  private Scanner input;
  private WordCollection capturedWords;

  int MINIMUM_WORD_LENGTH = 4;  // normally 4, but use other values to explore words

  public Sandbox(Set<String> dictionary, AnagramFinder anagramFinder, LetterSource letterSource, LetterCollection inPlay, Scanner input) {
    this.dictionary = dictionary;
    this.anagramFinder = anagramFinder;
    this.letterSource = letterSource;
    this.inPlay = inPlay;
    this.input = input;
  }

  public void play() {
    System.out.println("Type '-words' to add to completed word collection.");
    System.out.println("Type '-tiles' to add letters to the middle.");
    System.out.println("Hit enter again to finish adding words/letters");
    capturedWords = new WordCollection(anagramFinder);
    boolean gameRunning = true;
    int hintLevel = 0;
    while (gameRunning) {
      String userInput = input.nextLine();
      switch (userInput) {
        case "/help":
          System.out.println("'-words' : add words, hit enter after each word and again to finish");
          System.out.println("'-tiles' : add letters to middle, hit enter again to finish");
          System.out.println("'current anagrams' : show anagrams of individual captured words");
          System.out.println("'extensions' : show all possible word extensions");
          System.out.println("'analyze' : show all possible actions on current board");
          System.out.println("'potential' : show all letters that would allow new actions (does not consider remaining tile frequency)");
          System.out.println("dp, ds, dl : switch between plain, small, or large display style");
          System.out.println("q, e : quit/exit");
          System.out.println("h : next hint for current board");
          break;
        case "-words":
          boolean addWords = true;
          System.out.println("Type in words and hit enter after each one. Hit enter again when finished.");
          while (addWords) {
            String newWord = input.nextLine();
            switch (newWord) {
              case "":
                System.out.println("Words have been added.");
                addWords = false;
                break;
              default:
                if (newWord.length() < MINIMUM_WORD_LENGTH) {
                  System.out.println("Word is too short");
                  break;
                }
                if (!dictionary.contains(newWord.toLowerCase())) {   // comment out to search potentials on unfinished words
                  System.out.println("Word not in dictionary");
                  break;
                }
                capturedWords.addWord(newWord);
                break;
            }
          }
          break;
        case "-tiles":
          System.out.println("Type in letters adjacently in one string and hit enter to submit.");
          String inPlayString = input.nextLine().toUpperCase();
          char[] inPlayCharacters = inPlayString.toCharArray();
          for (char c : inPlayCharacters) {
            inPlay.add(c);
          }
          displayBoard();
          System.out.println("Letters have been added.");
          break;
        case "current anagrams":
          displayCurrentAnagrams();
          break;
        case "in play":
          Map<String, Set<String>> anagramsByCombo = anagramFinder.findSubAnagrams(inPlay.getAllLettersAsString());
          if (anagramsByCombo.isEmpty()) {
            System.out.println("There are no words in the middle");
          } else {
            for (Set<String> anagrams : anagramsByCombo.values()) {
              System.out.println(anagrams.stream().collect(Collectors.joining(" ")));
            }
          }
          break;
        case "tile extensions":
          displayTileExtensions();
          break;
        case "analyze":
          Instant start = Instant.now();
          displayCurrentAnagrams();
          displayAllExtensions();
          Instant end = Instant.now();
          Duration elapsedTime = Duration.between(start, end);
          System.out.println("Analysis completed in " + (elapsedTime.getNano() / 100_000) + "ms.");
          break;
        case "potential":
          start = Instant.now();
          displayNewTilePotentialsForExistingWords();
          end = Instant.now();
          elapsedTime = Duration.between(start, end);
          System.out.println("Analysis completed in " + (elapsedTime.getNano() / 100_000) + "ms.");
          break;
        case "crazy words":
          displayVolatileWords();
          break;
        case "safe words":
          displayStableWords();
          // displaySafestWords() //write a method that goes through the dictionary, gets all words of less than 8 characters,
          // then applies the "potential" to every word. We want to show the words with no results.
          // I could also do one for volatile words (most letters that would steal it).
          // need to map the mergeAttempts to their base word, and not filter out invalid ones.
          // in fact, we want to filter out valid ones, so that we are left with base words that are most safe (and maybe display its valid anagrams)
          // in short, we stream all merge attempts, group them by baseWord, then set the value to a count (or just keep a list of validMergeAttempts, then use .size() or something)
          break;
        default:
          // fill in in case mistype?
          break;
      }
    }
  }

  private void displayCurrentAnagrams() {
    Map<String, Set<String>> currentAnagrams = capturedWords.getCurrentAnagrams();
    if (currentAnagrams.isEmpty()) {
      System.out.println("No anagrams from current words.");
    } else {
      System.out.println("ANAGRAMS OF CURRENT WORDS:");
      for (Map.Entry<String, Set<String>> entry : currentAnagrams.entrySet()) {
        String currentWord = entry.getKey();
        Set<String> correspondingAnagrams = entry.getValue();
        if (!correspondingAnagrams.isEmpty()) {
          StringBuilder stringBuilder = new StringBuilder();
          stringBuilder.append(currentWord);
          stringBuilder.append("  ->  ");
          String chainedAnagrams = correspondingAnagrams.stream().collect(Collectors.joining("  "));
          stringBuilder.append(chainedAnagrams);
          System.out.println(stringBuilder.toString());
        }
      }
    }
  }

  private void displayTileExtensions() {
    System.out.println("ALL TILE WORD EXTENSIONS:");
    List<String> myWords = capturedWords.getWords().stream()
        .map(LetterCombination::getCurrentWord)
        .collect(Collectors.toList());
    Map<String, Set<MergeAttempt>> allTileExtensions = anagramFinder.findAllExtensionsWithoutWordMerges(inPlay.getAllLettersAsString(), myWords);
    allTileExtensions.values().stream().flatMap(set -> set.stream()).forEach(ma -> System.out.println(ma.toString()));
  }

  private void displayAllExtensions() {
    System.out.println("ALL WORD EXTENSIONS:");
    List<String> myWords = capturedWords.getWords().stream()
        .map(LetterCombination::getCurrentWord)
        .collect(Collectors.toList());
    Set<MergeAttempt> allExtensions = anagramFinder.findAllExtensions(inPlay.getAllLettersAsString(), myWords);
    allExtensions.stream().forEach(ma -> System.out.println(ma.toString()));
  }

  // this method only looks at existing words, and doesn't count center tiles towards progress
  // I might want a new 'TileSimulator' class, since speculating tiles seems distinct from finding anagrams out of existing tiles
  // For now I can mix the concerns but then I can refactor for clarity later.
  public void displayNewTilePotentialsForExistingWords() {
    Set<String> myWords = capturedWords.getWords().stream()
        .map(LetterCombination::getCurrentWord)
        .collect(Collectors.toSet());
    Set<MergeAttempt> allPotentialWords = anagramFinder.getAllPotentialWords(myWords);
    Map<Integer, List<MergeAttempt>> wordsByNewLetters = allPotentialWords.stream().collect(Collectors.groupingBy(MergeAttempt::getNumberOfSpeculativeLetters));
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<Integer, List<MergeAttempt>> entry : wordsByNewLetters.entrySet()) {
      System.out.println("LETTERS NEEDED: " + entry.getKey());
      entry.getValue().stream().sorted(Comparator.comparing(MergeAttempt::getBaseWord)).forEach(ma -> System.out.println(ma.toString()));
    }
  }

  public void displayVolatileWords() {
    // instead of dictionary entries, I could also go by anagramMap keys, to avoid technical duplicates
    Set<String> fourLetters = dictionary.stream()
        .filter(word -> word.length() == 4)
        .collect(Collectors.toSet());
    Set<MergeAttempt> crazyWords = anagramFinder.getCrazyWords(fourLetters);
    Map<Integer, List<MergeAttempt>> wordsByNewLetters = crazyWords.stream()
        .collect(Collectors.groupingBy(MergeAttempt::getNumberOfSpeculativeLetters));
    for (Map.Entry<Integer, List<MergeAttempt>> entry : wordsByNewLetters.entrySet()) {
      System.out.println("LETTERS NEEDED: " + entry.getKey());
      entry.getValue().stream()
          .sorted(Comparator.comparing(MergeAttempt::getBaseWord).thenComparing(MergeAttempt::getPotentialLetters))  // example of "then comparing"
          .forEach(ma -> System.out.println(ma.toString()));
    }
  }

  public void displayStableWords() {
    Set<String> filteredWords = dictionary.stream()
        .filter(word -> word.length() == 6)
        .collect(Collectors.toSet());
    Set<MergeAttempt> stableWords = anagramFinder.getStableWords(filteredWords);
    stableWords.stream().sorted(Comparator.comparing(MergeAttempt::getBaseWord))
        .forEach(ma -> System.out.println(ma.toString()));

  }

  // write one like above, but that also considers existing extension potential, so if a need letter is in the middle,
  // we point out that theres only one missing letter instead of 2 for a word.

  private void displayBoard() {
    System.out.println("\n----------");
    System.out.println("Letters in play: \n" + inPlay);
    System.out.println();
    if (!capturedWords.isEmpty()) {
      System.out.println("Your words: \n" + capturedWords);
      System.out.println();
    }
  }

}
