package com.hcl.feb.api;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines the filters that can be applied to the REST end points.
 * 
 * <pre>
 * {@code FEBFilters filters = new FEBFilters();
 * filters.addFilter(new FEBFilterParam("F_Owner",FEBFilterOperator.CONTAINS,"Dawes"));
 * filters.addFilter(new FEBFilterParam("F_Owner",FEBFilterOperator.CONTAINS,"Jones"));
 * filters.setFilterRelationship(FEBFilterRelationship.OR);
 * filters.setFrom(20);
 * filters.setTo(31);
 * filters.setSortBy("lastUpdated");
 * filters.setFilterOrder(FEBFilterOrder.DESCENDING);
 * r = fa.listRecords(appid, "F_Form1", filters);}
 * </pre>
 */
public class FEBFilters {
	private ArrayList<FEBFilterParam> filters = new ArrayList<FEBFilterParam>();
	private FEBFilterRelationship relation = null; //FEBFilterRelationship.AT_LEAST_ONE_MATCHES; //defaults to OR
	private FEBFilterOrder order = null; //FEBFilterOrder.ASCENDING;
	private String sortBy = null;
	private Integer from = null;
	private Integer to = null;
	private Integer pageSize = null; //FEB default is 100?
	
	private final Logger logger = LoggerFactory.getLogger(FEBFilters.class);
	
	public FEBFilters() {
		pageSize = 50;
	}
	
	public void addFilter(FEBFilterParam theFilter) {
		filters.add(theFilter);
	}
	
	public void addFilter(String filterParam, FEBFilterOperator operator, String value) throws FEBAPIException {
		
		FEBFilterParam newFilter = new FEBFilterParam(filterParam, operator, value);		
		filters.add(newFilter);
	}
	
	public FEBFilterRelationship getFilterRelationship() {
		return relation;
	}
	
	public void setFilterRelationship(FEBFilterRelationship relation) {
		this.relation = relation;
	}
	
	public ArrayList<FEBFilterParam> getFilterArray() {
		return filters;
	}
	
	public void setFilterOrder(FEBFilterOrder order) {
		this.order = order;
	}
	
	public FEBFilterOrder getFilterOrder() {
		return order;
	}
	
	public void setSortBy(String sortBy) {
		this.sortBy = sortBy;
	}
	
	public String getSortBy() {
		return sortBy;
	}
	
	public void setFrom(Integer from) {
		this.from = from;
	}
	
	public Integer getFrom() {
		return from;
	}
	
	public void setTo(Integer to) {
		this.to = to;
	}
	
	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
		from = 0;
		to = pageSize;
	}
	
	public Integer getPageSize() {
		return pageSize;
	}
	
	public void nextPage() {
		from = from + pageSize;
		to = to + pageSize;
	}
	
	public void firstPage() {
		from = 0;
		to = pageSize;
	}
	
	public void previousPage() {
		if(to >= pageSize && from > 0) {
			from = from - pageSize;
			to = to - pageSize;
		}
	}
	
	public void pageReset() {
		from = 0;
		to = pageSize;
	}
	
	public String getFilterURLString() {
		String urlStr = "";
		
		if(filters != null) {
			for(int i=0;i<filters.size();i++) {				
				urlStr = addURLParam(urlStr, filters.get(i).getFilterParam() + "__" + filters.get(i).getOperator() + "=" + filters.get(i).getValue());
				logger.debug("    Adding param {}__{}={}", filters.get(i).getFilterParam(), filters.get(i).getOperator(), filters.get(i).getValue());
			}
			
			//add filter relationship
			if(filters.size() > 1) {
				if(relation != null) {
					urlStr = addURLParam(urlStr, "searchOperator=" + relation);
					logger.debug("    Adding param searchOperator={}", relation);
				}
			}
		}
		
		if(from != null && to != null) {
			urlStr = this.addURLParam(urlStr, "from=" + from + "&to=" + to);
			logger.debug("    Adding param from={}&to={}", from, to);
		}
		
		if(sortBy != null) {
			urlStr = this.addURLParam(urlStr, "sortBy=" + sortBy);
			logger.debug("    Adding param sortBy={}", sortBy);
		}
		
		if(order != null) {
			urlStr = this.addURLParam(urlStr, "order=" + order);
			logger.debug("    Adding param order={}", order);
		}
		
		logger.debug("Returning formatted filter string = {}", urlStr);
		return urlStr;
	}
	
	/**
	 * Determines if the param needs to be preceded by a ? or &.
	 * 
	 * @param theParams
	 * @param paramToAdd
	 * @return
	 */
	private String addURLParam(String theURL, String paramToAdd) {
		String r = theURL;
		if(paramToAdd != null && !paramToAdd.isEmpty()) {
			if(r.indexOf("?") == -1) {
				r += "?";
			} else {
				r += "&";
			}
			r += paramToAdd;
		}
		return r;
	}
}
