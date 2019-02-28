# Process a New Rights Assignment from a Target - Step 6 - Add Permission to User.

## Description
Rights represent a permission scope on a permission when assigned to a user (via account). If you’re familiar with RACF, you can has a RACF account assigned to a RACF group with a default permission level. Similarly with Sharepoint, you might have a file access level associated with a shared file or folder.

Thus a rights object in an Add Right event represents a mapping of a right to a permission and account. The supplied rules are a good example of handling the various possible problems, such as mapping a permission or right that doesn’t exist. It also shows how the users, accounts, permissions and rights are related in the data model.

The rules are:
1. Check status of last operation – Check if the account exists, the permission exists and the user is not already
mapped to the permission.
2. Add New Permission if it does not exist – If the permission in the event doesn’t exist in IGI, create it.
3. Add New Permission to the User OU – Add visibility of this permission to the OU of the account user.
4. Add Right to Permission – If the permission doesn’t have this right in its list of rights values, add it.
5. Add Right Value to Assignment – Add the right value to the assignment of the user to the permission.
6. Add Permission to User – Add the permission to the user.

```
Note: All the above rules are co-located in the folder: "Assign a New Right".
```

This example code is for **Step 6 - Add Permission to User**. This rule is a bit of a catch-all to make sure any conditions raised in earlier rules have been processed. It is a series of “if this condition then do this” blocks to capture any outstanding conditions.

For a detailed explanation of the rule code, see the Rules Guide.

## Package Imports
The imports for this rule:
```java
import com.engiweb.logger.impl.Log4JImpl;
import com.engiweb.profilemanager.backend.dao.db.SQLH
import com.engiweb.pm.entity.BeanList;
import com.engiweb.profilemanager.common.bean.AccountBean;
import com.engiweb.profilemanager.common.bean.Block;
import com.engiweb.profilemanager.common.bean.OrgUnitBean;
import com.engiweb.profilemanager.common.bean.UserBean;
import com.engiweb.profilemanager.common.bean.entitlement.EntitlementBean;
import com.engiweb.profilemanager.common.bean.event.EntStateBean;
import com.engiweb.profilemanager.common.bean.event.EventTargetBean;
import com.engiweb.profilemanager.common.bean.rule.SyncStateBean;
import com.engiweb.profilemanager.common.ruleengine.action.EntitlementAction;
import com.engiweb.profilemanager.common.ruleengine.action.OrgUnitAction;
import com.engiweb.profilemanager.common.ruleengine.action.UserAction;
import com.engiweb.toolkit.common.DBMSException;
import com.engiweb.profilemanager.common.bean.AccountAttrValueList
import com.engiweb.profilemanager.common.interfaces.IAccountDirect
import com.engiweb.toolkit.interfaces.JndiNames
import com.engiweb.profilemanager.common.bean.targetattr.PwdManagementAttrValBean
import common.direct.DirectFactory

global com.engiweb.profilemanager.backend.dao.db.SQLH sql;
global com.engiweb.logger.impl.Log4JImpl logger;
```

## Rule Code
The code is:
```java
when
    userBean : UserBean(  )  
    orgUnitBean : OrgUnitBean(  )  
    event : EventTargetBean(  )  
    syncBean : SyncStateBean(  )
then
if (event.getLevel() == EventTargetBean.USER_NOT_FOUND) {
    throw new Exception("Account " + event.getCode() + " does not exist");
  }

  EntitlementBean entBean = syncBean.getProfile();

  String pName = entBean.getName();
  String uCode = userBean.getCode();

  // User already has the profile
  if (event.getLevel() == EventTargetBean.USER_PROFILE_FOUND) {
    logger.debug("Permission " + pName + " already assigned to User " + uCode);
    return;
  }

  // User does not have the profile
  if (event.getLevel() == EventTargetBean.USER_PROFILE_NOT_FOUND) {
    BeanList bl = new BeanList();
    bl.add(entBean);
    UserAction.addRoles(sql, userBean, null, bl, true, false);
    logger.debug("Permission " + pName + " assigned to User " + uCode);
    return;
  }

  throw new Exception("Unexpected sync level: " + event.getLevel());
```
