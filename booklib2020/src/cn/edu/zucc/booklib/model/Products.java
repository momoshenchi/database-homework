package cn.edu.zucc.booklib.model;

public class Products {
	private int ProductID;
	private String ProductName;
	private double UnitPrice;
	private int UnitsInStock;
	public int getProductID() {
		return ProductID;
	}
	public String getProductName() {
		return ProductName;
	}
	public double getUnitPrice() {
		return UnitPrice;
	}
	public int getUnitsInStock() {
		return UnitsInStock;
	}
	public void setProductID(int productID) {
		ProductID = productID;
	}
	public void setProductName(String productName) {
		ProductName = productName;
	}
	public void setUnitPrice(double unitPrice) {
		UnitPrice = unitPrice;
	}
	public void setUnitsInStock(int unitsInStock) {
		UnitsInStock = unitsInStock;
	}
	
}
