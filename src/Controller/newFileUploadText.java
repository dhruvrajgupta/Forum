package Controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import model.user;

import com.oreilly.servlet.MultipartRequest;

import database.databaseConnect;

@WebServlet("/newFileUploadText")
@MultipartConfig(maxFileSize = 16177215)  //16MB
public class newFileUploadText extends HttpServlet {
	private static final long serialVersionUID = 1L;
	Connection con;
	PreparedStatement pst;

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		databaseConnect obj = databaseConnect.getInstance();
		con = obj.getConnection();

		try {
			HttpSession session = request.getSession();
			user u = (user) session.getAttribute("user");
			int user_id = u.getId();
			String forum_id = request.getParameter("forum_id");
			String question = request.getParameter("question");

			String query = "Insert into threads(name,author,forum_id,no_of_messages,no_of_views,last_post) values (?,?,?,?,?,now())";
			pst = con.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);

			pst.setString(1, question);
			pst.setInt(2, user_id);
			pst.setInt(3, Integer.parseInt(forum_id));
			pst.setInt(4, 0);
			pst.setInt(5, 0);

			int row = pst.executeUpdate();
			ResultSet keys = pst.getGeneratedKeys();
			if (row > 0) {
				System.out.println("Question was posted successfuly");
			}
			keys.next();
			int key = keys.getInt(1);
			
			
			/* FIle Uploading part */
			
			InputStream inputStream = null;
			Part file = request.getPart("attachment");
			if(file != null){
				System.out.println(file.getName());
	            System.out.println(file.getSize());
	            System.out.println(file.getContentType());
	             
	            // obtains input stream of the upload file
	            inputStream = file.getInputStream();
			}
			String attachQuery = "Insert into documents (fileName,type,content,thread_id) values(?,?,?,?)";
			pst = con.prepareStatement(attachQuery);
			
			if(inputStream!=null){
				pst.setString(1, file.getName());
				pst.setString(2,file.getContentType());
				pst.setBlob(3, inputStream);
				pst.setInt(4, key);
			}
			
			int rown = pst.executeUpdate();
			if(rown>0){
				System.out.println("File was successfully uploaded");
			}
			
			response.sendRedirect("view_threads.html?id="+forum_id);
			
			pst.close();
			con.close();
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}