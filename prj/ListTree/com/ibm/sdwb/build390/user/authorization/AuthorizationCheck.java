package com.ibm.sdwb.build390.user.authorization;

public interface AuthorizationCheck extends java.io.Serializable{

	public boolean isAuthorizedTo(String authorityToCheck, String entityToCheckAgainst) throws AuthorizationException;
}
