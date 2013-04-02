package com.alexandru.tigaeru.android.utils;

import java.util.LinkedHashSet;

public class MyLinkedHashSet<E> extends LinkedHashSet<E> {

	private static final long serialVersionUID = -8954006746428161722L;

	@Override
	public String toString() {
		String result = "";
		for (E elem : this) {
			if (result.equals("")) {
				result = result + elem.toString();
			} else {
				result = result + ", " + elem.toString();
			}
		}
		return result;
	}
}
