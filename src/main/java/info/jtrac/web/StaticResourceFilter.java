package info.jtrac.web;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Filter that catches any request containing "/resources/" that was resolved
 * to a subpath (e.g. /app/resources/delete.gif or /app/space/resources/delete.gif)
 * and forwards it directly to the root /resources/ directory, ensuring static
 * images and assets never 404 regardless of page URL nesting level.
 */
public class StaticResourceFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            String uri = httpRequest.getRequestURI();
            String contextPath = httpRequest.getContextPath();
            String expectedPrefix = (contextPath != null ? contextPath : "") + "/resources/";
            
            int idx = uri.indexOf("/resources/");
            if (idx != -1 && !uri.startsWith(expectedPrefix)) {
                String targetResource = uri.substring(idx);
                request.getRequestDispatcher(targetResource).forward(request, response);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
