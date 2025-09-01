package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.HibernateUtil;
import hibernate.Link;
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
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "DeleteLink", urlPatterns = {"/DeleteLink"})
public class DeleteLink extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Gson gson = new Gson();

        BufferedReader reader = request.getReader();
        JsonObject requestBody = gson.fromJson(reader, JsonObject.class);

        String id = requestBody.get("id").getAsString();

        SessionFactory sf = HibernateUtil.getSessionFactory();
        Session s = sf.openSession();
        Transaction tx = null;

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        try {
            Criteria linkCriteria = s.createCriteria(Link.class);
            linkCriteria.add(Restrictions.eq("id", Integer.parseInt(id)));
            Link link = (Link) linkCriteria.uniqueResult();

            if (link != null) {
                tx = s.beginTransaction();
                s.delete(link);
                tx.commit();
                responseObject.addProperty("status", true);
                responseObject.addProperty("message", "Link Deleted");
            } else {
                responseObject.addProperty("status", false);
                responseObject.addProperty("message", "Link Not Found");
            }
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Server Error");
        } finally {
            s.close();
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));
    }
}
