package com.library.controller;

import com.library.dao.UserDAO;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/** On first start (empty users table) creates demo accounts. Password comes from SEED_PASSWORD. */
@WebListener
public class DataSeeder implements ServletContextListener {
    private static final Logger LOG = Logger.getLogger(DataSeeder.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent e) {
        String pw = System.getenv("SEED_PASSWORD");
        if (pw == null || pw.isEmpty()) pw = "ChangeMe@123";
        try {
            UserDAO dao = new UserDAO();
            if (dao.count() == 0) {
                dao.create("admin", "admin@library.local", "Admin", pw, "ADMIN");
                dao.create("librarian", "librarian@library.local", "Meera Librarian", pw, "LIBRARIAN");
                dao.create("asha", "asha@library.local", "Asha Rao", pw, "MEMBER");
                LOG.info("Demo users created: admin, librarian, asha");
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Seeding skipped. Is MySQL running and schema.sql applied?", ex);
        }
    }
}
