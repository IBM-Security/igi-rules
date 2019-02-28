# Notify if User Manager changes

## Description
This rule checks if the user manager of a given user has changed and if yes, it will notify the concerned.

For a detailed explanation of the rule code, see the Rules Guide.

## Package Imports
The imports for this rule:
```java
import com.engiweb.logger.impl.Log4JImpl
import com.engiweb.pm.entity.BeanList
import com.engiweb.profilemanager.backend.dao.db.SQLH
import com.engiweb.profilemanager.common.bean.AccountBean
import com.engiweb.profilemanager.common.bean.ExternalInfo
import com.engiweb.profilemanager.common.bean.OrgUnitBean
import com.engiweb.profilemanager.common.bean.OrgUnitErcBean
import com.engiweb.profilemanager.common.bean.PwdCfgBean
import com.engiweb.profilemanager.common.bean.UserBean
import com.engiweb.profilemanager.common.bean.UserErcBean
import com.engiweb.profilemanager.common.bean.event.EventBean
import com.engiweb.profilemanager.common.bean.event.EventInBean
import com.engiweb.profilemanager.common.ruleengine.action.OrgUnitAction
import com.engiweb.profilemanager.common.ruleengine.action.UserAction
import com.engiweb.profilemanager.common.ruleengine.action.UtilAction
import com.engiweb.toolkit.common.DBMSException
import com.engiweb.profilemanager.common.bean.Block
import com.engiweb.profilemanager.common.ruleengine.action.UtilAction
import com.engiweb.profilemanager.common.ruleengine.action.OrgUnitAction
import com.engiweb.profilemanager.common.ruleengine.action.UserAction
import com.engiweb.profilemanager.common.bean.UserBean
import com.engiweb.profilemanager.common.bean.UserErcBean
import com.engiweb.profilemanager.common.bean.entitlement.EntitlementBean
import com.engiweb.profilemanager.common.bean.event.EventInBean

global com.engiweb.profilemanager.backend.dao.db.SQLH sql
global com.engiweb.logger.impl.Log4JImpl logger

```

## Rule Code
The code is:
```java
when
		event : EventInBean(  )
		userBean : UserBean(  )
		externalInfo : ExternalInfo(  )
then
		// [ V1.1 - 2014-05-26 ]

		// Check MANAGER has changed
		String changedAttributes = (String) event.getExtAttr9();
  		if (changedAttributes == null) {
			changedAttributes = "";
		}
    	changedAttributes = changedAttributes.toUpperCase();
    	// MANAGER is mapped with column ATTR1
    	if (!changedAttributes.contains("ATTR1;")) {
      		return;
    	}

	    final String SMTP = "igi523.ibm.com";
	    final int SMTP_PORT = 25;
	
	    final String MAIL_FROM = "igi@ibm.com";
	    final String MAIL_SUBJECT = "New managed user";
	
	    String mailMsg = "Dear User Manager\n\nNew managed user:";
	
	    String managerUserID = (String) externalInfo.getAttribute("MANAGER");
	    UserBean managerBean = UtilAction.findUserByCode(sql, managerUserID);
	
	    String MAIL_TO = managerBean.getEmail();
	
	    mailMsg += " " + userBean.getName() + " " + userBean.getSurname() + " userID=" + userBean.getCode();
	
	    // Send email using specified SMTP
	    try {
	      UtilAction.sendMail(null, MAIL_FROM, MAIL_TO, null, MAIL_SUBJECT, mailMsg, SMTP, SMTP_PORT);
	      logger.debug("Mail sent");
	    } catch (Exception e) {
	      logger.error("Unable to send an email" + e);
	    }

```
