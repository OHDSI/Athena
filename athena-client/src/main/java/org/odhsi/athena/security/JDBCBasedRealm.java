package org.odhsi.athena.security;

import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.odhsi.athena.entity.SecPermission;
import org.odhsi.athena.entity.SecRole;
import org.odhsi.athena.entity.SecUser;
import org.odhsi.athena.services.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by GMalikov on 22.09.2015.
 */
public class JDBCBasedRealm extends AuthorizingRealm{

    @Autowired
    private SecurityService securityService;

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        final String username = (String) principalCollection.getPrimaryPrincipal();
        SecUser user = securityService.getUserByUsername(username);
        if (user == null){
            throw new UnknownAccountException("Username is null");
        }
        final Set<String> roleNames = new LinkedHashSet<>();
        final Set<String> permissionNames = new LinkedHashSet<>();
        for (SecRole role : securityService.getUserRoles(user.getId())){
            roleNames.add(role.getName());
        }
        for (SecPermission permission : securityService.getUserPermissions(user.getId())){
            permissionNames.add(permission.getWildcardTemplate());
        }

        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo(roleNames);
        info.setStringPermissions(permissionNames);
        return info;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        UsernamePasswordToken userPassToken = (UsernamePasswordToken) authenticationToken;
        String username = userPassToken.getUsername();

        if(username == null){
            throw new UnknownAccountException("Username is null");
        }

        SecUser user = securityService.getUserByUsername(username);

        if(user == null){
            throw new UnknownAccountException("No account found for user [" + username + "]");
        }

        SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(username, user.getPassword(),
                ByteSource.Util.bytes(user.getSalt().getBytes()), getName());
        return info;
    }

    class PasswdSalt{
        public String password;
        public String salt;

        public PasswdSalt(String password, String salt){
            super();
            this.password = password;
            this.salt = salt;
        }
    }
}
