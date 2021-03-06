package com.alexandru.tigaeru.android.mydictapp.main;

/**
 * 
 * @author Alexandru_Tigaeru
 *
 */
public class Lesson {
	private String name = "Lektion0";
	private int currentNumber = 0;
	private static int instances = 0;

	public Lesson() {
		currentNumber = instances + 1;
		Lesson.addNewLesson();
		this.name = "Lektion" + currentNumber;
	}

	public String getName() {
		return name;
	}

	public int getCurrentNumber() {
		return currentNumber;
	}

	@Override
	public String toString() {
		return this.name;
	}

	public static int getNumberOfLessons() {
		return Lesson.instances;
	}
	
	private synchronized static void addNewLesson(){
		Lesson.instances++;
	}

	public static void resetNrOfLessons() {
		instances = 0;
	}

	public static boolean decrementNrOfLessons() {
		if (instances != 0) {
			instances--;
			return true;
		}
		return false;
	}

}
