# anagrams

NEXT STEPS (9/6/2018)
in plain English

take each exising word: eg fish, plank, snake

might be nice to have intermediate class like ExtensionAttempt

it will have the original word and the attempted merge letters
it will be constructed out of each combination from the inPlay combination iterable
all the work will happen at construction.
These extension attempts will be the value of the map (and each extension attempt can unfold into a set of new words/anagrams)
Along with free letters, it can also come with the whole words attempted, so that they are available to print

They will have their raw sorted string (which might as well be used for hashing and equals if need be)
It will have a boolean that is set at construction that says if any actual words exist for the combo. Those that don't exist can be quickly filtered out.

Maybe each line can have its own "toString", since its a console based game and I end up doing a lot of printing.
To string will format like this:

<original word> + <new letters, separated by space> -> <anagrams, separated by space>


This might be wasting too much space with new objects though

getNumberOfNewLetters() {
    return inPlayCombo.size();
}


