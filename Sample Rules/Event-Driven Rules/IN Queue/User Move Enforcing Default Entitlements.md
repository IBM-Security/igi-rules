# User Move Enforcing Default Entitlements

## Description
The supplied User Move rules includes the actual method to move the user from one OU to another. This is in contrast to the standard method, UtilAction.moveUser(sql, userBean, orgUnitBean); which only moves the user, it does not enforce any Default Entitlements set on the new OU.

The following rule will take care of the gap. For a detailed explanation of the rule code, see the Rules Guide.

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
  userBean : UserBean( )
  orgUnitBean : OrgUnitBean( )
then
  // Check if the new OU is actually different
  if (userBean.getOrganizationalunit_code().equalsIgnoreCase(orgUnitBean.getCode())) {
    logger.debug("User " + userBean.getCode() + " not moved.");
    return;
  }

  // Make sure all the user entitlements will be available on the new OU
  BeanList entitlements = UserAction.findJobRoles(sql, userBean);

  // Set Visibility Violation
  for (int i = 0; i < entitlements.size(); i++) {
    EntitlementBean tmp = (EntitlementBean) entitlements.get(i);
    BeanList entAssignedToOU = OrgUnitAction.findJobRole(sql, orgUnitBean, tmp, null);

    if (entAssignedToOU == null || entAssignedToOU.isEmpty()) {
      tmp.setVisibilityViolation(1L);
    }
  }

  // Add entitlement set in VV to OU
  OrgUnitAction.addRoles(sql, orgUnitBean, entitlements, false);

  // Move the user
  UtilAction.moveUser(sql, userBean, orgUnitBean);

  // Assign default entitlements of OU
  EntitlementBean entBeanDefault = new EntitlementBean();
  entBeanDefault.setDefaultOption(true);

  BeanList entsDefault = OrgUnitAction.findJobRole(sql, orgUnitBean, entBeanDefault, null);

  if (!entsDefault.isEmpty()) {
    for (int k = 0; k < entsDefault.size(); k++) {
      EntitlementBean role = (EntitlementBean) entsDefault.get(k);
      BeanList roles = JobRoleAction.find(sql, role);

      if (roles == null || roles.isEmpty()) {
        throw new Exception("Role : " + role.getName() + " not found!");
      }

      UserAction.addRole(sql, userBean, orgUnitBean, roles, null, null, false, false);
      logger.info("Added role --> " + role.getName());
    }
  }

logger.debug("User " + userBean.getCode() + " moved to " + orgUnitBean.getCode());
```
