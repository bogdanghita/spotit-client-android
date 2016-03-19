package com.it.spot.login;

import com.google.gson.annotations.Expose;

/**
 * Created by Bogdan on 19/03/2016.
 */
public class UserInfo {

	@Expose
	private String name;
	@Expose
	private String email;
	@Expose
	private String picture;

	public UserInfo(String name, String email, String picture) {
		this.name = name;
		this.email = email;
		this.picture = picture;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public String getPicture() {
		return picture;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setPicture(String picture) {
		this.picture = picture;
	}

	@Override
	public String toString() {
		return name;
	}

	public UserInfo clone() {
		return new UserInfo(name, email, picture);
	}
}
