# User Move Alongwith existing Entitlements

## Description
This rule shows the procedure for moving entitlements along with the user movement, from one department to another.

For a detailed explanation of the rule code, see the Rules Guide.

## Package Imports
The imports for this rule:
```java
import com.engiweb.logger.impl.Log4JImpl;
import com.engiweb.pm.entity.BeanList;
import com.engiweb.profilemanager.backend.dao.db.SQLH;
import com.engiweb.profilemanager.common.bean.AccountBean;
import com.engiweb.profilemanager.common.bean.ExternalInfo;
import com.engiweb.profilemanager.common.bean.OrgUnitBean;
import com.engiweb.profilemanager.common.bean.OrgUnitErcBean;
import com.engiweb.profilemanager.common.bean.PwdCfgBean;
import com.engiweb.profilemanager.common.bean.UserBean;
import com.engiweb.profilemanager.common.bean.UserErcBean;
import com.engiweb.profilemanager.common.bean.event.EventBean;
import com.engiweb.profilemanager.common.bean.event.EventInBean;
import com.engiweb.profilemanager.common.ruleengine.action.OrgUnitAction;
import com.engiweb.profilemanager.common.ruleengine.action.UserAction;
import com.engiweb.profilemanager.common.ruleengine.action.UtilAction;
import com.engiweb.toolkit.common.DBMSException;
import com.engiweb.profilemanager.common.bean.Block;
import com.engiweb.profilemanager.common.ruleengine.action.UtilAction;
import com.engiweb.profilemanager.common.ruleengine.action.OrgUnitAction;
import com.engiweb.profilemanager.common.ruleengine.action.UserAction;
import com.engiweb.profilemanager.common.bean.UserBean;
import com.engiweb.profilemanager.common.bean.UserErcBean;
import com.engiweb.profilemanager.common.bean.entitlement.EntitlementBean;
import com.engiweb.profilemanager.common.bean.event.EventInBean;

global com.engiweb.profilemanager.backend.dao.db.SQLH sql;
global com.engiweb.logger.impl.Log4JImpl logger;
```

## Rule Code
The code is:
```java
when
		userBean : UserBean(  )
		externalInfo : ExternalInfo(  )
		orgUnitBean : OrgUnitBean(  )
then
  // Steps involved in this rule:
  // 1. Move user if OU changed.  
  // 2. Modify user:
  //    2.1. Check User Type. If User type changed, create new User with old ID and new type,
  // 	leave old User in IGI, with new ID *_OLD, transfer all old user entitlements to new User,
  //	and modify new user.
  //	2.2. OR simply modify user.
  // 3. Inform manager ( other rule )

  // Move user if OU changed
  if (userBean.getOrganizationalunit_id() == null) {
    //User not associated with an OU.
    String ouCode = (String) externalInfo.getAttribute("OU");
    if (ouCode != null) {
		OrgUnitBean ouBean = UtilAction.findOrgUnitByCode(sql, ouCode);
		if (ouBean != null) {
			UtilAction.moveUser(sql, userBean, ouBean);
			logger.debug("User " + userBean.getCode() + " moved to OU " + ouCode );
      }
    }
  }

  // Modify User
  // Check if user type changed. Change code for old user. Assign the code to new user with new type.
  // Suspend old user. Move all credentials of old user to new user. 
  String userCode = userBean.getCode();
  
  UserBean userExistsBean = UtilAction.findUserByCode(sql, userCode);
  if ( userExistsBean.getPersontype_name() != userBean.getPersontype_name() ) {

		// Construct new user
		UserBean userNewBean = new UserBean(  );
		OrgUnitBean orgUnitNewBean = new OrgUnitBean(  );
		ExternalInfo externalNewInfo = new ExternalInfo(  );
		
		String userNewCode = userCode + "_OLD";
		String userNewType = userBean.getPersontype_name();
		String userOldType = userExistsBean.getPersontype_name();
		String userMAIL = userBean.getEmail();
		String userSurname = userBean.getSurname();
		String userName = userBean.getName();
		String userNewOU = userExistsBean.getOrganizationalunit_code();

		userNewBean.setCode(userNewCode);
		userNewBean.setPersontype_name(userOldType);
		orgUnitNewBean.setCode(userNewOU);
	
		if ( userMAIL != null ) { 
			userNewBean.setEmail(userMAIL); 
		}

		if ( userSurname != null ) { 
			userNewBean.setSurname(userSurname); 
		}
	
		if ( userName != null ) { 
			userNewBean.setName(userName); 
		}
	
		try {
			UserAction.add(sql, userNewBean, orgUnitNewBean, externalNewInfo);
		} catch (DBMSException e) {
			if (e.getErrorCode() == DBMSException.OBJECT_NOT_UNIQUE) {
				throw new Exception("New User already exists with code: " + userBean.getCode());
			} else {
				throw e;
			}
		}
		
		logger.info("User type change found, from  " + userOldType + " to " + userNewType );
		
		userBean.setPersontype_name(userNewType);
		UserAction.modifyUser(sql, userBean, externalInfo);
		
		logger.info("User updated with type change management, moved from  " + userCode + " to " + userNewCode );
  }	else {
	// Else - just Update all user attributes
	UserAction.modifyUser(sql, userBean, externalInfo);
	logger.info("User updated " + userBean.getCode());
  }

```
