package cn.edu.zucc.booklib.control;
import java.util.Scanner;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import cn.edu.zucc.booklib.model.BeanPublisher;
import cn.edu.zucc.booklib.model.BeanReaderType;
import cn.edu.zucc.booklib.util.BaseException;
import cn.edu.zucc.booklib.util.BusinessException;
import cn.edu.zucc.booklib.util.DBUtil;
import cn.edu.zucc.booklib.util.DbException;

public class PublisherManager {
	public List<BeanPublisher> loadAllPublisher() throws BaseException {
		List<BeanPublisher> result = new ArrayList<BeanPublisher>();
		Connection conn = null;
		try {
			conn = DBUtil.getConnection();
			String sql = "select pubid,publisherName,address from BeanPublisher order by pubid";
			java.sql.Statement st = conn.createStatement();
			java.sql.ResultSet rs = st.executeQuery(sql);
			while (rs.next()) {
				BeanPublisher p = new BeanPublisher();
				p.setPubid(rs.getString(1));
				p.setPublisherName(rs.getString(2));
				p.setAddress(rs.getString(3));
				result.add(p);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DbException(e);
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return result;
	}

	public void createPublisher(BeanPublisher p) throws BaseException {
		if (p.getPubid() == null || "".equals(p.getPubid()) || p.getPubid().length() > 20) {
			throw new BusinessException("出版社编号必须是1-20个字");
		}
		if (p.getPublisherName() == null || "".equals(p.getPublisherName()) || p.getPublisherName().length() > 50) {
			throw new BusinessException("出版社名称必须是1-50个字");
		}
		if (p.getAddress() == null || "".equals(p.getAddress()) || p.getAddress().length() > 100) {
			throw new BusinessException("出版地址必须是1-100个字");
		}

		Connection conn = null;
		try {
			conn = DBUtil.getConnection();
			String sql = "select * from BeanPublisher where pubid=?";
			java.sql.PreparedStatement pst = conn.prepareStatement(sql);
			pst.setString(1, p.getPubid());
			java.sql.ResultSet rs = pst.executeQuery();
			if (rs.next())
				throw new BusinessException("出版社编号已经被占用");
			rs.close();
			pst.close();
			sql = "select * from BeanPublisher where publisherName=?";
			pst = conn.prepareStatement(sql);
			pst.setString(1, p.getPublisherName());
			rs = pst.executeQuery();
			if (rs.next())
				throw new BusinessException("出版社名称已经存在");
			rs.close();
			pst.close();
//			sql = "insert into BeanPublisher(pubid,publisherName,address) values(?,?,?)";
			sql = "insert into BeanPublisher(pubid,publisherName,address) values('"
			+p.getPubid().replaceAll("'", "\\\\'")+"','"+p.getPublisherName().replaceAll("'", "\\\\'")+"','"+p.getAddress().replaceAll("'", "\\\\'")+"')";
			
			java.sql.Statement st = conn.createStatement();
			st.execute(sql);
			st.close();
//			pst = conn.prepareStatement(sql);
//			pst.setString(1, p.getPubid());
//			pst.setString(2, p.getPublisherName());
//			pst.setString(3, p.getAddress());
//			pst.execute();
//			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DbException(e);
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}

	}

	public void modifyPublisher(BeanPublisher p) throws BaseException {
		if (p.getPubid() == null || "".equals(p.getPubid()) || p.getPubid().length() > 20) {
			throw new BusinessException("出版社编号必须是1-20个字");
		}
		if (p.getPublisherName() == null || "".equals(p.getPublisherName()) || p.getPublisherName().length() > 50) {
			throw new BusinessException("出版社名称必须是1-50个字");
		}
		if (p.getAddress() == null || "".equals(p.getAddress()) || p.getAddress().length() > 100) {
			throw new BusinessException("出版地址必须是1-100个字");
		}

		Connection conn = null;
		try {
			conn = DBUtil.getConnection();
			String sql = "select * from BeanPublisher where pubid=?";
			java.sql.PreparedStatement pst = conn.prepareStatement(sql);
			pst.setString(1, p.getPubid());
			java.sql.ResultSet rs = pst.executeQuery();
			if (!rs.next())
				throw new BusinessException("出版社不存在");
			rs.close();
			pst.close();
			sql = "select * from BeanPublisher where publisherName=? and pubid<>?";
			pst = conn.prepareStatement(sql);
			pst.setString(1, p.getPublisherName());
			pst.setString(2, p.getPubid());
			rs = pst.executeQuery();
			if (rs.next())
				throw new BusinessException("同名出版社已经存在");
			rs.close();
			pst.close();
			sql = "update  BeanPublisher set publisherName=?,address=? where pubid=?";
			pst = conn.prepareStatement(sql);
			pst.setString(1, p.getPublisherName());
			pst.setString(2, p.getAddress());
			pst.setString(3, p.getPubid());
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DbException(e);
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}

	}

	public List<BeanPublisher> searchPubsByName(String name) throws BaseException {
		List<BeanPublisher> result = new ArrayList<BeanPublisher>();
		Connection conn = null;
		try {
			conn = DBUtil.getConnection();
			String sql = "select * from BeanPublisher where publisherName like ?";
			java.sql.PreparedStatement pst = conn.prepareStatement(sql);
			pst.setString(1, "%" + name + "%");
			java.sql.ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				BeanPublisher p = new BeanPublisher();
				p.setPubid(rs.getString(1));
				p.setPublisherName(rs.getString(2));
				p.setAddress(rs.getString(3));
				result.add(p);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DbException(e);
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return result;
	}

	public BeanPublisher loadPubByName(String name) throws BaseException {
		Connection conn = null;
		try {
			conn = DBUtil.getConnection();
			String sql = "select * from BeanPublisher where publisherName='"+name+"'";
			java.sql.Statement st = conn.createStatement();
			java.sql.ResultSet rs = st.executeQuery(sql);
//			java.sql.PreparedStatement pst = conn.prepareStatement(sql);
//			pst.setString(1, name);
//			java.sql.ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				BeanPublisher p = new BeanPublisher();
				p.setPubid(rs.getString(1));
				p.setPublisherName(rs.getString(2));
				p.setAddress(rs.getString(3));
				rs.close();
				//pst.close();
				st.close();
				return p;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DbException(e);
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {

					e.printStackTrace();
				}
		}
		return null;
	}

	public void deletePublisher(String id) throws BaseException {
		if (id == null || "".equals(id) || id.length() > 20) {
			throw new BusinessException("出版社编号必须是1-20个字");
		}
		Connection conn = null;
		try {
			conn = DBUtil.getConnection();
			String sql = "select publisherName from BeanPublisher where pubid=?";
			java.sql.PreparedStatement pst = conn.prepareStatement(sql);
			pst.setString(1, id);
			java.sql.ResultSet rs = pst.executeQuery();
			if (!rs.next())
				throw new BusinessException("出版社不存在");
			String publisherName = rs.getString(1);
			rs.close();
			pst.close();
			sql = "select count(*) from BeanBook where pubid=?";
			pst = conn.prepareStatement(sql);
			pst.setString(1, id);
			rs = pst.executeQuery();
			rs.next();
			int n = rs.getInt(1);
			pst.close();
			if (n > 0)
				throw new BusinessException("已经有" + n + "本图书的出版社是" + publisherName + "了，不能删除");
			pst = conn.prepareStatement("delete from BeanPublisher where pubid=?");
			pst.setString(1, id);
			pst.execute();

		} catch (SQLException e) {
			e.printStackTrace();
			throw new DbException(e);
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	public List<BeanPublisher> searchPublisher(String keyword) throws BaseException 
	{
		List<BeanPublisher> result = new ArrayList<BeanPublisher>();
		Connection conn = null;
		java.sql.PreparedStatement pst=null;
		try {
			conn = DBUtil.getConnection();
			String sql = "select pubid ,publishername ,address from Beanpublisher ";
			if(keyword !=null  && !"".equals(keyword) )
			{
				sql+="where address like ?  or  publishername like ? ";
			}
			pst = conn.prepareStatement(sql);
			if(keyword !=null  && !"".equals(keyword) )
			{
			pst.setString(1,"%"+ keyword+"%");
			pst.setString(2, "%"+keyword+"%");
			}
			java.sql.ResultSet  rs = pst.executeQuery();
			while(rs.next()) {
			BeanPublisher p = new BeanPublisher();
			p.setPubid(rs.getString(1));
			p.setPublisherName(rs.getString(2));
			p.setAddress(rs.getString(3));
			result.add(p);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DbException(e);
		} finally {
			try
			{
				pst.close();
			} catch (SQLException e1)
			{
				// TODO 自动生成的 catch 块
				e1.printStackTrace();
			}
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return result;
	}

	public static void main(String[] args){
		PublisherManager pm = new PublisherManager();
		BeanPublisher p = new BeanPublisher();
		p.setAddress("法'国");
		p.setPubid("7");
		p.setPublisherName("昂贵的\"出版社");
		List<BeanPublisher> lst =null;
		try {
			pm.createPublisher(p);
		}
		catch(BaseException e){
			e.printStackTrace();
		}
//		try {
//			lst= pm.searchPublisher("路");
//			if (!lst .isEmpty())
//			{
//				for(int i=0;i<lst.size();i++){
//					p=lst.get(i);
//					System.out.println(p.getPubid()+","+p.getPublisherName()+","+p.getAddress());
//				}
//			}
//			else {
//				System.out.println("没有找到出版社");
//			}
//		} catch (BaseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		try {
//			lst = pm.searchPubsByName("小猴子");
//			if (!lst .isEmpty())
//			{
//				for(int i=0;i<lst.size();i++){
//					p=lst.get(i);
//					System.out.println(p.getPubid()+","+p.getPublisherName());
//				}
//			}
//			else {
//				System.out.println("没有找到出版社");
//			}
//		} catch (BaseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		//Scanner scanner =new 	Scanner(System.in);
//		try {
//			p = pm.loadPubByName("ABC");
//			if (p != null)
//				System.out.println(p.getPubid());
//			else {
//				System.out.println("没有找到出版社");
//			}
//		} catch (BaseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		try {
//			p = pm.loadPubByName("小\"菊花");
//			if (p != null)
//				System.out.println(p.getPubid());
//			else {
//				System.out.println("没有找到出版社");
//			}
//		} catch (BaseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		try {
//			p = pm.loadPubByName("菜\\'鸟");
//			if (p != null)
//				System.out.println(p.getPubid());
//			else {
//				System.out.println("没有找到出版社");
//			}
//		} catch (BaseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		try {
//			p = pm.loadPubByName("小猴子出版社");
//			if (p != null)
//				System.out.println(p.getPubid());
//			else {
//				System.out.println("没有找到出版社");
//			}
//		} catch (BaseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		BeanPublisher p=new BeanPublisher();
//		p.setAddress("测试地址haha");
//		p.setPubid("testpubid");
//		p.setPublisherName("测试出版社haha");
//		
//		PublisherManager pm=new PublisherManager();
//		try {
//			List<BeanPublisher> lst=pm.loadAllPublisher();
//			for(int i=0;i<lst.size();i++){
//				p=lst.get(i);
//				System.out.println(p.getPubid()+","+p.getPublisherName()+","+p.getAddress());
//			}
//		} catch (BaseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		try {
//			pm.deletePublisher("testpubid");
//		} catch (BaseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();}
		}
}
