package com.seginf.supersafeapp;

import java.util.List;

public class ListPresenter<T> {
	private List<T> theList;
	public ListPresenter(List<T> content) {
		this.theList = content;
	}

	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for(T c : theList){ 
			sb.append(c.toString());
			sb.append(",");
		}
		sb.setCharAt(sb.length()-1, ']');
		return sb.toString();
	}
}
