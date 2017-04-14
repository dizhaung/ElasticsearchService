package com.qishon.es.servlet;/**
 * Created by shuting.wu on 2017/4/11.
 */

import com.qishon.es.common.*;
import com.qishon.es.pojo.AggParam;
import com.qishon.es.pojo.Pagination;
import com.qishon.es.pojo.QueryParam;
import com.qishon.es.pojo.SortParam;
import com.qishon.es.service.ISearchService;
import com.qishon.es.service.impl.SearchServiceImpl;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author shuting.wu
 * @date 2017-04-11 17:29
 **/
public class SimpleSearchServlet  extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static ISearchService searchService;
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleSearchServlet.class);

    public SimpleSearchServlet() {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        response.setCharacterEncoding("utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        //TODO: 索引，类型和返回字段，分页，排序，统计，高亮
        try {
            //获取参数
            String keyword = request.getParameter("keyword");
            String origin = request.getParameter("origin");
            String filters = request.getParameter("filters");
            String aggFields = request.getParameter("aggFields");
            String aggSize = request.getParameter("aggSize");
            String pageNo = request.getParameter("pageNo");
            String pageSize = request.getParameter("pageSize");
            String sortParam = request.getParameter("sortParam");
            String highLightFlag= request.getParameter("highLightFlag");
            String returnField = request.getParameter("returnFields");
            String range = request.getParameter("range");
            if(StringUtils.isEmpty(origin)){
                throw new Exception("Error 999:parameters error(origin)");
            }
            LOGGER.info("查询参数：" + CommonUtils.getIpAddr(request)
                    + ",keyword=" + keyword + ",origin=" + origin + ",filters=" + filters
                    + ",aggFields=" + aggFields + ",aggSize=" + aggSize + ",sortParam=" + sortParam
                    + ",highLightFlag=" + highLightFlag + ",returnField="+ returnField);

            //返回字段
            String[] returnFields = !StringUtils.isEmpty(returnField)?returnField.split(","):null;
            //高亮
            String[] highLightFields = (!StringUtils.equals(highLightFlag,"1")?null:returnFields);

            //分页
            Pagination pagination = new Pagination();
            if(!StringUtils.isEmpty(pageNo)) {
                pagination.setPageNo(Integer.parseInt(pageNo));
            }
            if(!StringUtils.isEmpty(pageSize)) {
                pagination.setPageSize(Integer.parseInt(pageSize));
            }

            //排序
            SortParam[] sortParams = (!StringUtils.isEmpty(sortParam)? JsonStrUtils.jsonStrToList(sortParam, new TypeReference<SortParam[]>() {
            }):null);

            //聚合
            AggParam[] aggParams = null;
            if(!StringUtils.isEmpty(aggFields)) {
                String[] fields = aggFields.split(",");
                aggParams = new AggParam[fields.length];
                AggParam aggParam ;
                int iAggSize = 100;
                if(!StringUtils.isEmpty(aggSize)) {
                    iAggSize = Integer.parseInt(aggSize);
                }
                for(int i=0;i < fields.length;i++){
                    aggParam = new AggParam();
                    aggParam.setAggName(fields[i]);
                    aggParam.setField(fields[i]);
                    aggParam.setSize(iAggSize);
                    aggParams[i] = aggParam;
                }
            }

            //查询条件
            QueryParam[] queryParams = null;
            if(!StringUtils.isEmpty(keyword)) {
                QueryParam mainQuery = new QueryParam();
                mainQuery.setType(SearchType.MATCH);
                mainQuery.setValue(keyword);
                queryParams = new QueryParam[]{mainQuery};
            }
            //过滤条件
            if(!StringUtils.isEmpty(filters)) {
                QueryParam[] filterParams = (!StringUtils.isEmpty(filters)? JsonStrUtils.jsonStrToList(filters, new TypeReference<QueryParam[]>() {
                }):null);
                if(filterParams != null) {
                    queryParams = (QueryParam[]) ArrayUtils.addAll(queryParams,filterParams);
                }
            }
            //范围查找
            if(!StringUtils.isEmpty(range)) {
                String[] ranges = RegexpUtils.getArrayByPattern(range,"pattern.param.range");
                String[] op = null;
                QueryParam rangeParam = null;
                QueryParam[] rangeParams = new QueryParam[ranges.length];
                for(int i = 0;i < ranges.length;i++) {
                    String rp = ranges[i];
                    rangeParam = new QueryParam();
                    op = RegexpUtils.getArrayByPattern(rp.toString(),"pattern.param.rangeOp");
                    rangeParam.setQueryField(StringUtils.substringBefore(rp.toString(),op[0]));
                    QueryParam.RangeParam r = rangeParam.new RangeParam();
                    r.setLower(StringUtils.substring(rp,rp.indexOf(op[0])+1,rp.indexOf(",")));
                    r.setUpper(StringUtils.substring(rp,rp.indexOf(",")+1,rp.indexOf(op[1])));
                    if(op[0].equals("(")) {
                        r.setIncludeLower(false);
                    }
                    if(op[1].equals(")")) {
                        r.setIncludeUpper(false);
                    }
                    rangeParam.setRange(r);
                    rangeParam.setType(SearchType.RANGE);
                    rangeParams[i] = rangeParam;
                }
                queryParams = (QueryParam[]) ArrayUtils.addAll(queryParams,rangeParams);
            }

            //TODO 搜索
            if(searchService == null) {
                searchService = new SearchServiceImpl();
            }
            String result = searchService.commonQuery(origin,null,null,pagination,returnFields,queryParams,aggParams,sortParams,highLightFields);
            response.getWriter().println(result);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(),e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println(e.getMessage());
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
