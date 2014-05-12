package fr.auffredou.transfer;

import org.apache.ftpserver.ftplet.*;
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class FtpDummyUserManager implements UserManager {
    private static final Logger LOG = LoggerFactory.getLogger(FtpDummyUserManager.class);

    @Override
    public User getUserByName(String s) throws FtpException {
        return createUser(s);
    }

    @Override
    public String[] getAllUserNames() throws FtpException {
        return new String[0];
    }

    @Override
    public void delete(String s) throws FtpException {}

    @Override
    public void save(User user) throws FtpException {}

    @Override
    public boolean doesExist(String s) throws FtpException {
        return true;
    }

    @Override
    public User authenticate(Authentication authentication) throws AuthenticationFailedException {
        UsernamePasswordAuthentication auth = (UsernamePasswordAuthentication) authentication;
        LOG.debug("Authenticating - username=" + auth.getUsername());
        return createUser(auth.getUsername());
    }

    private User createUser(String username) {
        BaseUser user = new BaseUser() {
            @Override
            public AuthorizationRequest authorize(AuthorizationRequest request) {
                return request;
            }
        };
        user.setName(username);
        user.setPassword(username);
        user.setHomeDirectory(System.getProperty("java.io.tmpdir") + File.separator + username);
        File home = new File(user.getHomeDirectory());
        if (!home.exists()) {
            home.mkdirs();
        }
        user.setEnabled(true);
        return user;
    }

    @Override
    public String getAdminName() throws FtpException {
        return "admin";
    }

    @Override
    public boolean isAdmin(String s) throws FtpException {
        return false;
    }


}
