package control.command;

import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CommandLogin implements Command {

    @Override
    public String execute(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String page = null;
        String login = req.getParameter("email");
        String password = req.getParameter("password");

        Logger logger = Logger.getLogger(CommandLogin.class);
        logger.info(login + ":"+ password+ ":"+SERVICE.getLogin().checkCredentials(login, password));


        if(SERVICE.getLogin().checkCredentials(login, password)){

            req.setAttribute("auth", true);
            page = PROPS.getString("user.authorized");
        } else {
            if(!req.getMethod().equals("GET")) {
                req.setAttribute("errormsg", "Wrong login [" + login + "] or password [" + password + "]");
                req.setAttribute("errormsg_html",
                        "<br><div class=\"alert alert-danger\" role=\"alert\">Wrong login [" + login + "] or password [" + password + "]</div>");
                req.setAttribute("errorcode", 9401);
            }
//            page = PROPS.getString("user.login");

            req.setAttribute("action","login");

            page = PROPS.getString("user.main");

        }

        return page;
    }
}
