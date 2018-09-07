package jca.anagrams;

import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import jca.anagrams.AnagramSolvingService.AnagramFinder;

// sandbox has a letter source, but just to keep track of available letters (letters are pulled out as desired)
// unless user wants to ignore letter constraints altogether and just explore words
// the next case I want to add is to go through each word and see what words can be made
// by adding every combination of middle tiles to it. Do not include "standing anagrams", or anagrams that take no extra tiles

public class Sandbox {

  private Set<String> dictionary;
  private AnagramFinder anagramFinder;
  private LetterSource letterSource;
  private LetterCollection inPlay;
  private Scanner input;
  private WordCollection capturedWords;

  int MINIMUM_WORD_LENGTH = 4;

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
                if (!dictionary.contains(newWord.toLowerCase())) {
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
          displayBoard(inPlay, capturedWords);
          System.out.println("Letters have been added.");
          break;
        case "current anagrams":
          Map<String, Set<String>> currentAnagrams = capturedWords.getCurrentAnagrams();
          if (currentAnagrams.isEmpty()) {
            System.out.println("No anagrams from current words.");
          }
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
      }
    }
  }

  // go through each number of addon tiles: 1,2,3---> until end
  private void displayAllExtensionsFromFreeTiles() {
    for (LetterCombination word : capturedWords.getWords()) {

    }

  }

  private static void displayBoard(LetterCollection inPlay, WordCollection myWords) {
    System.out.println("\n----------");
    System.out.println("Letters in play: \n" + inPlay);
    System.out.println();
    if (!myWords.isEmpty()) {
      System.out.println("Your words: \n" + myWords);
      System.out.println();
    }
  }

}
