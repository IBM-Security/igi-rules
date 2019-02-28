# Reset password on the target

## Description
This rule helps in prompting the user for changing his password the next time he logs onto the applicable system.

For a detailed explanation of the rule code, see the Rules Guide.

## Package Imports
The imports needed for this rule:

```java
import com.engiweb.logger.impl.Log4JImpl
import com.engiweb.pm.entity.BeanList
import com.engiweb.profilemanager.backend.dao.db.SQLH
import com.engiweb.profilemanager.common.bean.AccountBean
import com.engiweb.profilemanager.common.bean.Block
import com.engiweb.profilemanager.common.enumerations.LockType
import com.engiweb.profilemanager.common.bean.entitlement.EntitlementBean
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
import com.engiweb.profilemanager.common.ruleengine.action.JobRoleAction
import com.engiweb.profilemanager.backend.business.direct.AccountDirect
import com.engiweb.toolkit.common.DBMSException

global com.engiweb.profilemanager.backend.dao.db.SQLH sql
global com.engiweb.logger.impl.Log4JImpl logger
```



## Rule Code
The code is:
```java
when
    accountBean : AccountBean(  )  
    event : EventTargetBean(  )
then
// [ V1.0 ]

	if (accountBean.getId() == null) {
    	throw new Exception("Account " + event.getCode() + " not found for target " + event.getTarget());
	}

	String encryptedPassword = event.getAttr1();
	String password = null;

	if (!event.getTarget().equals("Win2017 AD Application")) {
    	// do nothing
	} else if (accountBean.getLastlogin() <= Java.util.date("CurrentDate") ) {
		accountBean.setAttribute("RESETATNEXTLOGIN", true );
		logger.debug("EVENT TARGET: Account "+ event.getCode() + " was processed" );
		logger.info("EVENT TARGET: Account "+ event.getCode() + " was processed" );
	}
```

Note the rule is hardcoded to apply to the "Win2017 AD Application" application.
