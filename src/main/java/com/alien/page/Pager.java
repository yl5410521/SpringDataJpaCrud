package com.alien.page;


import java.io.Serializable;
import java.util.List;



/**
 * Bean类 - 分页
 * ============================================================================
 * ============================================================================
 */
public class Pager implements Serializable{

	private static final long serialVersionUID = -3718782091043694321L;

	public static final Integer MAX_PAGE_SIZE = 500;// 每页最大记录数限制
	
	// 排序方式（递增、递减）
	public enum Order {
		asc, desc
	}

	private int pageNumber=1 ;// 当前页码
	private int pageSize=5 ;// 每页记录数
	private String searchBy;// 查找字段
	private String keyword;// 查找关键字
	private String orderBy="createDate";// 排序字段
	private Order order;// 排序方式
	private int totalPages; // 总页数
	private int prePage; // 上一页页码
	private int nextPage; // 下一页页码
	private int totalCount;// 总记录数
	private int currentCount;//当前记录数
	private List<?> result;// 返回结果
	
	
	// 获取总页数
	public int getPageCount() {
		int pageCount = totalCount / pageSize;
		if (totalCount % pageSize > 0) {
			pageCount++;
		}
		return pageCount;
	}
	
	

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(String pageNumber) {
		
		if(pageNumber!=null&&!"".equals(pageNumber)){
		
			Integer pageNumb = Integer.parseInt(pageNumber);
			
			if (pageNumb < 1) {
				
				pageNumb = 1;
				
			}
			
			this.pageNumber = pageNumb;
			
		}
	
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		if (pageSize < 1) {
			pageSize = 1;
		} else if (pageSize > MAX_PAGE_SIZE) {
			pageSize = MAX_PAGE_SIZE;
		}
		this.pageSize = pageSize;
	}
	
	 /**
     * 初始化分页bean对象
     * @param totalCount 总记录数
     * @param pageSize 每页显示的记录数
     */
	public void initPageBean(int totalCount, int pageSize){
		this.pageSize = pageSize;
		this.totalCount = totalCount;
		this.totalPages = (this.totalCount)/this.pageSize;
		if((this.totalCount-1)%(this.pageSize)>0)
		{
			this.totalPages=this.totalPages+1;
		}
		// 设置上一页
		if(this.pageNumber == 1){
			this.setPrePage(1);
		}else{
			this.setPrePage(this.pageNumber-1);
		}
		// 设置下一页
		if(this.pageNumber == this.totalPages){
			this.setNextPage(this.totalPages);
		}else{
			this.setNextPage(this.pageNumber + 1);
		}
	}
	
	/**
	 * 生成SQLServer的分页查询语句
	 * @param sql 原始sql语句
	 * @param curPage 第几页
	 * @param rowsPerPage 每页多少行 
	 */
	public String getPageSQLServer(String sql, int pageNumber, int pageSize){
		String afterFrom = sql.toLowerCase().substring(sql.indexOf("from"));
		String pageSql = null;
		if(afterFrom.indexOf("where") == -1){
			 pageSql = "select top "+ pageSize + " * "+afterFrom
			+" where id not in(select top "+pageSize*(pageNumber-1)+" id "
			+afterFrom+" order by id desc)"+"order by id desc";
		}else{
			pageSql = "select top "+ pageSize + " * "+afterFrom
			+" and id not in(select top "+pageSize*(pageNumber-1)+" id "
			+afterFrom+" order by id desc)"+"order by id desc";
		}
		return pageSql;
	}
	
	/**
	 * 生成MySql分页sql语句
	 * @param sql 原始sql语句
	 * @param curPage 第几页
	 * @param rowsPerPage 每页多少行 
	 * @return 返回分页SQL语句
	 */
	public String getPageMySQL(String sql, int pageNumber, int pageSize){
		String pageSql = sql+" limit "+ (pageNumber-1)*pageSize+","+pageSize;
		return pageSql;
	}
	
	/**
	 * 生成Oracle分页查询语句
	 * @param sql 原始sql语句
	 * @return 返回分页SQL语句
	 */
	public String getOrclPageSql(String sql,int pageNumber, int pageSize){
		int begin = (pageNumber - 1) * pageSize;
		int end = begin + pageSize;
		StringBuffer pagingSelect = new StringBuffer(300);
		pagingSelect.append("select * from ( select row_.*, rownum rownum_ from ( ");
		pagingSelect.append(sql);
		pagingSelect.append(" ) row_ where rownum <= "+end+") where rownum_ > "+begin);
		return pagingSelect.toString();
	}
	
	

	public String getSearchBy() {
		return searchBy;
	}

	public void setSearchBy(String searchBy) {
		this.searchBy = searchBy;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public List<?> getResult() {
		return result;
	}

	public void setResult(List<?> result) {
		this.result = result;
	}



	public int getTotalPages() {
		return totalPages;
	}



	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}



	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}



	public int getPrePage() {
		return prePage;
	}



	public void setPrePage(int prePage) {
		this.prePage = prePage;
	}



	public int getNextPage() {
		return nextPage;
	}



	public void setNextPage(int nextPage) {
		this.nextPage = nextPage;
	}



	public int getCurrentCount() {
		return currentCount;
	}



	public void setCurrentCount(int currentCount) {
		this.currentCount = currentCount;
	}
	
	
	

}