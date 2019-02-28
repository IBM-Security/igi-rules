# Transform Modify OU Operation to Add OU

## Description
This rule shows the procedure for converting an Modify OU Operation to an ADD OU Operation

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

	// ----------
	// Base request is to MODIFY the OU, however if the OU does not exist, transform the operation to ADD
	// Transform MODIFY to ADD
	// ----------

	if (currentOperation == MODIFY) {
		if (ouCode == null) {
			// In this example we assume that OU CODE is calculated by the ADD OU rules.
			// That way, if the OU CODE is null, the OU can't exist
       		eventin.setOperation(ADD);
			// save the real operation code
			eventin.setExtAttr10(Long.toString(currentOperation));
     	} else {
			// look for the OU
			OrgUnitBean ouBean = UtilAction.findOrgUnitByCode(sql, ouCode);
			if (ouBean == null) {
				eventin.setOperation(ADD);
				// save the real operation code
				eventin.setExtAttr10(Long.toString(currentOperation));
			}
		}
   }
```
