package no.nav.sbl.dialogarena.filter;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import no.nav.sbl.dialogarena.utils.TestTokenUtils;

public class FakeLoginFilter implements Filter {

    private static final Logger logger = getLogger(FakeLoginFilter.class);
    private FilterConfig filterConfig;

	@Override
	public void init(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }

    // Checkstyle tror det er redundante Exceptions
    // CHECKSTYLE:OFF
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        if (req.getRequestURI().matches("^(.*internal/selftest.*)|(.*index.html)|(.*feil.*)|((.*)\\.(js|css|jpg))")) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        if (req.getParameter("fnr") != null) {
            req.getSession().setAttribute("fnr", req.getParameter("fnr"));
        }

        String fnr  = getFnr(req);
        try {
            if (StringUtils.isEmpty(fnr)) {
                TestTokenUtils.setSecurityContext();
            }
            else {
                TestTokenUtils.setSecurityContext(fnr);    
            }
            
        }
        catch(Exception e) {
          e.printStackTrace();
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    /**
     * Hent Fødselsnummer fra attributt. Hvis fnr ikke er satt,
     * bruk defaultFnr som blir definert i web.xml
     */
    private String getFnr(HttpServletRequest req) {
        String fnr;
        fnr = (String) req.getSession().getAttribute("fnr");

        if (fnr == null) {
            fnr = filterConfig.getInitParameter("defaultFnr");
            logger.debug("FNR ikke sendt med, bruker default fnr: {}", fnr);
        }

        return fnr;
    }

	@Override
	public void destroy() {
        this.filterConfig = null;
	}
}
