package model.dao.jdbc;

import model.dao.connection.DataSource;
import model.dao.interfaces.UsersDAO;
import model.entity.Client;
import model.entity.Entity;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.log4j.Logger;
import service.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tonchief on 05/21/2017.
 */
public class UsersDAOimpl implements UsersDAO {

    private static UsersDAOimpl instance = null;
    private final Logger logger = Logger.getLogger(UsersDAOimpl.class);
    private BasicDataSource pool = DataSource.getInstance().getBds();
    private static final int UID = 1;
    private static final int NAM = 2;
    private static final int EML = 3;
    private static final int PWD = 4;
    private static final int ROL = 5;
    private static final int ACC = 6;
    private static final int FEE = 6;
    private static final int FID = 6;
    //Checked for fields equality b/w dao and db(v2), 2017-05-27


    public static UsersDAOimpl getInstance() {
        if(instance==null) instance = new UsersDAOimpl();
        return instance;
    }



    /**
     * @param  user
     * @return int : userId obtained from db
     */
    public Integer insert(Object user) {
        //TO+DO: change new user registration to include new account creation at the time of submitting application
        //TODO - Redesign DB, set email - as id (unique) -> catch exception &

        logger.info("Insert into [clients] - [user] passed by account:" + user);

        try (Connection conn = pool.getConnection();
             PreparedStatement ps = conn.prepareStatement(BUNDLE.getString("clients.insert"), 1);
        ) {
            User client = (User) user;

            ps.setString    (NAM-1, client.getName());
            ps.setString    (EML-1, client.getEmail());
            ps.setString    (PWD-1, client.getPassword());
            ps.setLong      (ROL-1, client.getRole());
            ps.setInt       (FEE-1, client.getFeeId());

            logger.info("PS: " + ps.toString());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();  // logger.info(PrintResultSet.getDump(rs));
                Integer newUserId = rs.getInt(1);
                logger.info("New client Registration: ID="+newUserId);
                return newUserId; //rs.getLong(1)
            } finally {
                ps.close();
            }
        } catch (SQLException sqlEx) {
            if(isConstraintViolation(sqlEx)){
                return -23; //made up code to indicate existing login (email)
            }
            logger.error("SQL exception", sqlEx);
        }

        return 0;
    }

    public static boolean isConstraintViolation(SQLException e) {
        return e.getSQLState().startsWith("23");
    }

    public Integer authenticateUser(String email, String password) {
        logger.info("fetching User with given credentials: " + email + ":*****");
        if(email==null || email.isEmpty() || password==null || password.isEmpty()) return null;
        try (Connection conn = pool.getConnection();
             PreparedStatement ps = conn.prepareStatement(BUNDLE.getString("clients.checkLoginPwd"),0);
        ){
            logger.info("got conn.");
            ps.setString(1, email);
            ps.setString(2, password);
            logger.info("Trying PS:" + ps);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                Integer userId = rs.getInt(1);
                rs.close();
                return userId;
            } catch (SQLException e) {
                logger.error("SQLex." + e.toString());
            }
        } catch (SQLException e) {
            logger.error("SQL exception.", e);
        } catch (Exception e) {
            logger.error("Fatal General Exception.", e);
        }
        return null;
    }

    public List<Client> getUsersByRole(Long role){

        List<Client> resultList = new ArrayList<>();
        logger.info("fetching Client Entities for RoleId:" + role);
        try (Connection conn = pool.getConnection();
             PreparedStatement ps = conn.prepareStatement(BUNDLE.getString("clients.getByRole"), 0);
        ){
            logger.info("Got connection.");
            ps.setLong(1, role);

            logger.info("Trying PS:" + ps);

            try (ResultSet rs = ps.executeQuery()) {
//                logger.info(PrintResultSet.getDump(rs));
                int i=1;
                while(rs.next()) {
/*                    logger.warn(i++);
                    logger.info("\t1\t"+ rs.getString(UID));
                    logger.info("\t2\t"+ rs.getInt(UID));
                    logger.info("\t3\t"+ rs.getString(NAM));
                    logger.info("\t4\t"+ rs.getString(EML));
                    logger.info( rs.getLong(ROL));*/

                    Client cl= new Client( rs.getInt(UID), rs.getString(NAM),
                            rs.getString(EML), rs.getInt(ROL));
                    cl.setAccount(""+rs.getLong(ACC));
                    resultList.add(cl);
                }
                rs.close();
                return resultList;
            } catch (SQLException e) {
                logger.error("SQLex." + e.toString());
            }
        } catch (SQLException e) {
            logger.error("SQL exception.", e);
        } catch (Exception e) {
            logger.error("Major General Exception.", e);
        }
        return null;
    }

    public Client getDetailedById(Integer clientId) {
        try (Connection conn = pool.getConnection();
             PreparedStatement ps = conn.prepareStatement(BUNDLE.getString("clients.getDetailedById"), 1);
             //id_client,clients.name,email,password,role,fees.name
        ){
            logger.info("Got connection.");
            ps.setInt(1, clientId);
            logger.info("PS:" + ps);
            try (ResultSet rs = ps.executeQuery()) {
//                logger.info(PrintResultSet.getDump(rs));
                rs.next();
                Client cl= new Client( rs.getInt(UID), rs.getString(NAM), rs.getString(EML), rs.getInt(ROL) );
                cl.setFeeName(rs.getString(FID));
                rs.close();
                return cl;
            } catch (SQLException e) {
                logger.error("SQLex." + e.toString());
            }
        } catch (SQLException e) {
            logger.error("SQL exception.", e);
        } catch (Exception e) {
            logger.error("Major General Exception.", e);
        }
        return null;
    }


    public boolean update(int id, Entity data) {
        return false;
    }

    public boolean delete(long id) {
        return false;
    }

    /* For security reasons: Password is not passed / nor stored here! */
    public Entity getById(int cid) {
        try (Connection conn = pool.getConnection();
             PreparedStatement ps = conn.prepareStatement(BUNDLE.getString("clients.getById"), 1);
        ){
            logger.info("Got connection.");
            ps.setInt(1, cid);
            logger.info("Trying PS:" + ps);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                Client cl= new Client( rs.getInt(UID), rs.getString(NAM), rs.getString(EML), rs.getInt(ROL) );
                cl.setFeeId(rs.getInt(FEE));
                rs.close();
                return cl;
            } catch (SQLException e) {
                logger.error("SQLex." + e.toString());
            }
        } catch (SQLException e) {
            logger.error("SQL exception.", e);
        } catch (Exception e) {
            logger.error("Major General Exception.", e);
        }
        return null;
    }




}
