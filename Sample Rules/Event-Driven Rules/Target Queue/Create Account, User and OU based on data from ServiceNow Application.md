# Create Account, User and corresponding OU based on data from ServiceNow Application

## Description
This rule attempts to check for an unmatched account from ServiceNow. In case of unmatched account, create a user based on attributes available in the events.
Likewise, also create the Organizational Unit of the user being created. All the data is deduced from events OR for that matter ServiceNow application. 

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
import com.engiweb.profilemanager.common.bean.AccountAttrValueList
import com.engiweb.profilemanager.common.bean.ExternalInfo
import com.engiweb.profilemanager.common.ruleengine.action.UtilAction
```

## Rule Code
The code is:
```java
when
    event : EventTargetBean(  )  
    account : AccountBean(  )  
    attributes : AccountAttrValueList(  )
then
	// Ver. 5.2, D.Chilovich IBM, 2018-09-12 - add User Owner for all SN Accounts, and add OUs from account erServiceNowDepartment

	// Exit if account_code is null
	if (event.getCode() == null) {
	    logger.info("Account code is empty. Account cannot be created - no code!");
	    return;
	}
	
	// Exit if already matched to existing User already
	if (account.getPerson_id() != null) {
    	logger.info("Account Owner User exists, and is already matched to user ID: " + account.getPerson_id() );
    	return;
	}
  
	// Construct user attributes
	UserBean userBean = new UserBean(  );
	OrgUnitBean orgUnitBean = new OrgUnitBean(  );
	ExternalInfo externalInfo = new ExternalInfo(  );
	String userTargetName = null;
	String userName = null;
	String userSurname = null;
	String userMAIL = null;
	String userADDn = null;

	// User Attributes from Account object
	String userCode = event.getCode().toString();
	if ( event.getEmail() != null ) {  
		userMAIL = event.getEmail(); 
	}
	
	if ( event.getSurname() != null ) {  
		userSurname = event.getSurname(); 
	}
	
	if ( event.getName() != null ) {  
		userName = event.getName(); 
	}
	
	if ( event.getTarget() != null ) {  
		userTargetName = event.getTarget(); 
	}
	
	if ( event.getTarget() != null ) {  
		userADDn  = event.getDn(); 
	}
	
	logger.info("Rule is processing account DN: " + userADDn );
	
	// Based on the application, the account belongs to, decide if the corresponding owner should be created.
	if ( userTargetName != null ) {
		if ( userTargetName.equals("Service-Now Democenter") ) {
			logger.info("Account belongs to application under IGI Marker " + userTargetName  + ". Owner User will be created, if does not exist.");
		} else {
			logger.info("Account belongs to application under IGI Marker " + userTargetName  + ". Owner User will not be created!");
			return;
		}
  	} else {
		logger.info("Account does not belong to any application!");
		return;
	}
  
	// Determine if parent OU exists, add if missing
	String parentCode = "SNHR";
	OrgUnitBean parentBean = UtilAction.findOrgUnitByCode(sql, parentCode);

	if (parentBean == null) {
		OrgUnitBean root = new OrgUnitBean();
     	root.setId(1L);
     	parentBean = UtilAction.createOrgUnit(sql, parentCode, parentCode, "", root, false, false);
     	logger.info("Account Owner Parent OU created with code: " + parentCode);
	}
	
	// Determine Account Owner OU name and code from account name
	String [] DNArray = userADDn.split(",");
	String userOU = DNArray[2].substring(DNArray[2].indexOf("=")+1).toUpperCase();
    
	if (userOU == null) {
  		userOU = "SNHR";
	}
	logger.info("Owner OU to be: " + userOU );

	// Determine if User OU exists. If it is missing, add it under parent OU.
	OrgUnitBean userOUBean = UtilAction.findOrgUnitByCode(sql, userOU);

	if (userOUBean == null) {
     	userOUBean = UtilAction.createOrgUnit(sql, userOU, userOU, "", parentBean, false, false);
     	logger.info("Account Owner OU created with code: " + userOU);
	}

	// Construct new owner User
	orgUnitBean.setCode(userOU);
	userBean.setCode(userCode);
	if ( userMAIL != null ) { 
		userBean.setEmail(userMAIL); 
	}
	
	if ( userSurname != null ) { 
		userBean.setSurname(userSurname);
	}
	
	if ( userName != null ) { 
		userBean.setName(userName); 
	}

	// Create user in IGI, if it is based on SN Account
	if ( userTargetName != null ) {
		if ( userTargetName.equals("Service-Now Democenter") ) {
			try {
    			UserAction.add(sql, userBean, orgUnitBean, externalInfo);
			} catch (DBMSException e) {
 			   	if (e.getErrorCode() == DBMSException.OBJECT_NOT_UNIQUE) {
      				throw new Exception("Account Owner already exists with Code: " + userBean.getCode());
			   	} else {
      				throw e;
			   	}
			}
  		}
		logger.info("Account " +  userCode + " owner User created with Code ID: " + userBean.getCode());
	}
```
