package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hibernate.HibernateUtil;
import hibernate.User;
import hibernate.Link;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "UserDetails", urlPatterns = {"/UserDetails"})
public class UserDetails extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Gson gson = new Gson();
        String email = request.getParameter("email");

        SessionFactory sf = HibernateUtil.getSessionFactory();
        Session s = sf.openSession();

        JsonObject responseObject = new JsonObject();

        try {

            Criteria userCriteria = s.createCriteria(User.class);
            userCriteria.add(Restrictions.eq("email", email));
            User user = (User) userCriteria.uniqueResult();

            if (user != null) {
                responseObject.addProperty("email", user.getEmail());

                String avatar = user.getImg_url();
                if (avatar != null && avatar.contains("/")) {
                    avatar = avatar.substring(avatar.lastIndexOf("/") + 1);
                }
                responseObject.addProperty("avatar", avatar);

                Criteria linkCriteria = s.createCriteria(Link.class);
                linkCriteria.add(Restrictions.eq("user_id", user));
                List<Link> links = linkCriteria.list();

                JsonArray linksArray = new JsonArray();
                for (Link link : links) {
                    JsonObject linkObj = new JsonObject();
                    linkObj.addProperty("id", String.valueOf(link.getId()));
                    linkObj.addProperty("title", link.getTitle());
                    linkObj.addProperty("description", link.getDescription());
                    linkObj.addProperty("url", link.getUrl());

                    
                    linksArray.add(linkObj);
                }
                responseObject.add("links", linksArray);

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
