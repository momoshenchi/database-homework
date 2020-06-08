package cn.edu.zucc.booklib.model;

import java.util.Date;

public class Orders {
	private int OrderID;
	private String CustomerID;
	private int EmployeeID;
	private Date OrderDate;
	public int getOrderID() {
		return OrderID;
	}
	public String getCustomerID() {
		return CustomerID;
	}
	public int getEmployeeID() {
		return EmployeeID;
	}
	public Date getOrderDate() {
		return OrderDate;
	}
	public void setOrderID(int orderID) {
		OrderID = orderID;
	}
	public void setCustomerID(String customerID) {
		CustomerID = customerID;
	}
	public void setEmployeeID(int employeeID) {
		EmployeeID = employeeID;
	}
	public void setOrderDate(Date orderDate) {
		OrderDate = orderDate;
	}
}
