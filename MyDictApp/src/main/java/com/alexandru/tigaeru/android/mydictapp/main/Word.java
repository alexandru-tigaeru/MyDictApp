package com.alexandru.tigaeru.android.mydictapp.main;

public class Word {

	/**
	 * create a dummy Word
	 */
	public Word() {
		this(1, "dummy", "", "", "", "", "", "", "", "", Type.UNKNOWN);
	}

	public Word(int lesson, String name, String english, String romanian, String antonym, String synonym,
			String flexion, String relatedTerms, String comments, String french, Type type) {
		super();
		this.lesson = lesson;
		this.name = name;
		this.english = english;
		this.romanian = romanian;
		this.antonym = antonym;
		this.synonym = synonym;
		this.flexion = flexion;
		this.relatedTerms = relatedTerms;
		this.comments = comments;
		this.french = french;
		this.type = type;
	}

	private int lesson;

	private String name;

	private String english;

	private String romanian;

	private String antonym;

	private String synonym;

	private String flexion;

	private String relatedTerms;

	private String comments;

	private String french;

	private Type type;

	public enum Type {
		ADVERB, NOUN, ADJECTIVE, VERB, PREPOSITION, CONJUCTION, UNKNOWN
	}

	@Override
	public String toString() {
		return "Word [lesson=" + lesson + ", name=" + name + ", english=" + english + ", romanian="
				+ romanian + ", antonym=" + antonym + ", synonym=" + synonym + ", flexion=" + flexion
				+ ", relatedTerms=" + relatedTerms + ", comments=" + comments + ", french=" + french
				+ ", type=" + type + "]";
	}

	public int getLesson() {
		return lesson;
	}

	public String getName() {
		return name;
	}

	public String getEnglish() {
		return english;
	}

	public String getRomanian() {
		return romanian;
	}

	public String getAntonym() {
		return antonym;
	}

	public String getSynonym() {
		return synonym;
	}

	public String getFlexion() {
		return flexion;
	}

	public String getRelatedTerms() {
		return relatedTerms;
	}

	public String getComments() {
		return comments;
	}

	public String getFrench() {
		return french;
	}

	public Type getType() {
		return type;
	}

}
