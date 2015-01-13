package com.bitmerchant.db;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

public class Tables {
	
	@Table("orders") 
	public static class Order extends Model {}
	
	@Table("orders_view") 
	public static class OrderView extends Model {}
	
	@Table("order_statuses") 
	public static class OrderStatus extends Model {}
	
	@Table("buttons") 
	public static class Button extends Model {}
	
	@Table("buttons_view") 
	public static class ButtonView extends Model {}
	
	@Table("button_types") 
	public static class ButtonType extends Model {}
	
	@Table("button_styles") 
	public static class ButtonStyle extends Model {}
	
	@Table("currencies") 
	public static class Currency extends Model {}
	
	@Table("transactions") 
	public static class Transaction extends Model {}
	
	@Table("refunds") 
	public static class Refund extends Model {}
	

	
	
}
