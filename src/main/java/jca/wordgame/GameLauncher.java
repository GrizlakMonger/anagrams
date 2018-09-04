package jca.wordgame;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * The simplest word list I've found that includes inflections is the 2d12 dictionary with a link from the SCOWL site.
 * The 2of4brif is a very clean document, no markings, just words. It has both -ize and -ise endings with inflections for some words.
 * It also does not have proper nouns/capitalized words OR words with apostrophes/other punctuation.
 * The down side is that it does not contain many words. For example, eta (Greek letter) is not in the list.
 *
 * 3of6game list is also likely a good option (contains inflections, no proper names)
 *
 * 2of4brif and 3of6 game are both from his International set, and so are the most common and least borderline words
 *
 * 2of12inf is intended for games.
 *
 * 2+2+3lem seems like a great list, too. It has more formatting, so it would be a more interesting challenge to make
 * a parser for it. For now I used Sublime to format it to a plain list. It has hypens, but that doesn't matter because
 * hyphenated words won't be queried anyways: there are no hyphen tiles.
 *
 * I've heard that the Moby Project has extensive word lists. The common.txt might be a good one to see.
 * from (http://wordlist.aspell.net/12dicts-readme/#2of12inf)
 *
 * I can make several word list options with commands to access them. This will allow you to practice with obscure words
 * or uncontroversial words if you want to expand your vocabulary in a way that won't lead to discussions and word lookups.
 *
 * http://wordlist.aspell.net/12dicts-readme/
 *
 * Anagram solver I've been using: http://www.thewordfinder.com/anagram-solver/
 *
 * Created by janstett on 1/19/17.
 */
public class GameLauncher {

  public static void main(String... args) {
    Scanner input = new Scanner(System.in);
    int MINIMUM_WORD_LENGTH = 4;
    Set<String> dictionary = populateDictionary();

    System.out.println("Enter 'p' to flip a tile! \nType a word to claim it!");

    boolean gameRunning = true;

    LetterSource letterSource = new LetterSource();
    LetterCollection inPlay = new LetterCollection();
    inPlay.setDisplayType(LetterCollection.DisplayType.LARGE);
    WordCollection myWords = new WordCollection();

    while (gameRunning) {
      String userInput = input.nextLine();
      switch (userInput) {
        case "/help":
          System.out.println("p : peel - reveal another character from tiles");
          System.out.println("dp, ds, dl : switch between plain, small, or large display style");
          System.out.println("q, e : quit/exit");
          break;
        case "p": //as in "peel"
          Character nextCharacter = letterSource.nextCharacter();
          if (nextCharacter == null) {
            System.out.println("No more letters");
          } else {
            inPlay.add(nextCharacter);
            System.out.println("New letter: " + nextCharacter);
          }
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

        case "e":
        case "q":
          gameRunning = false;
          break;

        default:
          if (userInput.length() >= MINIMUM_WORD_LENGTH) {
            String word = userInput.toUpperCase();
            boolean isAvailable = inPlay.contains(word);
            if (isAvailable) {
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
    }
  }

  private static Set<String> populateDictionary() {
    Set<String> dictionary = new HashSet<>();
    try {
      Scanner scanner = new Scanner(new File("/Users/janstett/projects/anagrams/src/main/resources/wordlist"));
      while (scanner.hasNextLine()) {
        String word = scanner.nextLine();
        dictionary.add(word);
      }
      scanner.close();
    } catch (FileNotFoundException exception) {
      System.out.println("Word list couldn't be found!");
    }
    return dictionary;
    }
}


// for later: if all caps, then process the input.
// 1) Make a temp copy of in play characters
// 2) For each character in the entered word, check that tempInPlay contains the letter, then remove that character from the list,
// if any .contains fails, then its an invalid word. Just let user know, no need to say which characters are missing.
// 3) now check the dictionary for the word. Can I store the words in a tree of sorts to navigate it faster? I won't be able to
// use an external database, so this will be interesting. Should the words have their own objects with properties? Should I have
// different sets already prepared for different length words?
// DONE, but could improve

//    .---. *---*
//    | A | | D |
//    '---' *---*
//
//    make a method that turns any array/list into a tile display like above (go with simple corners) with its toString method
//  I can also make it so that letters can just be displayed as letters on their own. This may make an options menu desirable
// for more fine tuning, eg : upper case or lower case (then I will need a different marker for actions, like calling for a letter
// to distinguish from a call for a word
// since tiles in an actual game aren't just lined up in one row, I should break up the tiles, at MOST 12 per row (this
// will fit old school 80 column terminal, but maybe even less is better visually

// Have a command to scramble the letters, since display order is not essential in the actual game. Also, display the
// game board after every command. Also, /help is standard while -commands is not. Start checking the dictionary.
// Eventually, claimed words will have a history, so a word can't be reverted to a past form. In a really advanced version,
// I would use the coded dictionary and not allow a word to transform into a related derivation. Maybe even allow the
// viewing of a word's history in your collection of words. Later I will want to make an AI to find words for you.
//
// I could also make a different syntax for transforming one of your words. For example, type the whole existing word, then space,
// then the new word. Code will look at the difference in the two words and see if necessary letters are available.
// If so, transform the word and keep the previous state in the word's history.