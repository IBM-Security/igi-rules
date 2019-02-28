# Create Account and create corresponding User if Missing

## Description
This rule attempts to check for an unmatched account and in that case, creates a user based on attributes available in the events. 

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
```

## Rule Code
The code is:
```java
when
    event : EventTargetBean(  )  
    account : AccountBean(  )  
    attributes : AccountAttrValueList(  )
then
	// Ver. 3, D.Chilovich IBM 3-24-2017

	// Exit if account_code is null
	if (event.getCode() == null) {
    	logger.info("Account code is empty. Account can not be created - no code!");
    	return;
	}

	// Exit if already matched to existing User
	if (account.getPerson_id() != null) {
    	logger.info("Account Owner User exists, and is already matched to user ID: " + account.getPerson_id() );
    	return;
	}
  
	// Construct user attributes
	UserBean userBean = new UserBean(  );
	OrgUnitBean orgUnitBean = new OrgUnitBean(  );
	ExternalInfo externalInfo = new ExternalInfo(  );

	// Pull User Attributes from Account object
	String userOU = "AUTO-PROVISIONED";
	String userCode = event.getCode().toString();
	String userMAIL = null;
	String userSurname = null;
	String userName = null;
	String userDisplayName = null;
	String userIdentityUID = null;
	String userTargetName = null;
	
	if ( event.getEmail() != null ) {  
		userMAIL = event.getEmail(); 
	}

	if ( event.getSurname() != null ) {  
		userSurname = event.getSurname(); 
	}
	
	if ( event.getName() != null ) {  
		userName = event.getName(); 
	}
	
	if ( event.getDisplayName() != null ) {  
		userDisplayName = event.getDisplayName(); 
	}
	
	if ( event.getIdentityUID() != null ) {  
		userIdentityUID = event.getIdentityUID(); 
	}
	
	if ( event.getTarget() != null ) {  
		userTargetName = event.getTarget(); 
	}

	// Details in debug mode
	logger.debug("userOU " + userOU);
	logger.debug("userCode " + event.getCode().toString());
	logger.debug("userMAIL " + event.getEmail());
	logger.debug("userSurname " + event.getSurname());
	logger.debug("userName " + event.getName());
	logger.debug("userDisplayName " + event.getDisplayName());
	logger.debug("userIdentityUID " + event.getIdentityUID());
	logger.debug("userTargetName " + event.getTarget());

	// Based on the application, the account belongs to, decide if the corresponding owner should be created.
	if ( userTargetName != null ) {
		logger.info("Account belongs to application under IGI Marker " + userTargetName + ". Owner User will be created, if does not exist.");
	} else {
		logger.info("Account does not belong to any application!");
		return;
  	}
  
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
	} else if ( userIdentityUID != null ) {
		String fuserName = "FID_" + userIdentityUID;
		userBean.setName(fuserName);
	} else {
		String fuserName = "FID_" + userCode;
		userBean.setName(fuserName);
	}

	// Create user in IGI
	try {
		UserAction.add(sql, userBean, orgUnitBean, externalInfo);
  	} catch (DBMSException e) {
    	if (e.getErrorCode() == DBMSException.OBJECT_NOT_UNIQUE) {
      		throw new Exception("Account owner User already exists with Code: " + userBean.getCode());
		} else {
      		throw e;
      	}
	}

	logger.debug("Account " +  userCode + " owner User created with Code ID: " + userBean.getCode());
```
