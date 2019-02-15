# Set a Random Password on Re-Enabled Account

## Description
This rule generates an eight-character password for a restored LDAP account. The last four digits are random. The calculated value is added to the data bean in a current attribute that is named erpassword. This name is expected by the LDAP adapter.

The password attribute does not need to be mapped as it is created by the rule at post-mapping time.

This is a post-mapping rule. It is taken from the IGI product documentation.

For a detailed explanation of the rule code, see the Rules Guide.

## Package Imports
The following package import is required:
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

```java
import com.crossideas.ideasconnector.core.databean.Event
import com.crossideas.ideasconnector.common.enums.EventOperationType
import java.util.Random
```

## Rule Code
The code is:
```java
when
  event: Event()
then
//
  if (event.getOperation() == EventOperationType.OPERATION_ACCOUNT_ENABLE) {
    Random rnd = new Random(System.currentTimeMillis());
    int n = 1000 + rnd.nextInt(9000);
    String randomPwd = "Ibm$" + n;

    event.getBean().setCurrentAttributeValue("erpassword", randomPwd);
}
```
