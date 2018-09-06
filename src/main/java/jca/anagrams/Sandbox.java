package jca.anagrams;

import java.util.Scanner;
import java.util.Set;

import jca.anagrams.AnagramSolvingService.AnagramFinder;

// sandbox has a letter source, but just to keep track of available letters (letters are pulled out as desired)
// unless user wants to ignore letter constraints altogether and just explore words

public class Sandbox {

  private Set<String> dictionary;
  private AnagramFinder anagramFinder;
  private LetterSource letterSource;
  private LetterCollection inPlay;
  private Scanner input;

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
    System.out.println("Type '-inplay' to add letters to the middle.");
    System.out.println("Hit enter again to finish adding words/letters");
    WordCollection myWords = new WordCollection();
    boolean gameRunning = true;
    int hintLevel = 0;
    while (gameRunning) {
      String userInput = input.nextLine();
      switch (userInput) {
        case "/help":
          System.out.println("'-words' : add words, hit enter again to finish");
          System.out.println("'-inplay' : add letters to middle, hit enter again to finish");
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
                myWords.addWord(newWord); // or just use dictinoary check?
                break;
            }
          }
      }
    }
  }

}
