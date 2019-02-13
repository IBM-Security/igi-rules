# Process a New Rights Assignment from a Target - Step 1 - Check status of last operation

## Description
Rights represent the scope of a permission when assigned to a user (via account). If you’re familiar with RACF, you can has a RACF account assigned to a RACF group with a default permission level. Similarly with Sharepoint, you might have a file level access associated with a shared file or folder.

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

This example code is for **Step 1 - Check status of the last operation**. This simple piece of code operates on the EventTargetBean Level attribute. It checks that the account already exists (and it will throw an error if not found), the permission is not found (writes a debug message) and if the user is already mapped to this profile (writes a debug message).

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
  event : EventTargetBean( )
then
// [ V1.0 - 2014-05-26 ]
  int syncLevel = event.getLevel();
  logger.debug("Sync level: " + syncLevel);

  if (syncLevel == EventTargetBean.USER_NOT_FOUND) {
    logger.debug("Account not found");
    throw new Exception("Account " + event.getCode() + " does not exist");
  }

  if (syncLevel == EventTargetBean.PROFILE_NOT_FOUND) {
    // The following rules create the permission if it doesn't exist
    // throw new Exception("Profile not found ");
    logger.debug("Profile not found");
  }

  if (syncLevel == EventTargetBean.USER_PROFILE_FOUND) {
    logger.debug("The user already has the added profile");
  }
```
