package cn.edu.zucc.booklib.control;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import cn.edu.zucc.booklib.model.Orders;
import cn.edu.zucc.booklib.util.BaseException;
import cn.edu.zucc.booklib.util.BusinessException;
import cn.edu.zucc.booklib.util.DBUtil;

public class OrdersManager {
	public void addOrders (Orders ord)throws BaseException
	{
		if(ord.getCustomerID().equals(null)||ord.getOrderDate().equals(null))
		{
			throw new BusinessException("null");
		}
		
		Connection con=null;
		PreparedStatement pst=null;
		try
		{
			con=DBUtil.getConnection();
			String sql="select orderid from  orders where orderid=?";
			pst=con.prepareStatement(sql);
			pst.setInt(1, ord.getOrderID());
			ResultSet rs=pst.executeQuery();
			if(rs.next())
			{
				throw new BusinessException("exist");
			}
			pst.close();
			 sql="insert into orders(orderid, customerid,employeeid,orderdate) "
					+ "values (?,?,?,?) ";
			pst=con.prepareStatement(sql);
			pst.setInt(1, ord.getOrderID());
			pst.setString(2, ord.getCustomerID());
			pst.setInt(3, ord.getEmployeeID());
			pst.setDate(4, new java.sql.Date(ord.getOrderDate().getTime()));
			pst.execute();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		finally {
			try
			{
				con.close();
			} catch (SQLException e)
			{
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
		}
	}
	public List<Orders> searchOrdersByCID(String cid) throws BaseException
	{
		 List<Orders> result =new ArrayList< Orders>();
		Connection con=null;
		PreparedStatement pst=null;
		try
		{
			con=DBUtil.getConnection();
			String sql="select orderid, customerid,employeeid,orderdate from orders where customerid = ?";
			pst=con.prepareStatement(sql);
			pst.setString(1, cid);
			
			ResultSet rs=pst.executeQuery();
			while(rs.next())
			{
				Orders o=new Orders();
				o.setOrderID(rs.getInt(1));
				o.setCustomerID(rs.getString(2));
				o.setEmployeeID(rs.getInt(3));
				o.setOrderDate(rs.getDate(4));
				result.add(o);
			}
			return result;
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		finally {
			try
			{
				con.close();
			} catch (SQLException e)
			{
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
		}
		return null;
	}
	public static void main(String[] args) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			OrdersManager or=new OrdersManager();
	
			List<Orders> result =new ArrayList< Orders>();
		try{
			result =or.searchOrdersByCID("001");
			if(!result.isEmpty())
			{
				for(int i=0;i<result.size();++i)
				{
					Orders o=new Orders(); 
					o=result.get(i);
//					System.out.print(o.getOrderID()+" "+o.getCustomerID()+" "+o.getEmployeeID()+" ");
//					String s=sdf.format(o.getOrderDate());
					System.out.println(o);
				}
			}
		}catch(BaseException e){
			e.printStackTrace();
		}
		
		
		try{
			result =or.searchOrdersByCID("005");
			if(!result.isEmpty())
			{
				for(int i=0;i<result.size();++i)
					{
					Orders o=new Orders(); 
					o=result.get(i);
					System.out.println(o.getOrderID()+" "+o.getCustomerID()+" "+o.getEmployeeID());
					}
			}
			else
			{
				System.out.println("empty");
			}
			
		}catch(BaseException e){
			e.printStackTrace();
		}
		
		
	}
}
