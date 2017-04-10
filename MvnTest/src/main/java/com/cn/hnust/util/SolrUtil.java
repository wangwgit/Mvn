package com.cn.hnust.util;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;


import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import com.alibaba.fastjson.JSON;


public class SolrUtil {
	public static final String solr_appUrl ="http://192.168.12.37:8983/solr/applog" ;
	public static String[] allColumns=new String[]{"id","numId","businessSys_registeredId","userId","userName","organizationID","organization","operateTime","responePackage","terminalId","operateType","operateResult","errorCode","operateName","operatecndition","insertTime"};
	public static void main(String[] args) {
		/*Map<String, Object> params=new HashMap<>();
		 params.put("businessSys_registeredId", "4*");
		params.put("operateTime", "[2017-03-16T16:07:13Z TO 2017-03-17T16:07:13Z]"); 
		Map<String, Object> rest=SolrUtil.selectContent(params, 1, 1, "operateTime", true,null);
	System.out.println(JSON.toJSONString(rest));*/
		System.out.println(getlogTotal());
	}
	/**
	 * solr查询
	 * 只能查询详情，不支持分组等复杂操作
	 * @param params		查询的参数key要列名，value为参数值，模糊匹配请使用通配符*
	 * 					范围查询请使用[开始点 TO 结束点]
	 * 					时间格式请出入yyyy-MM-ddTHH:mm:ssZ格式
	 * 					例如16号12点到17号12点的时间范围为[2017-03-16T12:00:00Z TO 2017-03-17T12:00:00Z]
	 * @param pageSize		每页显示的数据量
	 * @param pageIndex		当前要查询的是第几页
	 * @param sortColum		排序的列
	 * @param isDesc		是否排序方法：true降序，false升序
	 * @param resoutColums	要查询的结果包括哪些列，不指定为返回所有列
	 * @return	固定可以的map各种key的解释
	 * 			contentList:数据详情的list,没一条数据为一个map,注意数据中的时间格式全部转化为yyyy-MM-dd HH:mm:ss类型的string
	 * 			totalSize:符合条件的数据总条数
	 * 			totalPage:数据的总页数
	 * 			
	 */
	public static Map<String, Object> selectContent(Map<String,Object> params,int pageSize,int pageIndex,String sortColum,Boolean isDesc,String[] resoutColums)
	{
		if(resoutColums==null||resoutColums.length==0)
		{
			resoutColums=allColumns;
		}
		Map<String, Object> content=new HashMap();
		List<Map<String, Object>> contentList=new ArrayList<>();
		content.put("contentList", contentList);
		
		SolrClient solr = null;
		try {
			solr = creatAppSolrClient();
			SolrQuery query = new SolrQuery();
			query.setFacet(true);
			String queryString=getQueryString(params);
			query.setQuery(queryString);
			query.setRows(pageSize);
			query.setStart(pageSize*(pageIndex-1));
			if(sortColum!=null&&!sortColum.equals("")&&isDesc!=null)
			{
				if(isDesc)
				{
					query.setSort(sortColum,ORDER.desc);
				}
				else
				{
					query.setSort(sortColum,ORDER.asc);
				}	
			}
			QueryResponse rsp;
			rsp = solr.query(query);
			SolrDocumentList resouts=rsp.getResults();
			long totalSize=resouts.getNumFound();
			content.put("totalSize", totalSize);
			content.put("totalPage", (totalSize+ pageSize -1)/pageSize);
			for(SolrDocument resout:resouts)
			{
				Map<String, Object> restData=new HashMap<>();
				for(String column:resoutColums)
				{
					Object val= resout.get(column);
					if(val instanceof Date)
					{
						TimeZone tz= TimeZone.getTimeZone("UTC");
						SimpleDateFormat sdfTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						sdfTime.setTimeZone(tz);
						val=sdfTime.format((Date) val);					
					}
					restData.put(column, val);
				}
				contentList.add(restData);
			}
		} catch (RuntimeException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			closeAppSolrClient(solr);
		}
		return content;
	}
	private static String getQueryString(Map<String,Object> params) {
		StringBuffer queryBuf=new StringBuffer();
		//首先加入一个查询所有的，避免空查询
		queryBuf.append("+*:* ");
		for(Map.Entry<String, Object> param:params.entrySet())
		{
			String key= param.getKey();
			Object val= param.getValue();
			if(val instanceof List)
			{
				queryBuf.append(getQueryString(key,((List)val).toArray()));
			}
			else
			{
				queryBuf.append(getQueryString(key,new Object[]{val}));
			}		
		}
		return queryBuf.toString();
	}
	private static HttpSolrClient creatAppSolrClient() {
		HttpSolrClient solr = new HttpSolrClient.Builder(solr_appUrl).build();
		return solr;
	}
	private static void closeAppSolrClient(SolrClient solr) {
		try {
			solr.close();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}
	private static String getQueryString(String columnName,Object[] values)
	{
		columnName+=":";
		StringBuffer strBuf= new StringBuffer();
		strBuf.append("+(");
		for(int i=0;i<values.length;i++)
		{
			String sysId=values[i].toString();
			strBuf.append(columnName).append(sysId);
			if(i!=values.length-1)
			{
				strBuf.append(" OR ");
			}			
		}
		strBuf.append(") ");
		return strBuf.toString();
	}
	
	/**
	 * 
	* TODO 获得全文中的日志总量
	*
	* @return
	* @author leiya
	* @version 2017年3月28日 下午4:54:27
	 */
	public static String getlogTotal(){
		Map<String, Object> params=new HashMap<>();
		/*params.put("businessSys_registeredId", "4*");
		params.put("operateTime", "[2017-03-16T16:07:13Z TO 2017-03-17T16:07:13Z]");*/
		Map<String, Object> rest=SolrUtil.selectContent(params, 1, 1, "operateTime", true,null);
		return rest.get("totalSize")+"";
	}
}
