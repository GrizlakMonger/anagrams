package jca.anagrams;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import jca.anagrams.AnagramSolvingService.AnagramFinder;

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
 * it highlights the wildcard letters which is nice
 *
 * 9/4/2018 -- I want to make a game analyzer feature where I can setup situations directly.
 * For example, after a real game, I can enter tiles in the pot, and words that players have. Then I can have it display a list
 * of all words. Words that are stolen will be formatted like "reduction + s -> discounter/introduces"
 * It would also be nice to have a letter counter for each player
 *
 * A replay history would also be nice
 *
 * An ingame "hint" button would be nice. Perhaps one could just be making anagrams out of existing words.
 * Another can be for finding steals and new words by including the pot.
 * Maybe even more finegrained: "find 1 letter additions", "find 2 letter additions" which would be based on available letters
 * in the middle, and then it would use the arrow notation to show every word that can be transformed with new tiles
 * (no + if just an anagram).
 *
 * The next hint button would show how likely existing words are to be stealable. This would be based on potential one letter
 * additions (so go through whole alphabet for each existing word to find one letter additions). Then I can either show all of them,
 * or show words that have a greater than x% chance of being stealable (based on either base distribution, or current ingame distribution
 * with tiles already taken).
 *
 * I don't need to divide words by "player" for now. For the word finding I want to do, I don't necessarily need that concept, and
 * can treat all words as being in a common pot. Eventually, I can have a player/team int value that goes with each word.
 * Each team won't even necessarily need its own collection of words. The words themselves will "know" what team they belong to,
 * and that value changes as the word is transformed and recaptured.
 *
 * Once I keep track of a letter combination's history, I can mark off anagrams as they are used, so they can't be reclaimed.
 * I can also at least do a simple plural check heuristic, where a word with a simple -s/-es ending on a used variation doesn't count.
 * This is nice because it handles many plural nouns and also third person present progressive verbs. It doesn't rule
 * out all plurals, and of course there are still plenty of word variations that normally are disallowed that get through
 * (requiring user memory/judgment to avoid those combos). A word "undo" feature would be nice, in case an anagram is realized to be
 * an invalid variation based on word combo history.
 *
 * Eventually also log every peel and word capture/transformation. These can be saved and replayed. Can even replay live games
 * with this feature in custom mode, where you input the "peel" values manually.
 *
 * If in the future there's a case where 2 players own the same word combo, and a player makes an anagram of one or steals with an addition,
 * the system can mention that the word exists with two different owners, and ask which owner you want to take from.
 *
 * The full analysis display will first display all new words from the middle, then anagrams of existing words, then extensions
 * on existing words (possibly separate by number of additional tiles required), then finally whole word combinations (with additional letters).
 *
 * A "potential word hint" will give possible words given the addition of one or two characters (or given the combination of existing words
 * with one or two new characters: a case that seems near impossible to pull off in game, but would be interesting to see).
 *
 * Can use this same basic format for both available plays, and future potential plays, just label above as available or speculative.
 * Speculative will have a different symbol (not +) to indicate that the character isn't available yet, since some speculative
 * will involve a full word, then available letters (indicated by '+') and then speculative, maybe after '?' or '&', or ':'
 * Letters from the middle are separated by spaces, whereas if you added a whole word, the letters would go together.
 * AVAILABLE:
 *  -> tike kite
 *  spear -> pears spare  (only shows unused anagrams in that letter combo history)
 *  join + t -> joint
 *  avenger + s c -> scavenger
 *  team + team + s -> teammates  (this entry would appear twice for now (each instance acting as base word), can dedupe with later code
 *
 *  SPECULATIVE:
 *  : s -> snug guns
 *
 * For speculative, since you can end up with a lot of possibilities per word, you can use a format like this
 *
 * word:
 *   <speculative letter> -> <new word/s>
 *   <speculative letter> -> <new word>
 *     ...
 *
 *
 * Created by janstett on 1/19/17.
 */
public class GameLauncher {

  public static void main(String... args) {
    Scanner input = new Scanner(System.in);
    int MINIMUM_WORD_LENGTH = 4;
    Set<String> dictionary = populateDictionary();

    System.out.println("Building game");
    Instant startTime = Instant.now();
    AnagramFinder anagramFinder = AnagramFinder.buildAnagramFinder(dictionary); // eventually, either inject, or use an anagram module api instead of directly accessing anagram finder methods
    Map<String, Set<String>> sortedMultiplicityAnagramMap = anagramFinder.buildMultiplicityFilteredAnagramMap();
    Map<String, Set<String>> sortedWordLengthAnagramMap = anagramFinder.buildLengthFilteredAnagramMap();
    LetterSource letterSource = new LetterSource();
    LetterCollection inPlay = new LetterCollection();
    inPlay.setDisplayType(LetterCollection.DisplayType.LARGE);
    Instant endTime = Instant.now();
    System.out.println(String.format("Game built in %s nanoseconds", (Duration.between(startTime, endTime).getNano())));

    System.out.println("Enter 'standard' to play a normal game. Enter 'sandbox' to analyze custom scenarios.");

    boolean gameRunning = true;

    while (gameRunning) {
      String userInput = input.nextLine();
      switch (userInput) {
        case "/help":
          System.out.println("standard : play a normal game of anagrams");
          System.out.println("sandbox : create a custom situation to analyze");
          System.out.println("q, e : quit/exit");
          break;
        case "standard":
          Standard standard = new Standard(dictionary, anagramFinder, letterSource, inPlay, input);
          standard.play();
          System.out.println("Welcome back to main menu.");
          break;
        case "sandbox": //as in "peel"
          Sandbox sandbox = new Sandbox(dictionary, anagramFinder, letterSource, inPlay, input);
          sandbox.play();
          System.out.println("Welcome back to main menu.");
          break;
        case "q":
        case "e":
          gameRunning = false;
          break;
      }
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

    // improved dictionary builder, get it to find path correctly (ideally without needing full path, for portability)

  // eventually replace this "monolith" class version. Should the client receive its own version of the dictionary, or call this module everytime?
  // I'm not distributing the system, so I don't have to worry about network stuff. I'm just trying to practice good modularization.

  private Set<String> buildDictionary() {
    List<String> wordList;
    try {
      wordList = Files.readAllLines(new File("wordlist").toPath(), Charset.defaultCharset());
    } catch (IOException e) {
      wordList = Collections.emptyList();
    }
    return new HashSet(wordList);
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