package transcription;


//The idea behind this wordparser is that if we order the horizontal distances
//between segments (corresponding to size of whitespace between characters)
//from smallest to largest
//We should be able to see a distinct jump where we move from characters
//to spaces

/*More rigorously: it's probably true that both word-spaces and inter-character-spaces
 * have their own normal distributions... If we could somehow split them into these
 * distributions we'd be sorted... but that would be difficult as then we'd
 * still have to choose a percentile on one of the distributions corresponding
 * to what no longer constitutes a space.
 * 
 * I suppose we could so some kind of thing like.. if the distributions non-negligibly overlap 
 * in say the 99th percentile then place it half way on their overlaps... this
 * would minimise space classifications without giving a preference to one particular
 * kind of space
 */

//For now... we'll lists in ascending order the size of spaces...
// Then we'll try to find the big step up from character spaces to word spaces
// {0, 0, 0, 0, 0, 1, 1, 2, 2, 3, 5, 5, 5, 7, 7, 8, 16, 24, 29}
//Here the step is probably between 8 and 16... so we could say that
// if the width is < 16+8/2 = 12 then its a character space
// if it's > then it's a wordspace... that's the idea/

//PROBLEM: How the ~!$# do you find a sensible definition for the "big step"
public class WordParserBySeparation implements Parser {

	public TranscriptionData parse(TranscriptionData data) {
		// empty method for now
		return null;
	}

}
