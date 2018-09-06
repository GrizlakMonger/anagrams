package jca.anagrams.utils;

import java.util.Arrays;

public class WordUtils {

  public static String sortString(String unsortedString) {
    char[] chars = unsortedString.toCharArray();
    Arrays.sort(chars);
    return new String(chars);
  }
}
