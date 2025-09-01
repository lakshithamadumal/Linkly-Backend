package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.HibernateUtil;
import hibernate.User;
import java.io.BufferedReader;
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

@WebServlet(name = "UserDetailsAccount", urlPatterns = {"/UserDetailsAccount"})
public class UserDetailsAccount extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        Gson gson = new Gson();
        BufferedReader reader = request.getReader();
        JsonObject requestBody = gson.fromJson(reader, JsonObject.class);

        String email = requestBody.get("email").getAsString();

        SessionFactory sf = HibernateUtil.getSessionFactory();
        Session s = sf.openSession();

        JsonObject responseObject = new JsonObject();

        try {
            Criteria userCriteria = s.createCriteria(User.class);
            userCriteria.add(Restrictions.eq("email", email));
            User user = (User) userCriteria.uniqueResult();

            if (user != null) {
                responseObject.addProperty("name", user.getName());
                responseObject.addProperty("email", user.getEmail());

                String avatar = user.getImg_url();
                if (avatar != null && avatar.contains("/")) {
                    avatar = avatar.substring(avatar.lastIndexOf("/") + 1);
                }
                responseObject.addProperty("avatar", avatar);

            } else {
                responseObject.addProperty("error", "User Not Found");
            }
        } catch (Exception e) {
            responseObject.addProperty("error", "Server Error");
        } finally {
            s.close();
        }

        String responseText = gson.toJson(responseObject);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
    }
}
