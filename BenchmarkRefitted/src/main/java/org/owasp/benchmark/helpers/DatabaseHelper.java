/**
* OWASP Benchmark Project
*
* This file is part of the Open Web Application Security Project (OWASP)
* Benchmark Project For details, please see
* <a href="https://www.owasp.org/index.php/Benchmark">https://www.owasp.org/index.php/Benchmark</a>.
*
* The OWASP Benchmark is free software: you can redistribute it and/or modify it under the terms
* of the GNU General Public License as published by the Free Software Foundation, version 2.
*
* The OWASP Benchmark is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
* even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details
*
* @author Juan Gama <a href="https://www.aspectsecurity.com">Aspect Security</a>
* @created 2015
*/

package org.owasp.benchmark.helpers;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.owasp.benchmark.service.pojo.StringMessage;
import org.owasp.esapi.ESAPI;

public class DatabaseHelper {
	private static Statement stmt;
	private static Connection conn;
	public static org.springframework.jdbc.core.JdbcTemplate JDBCtemplate;
	public static final boolean hideSQLErrors = false; // If we want SQL Exceptions to be suppressed from being displayed to the user of the web app.

	static {
		
		initDataBase();
		System.out.println("Spring context init() ");
		@SuppressWarnings("resource")
		org.springframework.context.ApplicationContext ac =
				new  org.springframework.context.support.ClassPathXmlApplicationContext("/context.xml", DatabaseHelper.class);
		DataSource data = (DataSource) ac.getBean("dataSource");
		JDBCtemplate = new org.springframework.jdbc.core.JdbcTemplate(data);
		System.out.println("Spring context loaded!");
	}
	
	public static void initDataBase(){
		try {
			executeSQLCommand("DROP PROCEDURE IF EXISTS verifyUserPassword");
			executeSQLCommand("DROP PROCEDURE IF EXISTS verifyEmployeeSalary");
			executeSQLCommand("DROP TABLE IF EXISTS USERS");
			executeSQLCommand("DROP TABLE IF EXISTS EMPLOYEE");
			executeSQLCommand("DROP TABLE IF EXISTS CERTIFICATE");
			executeSQLCommand("DROP TABLE IF EXISTS SCORE");
			
			executeSQLCommand("CREATE TABLE USERS (userid int NOT NULL GENERATED BY DEFAULT AS IDENTITY, username varchar(50), password varchar(50),PRIMARY KEY (userid));");
			executeSQLCommand("CREATE TABLE SCORE (userid int NOT NULL GENERATED BY DEFAULT AS IDENTITY, nick varchar(50), score INTEGER,PRIMARY KEY (userid));");
			executeSQLCommand("CREATE PROCEDURE verifyUserPassword(IN username_ varchar(50), IN password_ varchar(50))"
					+ " READS SQL DATA"
					+ " DYNAMIC RESULT SETS 1"
					+ " BEGIN ATOMIC"
					+ " DECLARE resultSet SCROLL CURSOR WITH HOLD WITH RETURN FOR SELECT * FROM USERS WHERE USERNAME = username_ AND PASSWORD = password_;"
					+ " OPEN resultSet;"
					+"END;");

			executeSQLCommand("create table EMPLOYEE ("
					+ "	   id INT NOT NULL GENERATED BY DEFAULT AS IDENTITY,"
					+ "	   first_name VARCHAR(20) default NULL,"
					+ "   last_name  VARCHAR(20) default NULL,"
					+ " salary     INT  default NULL," + " PRIMARY KEY (id)"
					+ "	);");

			executeSQLCommand("create table CERTIFICATE ("
					+ "	   id INT NOT NULL GENERATED BY DEFAULT AS IDENTITY,"
					+ " certificate_name VARCHAR(30) default NULL,"
					+ " employee_id INT default NULL," + " PRIMARY KEY (id)"
					+ ");");
			
			executeSQLCommand("CREATE PROCEDURE verifyEmployeeSalary(IN user_ varchar(50))"
					+ " READS SQL DATA"
					+ " DYNAMIC RESULT SETS 1"
					+ " BEGIN ATOMIC"
					+ " DECLARE resultSet SCROLL CURSOR WITH RETURN FOR SELECT * FROM EMPLOYEE WHERE FIRST_NAME = user_;"
					+ " OPEN resultSet;"
					+"END;");
			conn.commit();
			initData();
			
			System.out.println("DataBase tables/procedures created.");
		} catch (Exception e1) {
			System.out.println("Problem with database table/procedure creations: " + e1.getMessage());
		}
	}

	
	public static Statement getSqlStatement() {
		if (conn == null) {
			getSqlConnection();
		}

		if (stmt == null) {
			try {
				stmt = conn.createStatement();
			} catch (SQLException e) {
				System.out.println("Problem with database init.");
			}
		}

		return stmt;
	}
	
	public static void reset(){
		initData();
	}
	
	private static void initData() {
		try {
			executeSQLCommand("INSERT INTO USERS (username, password) VALUES('User01', 'P455w0rd')");
			executeSQLCommand("INSERT INTO USERS (username, password) VALUES('User02', 'B3nchM3rk')");
			executeSQLCommand("INSERT INTO USERS (username, password) VALUES('User03', 'a$c11')");
			executeSQLCommand("INSERT INTO USERS (username, password) VALUES('foo', 'bar')");
			
			executeSQLCommand("INSERT INTO SCORE (nick, score) VALUES('User03', 155)");
			executeSQLCommand("INSERT INTO SCORE (nick, score) VALUES('foo', 40)");
			
			executeSQLCommand("INSERT INTO EMPLOYEE (first_name, last_name, salary) VALUES('foo', 'bar', 100)");
			conn.commit();
		} catch (Exception e1) {
			System.out.println("Problem with database init/reset: " + e1.getMessage());
		}
	}
	
	public static Connection getSqlConnection() {
		if (conn == null) {
			try {
				InitialContext ctx = new InitialContext();
				DataSource datasource = (DataSource)ctx.lookup("java:comp/env/jdbc/BenchmarkDB");
				conn = datasource.getConnection();
				conn.setAutoCommit(false);
			} catch (SQLException | NamingException e) {
				System.out.println("Problem with getSqlConnection.");
				e.printStackTrace();
			}
		}
		return conn;
	}

	public static void executeSQLCommand(String sql) throws Exception {
		if (stmt == null) {
			getSqlStatement();
		}
		stmt.executeUpdate(sql);
	}

	public static void outputUpdateComplete(String sql, HttpServletResponse response) throws SQLException, IOException {
		
		PrintWriter out = response.getWriter();
		
		out.write("<!DOCTYPE html>\n<html>\n<body>\n<p>");
		out.write("Update complete for query: " + ESAPI.encoder().encodeForHTML(sql) + "<br>\n");
		out.write("</p>\n</body>\n</html>");
	}

	public static void outputUpdateComplete(String sql, List<StringMessage> resp) throws SQLException, IOException {
		resp.add(new StringMessage("Message",
				"Update complete for query: " + ESAPI.encoder().encodeForHTML(sql) + "<br>\n"
		));
	}

	public static void printResults(Statement statement, String sql, HttpServletResponse response) throws SQLException, IOException {
		
		PrintWriter out = response.getWriter();
		out.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n"
				+ "<html>\n"
				+ "<head>\n"
				+ "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n"
				+ "</head>\n"
				+ "<body>\n"
				+ "<p>\n");

		try {
			ResultSet rs = statement.getResultSet();
			if (rs == null) {
				out.write("Results set is empty for query: " + ESAPI.encoder().encodeForHTML(sql));
				return;
			}
			ResultSetMetaData rsmd = rs.getMetaData();
	      
//			printColTypes(rsmd, out);
//			out.write("<br>\n");
	
		    int numberOfColumns = rsmd.getColumnCount();
		
/*			for (int i = 1; i <= numberOfColumns; i++) {
				if (i > 1) out.write(",  ");
				String columnName = rsmd.getColumnName(i);
				out.write(columnName);
			}  // end for
			out.write("<br>\n");
*/
		    out.write("Your results are:<br>\n");
		    //System.out.println("Your results are:<br>\n");
		    while (rs.next()) {
		    	for (int i = 1; i <= numberOfColumns; i++) {
		          if (i > 1){ out.write(",  "); 
		          	//System.out.println(",  ");
		          }
		          String columnValue = rs.getString(i);
		          out.write(ESAPI.encoder().encodeForHTML(columnValue));
		          //System.out.println(columnValue);
		    	} // end for
				out.write("<br>\n");
				//System.out.println("<br>\n");
		    } // end while
		    
		} finally {
	    out.write("</p>\n</body>\n</html>");
		}
		
	} //end printResults
	
public static void printResults(Statement statement, String sql, List<StringMessage> resp) throws SQLException, IOException {
			try {
			ResultSet rs = statement.getResultSet();
			if (rs == null) {
				resp.add(new StringMessage("Message",
						"Results set is empty for query: " + ESAPI.encoder().encodeForHTML(sql)
				));
				return;
			}
			ResultSetMetaData rsmd = rs.getMetaData();
		    int numberOfColumns = rsmd.getColumnCount();
		    resp.add(new StringMessage("Message",
		    		"Your results are:<br>\n"
		    ));
		    while (rs.next()) {
		    	for (int i = 1; i <= numberOfColumns; i++) {
		          if (i > 1){ 
		        	  resp.add(new StringMessage("Message",
		        	  ",  "
		        			 )); 
		          	//System.out.println(",  ");
		          }
		          String columnValue = rs.getString(i);
		          resp.add(new StringMessage("Message",
		        		  ESAPI.encoder().encodeForHTML(columnValue)
		        		  ));
		    	} // end for
		    	resp.add(new StringMessage("Message",
		    			"<br>\n"
		    		));
		    } // end while
		    
		} finally {
			resp.add(new StringMessage("Message",
					"</p>\n</body>\n</html>"
	    		));
		}
		
	} //end printResults
	
	public static void printResults(ResultSet rs, String sql, HttpServletResponse response) throws SQLException, IOException {
		
		PrintWriter out = response.getWriter();
		out.write("<!DOCTYPE html>\n<html>\n<body>\n<p>");

		try {
			if (rs == null) {
				out.write("Results set is empty for query: " + ESAPI.encoder().encodeForHTML(sql));
				return;
			}
			ResultSetMetaData rsmd = rs.getMetaData();
		    int numberOfColumns = rsmd.getColumnCount();
		    out.write("Your results are:<br>\n");
//		    System.out.println("Your results are:<br>\n");
		    while (rs.next()) {
		    	for (int i = 1; i <= numberOfColumns; i++) {
//		          if (i > 1){ out.write(",  "); System.out.println(",  ");}
		          String columnValue = rs.getString(i);
		          out.write(ESAPI.encoder().encodeForHTML(columnValue));
//		          System.out.println(columnValue);
		    	} // end for
				out.write("<br>\n");
//				System.out.println("<br>\n");
		    } // end while
		    
		} finally {
	    out.write("</p>\n</body>\n</html>");
		}
	} //end printResults

public static void printResults(ResultSet rs, String sql, List<StringMessage> resp) throws SQLException, IOException {
		try {
			if (rs == null) {
	        	  resp.add(new StringMessage("Message",
	        			  "Results set is empty for query: " + ESAPI.encoder().encodeForHTML(sql)
	        			 ));
				return;
			}
			ResultSetMetaData rsmd = rs.getMetaData();
		    int numberOfColumns = rsmd.getColumnCount();
      	  	resp.add(new StringMessage("Message",
      	  			"Your results are:<br>\n"
      	  		));
		    while (rs.next()) {
		    	for (int i = 1; i <= numberOfColumns; i++) {
//		          if (i > 1){ out.write(",  "); System.out.println(",  ");}
		          String columnValue = rs.getString(i);
	        	  resp.add(new StringMessage("Message",
	        			  ESAPI.encoder().encodeForHTML(columnValue)
	        			));
		    	} // end for
	        	  resp.add(new StringMessage("Message",
	        			  "<br>\n"
	        			  ));
		    } // end while
		    
		} finally {
      	  resp.add(new StringMessage("Message",
      			  "</p>\n</body>\n</html>"
      			  ));
		}
	} //end printResults

	public static void printResults(String query, int[] counts, HttpServletResponse response) throws IOException{
		PrintWriter out = response.getWriter();
		out.write("<!DOCTYPE html>\n<html>\n<body>\n<p>");
		out.write("For query: " + ESAPI.encoder().encodeForHTML(query) + "<br>");
		try {
			if(counts.length > 0){
				if(counts[0] == Statement.SUCCESS_NO_INFO){
					out.write("The SQL query was processed successfully but the number of rows affected is unknown.");
					System.out.println("The SQL query was processed successfully but the number of rows affected is unknown.");
				}else if(counts[0] == Statement.EXECUTE_FAILED){
					out.write("The SQL query failed to execute successfully and occurs only if a driver continues to process commands after a command fails");
					System.out.println("The SQL query failed to execute successfully and occurs only if a driver continues to process commands after a command fails");
				}else{
					out.write("The number of affected rows are: " + counts[0]);
					System.out.println("The number of affected rows are: " + counts[0]);
				}
			}
		} finally {
			out.write("</p>\n</body>\n</html>");
		}
	} //end printResults
	
	public static void printResults(String query, int[] counts, List<StringMessage> resp) throws IOException{
		resp.add(new StringMessage("Message",
				"For query: " + ESAPI.encoder().encodeForHTML(query) + "<br>"
			));
		try {
			if(counts.length > 0){
				if(counts[0] == Statement.SUCCESS_NO_INFO){
					resp.add(new StringMessage("Message",
							"The SQL query was processed successfully but the number of rows affected is unknown."
						));
					System.out.println("The SQL query was processed successfully but the number of rows affected is unknown.");
				}else if(counts[0] == Statement.EXECUTE_FAILED){
					resp.add(new StringMessage("Message",
							"The SQL query failed to execute successfully and occurs only if a driver continues to process commands after a command fails"
						));
					System.out.println("The SQL query failed to execute successfully and occurs only if a driver continues to process commands after a command fails");
				}else{
					resp.add(new StringMessage("Message",
							"The number of affected rows are: " + counts[0]
									));
					System.out.println("The number of affected rows are: " + counts[0]);
				}
			}
		} finally {
			resp.add(new StringMessage("Message",
					"</p>\n</body>\n</html>"
					));
		}
	} //end printResults
	
	public static void printColTypes(ResultSetMetaData rsmd, PrintWriter out) throws SQLException {
	    int columns = rsmd.getColumnCount();
	    for (int i = 1; i <= columns; i++) {
	      int jdbcType = rsmd.getColumnType(i);
	      String name = rsmd.getColumnTypeName(i);
	      out.write("Column " + i + " is JDBC type " + jdbcType);
	      out.write(", which the DBMS calls " + name + "<br>\n");
	    }
   }
	
}
