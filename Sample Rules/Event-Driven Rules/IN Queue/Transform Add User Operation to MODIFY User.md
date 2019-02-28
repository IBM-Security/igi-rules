# Transform Add User Operation to Modify User

## Description
This rule shows the procedure for converting an Add User Operation to a Modify User Operation

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
    event : EventInBean(  )  
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

	if (userid==null) {
		return;
	}

	// ----------
	// Base request is to ADD the User, however if the user already exists, transform the operation to MODIFY
	// NOTE: This situation can occur, since users created from the Console, don't have a related record in USER_ERC
	// ----------

	if (currentOperation == ADD) {
		// look for the User
		UserBean userBean = UtilAction.findUserByCode(sql, userid);
		if (userBean != null) {

			eventin.setOperation(MODIFY);
			// Save the real operation code
			eventin.setExtAttr10(Long.toString(currentOperation));

			// Check the fk to userErc
			Long realErcTableId = userBean.getUserErc();
			if (realErcTableId == null) {
				// Align the fk
				userBean.setUserErc(ercTableId);
				UserAction.modifyUser(sql, userBean, null);
			} else if (realErcTableId.equals(ercTableId)) {
        		// Nothing to do
      		} else {
        		throw new Exception("User " + userid + " already exists and it is linked to a different user_erc record, id=" + realErcTableId);
      		}
    	}
	}
```
