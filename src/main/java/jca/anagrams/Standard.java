package jca.anagrams;

import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import jca.anagrams.AnagramSolvingService.AnagramFinder;

public class Standard  {

  private Set<String> dictionary;
  private AnagramFinder anagramFinder;
  private LetterSource letterSource;
  private LetterCollection inPlay;
  private Scanner input;

  int MINIMUM_WORD_LENGTH = 4;

  public Standard(Set<String> dictionary, AnagramFinder anagramFinder, LetterSource letterSource, LetterCollection inPlay, Scanner input) {
    this.dictionary = dictionary;
    this.anagramFinder = anagramFinder;
    this.letterSource = letterSource;
    this.inPlay = inPlay;
    this.input = input;
  }


  public void play() {
    System.out.println("Enter 'p' to flip a tile! \nType a word to claim it!");
    WordCollection myWords = new WordCollection(anagramFinder);
    boolean gameRunning = true;
    int hintLevel = 0;
    while (gameRunning) {
      String userInput = input.nextLine();
      switch (userInput) {
        case "/help":
          System.out.println("p : peel - reveal another character from tiles");
          System.out.println("dp, ds, dl : switch between plain, small, or large display style");
          System.out.println("q, e : quit/exit");
          System.out.println("h : next hint for current board");
          break;
        case "p": //as in "peel"
          Character nextCharacter = letterSource.nextCharacter();
          if (nextCharacter == null) {
            System.out.println("No more letters");
          } else {
            inPlay.add(nextCharacter);
            System.out.println("New letter: " + nextCharacter);
          }
          hintLevel = 0;
          displayBoard(inPlay, myWords);
          break;
        case "dp": // as in display-plain
          String tileSize = inPlay.setDisplayType(LetterCollection.DisplayType.PLAIN);
          System.out.println(tileSize + " display");
          displayBoard(inPlay, myWords);
          break;
        case "ds":
          tileSize = inPlay.setDisplayType(LetterCollection.DisplayType.SMALL);
          System.out.println(tileSize + " display");
          displayBoard(inPlay, myWords);
          break;
        case "dl":
          tileSize = inPlay.setDisplayType(LetterCollection.DisplayType.LARGE);
          System.out.println(tileSize + " display");
          displayBoard(inPlay, myWords);
          break;

        // I could also make this a progressive case, where there is a counter: first hint just lets you know there are words,
        // second time (in same turn) tells you how many combos there are, third time shows the combos, fourth time shows the words
        // eventually have separate hint categories in message, eg: "3 new combos, and 2 anagrams of current words, and 1 extension of current word"
        case "h":
          Map<String, Set<String>> anagramsByCombo = anagramFinder.findSubAnagrams(inPlay.getAllLettersAsString());
          if (anagramsByCombo.isEmpty()) {
            System.out.println("There are no words in the middle");
          } else {
            switch (hintLevel) {
              case 0:
                System.out.println("There is something there.");
                break;
              case 1:
                for (String combo: anagramsByCombo.keySet()) {
                  System.out.println(combo);
                }
                break;
              case 2:
                for (Set<String> anagrams : anagramsByCombo.values()) {
                  System.out.println(anagrams.stream().collect(Collectors.joining(" ")));
                }
                hintLevel -= 1; // hacky way of keeping the value constant at the end
                break;
            }
          }
          hintLevel += 1;
          break;
        case "-combos":
          anagramsByCombo = anagramFinder.findSubAnagrams(inPlay.getAllLettersAsString());
          if (anagramsByCombo.isEmpty()) {
            System.out.println("There are no combos in the middle");
          } else {
            for (String combo: anagramsByCombo.keySet()) {
              System.out.println(combo);
            }
          }
          displayBoard(inPlay, myWords);
          break;
        case "-words":
          anagramsByCombo = anagramFinder.findSubAnagrams(inPlay.getAllLettersAsString());
          if (anagramsByCombo.isEmpty()) {
            System.out.println("There are no words in the middle");
          } else {
            for (Set<String> anagrams : anagramsByCombo.values()) {
              System.out.println(anagrams.stream().collect(Collectors.joining(" ")));
            }
          }
          displayBoard(inPlay, myWords);
          break;
        case "e":
        case "q":
          gameRunning = false;
          break;
        default:
          if (userInput.length() >= MINIMUM_WORD_LENGTH) {


            String word = userInput.toUpperCase();

            boolean isAvailableInMiddle = inPlay.contains(word);
            if (isAvailableInMiddle) {
              if (dictionary.contains(word.toLowerCase())) {
                inPlay.removeWord(word);
                myWords.addWord(word);
              } else {
                System.out.println(word + " is not a valid word");
              }
            } else {
              System.out.println("Necessary letters not available");
            }
          }
          displayBoard(inPlay, myWords);
          break;
      }
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
