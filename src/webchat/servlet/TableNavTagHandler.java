package webchat.servlet;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import webchat.util.StringUtils;
import javax.servlet.jsp.PageContext;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Edward
 */
public class TableNavTagHandler extends SimpleTagSupport {

    private String baseUrl;
    private int numResults;
    private int resultsPerPage;
    private int startIndex;
    private int pagesPerGroup = 5;

    String query;
    JspWriter out;
    /**
     * Called by the container to invoke this tag. The implementation of this
     * method is provided by the tag library developer, and handles all tag
     * processing, body iteration, etc.
     */
    @Override
    public void doTag() throws JspException {
        this.out = getJspContext().getOut();
        PageContext pagesCtx = (PageContext) getJspContext();
        HttpServletRequest req = (HttpServletRequest) pagesCtx.getRequest();
        this.query = req.getQueryString() == null ? "" : req.getQueryString();

        int numPages = (int) Math.ceil(((double)numResults) / ((double)resultsPerPage));
        int numPageGroups = (int) Math.ceil(((double)numPages) / ((double)pagesPerGroup));
        int currPage = (int) Math.ceil(((double)startIndex) / ((double)resultsPerPage));
        int currPageGroup = currPage /pagesPerGroup;

        String firstPageUrl = null;
        String prevPageUrl = null;
        if (currPage > 0) {
            firstPageUrl = createUrl(0);
            prevPageUrl = createUrl(startIndex - resultsPerPage);
        }
        String prevPageGroupUrl = null;
        String nextPageGroupUrl = null;
        if (currPageGroup > 0) {
            int prevGroupBase = (currPageGroup - 1) * resultsPerPage * pagesPerGroup;
            int index = prevGroupBase + (pagesPerGroup - 1) * resultsPerPage;
            prevPageGroupUrl = createUrl(index);
        }
        if (currPageGroup < numPageGroups - 1) {
            int nextGroupBase = (currPageGroup + 1) * resultsPerPage * pagesPerGroup;
            nextPageGroupUrl = createUrl(nextGroupBase);
        }
        String nextPageUrl = null;
        String lastPageUrl = null;
        if (currPage < numPages - 1) {
            nextPageUrl = createUrl(startIndex + resultsPerPage);
            lastPageUrl = createUrl(resultsPerPage*(numPages - 1));
        }

        try {
            out.println("<ul>");
            writeLink("First", firstPageUrl);
            writeLink("Prev", prevPageUrl);
            writeLink("...", prevPageGroupUrl);
            for (int j = currPageGroup * pagesPerGroup; j < (currPageGroup + 1) * pagesPerGroup; j += 1) {
                int index = j * resultsPerPage;
                if (index >= numResults) {
                    break;
                }
                String url = null;
                if (j != currPage) {
                    url = createUrl(index);
                } 
                writeLink(Integer.toString(j + 1), url);
            }
            writeLink("...", nextPageGroupUrl);
            writeLink("Next", nextPageUrl);
            writeLink("Last", lastPageUrl);
            out.println("</ul>");
        } catch (java.io.IOException ex) {
            throw new JspException("Error in TableNavTagHandler tag", ex);
        }
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setNumResults(int numResults) {
        this.numResults = numResults;
    }

    public void setResultsPerPage(int resultsPerPage) {
        this.resultsPerPage = resultsPerPage;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public void setPagesPerGroup(int pagesPerGroup) {
        this.pagesPerGroup = pagesPerGroup;
    }

    private String createUrl(int startIndex) {
        String s0 = query.replaceAll("start=[0-9]+&", "");
        s0 = s0.replaceAll("start=[0-9]+", "");
        String s1 = "start=" + startIndex;
        if (StringUtils.isNullOrEmpty(s0)) {
            return baseUrl + "?" + s1;
        } else {
            return baseUrl + '?' + s0 + '&' + s1;
        }
    }

    private void writeLink(String text, String href) throws java.io.IOException{
        if (href == null) {
            out.println("<li>" + text + "</li>");
        } else {
            out.println("<li><a href=\"" + href + "\">" + text + "</a></li>");
        }
    }

}
