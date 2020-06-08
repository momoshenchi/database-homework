package cn.edu.zucc.booklib.control;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import cn.edu.zucc.booklib.model.Products;
import cn.edu.zucc.booklib.util.BaseException;
import cn.edu.zucc.booklib.util.BusinessException;
import cn.edu.zucc.booklib.util.DBUtil;

public class ProductsManager {

	public void modifyProducts (Products p)throws BaseException
	{
		if(p.getProductID()==0||p.getProductName()==null||p.getUnitPrice()==0||p.getUnitsInStock()==0)
			throw new BusinessException("null");
		Connection con=null;
		Statement st=null;
		try
		{
			con=DBUtil.getConnection();
			st=con.createStatement();
			String sql="update products set productname ='"+p.getProductName()+"', unitprice = "+p.getUnitPrice()
					+ " , unitsinstock="+p.getUnitsInStock()+" where productid="+p.getProductID();
		st.execute(sql);
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		finally {
			try
			{
				st.close();
			} catch (SQLException e)
			{
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
			try
			{
				con.close();
			} catch (SQLException e)
			{
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			};
		}
	}
	public void modifyProductsByPrepared(Products p)throws BaseException
	{
		if(p.getProductID()==0||p.getProductName()==null||p.getUnitPrice()==0||p.getUnitsInStock()==0)
			throw new BusinessException("null");
		Connection con=null;
		PreparedStatement pst=null;
		try
		{
			con=DBUtil.getConnection();
			String sql="select * from products where productid=?";
			pst=con.prepareStatement(sql);
			pst.setInt(1, p.getProductID());
			java.sql.ResultSet rs=pst.executeQuery();
			if(!rs.next())
				throw new BusinessException("not exist");
			rs.close();
			pst.close();
			 sql="update products set productname =?, unitprice = ?,"
					+ "  unitsinstock=? where productid= ? ";
			pst=con.prepareStatement(sql);
			pst.setString(1, p.getProductName());
			pst.setDouble(2, p.getUnitPrice());
			pst.setInt(3, p.getUnitsInStock());
			pst.setInt(4, +p.getProductID());
			pst.execute();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		finally {
			try
			{
				pst.close();
			} catch (SQLException e)
			{
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
			try
			{
				con.close();
			} catch (SQLException e)
			{
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			};
		}
	}
	public static void main(String[] args) {

		//ģ����ѯ
		ProductsManager pm=new ProductsManager();
			Products p=new Products();
		try{
			
			p.setProductID(6);
			p.setProductName("asd");
			p.setUnitPrice(12.2);
			p.setUnitsInStock(26);
			pm.modifyProductsByPrepared(p);
			
		}catch(BaseException e){
			e.printStackTrace();
		}
//		try{
//			p.setProductID(0);
//			p.setProductName("效果");
//			p.setUnitPrice(12);
//			p.setUnitsInStock(56);
//			pm.modifyProducts(p);
//		}catch(BaseException e){
//			e.printStackTrace();
//		}
		
		
	}
}
