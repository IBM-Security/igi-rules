# Create Business Role for a new Organizational Unit

## Description
This rule creates a new business role for any new OU created through the IN queue and makes it as a default role in this OU. Customers often call this an "Organizational Role".

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

```java
import com.engiweb.profilemanager.common.enumerations.EntitlementType
import com.engiweb.profilemanager.common.ruleengine.action.EntitlementAction
```



## Rule Code
The code is:
```java
when
    orgUnit : OrgUnitBean(  )
    orgUnitErcBean : OrgUnitErcBean(  )
then

    String ouname = orgUnit.getName();                                    // get name of new OU
    EntitlementBean entBean = new EntitlementBean();                      // create new entitlement
    entBean.setName(ouname);                                              // set name same as OU name
    entBean.setType(EntitlementType.BUSINESS_ROLE.getCode());             // set type to "Business role"
    entBean = EntitlementAction.insertEntitlement(sql, entBean, true);    // create business role, return new EntitlementBean object
    entBean.setDefaultOption(true);                                       // make this entitlement default in the OUs, where we add it
    BeanList<EntitlementBean> roleList = new BeanList<EntitlementBean>();
    roleList.add(entBean);                                                // build list of entitlement to be added to OU
    OrgUnitAction.addRoles(sql, orgUnit, roleList, false);                // add list to OU
```
