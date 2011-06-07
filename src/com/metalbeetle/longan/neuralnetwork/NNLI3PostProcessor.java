package com.metalbeetle.longan.neuralnetwork;

import com.metalbeetle.longan.Letter;
import com.metalbeetle.longan.stage.PostProcessor;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

/** Post-processor to fix the systemic errors NNLetterIdentifier3 makes. */
public class NNLI3PostProcessor implements PostProcessor {
	static final double NUDGE = 0.25;
	
	static final HashMap<String, Double> LOWER_TO_UPPER_SIZE_BOUNDARY = new HashMap<String, Double>();
	static {
		// Values arrived at by eyeballing the {letter}-size.atr files and some fudging.
		LOWER_TO_UPPER_SIZE_BOUNDARY.put("c", 1.25);
		LOWER_TO_UPPER_SIZE_BOUNDARY.put("m", 1.5);
		LOWER_TO_UPPER_SIZE_BOUNDARY.put("o", 1.25);
		LOWER_TO_UPPER_SIZE_BOUNDARY.put("p", 1.5);
		LOWER_TO_UPPER_SIZE_BOUNDARY.put("s", 1.25);
		LOWER_TO_UPPER_SIZE_BOUNDARY.put("u", 1.22);
		LOWER_TO_UPPER_SIZE_BOUNDARY.put("v", 1.22);
		LOWER_TO_UPPER_SIZE_BOUNDARY.put("w", 1.5);
		LOWER_TO_UPPER_SIZE_BOUNDARY.put("x", 1.25);
		LOWER_TO_UPPER_SIZE_BOUNDARY.put("z", 1.22);
	}
	
	public void process(
			ArrayList<ArrayList<ArrayList<Letter>>> lines,
			BufferedImage img,
			HashMap<String, String> metadata)
	{
		for (ArrayList<ArrayList<Letter>> line : lines) {
			for (ArrayList<Letter> word : line) {
				for (int i = 0; i < word.size(); i++) {
					Letter l = word.get(i);
					boolean first = i == 0;
					boolean last = i == word.size() - 1;
					
					// Zero identification
					if (l.bestLetter().matches("[o0]") &&
						(
							(!first && word.get(i - 1).bestLetter().matches("[0-9]")) ||
							(!last && word.get(i + 1).bestLetter().matches("[0-9]"))
						)
					)
					{
						l.possibleLetters.put("0", l.bestScore() + NUDGE);						
					} else {
						l.possibleLetters.put("0", l.bestScore() - NUDGE);
					}
										
					//for (String s : NNLetterIdentifier3.CASE_MERGED) {
					if (NNLetterIdentifier3.CASE_MERGED.contains(l.bestLetter().toLowerCase())) {
						String s = l.bestLetter().toLowerCase();
						if (l.location.relativeSize > LOWER_TO_UPPER_SIZE_BOUNDARY.get(s)) {
							l.possibleLetters.put(s, l.possibleLetters.get(s) - NUDGE);
						} else {
							l.possibleLetters.put(s.toUpperCase(), l.possibleLetters.get(s.toUpperCase()) - NUDGE);
						}
					}
					
					// Comma/single quote identification
					if (l.bestLetter().matches("[,']")) {
						l.possibleLetters.put(l.location.relativeLineOffset > 0 ? "," : "'",
								l.bestScore() + NUDGE);
					}
				}
			}
		}
	}
}
