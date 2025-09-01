package controller;

import com.google.gson.Gson;
import hibernate.Link;
import hibernate.User;

import javax.persistence.*;
import javax.persistence.criteria.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@WebServlet(name = "UserDetails", urlPatterns = {"/UserDetails"})
public class UserDetails extends HttpServlet {

    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("LinklyPU");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Gson gson = new Gson();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // --- Read Request JSON ---
        String body = new BufferedReader(request.getReader())
                .lines().collect(Collectors.joining());
        Map<String, String> req = gson.fromJson(body, Map.class);

        String email = req.get("email");
        if (email == null || email.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Email required\"}");
            return;
        }

        EntityManager em = emf.createEntityManager();
        try {
            // --- Get User by email ---
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<User> cq = cb.createQuery(User.class);
            Root<User> userRoot = cq.from(User.class);
            cq.where(cb.equal(userRoot.get("email"), email));
            List<User> users = em.createQuery(cq).getResultList();

            if (users.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\":\"User not found\"}");
                return;
            }

            User user = users.get(0);

            // --- Get Links for user ---
            CriteriaQuery<Link> linkQuery = cb.createQuery(Link.class);
            Root<Link> linkRoot = linkQuery.from(Link.class);
            linkQuery.where(cb.equal(linkRoot.get("user_id"), user));
            List<Link> links = em.createQuery(linkQuery).getResultList();

            // --- Build JSON Response ---
            Map<String, Object> resp = new HashMap<>();
            resp.put("email", user.getEmail());
            resp.put("avatar", user.getImg_url() != null ? user.getImg_url() : "");

            List<Map<String, Object>> linkList = new ArrayList<>();
            for (Link l : links) {
                Map<String, Object> linkMap = new HashMap<>();
                linkMap.put("id", String.valueOf(l.getId()));
                linkMap.put("title", l.getTitle());
                linkMap.put("description", l.getDescription());
                linkMap.put("url", l.getUrl());
                linkMap.put("image", l.getImg_url() != null ? l.getImg_url() : "");
                linkList.add(linkMap);
            }

            resp.put("links", linkList);

            response.getWriter().write(gson.toJson(resp));

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Server error\"}");
        } finally {
            em.close();
        }
    }
}
