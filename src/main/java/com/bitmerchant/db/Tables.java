package com.bitmerchant.db;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

public class Tables {
	@Table("employees") 
	public static class Employee extends Model {}
}
