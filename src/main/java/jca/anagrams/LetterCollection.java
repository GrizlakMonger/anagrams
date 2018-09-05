package jca.anagrams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class represents a collection of letters that have no meaningful order or partitions.
 * For now, this is being used to represent all of the letters available in the middle.
 * Because there are duplicate tiles, a set is not appropriate.
 * A letter collection is DISTINCT from a word, in that words are bounded and stay together as
 * letter combinations once they are formed.
 *
 * Created by janstett on 1/25/17.
 */

public class LetterCollection {
  private List<Character> characters = new ArrayList<>();
  private DisplayType displayType = DisplayType.PLAIN;

  private String TOP_LINE    = ".---. ";
  private String BOTTOM_LINE = "'---' ";
  private int TILES_PER_LINE = 10;

  public LetterCollection() {
  }

  public LetterCollection(List<Character> characters) {
    this.characters = characters;
  }

  public boolean add(Character character) {
    return characters.add(character);
  }

  public boolean removeAll(List<Character> characters) {
    return characters.removeAll(characters);
  }

  public boolean removeWord(String word) {
    return removeWordFromCharacterList(word, characters);
  }

  public boolean contains(String word) {
    List<Character> tempCharacters = new ArrayList<>(characters);
    return removeWordFromCharacterList(word, tempCharacters);
  }

  public String getAllLettersAsString() {
    return characters.stream()
        .map(String::valueOf)
        .collect(Collectors.joining());
  }

  private boolean removeWordFromCharacterList(String word, List<Character> characters) {
    for (int i = 0; i < word.length(); i += 1) {
      Character character = word.charAt(i);
      boolean isValidCharacter = characters.remove(character); // can't just use .contains() because duplicate letters
      if (!isValidCharacter) {
        return false;
      }
    }
    return true;
  }

  public String asWordString() {
    StringBuilder stringBuilder = new StringBuilder(characters.size());
    for (Character character : characters) {
      stringBuilder.append(character);
    }
    return stringBuilder.toString();
  }

  public String setDisplayType(DisplayType displayType) {
    this.displayType = displayType;
    return displayType.toString().toLowerCase();
  }

  // this can be redone with polymorphism: just have subclasses with different toString methods
  private String buildTileDisplay() {
    StringBuilder stringBuilder = new StringBuilder();
    int indexBegin = 0;
    int indexEnd = TILES_PER_LINE;
    char[] primitiveChar = new char[characters.size()];
    for (int i = 0; i < characters.size(); i += 1) {
      primitiveChar[i] = characters.get(i);
    }

    char[] tempArray;
    while (characters.size() > indexEnd) {
      tempArray = Arrays.copyOfRange(primitiveChar, indexBegin, indexEnd);
      String rowOfTiles = buildRowOfTiles(tempArray);
      indexBegin += TILES_PER_LINE;
      indexEnd += TILES_PER_LINE;
      stringBuilder.append(rowOfTiles + "\n");
    }
    tempArray = Arrays.copyOfRange(primitiveChar, indexBegin, characters.size());
    String lastRow = buildRowOfTiles(tempArray);
    stringBuilder.append(lastRow);

    return stringBuilder.toString();
  }

  private String buildRowOfTiles(char[] characters) {
    StringBuilder stringBuilder = new StringBuilder();
    switch (displayType) {
      case PLAIN:
        for (Character character : characters) {
          stringBuilder.append(character + " ");
        }
        break;

      case SMALL:
        for (Character character : characters) {
          stringBuilder.append("[" + character + "] ");
        }
        break;

      case LARGE:
        int size = characters.length;
        for (int i = 0; i < size; i += 1) {
          stringBuilder.append(TOP_LINE);
        }
        stringBuilder.append("\n");

        for (Character character : characters) {
          stringBuilder.append("| " + character + " | ");
        }
        stringBuilder.append("\n");

        for (int i = 0; i < size; i += 1) {
          stringBuilder.append(BOTTOM_LINE);
        }
        break;

      default:
        break;
    }
    return stringBuilder.toString();
  }

  @Override
  public String toString() {
    return buildTileDisplay();
  }

  public enum DisplayType {
    PLAIN(0),
    SMALL(1),
    LARGE(2);

    private final int index;

    DisplayType(int index) {
      this.index = index;
    }

    public int getIndex() {
      return index;
    }
  }
}
