package com.alexandru.tigaeru.android.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class TranslationXmlHandler extends DefaultHandler {
	private Set<String> data;
	private String translation = "";
	private boolean isTranslation = false;
	private boolean tokenFound = false;
	private boolean isDeclension = false;
	private boolean isGerman = false;
	private boolean isRelated = false;
	private boolean isAntonym = false;
	private boolean wantDeclension = false;
	private boolean wantRelated = false;
	private boolean wantAntonym = false;
	private String toTranslate;
	private int nrOfTranslations;
	private static final boolean SEARCH_FOR_DECLENSION = true;
	private static final boolean SEARCH_FOR_RELATED = true;
	private static final boolean SEARCH_FOR_ANTONYMS = true;

	private TranslationXmlHandler(Set<String> data, String toTranslate, int nrOfTranslations,
			boolean wantDeclension, boolean wantRelated, boolean wantAntonym) {
		this.data = data;
		this.toTranslate = toTranslate;
		this.nrOfTranslations = nrOfTranslations;
		this.wantDeclension = wantDeclension;
		this.wantRelated = wantRelated;
		this.wantAntonym = wantAntonym;
	}

	public static TranslationXmlHandler createAsAntonym(Set<String> data, String toTranslate, int count) {
		return new TranslationXmlHandler(data, toTranslate, count, !SEARCH_FOR_DECLENSION,
				!SEARCH_FOR_RELATED, SEARCH_FOR_ANTONYMS);
	}

	public static TranslationXmlHandler createAsDeclension(Set<String> data, String toTranslate, int count) {
		return new TranslationXmlHandler(data, toTranslate, count, SEARCH_FOR_DECLENSION,
				!SEARCH_FOR_RELATED, !SEARCH_FOR_ANTONYMS);
	}

	public static TranslationXmlHandler createAsRelated(Set<String> data, String toTranslate, int count) {
		return new TranslationXmlHandler(data, toTranslate, count, !SEARCH_FOR_DECLENSION,
				SEARCH_FOR_RELATED, !SEARCH_FOR_ANTONYMS);
	}

	public static TranslationXmlHandler createAsTranslation(Set<String> data, String toTranslate, int count) {
		return new TranslationXmlHandler(data, toTranslate, count, !SEARCH_FOR_DECLENSION,
				!SEARCH_FOR_RELATED, !SEARCH_FOR_ANTONYMS);
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		super.endElement(uri, localName, qName);
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes)
			throws SAXException {
		super.startElement(uri, localName, qName, attributes);

		if (localName.equals("values")) {
			tokenFound = true;
			isTranslation = true;
		} else {
			if (tokenFound && localName.equals("string")) {
				isTranslation = true;
			} else {
				isTranslation = false;	
			}
			//reset lookup
			tokenFound = false;
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		super.characters(ch, start, length);

		String currText = "";
		for (int i = start; i < start + length; i++) {
			currText += ch[i];
		}

		// Log.d("LINE out: ", curText);
		handleGlosbe(currText);
		if (wantDeclension) {
			handleWikiDeclension(currText);
		}

		if (wantRelated) {
			handleWikiRelated(currText);
		}

		if (wantAntonym) {
			handleWikiAntonym(currText);
		}
	}

	private void handleWikiAntonym(String currText) {
		boolean searchFurther = true;
		if (currText.equals("\n")) {
			// ignore
		} else {
			if (currText.trim().equals("==German==")) {
				isGerman = true;
			}
			if (isAntonym) {
				if (currText.startsWith("*")) {
					currText = currText.replace("*", "");

					if (currText.contains("[[")) {
						currText = currText.replace("[[", "");
						currText = currText.replace("]]", "");
					}
					translation = currText.trim();

					if (isGerman && data.size() < nrOfTranslations && !translation.equals("")) {
						data.add(translation);
					}
					searchFurther = true;
				} else {
					isAntonym = false;
				}
			}

			if (currText.trim().equals("====Antonyms====")) {
				// next non emty line is the one we want
				isAntonym = true;
			} else if (isAntonym && searchFurther) {
				// another related word
				isAntonym = true;
			} else {
				isAntonym = false;
			}
		}
	}

	private void handleWikiRelated(String currText) {
		boolean searchFurther = true;
		if (currText.equals("\n")) {
			// ignore
		} else {
			if (currText.trim().equals("==German==")) {
				isGerman = true;
			}
			if (isRelated) {
				if (currText.startsWith("*")) {
					currText = currText.replace("*", "");

					if (currText.contains("{{")) {
						currText = currText.replace("}}", "");
						currText = currText.replace("{{", "");
						currText = currText.replace("l|de|", "");
					}

					if (currText.contains("[[")) {
						currText = currText.replace("[[", "");
						currText = currText.replace("]]", "");
					}
					translation = currText.trim();

					if (isGerman && data.size() < nrOfTranslations && !translation.equals("")) {
						data.add(translation);
					}
					searchFurther = true;
				} else {
					isRelated = false;
				}
			}

			if (currText.trim().equals("====Derived terms====")) {
				// next non emty line is the one we want
				isRelated = true;
			} else if (isRelated && searchFurther) {
				// another related word
				isRelated = true;
			} else {
				isRelated = false;
			}
		}
	}

	private void handleWikiDeclension(String currText) {
		if (currText.equals("\n")) {
			// ignore
		} else {
			if (currText.trim().equals("==German==")) {
				isGerman = true;
			}
			if (isDeclension) {
				currText = currText.replace("{", "");
				currText = currText.replace("}", "");
				translation = currText.substring(currText.indexOf("|") + 1, currText.length());

				if (translation.contains("|s|")) {
					translation = translation.substring(0, translation.indexOf("|s|") + 2);
				}

				if (translation.contains("|h|")) {
					translation = translation.substring(0, translation.indexOf("|h|") + 2);
				}

				if (translation.contains("|hs|")) {
					translation = translation.substring(0, translation.indexOf("|hs|") + 3);
				}

				// translation = translation.replace("|", "-");

				removeGenitive();

				if (isGerman && data.size() < nrOfTranslations) {
					data.add(translation);
				}
			}

			if (currText.trim().equals("===Noun===") || currText.trim().equals("===Adjective===")
					|| currText.trim().equals("====Conjugation====")) {
				// next non emty line is the one we want
				isDeclension = true;
			} else {
				isDeclension = false;
			}
		}
	}

	private void handleGlosbe(String currText) {
		if (isTranslation) {

			if (currText.trim().equals("") || currText.trim().equals("\n")
					|| currText.trim().equals(toTranslate)) {
				// ignore empty and same word
			} else {
				translation = currText;
				if (data.size() < nrOfTranslations) {
					data.add(translation);
				}
			}
		}
	}

	private void removeGenitive() {
		List<String> tokens = new java.util.ArrayList<String>(Arrays.asList(translation.split(Pattern
				.quote("|"))));

		if (tokens != null && tokens.size() >= 2) {
			if (tokens.get(1).endsWith("s")) {
				tokens.remove(1);
			}
			if (tokens.size() >= 2 && tokens.get(1).startsWith("gen2")) {
				tokens.remove(1);
			}

			// remove unneeded dash after feminin
			if (tokens.size() >= 2 && tokens.get(0).equals("f")) {
				tokens.remove(1);
			}

			if (tokens.size() >= 2 && tokens.get(1).equals("")) {
				tokens.remove(1);
			}

			translation = tokens.toString();
			translation = translation.replace(", ", "-");
			translation = translation.replace("]", "");
			translation = translation.replace("[", "");
		}
	}
}