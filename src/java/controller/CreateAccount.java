package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.HibernateUtil;
import hibernate.User;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "CreateAccount", urlPatterns = {"/CreateAccount"})
public class CreateAccount extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        JsonObject accountJSON = gson.fromJson(request.getReader(), JsonObject.class);

        String fullName = accountJSON.get("fullName").getAsString();
        String email = accountJSON.get("email").getAsString();
        String password = accountJSON.get("password").getAsString();
        String profileUrl = accountJSON.get("profileUrl").getAsString();

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", Boolean.FALSE);

        if (fullName.isEmpty()) {
            responseObject.addProperty("message", "Full Name Required");
        } else if (email.isEmpty()) {
            responseObject.addProperty("message", "Email Required Here");
        } else if (!Pattern.matches("^[A-Za-z0-9+_.-]+@(.+)$", email)) {
            responseObject.addProperty("message", "Invalid Email Address");
        } else if (password.isEmpty()) {
            responseObject.addProperty("message", "Password Required Here");
        } else if (password.length() < 6) {
            responseObject.addProperty("message", "PPassword must be at least 6 characters!");
        } else {

            SessionFactory sf = HibernateUtil.getSessionFactory();
            Session s = sf.openSession();

            //Have A User
            Criteria criteriaEmail = s.createCriteria(User.class);
            criteriaEmail.add(Restrictions.eq("email", email));

            if (!criteriaEmail.list().isEmpty()) {
                responseObject.addProperty("message", "Email Already Registered");
            } else {

                User u = new User();
                u.setName(fullName);
                u.setEmail(email);
                u.setPassword(password);
                u.setImg_url(profileUrl);
                u.setCreated_at(new Date());

                s.save(u);
                s.beginTransaction().commit();

                responseObject.addProperty("status", Boolean.TRUE);
                responseObject.addProperty("message", "Registration Successful!");
            }
            s.close();

        }
        String responseText = gson.toJson(responseObject);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
    }
}
