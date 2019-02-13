# Rebuild an Admin Role

## Description
In this example, there is a Department Manager admin role. The role is assigned to any user who is flagged as a manager, and their scope is the OU that they are placed in. This is achieving the same thing, defining members of the Department Manager admin role, as was done in an another (IN event) rule example. However that rule was driven by a single user change. This rule will completely rebuild the Department Manager admin role membership.

For a detailed explanation of the rule code, see the Rules Guide.

## Package Imports
The imports for this rule:
```java
import com.engiweb.logger.impl.Log4JImpl
import com.engiweb.profilemanager.backend.dao.db.SQLH
import com.engiweb.pm.entity.BeanList
import com.engiweb.profilemanager.common.bean.AccountBean
import com.engiweb.profilemanager.common.bean.Block
import com.engiweb.profilemanager.common.bean.OrgUnitBean
import com.engiweb.profilemanager.common.bean.UserBean
import com.engiweb.profilemanager.common.bean.entitlement.EntitlementBean
import com.engiweb.profilemanager.common.bean.event.EntStateBean
import com.engiweb.profilemanager.common.bean.event.EventTargetBean
import com.engiweb.profilemanager.common.bean.rule.SyncStateBean
import com.engiweb.profilemanager.common.ruleengine.action.EntitlementAction
import com.engiweb.profilemanager.common.ruleengine.action.OrgUnitAction
import com.engiweb.profilemanager.common.ruleengine.action.UserAction
import com.engiweb.toolkit.common.DBMSException
import com.engiweb.profilemanager.common.bean.AccountAttrValueList
import com.engiweb.profilemanager.common.interfaces.IAccountDirect
import com.engiweb.toolkit.interfaces.JndiNames
import com.engiweb.profilemanager.common.bean.targetattr.PwdManagementAttrValBean
import common.direct.DirectFactory
import com.crossideas.ideasconnector.common.enums.EventOperationType
import java.util.Random

global com.engiweb.profilemanager.backend.dao.db.SQLH sql
global com.engiweb.logger.impl.Log4JImpl logger
```

```java
import com.engiweb.profilemanager.common.ruleengine.action.UtilAction;
import java.sql.Statement;
import java.sql.ResultSet;
```

## Rule Code
The code is:
```java
when
  eval ( true )
then

  String MANAGER_ROLE_NAME = "Department Manager";
  String IS_MANAGER_ATTR = "ATTR2"; // allowed values Y/N

  String SQL_QUERY_GET_MANAGERS = "select PM_CODE, OU from " + sql.getCntSQL().dbUser + ".USER_ERC " + "where deleted=0 and " + IS_MANAGER_ATTR + "='Y'";

  logger.debug("Rebuild an Admin Role RULE START");

  UtilAction.setCodOperation(sql, "MR_advanced_createManagers2_" + System.currentTimeMillis());

  // Get manager role object
  EntitlementBean role = UtilAction.findEntitlementByName(sql, MANAGER_ROLE_NAME);
  if (role == null) {
    throw new Exception("Role with name " + MANAGER_ROLE_NAME + " not found!");
  }

  //Remove the role from all users
  UtilAction.removeEntitlementToAllUsers(sql, role);

  //then reassign everything ...

  // look for the managers
  Statement stmt = sql.getCntSQL().getConnection().createStatement();
  ResultSet rs = stmt.executeQuery(SQL_QUERY_GET_MANAGERS);
  logger.debug("EXECUTE QUERY " + SQL_QUERY_GET_MANAGERS);

  // For each manager assign role and visibility
  String managerCode = null;
  String managedOUCode = null;
  UserBean managerBean = null;

  while (rs.next()) {
    logger.debug("LOOP ...");

    managerCode = rs.getString(1);
    managedOUCode = rs.getString(2);

    // Get the userBean
    managerBean = UtilAction.findUserByCode(sql, managerCode);
    if (managerBean == null) {
      logger.error("Error, manager not found in IDEAS " + managerCode);
      continue;
    }

    // assign the UM role
    OrgUnitBean currentOU = new OrgUnitBean();
    currentOU.setId(managerBean.getOrganizationalunit_id());

    BeanList roles = new BeanList();
    roles.add(role);

    // add role to OU
    OrgUnitAction.addRoles(sql, currentOU, roles, false);

    // add role to the user
    UserAction.addRole(sql, managerBean, currentOU, roles, null, null, false, false);

    OrgUnitBean managedOU = UtilAction.findOrgUnitByCode(sql, managedOUCode);
    if (managedOU == null) {
      logger.error("Error OU not found into IDEAS " + managedOUCode);
      continue;
    }

    // Assign the visibility to the manager
    UtilAction.addResourcesToEmployment(sql, managerBean, role, managedOU);
  }

  rs.close();

  logger.debug("Rebuild an Admin Role RULE END");
```
