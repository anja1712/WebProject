package com.bank.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Constants {

	public static final DateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd");
	public static final DateFormat FORMATTER_DATETIME = new SimpleDateFormat("yyyy-MM-dd_hh-mm");

	public static final String SECRET_KEY = "Bar12345Bar12345";
	public static final String IV_KEY = "KrambabuliPunch@";
	public static final String DB_SCHEMA = "bank";

	public static String BASE_CONFIRMATION_DOMAIN = "http://213.199.32.117";

	public static final String ADMIN_EMAIL = "admin@admin.com";
	public static final String ADMIN_PASS = "adminpass";
	public static final String USER_ROLE = "USER";
	public static final String ADMIN_ROLE = "ADMIN";

	//	"email": "adriana.anja@myemail.com",
	//	"password": "123456"

	//"email": "nina.antic@myemail.com",
	//"password": "123123"

}
