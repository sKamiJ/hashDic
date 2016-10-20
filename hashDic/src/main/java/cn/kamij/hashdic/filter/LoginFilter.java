package cn.kamij.hashdic.filter;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

// 用户登录状态的过滤器
public class LoginFilter implements Filter {

	public void destroy() {
		System.out.println("LoginFilter destroy>>>>>>>>>>>>>>>>>>");
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;
		String path = req.getContextPath();
		String basePath = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort() + path + "/";
		HttpSession session = req.getSession(true);
		// 确保用户登录后才能访问主页
		Object obj = session.getAttribute("userId");
		if (obj == null) {
			//警告并跳转
			resp.setContentType("text/html; charset=UTF-8");
			PrintWriter out = resp.getWriter();
			out.println("<script type=\"text/javascript\">");
			out.println("alert(\"您尚未登录或登录超时，请重新登录！\");");
			out.println("window.location.href=\""+basePath+"user/login\";");
			out.println("</script>");
		} else {
			chain.doFilter(req, resp);
		}
	}

	public void init(FilterConfig arg0) throws ServletException {
		System.out.println("LoginFilter init>>>>>>>>>>>>>>>>>");
	}

}
