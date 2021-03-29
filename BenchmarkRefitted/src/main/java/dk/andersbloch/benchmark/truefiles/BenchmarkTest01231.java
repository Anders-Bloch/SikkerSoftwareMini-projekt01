/**
* OWASP Benchmark Project v1.2
*
* This file is part of the Open Web Application Security Project (OWASP)
* Benchmark Project. For details, please see
* <a href="https://www.owasp.org/index.php/Benchmark">https://www.owasp.org/index.php/Benchmark</a>.
*
* The OWASP Benchmark is free software: you can redistribute it and/or modify it under the terms
* of the GNU General Public License as published by the Free Software Foundation, version 2.
*
* The OWASP Benchmark is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
* even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* @author Dave Wichers <a href="https://www.aspectsecurity.com">Aspect Security</a>
* @created 2015
*/

package dk.andersbloch.benchmark.truefiles;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(value="/pathtraver-01/BenchmarkTest01231")
public class BenchmarkTest01231 extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");
	
		String param = request.getParameter("BenchmarkTest01231");
		if (param == null) param = "";

		String bar = new Test().doSomething(request, param);
		
		java.io.File fileTarget = new java.io.File(new java.io.File(org.owasp.benchmark.helpers.Utils.testfileDir),bar);
		response.getWriter().println(
"Access to file: '" + org.owasp.esapi.ESAPI.encoder().encodeForHTML(fileTarget.toString()) + "' created." 
);
		if (fileTarget.exists()) {
			response.getWriter().println(
" And file already exists."
);
		} else { response.getWriter().println(
" But file doesn't exist yet."
); }
	}  // end doPost

	
    private class Test {

        public String doSomething(HttpServletRequest request, String param) throws ServletException, IOException {

		String bar = "safe!";
		java.util.HashMap<String,Object> map35717 = new java.util.HashMap<String,Object>();
		map35717.put("keyA-35717", "a-Value"); // put some stuff in the collection
		map35717.put("keyB-35717", param); // put it in a collection
		map35717.put("keyC", "another-Value"); // put some stuff in the collection
		bar = (String)map35717.get("keyB-35717"); // get it back out

            return bar;
        }
    } // end innerclass Test

} // end DataflowThruInnerClass