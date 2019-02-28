# Create Account and Match it to the User specified against Gecos

## Description
This rule attempts to create an account and match it to a user. If it is not able to match on first attempt, it will attempt to extract
the matching user name noted in the comments against the user (gecos) and use that to match against the account. 

For a detailed explanation of the rule code, see the Rules Guide.

## Package Imports
The imports needed for this rule:

```java
import com.engiweb.logger.impl.Log4JImpl
import org.apache.log4j.Logger
import com.engiweb.pm.entity.BeanList
import com.engiweb.profilemanager.backend.dao.db.SQLH
import com.engiweb.profilemanager.common.bean.AccountBean
import com.engiweb.profilemanager.common.bean.Block
import com.engiweb.profilemanager.common.bean.OrgUnitBean
import com.engiweb.profilemanager.common.bean.UserBean
import com.engiweb.profilemanager.common.bean.entitlement.EntitlementBean
import com.engiweb.profilemanager.common.bean.event.EntStateBean
import com.engiweb.profilemanager.common.bean.event.EventTargetBean
import com.engiweb.profilemanager.common.bean.rule.SyncStateBean
import com.engiweb.profilemanager.common.ruleengine.action.EntitlementAction
import com.engiweb.profilemanager.common.ruleengine.action.OrgUnitAction
import com.engiweb.profilemanager.common.ruleengine.action.UserAction
import com.engiweb.toolkit.common.DBMSException

global com.engiweb.profilemanager.backend.dao.db.SQLH sql
global com.engiweb.logger.impl.Log4JImpl logger
```

```java
import javax.naming.directory.DirContext
import java.util.Hashtable 
import javax.naming.Context
import javax.naming.ldap.InitialLdapContext
import javax.naming.NamingException
import javax.naming.directory.SearchControls
import javax.naming.NamingEnumeration
import javax.naming.directory.SearchResult
import javax.naming.directory.Attributes
import javax.naming.directory.Attribute

```



## Rule Code
The code is:
```java
when
    event : EventTargetBean(  )  
    account : AccountBean(  )
then
	String eventUserCode="";
	if (account.getId() != null) {
    	// Account already exists
    	logger.info("Account already exists!");
    	event.setTrace("Account already exists!");
    	return;
	}

	if (account.getPerson_id() == null) {
    	// Create the UnMatched account
    	eventUserCode = event.getCode();
    	if (eventUserCode != null) {
      		account.setCode(eventUserCode);
  	  	}
    	UserAction.addAccount(sql, account);
    	logger.info("Account created!");
    	event.setTrace("Unable to match Identity!");
	}

	String filter;
	DirContext dirContext = null;


	Hashtable<String, String> env = new Hashtable<String, String>();
	env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
	env.put(Context.PROVIDER_URL, "ldap://192.168.42.140:389");
	env.put(Context.SECURITY_AUTHENTICATION, "simple");
	env.put(Context.SECURITY_PRINCIPAL, "cn=root");
	env.put(Context.SECURITY_CREDENTIALS, "igi");
   
	try {
		dirContext = new InitialLdapContext(env, null);
	} catch (NamingException e) {

	}

	filter = "(eruid="+eventUserCode+")";
	SearchControls controls = new SearchControls();
	controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
	NamingEnumeration<?> result = dirContext.search("dc=com", filter, controls);
	while (result.hasMoreElements()) {
		logger.info("Account Found in LDAP");
		SearchResult searchResult = (SearchResult)  result.next();
		Attributes attrs = searchResult.getAttributes();
		Attribute gecos = attrs.get("erposixgecos");
		if(gecos != null) {
			String masteruid= (String) gecos.get();
  
  			// Look for the User into IDEAS
			UserBean userFilter = new UserBean();
			userFilter.setCode(masteruid);
			BeanList ul = UserAction.find(sql, userFilter);
			logger.info("value of ul--"+ul);
			if (ul.size() == 0) {
				// userNotFound
				logger.info("user not found into IDEAS");
				throw new Exception("User not found into IDEAS, description=" + masteruid);

			} else {
				// found
				userFilter = (UserBean) ul.get(0);
				account.setPerson_id(userFilter.getId());
				UserAction.updateAccount(sql,account);
				logger.info("Account ownership Updated Successfully");
			}
	} else {  
		logger.info("gecos attribute not found!"); }
	}
```
