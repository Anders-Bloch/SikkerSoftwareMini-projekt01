/**
* OWASP Benchmark Project v1.2
*
* This file is part of the Open Web Application Security Project (OWASP)
* Benchmark Project. For details, please see
* <a href="https://owasp.org/www-project-benchmark/">https://owasp.org/www-project-benchmark/</a>.
*
* The OWASP Benchmark is free software: you can redistribute it and/or modify it under the terms
* of the GNU General Public License as published by the Free Software Foundation, version 2.
*
* The OWASP Benchmark is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
* even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* @author Dave Wichers
* @created 2015
*/

package dk.andersbloch.benchmark.truefiles;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(value="/securecookie-00/BenchmarkTest01521")
public class BenchmarkTest01521 extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");
	
		org.owasp.benchmark.helpers.SeparateClassRequest scr = new org.owasp.benchmark.helpers.SeparateClassRequest( request );
		String param = scr.getTheParameter("BenchmarkTest01521");
		if (param == null) param = "";

		String bar = new Test().doSomething(request, param);
		
		byte[] input = new byte[1000];
		String str = "?";
		Object inputParam = param;
		if (inputParam instanceof String) str = ((String) inputParam);
		if (inputParam instanceof java.io.InputStream) {
			int i = ((java.io.InputStream) inputParam).read(input);
			if (i == -1) {
				response.getWriter().println(
"This input source requires a POST, not a GET. Incompatible UI for the InputStream source."
);
				return;
			}			
			str = new String(input, 0, i);
		}
		if ("".equals(str)) str="No cookie value supplied";
		javax.servlet.http.Cookie cookie = new javax.servlet.http.Cookie("SomeCookie", str);
		
		cookie.setSecure(false);
		cookie.setHttpOnly(true);
		cookie.setPath(request.getRequestURI()); // i.e., set path to JUST this servlet
												 // e.g., /benchmark/sql-01/BenchmarkTest01001
		response.addCookie(cookie);

        response.getWriter().println(
			"Created cookie: 'SomeCookie': with value: '"
			+ org.owasp.esapi.ESAPI.encoder().encodeForHTML(str) + "' and secure flag set to: false"
		);
	}  // end doPost

	
    private class Test {

        public String doSomething(HttpServletRequest request, String param) throws ServletException, IOException {

		String bar = "";
		if (param != null) bar = param.split(" ")[0];

            return bar;
        }
    } // end innerclass Test

} // end DataflowThruInnerClass
