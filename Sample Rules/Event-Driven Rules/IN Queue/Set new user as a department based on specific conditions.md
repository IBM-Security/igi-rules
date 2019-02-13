# Set New User as Department Manager if he happens to be a Manager

## Description
Every new user added to IGI is assigned to an Org Unit, which may represent a department. Some
organisations may define a department manager admin role for access request approval or other
managerial duties.

In this example, the new user is checked to see if they are flagged as a Manager (from HR) and if so,
assigns them to the Department Manager admin role with a scope of the OU that they are assigned
to.

This is one of the sample rules with IGI and is applied to the IN queue for user add event.

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
   event : EventInBean( )
   userErcBean : UserErcBean( )
   userBean : UserBean( )
   extInfoBean : ExternalInfo( )
   orgUnitBean : OrgUnitBean( )
then
// [ V1.1 - 2014-05-26 ]
   String MANAGER_ROLE_NAME = "Department Manager";
   String IS_MANAGER_ATTR = "ATTR2"; // Y/N

   String isManager = (String) userErcBean.getAttribute(IS_MANAGER_ATTR);

   if (isManager != null) {

      if (isManager.toUpperCase().equals("Y")) {

        // Get manager role object
        EntitlementBean role = UtilAction.findEntitlementByName(sql, MANAGER_ROLE_NAME);
        if (role == null) {
          throw new Exception("Role with name " + MANAGER_ROLE_NAME + " not found!");
        }

        BeanList roles = new BeanList();
        roles.add(role);

        OrgUnitAction.addRoles(sql, orgUnitBean, roles, false);
        UserAction.addRole(sql, userBean, orgUnitBean, roles, null, null, false, false);
        UtilAction.addResourcesToEmployment(sql, userBean, role, orgUnitBean);
      }
    }
```


## Java Code
To pull this rule in the Workspace that comes along with the Rule Engine Toolkit, you can refer the
[Java variant](../../../JavaRules/ExampleSetDepartmentManager.java) of the above rule.

