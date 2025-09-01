package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.HibernateUtil;
import hibernate.Link;
import hibernate.User;
import java.io.IOException;
import java.io.PrintWriter;
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
@WebServlet(name = "AddLink", urlPatterns = {"/AddLink"})
public class AddLink extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Gson gson = new Gson();
        JsonObject linkJSON = gson.fromJson(request.getReader(), JsonObject.class);

        String title = linkJSON.get("title").getAsString();
        String url = linkJSON.get("url").getAsString();
        String email = linkJSON.get("email").getAsString();

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", Boolean.FALSE);

        if (title.isEmpty()) {
            responseObject.addProperty("message", "Link Name Required");
        } else if (url.isEmpty()) {
            responseObject.addProperty("message", "Link Required Here");
        } else if (!url.matches("^(https?|ftp)://[^\\s/$.?#].[^\\s]*$")) {
            responseObject.addProperty("message", "Invalid Link Format");
        } else {
            SessionFactory sf = HibernateUtil.getSessionFactory();
            Session s = sf.openSession();

            //Have A Link
            Criteria criteriaLink = s.createCriteria(Link.class);
            criteriaLink.add(Restrictions.eq("url", url));

            if (!criteriaLink.list().isEmpty()) {
                responseObject.addProperty("message", "Link Already Added!");
            } else {
                Link l = new Link();
                l.setTitle(title);
                l.setUrl(url);

                Criteria criteriaUser = s.createCriteria(User.class);
                criteriaUser.add(Restrictions.eq("email", email));
                User user = (User) criteriaUser.uniqueResult();

                if (user != null) {
                    l.setUser_id(user);

                    s.save(l);
                    s.beginTransaction().commit();

                    responseObject.addProperty("status", Boolean.TRUE);
                    responseObject.addProperty("message", "Link saved successfully!");
                } else {
                    responseObject.addProperty("message", "User not found!");
                }
                s.close();
            }
        }
        String responseText = gson.toJson(responseObject);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
    }

}
