package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.HibernateUtil;
import hibernate.User;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author Laky
 */
@WebServlet(name = "Login", urlPatterns = {"/Login"})
public class Login extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Gson gson = new Gson();
        JsonObject loginJSON = gson.fromJson(request.getReader(), JsonObject.class);

        String email = loginJSON.get("email").getAsString().trim();
        String password = loginJSON.get("password").getAsString().trim();

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        if (email.isEmpty()) {
            responseObject.addProperty("message", "Email Required Here");
        } else if (password.isEmpty()) {
            responseObject.addProperty("message", "Password Required Here");
        } else {
            SessionFactory sf = HibernateUtil.getSessionFactory();
            Session session = sf.openSession();

            try {
                Criteria criteria = session.createCriteria(User.class);
                criteria.add(Restrictions.eq("email", email));
                User user = (User) criteria.uniqueResult();

                if (user != null) {
                    if (user.getPassword().equals(password)) {
                        responseObject.addProperty("status", true);
                        responseObject.addProperty("message", "You are now logged in!");
                    } else {
                        responseObject.addProperty("message", "Incorrect Password");
                    }
                } else {
                    responseObject.addProperty("message", "User Not Found");
                }
            } catch (Exception e) {
                responseObject.addProperty("message", "Server Error: " + e.getMessage());
            } finally {
                session.close();
            }
        }

        String responseText = gson.toJson(responseObject);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
    }
}
