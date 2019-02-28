# Create User

## Description
Intercept the create user operation, update any user attribute, based on business policies and then let it flow through.


## Package Imports
The imports for this rule:
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
    userBean : UserBean(  )  
    orgUnitBean : OrgUnitBean(  )  
    externalInfo : ExternalInfo(  )
then
	// [ V1.1 - 2014-05-26 ]
  
	try {
	    // Tweak the objects, before triggering the add action here.
		UserAction.add(sql, userBean, orgUnitBean, externalInfo);
	} catch (DBMSException e) {
		if (e.getErrorCode() == DBMSException.OBJECT_NOT_UNIQUE) {
			throw new Exception("User already exists with code=" + userBean.getCode());
		} else {
			throw e;
		}
	}

	logger.debug("User created : " + userBean.getCode());
```
