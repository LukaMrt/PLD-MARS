package com.lukamaret.pld_mars_common;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 *
 * @author WASO Team
 */
public class MicroCasFilter implements Filter {

    public static final String INIT_PARAMETER_SERVER_URL = "MicroCasServerUrl";

    public static final String REQUEST_RESET = "CAS:reset";
    public static final String REQUEST_ACTION = "action";
    public static final String REQUEST_ACTION_CHECK = "check";
    public static final String REQUEST_SERVICE = "service";
    public static final String REQUEST_TICKET = "ticket";

    public static final String SESSION_MICROCAS_CLIENT_INIT_DATE = "MicroCAS:Client:Init:Date";
    public static final String SESSION_MICROCAS_CLIENT_INIT_SERVICE = "MicroCAS:Client:Init:Service";
    public static final String SESSION_MICROCAS_CLIENT_VALID_DATE = "MicroCAS:Client:Valid:Date";
    public static final String SESSION_MICROCAS_CLIENT_VALID_LOGIN = "MicroCAS:Client:Valid:Login";
    public static final String SESSION_MICROCAS_CLIENT_VALID_USER = "MicroCAS:Client:Valid:User";

    public static final String ENCODING_UTF8 = "UTF-8";

    private FilterConfig filterConfig = null;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        HttpSession session = httpServletRequest.getSession(true);

        httpServletResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
        httpServletResponse.setHeader("Pragma","no-cache"); // HTTP 1.0
        httpServletResponse.setDateHeader("Expires", 0);
        
        if (httpServletRequest.getParameter(REQUEST_RESET) != null) {
            session.removeAttribute(SESSION_MICROCAS_CLIENT_INIT_DATE);
            session.removeAttribute(SESSION_MICROCAS_CLIENT_INIT_SERVICE);
            session.removeAttribute(SESSION_MICROCAS_CLIENT_VALID_DATE);
            
            String currentUrl = httpServletRequest.getRequestURL().toString();
            
            String targetUrl = this.filterConfig.getInitParameter(INIT_PARAMETER_SERVER_URL) + "?action=logout&" + REQUEST_SERVICE + "=" + URLEncoder.encode(currentUrl, StandardCharsets.UTF_8);

            System.err.println("Redirection to CAS Logout => " + targetUrl);
            httpServletResponse.sendRedirect(targetUrl);

            return;
        }

        if (session.getAttribute(SESSION_MICROCAS_CLIENT_INIT_DATE) == null || session.getAttribute(SESSION_MICROCAS_CLIENT_VALID_DATE) == null && httpServletRequest.getParameter(REQUEST_TICKET) == null) { // || httpServletRequest.getParameter(REQUEST_RESET) != null) {

            String currentUrl = httpServletRequest.getRequestURL().toString();

            String targetUrl = this.filterConfig.getInitParameter(INIT_PARAMETER_SERVER_URL) + "?" + REQUEST_SERVICE + "=" + URLEncoder.encode(currentUrl, StandardCharsets.UTF_8);

            session.setAttribute(SESSION_MICROCAS_CLIENT_INIT_DATE, new Date());
            session.setAttribute(SESSION_MICROCAS_CLIENT_INIT_SERVICE, currentUrl);
            session.removeAttribute(SESSION_MICROCAS_CLIENT_VALID_DATE);

            System.err.println("Redirection to CAS => " + targetUrl);
            httpServletResponse.sendRedirect(targetUrl);

        } else {

            if (session.getAttribute(SESSION_MICROCAS_CLIENT_VALID_DATE) == null) {

                String ticket = httpServletRequest.getParameter(REQUEST_TICKET);

                if (ticket != null) {
                    // Validate Ticket
                    System.err.println("CAS-Session: ticket to be validated => " + ticket);

                    String service = (String) session.getAttribute(SESSION_MICROCAS_CLIENT_INIT_SERVICE);

                    JsonHttpClient jsonHttpClient = new JsonHttpClient();
                    JsonObject result = jsonHttpClient.post(
                            this.filterConfig.getInitParameter(INIT_PARAMETER_SERVER_URL),
                            new JsonHttpClient.Parameter(REQUEST_ACTION, REQUEST_ACTION_CHECK),
                            new JsonHttpClient.Parameter(REQUEST_SERVICE, service),
                            new JsonHttpClient.Parameter(REQUEST_TICKET, ticket)
                    );

                    JsonObject authentication = result.getAsJsonObject("authentication");
                    System.err.println("Authentication from CAS => " + authentication.toString());

                    if (authentication.get("error") != null) {
                        // Validation Error...
                        System.err.println("Authentication Error: " + authentication.get("error").getAsString());
                    } else {
                        // CAS Session validated
                        session.setAttribute(SESSION_MICROCAS_CLIENT_VALID_DATE, new Date());
                        session.setAttribute(SESSION_MICROCAS_CLIENT_VALID_LOGIN, authentication.get("login").getAsString());

                        Map<String,String> user = new HashMap<String, String>();
                        for (Map.Entry<String, JsonElement> attribute : authentication.getAsJsonObject("details").entrySet()) {
                            user.put(attribute.getKey(), attribute.getValue().getAsString());
                        }
                        
                        session.setAttribute(SESSION_MICROCAS_CLIENT_VALID_USER, user);
                    }
                }
            }

            if (session.getAttribute(SESSION_MICROCAS_CLIENT_VALID_DATE) == null) {

                session.removeAttribute(SESSION_MICROCAS_CLIENT_INIT_DATE);
                session.removeAttribute(SESSION_MICROCAS_CLIENT_INIT_SERVICE);
                session.removeAttribute(SESSION_MICROCAS_CLIENT_VALID_DATE);

                httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "CAS Authentication failed. Please reload the page to try again.");
            } else {

                chain.doFilter(request, response);
            }
        }

    }

    @Override
    public void destroy() {
    }

}
