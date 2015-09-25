package org.odhsi.athena.entity;

/**
 * Created by GMalikov on 18.09.2015.
 */
public class SecPermission {
    private Long id;
    private SecUser user;
    private SecAction action;
    private SecDomain domain;
    private String instanceId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SecUser getUser() {
        return user;
    }

    public void setUser(SecUser user) {
        this.user = user;
    }

    public SecAction getAction() {
        return action;
    }

    public void setAction(SecAction action) {
        this.action = action;
    }

    public SecDomain getDomain() {
        return domain;
    }

    public void setDomain(SecDomain domain) {
        this.domain = domain;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getWildcardTemplate(){
        String result = "";
        if (this.getDomain() != null){
            result = result + this.getDomain().getName() + ":";
        } else {
            result = result + ":";
        }
        if (this.getAction() != null){
            result = result + this.getAction().getName() + ":";
        } else {
            result = result + ":";
        }
        if (this.getInstanceId() != null){
            result = result + this.getInstanceId();
        }
        return result;
    }
}
