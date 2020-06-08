package cn.edu.zucc.booklib.model;

import java.util.Date;

public class BeanvieworderDetail {
	private  int OrderID ;
	private String ProductName;
	private double UnitPrice;
	private int Quantity;
	private Date OrderDate;
	public int getOrderID()
	{
		return OrderID;
	}
	public String getProductName()
	{
		return ProductName;
	}
	public double getUnitPrice()
	{
		return UnitPrice;
	}
	public int getQuantity()
	{
		return Quantity;
	}
	public Date getOrderDate()
	{
		return OrderDate;
	}
	public void setUnitPrice(double p)
	{
		UnitPrice=p;
	}
	public void setQuantity(int p)
	{
		Quantity=p;
	}
	public void setProductName(String p)
	{
		ProductName=p;
	}
	public void setOrderID(int p)
	{
		OrderID=p;
	}
	public void setOrderDate(Date p)
	{
		OrderDate=p;
	}
}
