# Transform Modify User Operation to Add User

## Description
This rule shows the procedure for converting a Modify User Operation to an Add User Operation

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

global com.engiweb.profilemanager.backend.dao.db.SQLH sql
global com.engiweb.logger.impl.Log4JImpl logger
```

## Rule Code
The code is:
```java
when
    event : EventBean(  )  
    userErcBean : UserErcBean(  )
then
	// [ V1.1 - 2014-05-26 ]

	logger.debug("USERS input transformation, event:" + event);

	EventInBean eventin = (EventInBean) event;

	final long ADD = 1;
	final long MODIFY = 2;
	final long DISCARDED = 99;

	long currentOperation = eventin.getOperation();
	String userid = (String) userErcBean.getAttribute("PM_CODE");
	Long ercTableId = (Long) userErcBean.getAttribute("ID");

	// ----------
	// Base request is to MODIFY the User, however if the user does not exist, transform the operation to ADD
	// Transform MODIFY to ADD
	// ----------

	if (currentOperation == MODIFY) {
		if (userid == null) {
			// In this example, we assume that PM_CODE is calculated by the rule around ADD User.
			// That would mean, if PM_CODE is null, the user can't exist
      		eventin.setOperation(ADD);
			// save the real operation code
			eventin.setExtAttr10(Long.toString(currentOperation));
    	} else {
			// look for the user using the ercId
			UserBean userBean = UtilAction.findUserByErcId(sql, ercTableId);
			if (userBean == null) {
				eventin.setOperation(ADD);
				// Save the real operation code
				eventin.setExtAttr10(Long.toString(currentOperation));
			}
		}
	}
```
