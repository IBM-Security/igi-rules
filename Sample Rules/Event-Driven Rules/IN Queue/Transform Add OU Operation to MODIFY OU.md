# Transform Add OU Operation to Modify OU

## Description
This rule shows the procedure for converting an Add OU Operation to a Modify OU Operation

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
    orgUnitErcBean : OrgUnitErcBean(  )
then
	// [ V1.1 - 2014-05-26 ]

	logger.debug("OU input transformation, event:" + event);

	EventInBean eventin = (EventInBean) event;

	final long ADD = 9;
	final long MODIFY = 10;
	final long DISCARDED = 99;

	long currentOperation = eventin.getOperation();
	String ouCode = (String) orgUnitErcBean.getAttribute("OU");
	String ouTableId = (String) orgUnitErcBean.getAttribute("ID");


	// Base request is to ADD the OU, however if the OU already exists, transform the operation to MODIFY
	// NOTE: This situation can occur, since OUs created from the Console, don't have a related record in the external table.

	if (currentOperation == ADD) {
		// look for the OU
		OrgUnitBean ouBean = UtilAction.findOrgUnitByCode(sql, ouCode);
		if (ouBean != null) {
			eventin.setOperation(MODIFY);
			// save the real operation code
			eventin.setExtAttr10(Long.toString(currentOperation));

			// Align the orgUnit fk to orgUnitErc (attr1)
			ouBean.setAttr1(ouTableId);
			OrgUnitAction.modifyOrgUnit(sql, ouBean);
		}
	}
```
