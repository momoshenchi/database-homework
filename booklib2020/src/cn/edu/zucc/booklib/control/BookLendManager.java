package cn.edu.zucc.booklib.control;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.edu.zucc.booklib.model.BeanBook;
import cn.edu.zucc.booklib.model.BeanBookLendRecord;
import cn.edu.zucc.booklib.model.BeanReader;
import cn.edu.zucc.booklib.model.StaticBeanBookLend;
import cn.edu.zucc.booklib.model.StaticBeanReaderLend;
import cn.edu.zucc.booklib.util.BaseException;
import cn.edu.zucc.booklib.util.BusinessException;
import cn.edu.zucc.booklib.util.DBUtil;
import cn.edu.zucc.booklib.util.DbException;

public class BookLendManager {

	public List<BeanBook> loadReaderLentBooks(String readerId) throws DbException {
		List<BeanBook> result=new ArrayList<BeanBook>();
		Connection conn=null;
		try {
			conn=DBUtil.getConnection();
			String sql="select b.barcode,b.bookname,b.pubid,b.price,b.state,p.publishername " +
					" from beanbook b left outer join beanpublisher p on (b.pubid=p.pubid)" +
					" where  b.barcode in (select bookBarcode from BeanBookLendRecord where returnDate is null and readerid=?) ";
			sql+=" order by b.barcode";
			java.sql.PreparedStatement pst=conn.prepareStatement(sql);
			pst.setString(1, readerId);
			java.sql.ResultSet rs=pst.executeQuery();
			while(rs.next()){
				BeanBook b=new BeanBook();
				b.setBarcode(rs.getString(1));
				b.setBookname(rs.getString(2));
				b.setPubid(rs.getString(3));
				b.setPrice(rs.getDouble(4));
				b.setState(rs.getString(5));
				b.setPubName(rs.getString(6));
				result.add(b);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DbException(e);
		}
		finally{
			if(conn!=null)
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return result;
	}

	public void lend(String barcode, String readerid) throws BaseException {
		BeanReader r=(new ReaderManager()).loadReader(readerid);
		if(r==null) throw new BusinessException("读者不存在");
		if(r.getRemoveDate()!=null) throw new BusinessException("读者已注销");
		if(r.getStopDate()!=null) throw new BusinessException("读者已挂失");
		BeanBook book=(new BookManager()).loadBook(barcode);
		if(book==null) throw new BusinessException("图书不存在");
		if(!"在库".equals(book.getState())) throw new BusinessException("图书"+book.getState());
		List<BeanBook> lentbooks=this.loadReaderLentBooks(readerid);
		if(r.getLendBookLimitted()<=lentbooks.size()){
			throw new BusinessException("超出限额");
		}
		Connection conn=null;
		try {
			conn=DBUtil.getConnection();
			conn.setAutoCommit(false);
			String sql="insert into BeanBookLendRecord(readerid,bookBarcode,lendDate,lendOperUserid,penalSum) values(?,?,?,?,0)";
			java.sql.PreparedStatement pst=conn.prepareStatement(sql);
			pst.setString(1, readerid);
			pst.setString(2, barcode);
			pst.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis()));
			pst.setString(4, SystemUserManager.currentUser.getUserid());
			pst.execute();
			pst.close();
			sql="update BeanBook set state='已借出' where barcode=?";
			pst=conn.prepareStatement(sql);
			pst.setString(1,barcode);
			pst.execute();
			conn.commit();
		} catch (SQLException e) {
			try
			{
				conn.rollback();
			} catch (SQLException e1)
			{
				// TODO 自动生成的 catch 块
				e1.printStackTrace();
			}
			e.printStackTrace();
			
			throw new DbException(e);
			
		}
		finally{
			if(conn!=null)
				try {
					
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
		
	}
	public void returnBook(String barcode) throws BaseException {
		Connection conn=null;
		try {
			conn=DBUtil.getConnection();
			conn.setAutoCommit(false);
			//提取借阅记录
			String sql="select id,lendDate from BeanBookLendRecord where bookBarcode=? and returnDate is null";
			java.sql.PreparedStatement pst=conn.prepareStatement(sql);
			pst.setString(1, barcode);
			java.sql.ResultSet rs=pst.executeQuery();
			if(!rs.next()){
				throw new BusinessException("该图书没有借阅记录");
			}
			int id=rs.getInt(1);
			Date lendDate=rs.getDate(2);
			rs.close();
			pst.close();
			long x=(System.currentTimeMillis()-lendDate.getTime())/(1000*60*60*24);
			double penalSum=0;
			if(x>60){//超过60天需要处罚
				penalSum=(x-60)*0.1;
			}
			sql="update BeanBookLendRecord set returnDate=?,returnOperUserid=?,penalSum="+penalSum+" where id=?";
			pst=conn.prepareStatement(sql);
			pst.setTimestamp(1, new java.sql.Timestamp(System.currentTimeMillis()));
			pst.setString(2, SystemUserManager.currentUser.getUserid());
			pst.setInt(3, id);
			pst.execute();
			pst.close();
			sql="update BeanBook set state='在库' where barcode=?";
			pst=conn.prepareStatement(sql);
			pst.setString(1,barcode);
			pst.execute();
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DbException(e);
		}
		finally{
			if(conn!=null)
				try {
					conn.rollback();
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
		
	}

	public BeanBookLendRecord loadUnReturnRecord(String barcode) throws DbException {
		Connection conn=null;
		try {
			conn=DBUtil.getConnection();
			String sql="select id,readerid,bookBarcode,lendDate,lendOperUserid from BeanBookLendRecord where bookBarcode=? and returnDate is null";
			java.sql.PreparedStatement pst=conn.prepareStatement(sql);
			pst.setString(1, barcode);
			java.sql.ResultSet rs=pst.executeQuery();
			if(rs.next()){
				BeanBookLendRecord r=new BeanBookLendRecord();
				r.setId(rs.getInt(1));
				r.setReaderid(rs.getString(2));
				r.setBookBarcode(rs.getString(3));
				r.setLendDate(rs.getDate(4));
				r.setLendOperUserid(rs.getString(5));
				return r;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DbException(e);
		}
		finally{
			if(conn!=null)
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return null;
	}

	public List<BeanBookLendRecord> loadBookAllRecode(String barcode) throws DbException {
		List<BeanBookLendRecord> result=new ArrayList<BeanBookLendRecord>();
		Connection conn=null;
		try {
			conn=DBUtil.getConnection();
			String sql="select id,readerid,lendDate,returnDate,penalSum from BeanBookLendRecord where bookBarcode=? order by lendDate desc";
			java.sql.PreparedStatement pst=conn.prepareStatement(sql);
			pst.setString(1, barcode);
			java.sql.ResultSet rs=pst.executeQuery();
			while(rs.next()){
				BeanBookLendRecord r=new BeanBookLendRecord();
				r.setId(rs.getInt(1));
				r.setReaderid(rs.getString(2));
				r.setBookBarcode(barcode);
				r.setLendDate(rs.getTimestamp(3));
				r.setReturnDate(rs.getTimestamp(4));
				r.setPenalSum(rs.getDouble(5));
				result.add( r);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DbException(e);
		}
		finally{
			if(conn!=null)
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return result;
	}
	public List<BeanBookLendRecord> loadReaderAllRecode(String readerid) throws DbException {
		List<BeanBookLendRecord> result=new ArrayList<BeanBookLendRecord>();
		Connection conn=null;
		try {
			conn=DBUtil.getConnection();
			String sql="select id,bookBarcode,lendDate,returnDate,penalSum from BeanBookLendRecord where readerid=? order by lendDate desc";
			java.sql.PreparedStatement pst=conn.prepareStatement(sql);
			pst.setString(1, readerid);
			java.sql.ResultSet rs=pst.executeQuery();
			while(rs.next()){
				BeanBookLendRecord r=new BeanBookLendRecord();
				r.setId(rs.getInt(1));
				r.setReaderid(readerid);
				r.setBookBarcode(rs.getString(2));
				r.setLendDate(rs.getTimestamp(3));
				r.setReturnDate(rs.getTimestamp(4));
				r.setPenalSum(rs.getDouble(5));
				result.add( r);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DbException(e);
		}
		finally{
			if(conn!=null)
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return result;
	}
	public List<StaticBeanReaderLend> staticReaderLend() throws DbException {
		List<StaticBeanReaderLend>  result=new ArrayList<StaticBeanReaderLend>();
		Connection conn=null;
		try {
			conn=DBUtil.getConnection();
			String sql="select r.readerid,r.readerName,count(*),sum(penalSum) from BeanReader r,BeanBookLendRecord rc " +
					" where r.readerid=rc.readerid group by  r.readerid,r.readerName order by count(*) desc";
			java.sql.PreparedStatement pst=conn.prepareStatement(sql);
			java.sql.ResultSet rs=pst.executeQuery();
			while(rs.next()){
				StaticBeanReaderLend r=new StaticBeanReaderLend();
				r.setReaderId(rs.getString(1));
				r.setReaderName(rs.getString(2));
				r.setCount(rs.getInt(3));
				r.setPenalSum(rs.getDouble(4));
				result.add( r);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DbException(e);
		}
		finally{
			if(conn!=null)
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return result;
	}
	public List<StaticBeanBookLend> staticBookLend() throws DbException {

		List<StaticBeanBookLend>  result=new ArrayList<StaticBeanBookLend>();
		Connection conn=null;
		try {
			conn=DBUtil.getConnection();
			String sql="select b.barcode,b.bookname,count(*) from BeanBook b,BeanBookLendRecord rc where b.barcode=rc.bookBarcode " +
					" group by  b.barcode,b.bookname order by count(*) desc";
			java.sql.PreparedStatement pst=conn.prepareStatement(sql);
			java.sql.ResultSet rs=pst.executeQuery();
			while(rs.next()){
				StaticBeanBookLend r=new StaticBeanBookLend();
				r.setBarcode(rs.getString(1));
				r.setBookname(rs.getString(2));
				r.setCount(rs.getInt(3));
				result.add( r);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DbException(e);
		}
		finally{
			if(conn!=null)
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return result;
	}
	public String loadBookLendOperator(String barcode)throws BaseException{
		
		   //参数为图书条码，返回这本图书最近一次被借出时的操作员姓名，要求采用连接查询实现。难点：如何识别出最近一次
		Connection conn=null;
		java.sql.Statement st=null;
		try {
			conn=DBUtil.getConnection();
//			String sql="select username from beansystemuser s, beanbooklendrecord l where s.userid ="
//				+"	l.lendoperuserid and bookbarcode ='"+ barcode+"'  ORDER BY lendDate DESC";
			st=conn.createStatement();
			String sql="select username from beansystemuser  where userid ="
					+"(select lendoperuserid  from beanbooklendrecord  where bookbarcode = '"+barcode+ "' and lenddate >="
		+"all(SELECT lenddate from beanbooklendrecord  where bookbarcode = '"+barcode+"'))";
			java.sql.ResultSet rs=st.executeQuery(sql);
			if(rs.next())
				return rs.getString(1);
				else
					return null;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DbException(e);
		}
		finally{
			if(st!=null)
				try {
					st.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			if(conn!=null)
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		} 
	public void showAllLendRecord(){
		//通过System.out.println方法，输出所有借阅记录的明细数据，要求结果中包括读者姓名、图书名称、所属出版社名称、借阅操作员姓名、归还操作员姓名、借阅时间、归还时间等
		//注意：需要注意未归还图书的情况
		Connection conn=null;
		java.sql.Statement st=null;
		try {
			conn=DBUtil.getConnection();
			String sql="select readername,bookname,publishername,s1.username lenduser,s2.username returnuser ,l.lenddate, l.returndate from beanbooklendrecord l,"
					+ "beanbook b ,beanreader r , beanpublisher p, "
			+"beansystemuser s1,beansystemuser s2 where s1.userid =l.lendoperuserid and s2.userid =l.returnOperUserid and"
				+" l.bookbarcode = b.barcode and l.readerid =r.readerid and b.pubid = p.pubid  and  l.returndate is not null"; 
			st=conn.createStatement();
			java.sql.ResultSet rs=st.executeQuery(sql);
			if(rs.next())
				System.out.println("读者姓名: "+rs.getString(1)+" 图书名称:"+rs.getString(2)+" 出版社名称:"+rs.getString(3)
				+" 借阅操作员姓名:"+rs.getString(4)+" 归还操作员姓名:"+rs.getString(5)+"\t"+rs.getDate(6)+rs.getDate(7));	
		} catch (SQLException e) {
			e.printStackTrace();
			//throw new DbException(e);
		}
		finally{
			if(st!=null)
				try {
					st.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			if(conn!=null)
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return;
		
		}
	public void showTop5Books(){
		//通过System.out.println方法，输出借阅次数最多的5本图书及其借阅次数
		Connection conn=null;
		java.sql.Statement st=null;
		try {
			conn=DBUtil.getConnection();
			String sql="select bookname, count(*) from beanbook b, beanbooklendrecord l where b.barcode =l.bookBarcode group by bookBarcode ORDER BY count(*)desc";
			st=conn.createStatement();
			java.sql.ResultSet rs=st.executeQuery(sql);
			int cnt=0;
			while(rs.next()&&cnt<5)
				{System.out.println( "图书名称:"+rs.getString(1)+" number:"+rs.getInt(2));	
					cnt++;
				}
		} catch (SQLException e) {
			e.printStackTrace();
			//throw new DbException(e);
		}
		finally{
			if(st!=null)
				try {
					st.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			if(conn!=null)
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return;
		}
	public void showTop5Publisher(){

		
		//通过System.out.println方法，输出被借阅图书次数最多的5个出版名称及其总借阅次数和被借阅过的图书次数
		Connection conn=null;
		java.sql.Statement st=null;
		try {
			conn=DBUtil.getConnection();
			String sql="select publisherName, count(*)cnt,count(Distinct bookBarcode)cnt2 from beanbook b, beanbooklendrecord l ,beanpublisher p where b.barcode =l.bookBarcode \r\n" + 
					"and b.pubid =p.pubid group by p.pubid ORDER BY count(*)desc;";
			st=conn.createStatement();
			java.sql.ResultSet rs=st.executeQuery(sql);
			int cnt=0;
			while(rs.next()&&cnt<5)
			{System.out.println( "出版社名称:"+rs.getString(1)+" 总借阅次数:"+rs.getInt(2)+"借阅不同图书个数 "+rs.getInt(3));	
					cnt++;
				}
		} catch (SQLException e) {
			e.printStackTrace();
			//throw new DbException(e);
		}
		finally{
			if(st!=null)
				try {
					st.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			if(conn!=null)
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return;
		
		
		}
	public void backup()
	{
		Connection con=null;
		
		java.sql.PreparedStatement pst=null;
		try
		{
			con=DBUtil.getConnection();
			con.setAutoCommit(false);
			String sql="insert into BeanBookLendRecord_backup (select * from BeanBookLendRecord "
					+ "where returndate is not null )";
					
			pst=con.prepareStatement(sql);
			pst.execute();
			pst.close();
			sql="delete from BeanBookLendRecord  where returndate is not null";
			pst=con.prepareStatement(sql);
			pst.execute();
			pst.close();
			con.commit();
		} catch (SQLException e)
		{
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		finally
		{
			if(con!=null)
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
	public void printDateLendRecord(String date)throws  DbException
	{
		Connection con=null;
		java.sql.PreparedStatement pst=null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try
		{
			con=DBUtil.getConnection();
			//TODO: shuchushijianbianle
			String sql="select readerid ,bookbarcode,lenddate,returndate from beanbooklendrecord where lenddate =?";
			pst=con.prepareStatement(sql);
//			pst.setTime
			
			pst.setString(1, date);
			java.sql.ResultSet rs = pst.executeQuery();
			while(rs.next())
			{
				System.out.print("readerid="+rs.getInt(1)+",bookbarcode="+rs.getInt(2)+",lenddate="+rs.getTimestamp(3)+",returndate=");
				if(rs.getDate(4)==null)
				{
					System.out.println("未归还");
				}
				else
				{
					System.out.println(rs.getString(4));
				}  
			}
			pst.close();
		} catch (SQLException e)
		{
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		finally
		{
			if(con!=null)
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
	public void printDateLendRecord2(String date)throws  DbException
	{
		Connection con=null;
		java.sql.PreparedStatement pst=null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try
		{
			con=DBUtil.getConnection();
	
			String sql="select readerid ,bookbarcode,lenddate,returndate from beanbooklendrecord where lenddate =?";
			pst=con.prepareStatement(sql);
			try
			{
				pst.setDate(1, new java.sql.Date(sdf.parse(date).getTime()));
			} catch (ParseException e)
			{
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
			java.sql.ResultSet rs = pst.executeQuery();
			while(rs.next())
			{
				System.out.print("readerid="+rs.getInt(1)+",bookbarcode="+rs.getInt(2)+",lenddate="+rs.getString(3)+",returndate=");
				if(rs.getDate(4)==null)
				{
					System.out.println("未归还");
				}
				else
				{
					System.out.println(rs.getString(4));
				}  
			}
//			System.currentTimeMillis();
//			java.util.Date  dt=new Date(); 
			pst.close();
		} catch (SQLException e)
		{
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		finally
		{
			if(con!=null)
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
	public void setPenalSum(int id,double p)throws BaseException
	{
		Connection con=null;
		java.sql.PreparedStatement pst=null;
		try
		{
			con=DBUtil.getConnection();
			String sql="update beanbooklendrecord set penalsum =? where id=?";
			pst=con.prepareStatement(sql);
			if(p>0)
				pst.setDouble(1, p);
			else
				pst.setString(1,null); //or  pst.setNull(1,Types.DOUBLE);
				
	
			pst.setInt(2, id);
			pst.execute();
			
			pst.close();
		} catch (SQLException e)
		{
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		finally
		{
			if(con!=null)
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

	public static void main(String[] args)
	{
		BookLendManager bm=new BookLendManager();
		try {
			bm.setPenalSum(1, -2);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		}
}
