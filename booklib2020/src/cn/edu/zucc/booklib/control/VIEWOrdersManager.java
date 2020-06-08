package cn.edu.zucc.booklib.control;
import java.util.List;
import java.util.Map;



import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import cn.edu.zucc.booklib.model.BeanvieworderDetail;
import cn.edu.zucc.booklib.util.BaseException;
import cn.edu.zucc.booklib.util.DBUtil;

public class VIEWOrdersManager {
	public List<BeanvieworderDetail> orderQuery2 (int orderid)throws BaseException{
		   //参数为OrderID号：订单号
		List <BeanvieworderDetail> r=new ArrayList<BeanvieworderDetail>();
		Connection con=null;
		PreparedStatement pst=null;
		try {
			con=DBUtil.getConnection();
			String sql="select * from viewOrderDetail where orderid =?";
			pst=con.prepareStatement(sql);
			pst.setInt(1,orderid);
			ResultSet re =pst.executeQuery();
			while(re.next())
			{
				BeanvieworderDetail b=new BeanvieworderDetail();
				b.setOrderID(re.getInt(1));
				b.setProductName(re.getString(2));
				b.setQuantity(re.getInt(3));
				b.setUnitPrice(re.getDouble(4));
				b.setOrderDate(re.getDate(5));
				r.add(b);
			}
			return r;
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		finally {
			if(con!=null)
			{
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
		return null;
		} 
	
	public Map<String,Double> getProductTotalFee(String productName) throws BaseException{
		Connection con=null;
		Map<String,Double>map =new HashMap<String,Double>();
		PreparedStatement pst=null;
		try {
			 con=DBUtil.getConnection();
				String sql="select productname ,sum(quantity *unitprice ) from viewOrderDetail  "
						+ " where  productname like ?  group by productname";
				pst=con.prepareStatement(sql);
				pst.setString(1, "%"+	productName+"%");
				ResultSet re =pst.executeQuery();
				while(re.next())
				{
					map.put(re.getString(1), re.getDouble(2));
				}
				return map;
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		finally {
			if(con!=null)
			{
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
		return null;
		} 
	
	public List<String> getMaxOrderedProductName() throws BaseException{
		   Connection con=null;
			List <String> r=new ArrayList<String>();
			PreparedStatement pst=null;
		   try {
			   con=DBUtil.getConnection();
				String sql="select productname from viewOrderDetail  "
						+ "group by productname having sum(Quantity)>=all(select sum(Quantity)"
						+ " from viewOrderDetail group by productname)";
				pst=con.prepareStatement(sql);
				ResultSet re =pst.executeQuery();
				while(re.next())
				{
					String s=re.getString(1);
					r.add(s);
				}
				return r;
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
	public static void main(String[] args)
	{
		VIEWOrdersManager v=new VIEWOrdersManager();
		BeanvieworderDetail bv=new BeanvieworderDetail();
		try {
			
//			Map<String,Double>map=v.getProductTotalFee("sdg");
			List<String >r=v.getMaxOrderedProductName();
			if(!r.isEmpty())
			{
				for(var s:r)
				{
					System.out.println(s);
				}
				
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		// TODO 自动生成的方法存根

	}

}
