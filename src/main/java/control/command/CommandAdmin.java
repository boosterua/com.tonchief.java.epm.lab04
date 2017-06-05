package control.command;


import model.dao.exceptions.ExceptionDAO;
import model.dao.exceptions.MySqlPoolException;
import model.entity.Card;
import model.entity.Client;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class CommandAdmin implements Command {
    private static final Logger LOGGER = Logger.getLogger(CommandAdmin.class);
    private String page = RB_PAGEMAP.getString("jsp.admin");


    @Override
    public String execute(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, ExceptionDAO, MySqlPoolException {
        LOGGER.info("servlet get Param/Attr "+ req.getParameter("action") + " : " + req.getAttribute("action"));
        String act = req.getParameter("action");  if (act == null) act = "";
        String uid = req.getParameter("user_id"); Integer clientId = uid==null? 0 : Integer.parseInt(uid);;
        String acctIdStr = req.getParameter("account_id"); Integer acctId = acctIdStr==null ? 0 : Integer.parseInt(acctIdStr);
        String feeIdStr = req.getParameter("fee_id"); Integer feeId = feeIdStr==null? 0 : Integer.parseInt(feeIdStr);
        disablePageCache(resp);

        switch (act){
            case ("set_user_role"):
                String roleStr = req.getParameter("role");
                LOGGER.info("clientID:"+clientId + " > setuserrole " + roleStr);
                setRole(req, resp, clientId, roleStr);
                break;
            case ("unblock_account"):
                setAccountBlock(req, resp, acctId, false);
                break;
            case ("issue_new_card"):
                issueNewCard(req, resp, acctId, feeId, clientId);
                break;
            case ("show_clients_by_role"):
                Long role = Long.parseLong(req.getParameter("role"));
                List<Client> clients = SERVICE.getAdmin().getClientsByRole(role);
                req.setAttribute("clientList", clients);
                req.setAttribute("action", "show_clients_by_role");
                break;
            case ("get_one_client"):
                Client client = null;
                try {
                    req.setAttribute("action", "show_сlient_to_approve");
                    client = SERVICE.getAdmin().getClientDetailedById(clientId);
                    req.setAttribute("client", client);
                } catch (ExceptionDAO exceptionDAO) {
                    throw exceptionDAO;
                }
                break;
            case (""):
                req.setAttribute("action", "index");
                break;
            default:
                wrongParam(req);
        }
        return page;
    }

    private void setAccountBlock(HttpServletRequest req, HttpServletResponse resp, Integer accountId, boolean b) throws MySqlPoolException, ExceptionDAO {
        if(accountId==0){wrongParam(req); return;}
        boolean res = SERVICE.getAdmin().removeAccountBlock(accountId);
        LOGGER.info(""+accountId+ " > unblocking acct " );
        req.setAttribute("result", res);
        req.setAttribute("infomsg", "RESULT_" + (res?"OK":"ERROR"));
        if(req.getParameter("content_type")!=null) {//ajax
            resp.setContentType("text/plain");
            resp.setCharacterEncoding("UTF-8");
            try {
                resp.getWriter().write("OK");
            } catch (IOException e) {
                LOGGER.error(e);
            }
            page="";
        }
    }

    private void setRole(HttpServletRequest req, HttpServletResponse resp, Integer clientId, String roleStr ) throws ExceptionDAO {

        if(roleStr==null || clientId==0){
            LOGGER.info("client_id="+clientId + "; roleStr="+roleStr);
            req.setAttribute("errormsg", "NO_USER_ID");
            wrongParam(req);
            return;
        }
        Long role = Long.parseLong(roleStr);

        try {
            boolean res = SERVICE.getAdmin().setRole(clientId, role);
            LOGGER.info(""+clientId + " > set.user.role " + roleStr);
            req.setAttribute("command", "admin");
            req.setAttribute("action", "get"+"_one_client");
            req.setAttribute("result", res);
            req.setAttribute("infomsg", "RESULT_" + (res?"OK":"ERROR"));
            if(req.getParameter("content_type")!=null) {//ajax
                resp.setContentType("text/plain");
                resp.setCharacterEncoding("UTF-8");
                resp.getWriter().write(role>0?"OK":"0");
                page="";
            }
        } catch (ExceptionDAO exceptionDAO) {
            throw exceptionDAO;
        } catch (IOException e) {
            LOGGER.error(e);
        }
    }


    private void issueNewCard(HttpServletRequest req, HttpServletResponse resp, Integer accountId,
                              Integer feeId, Integer clientId) throws MySqlPoolException, ExceptionDAO {
            if(accountId==0){wrongParam(req); return;}
            Card card = SERVICE.getAdmin().issueNewCard(accountId, feeId);
            LOGGER.info("Issue new card for Acct="+accountId+ " >  " + card );
            if(card==null) {
                req.setAttribute("errormsg", "CARD_NOT_ISSUED");
            } else {

                String referer = req.getHeader("Referer");
                resp.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
                resp.setHeader("Location", referer);

                req.setAttribute("imfomsg", "!!!");
                req.setAttribute("command", "admin");
                req.setAttribute("action", "get_one_client");
//                Client client = null;
//                req.setAttribute("user_id", clientId);
//                try {
//                    client = SERVICE.getAdmin().getClientDetailedById(clientId);
//                    req.setAttribute("client", client);
//                    LOGGER.warn(clientId+" : " + client);
//                } catch (ExceptionDAO exceptionDAO) {
//                    LOGGER.error(exceptionDAO);
//                }
            }









//            getServletContext().getRequestDispatcher(page).forward(req, resp);



        req.setAttribute("infomsg", "RESULT_" + (card!=null?"OK":"ERROR"));
            if(ifAjaxPrintOK(req, resp, card!=null?"OK":"ERROR"))
                page = "";
    }



    private void wrongParam(HttpServletRequest req){
        req.setAttribute("errormsg", "WRONG_PARAM_REQUEST");
    }

    private boolean ifAjaxPrintOK(HttpServletRequest req, HttpServletResponse resp, String message){
        if(req.getParameter("content_type")!=null) {//ajax
            resp.setContentType("text/plain");
            resp.setCharacterEncoding("UTF-8");
            try {
                resp.getWriter().write(message);
            } catch (IOException e) {
                LOGGER.warn(e);
            }
            return true;
        }
        return false;
    }
}
