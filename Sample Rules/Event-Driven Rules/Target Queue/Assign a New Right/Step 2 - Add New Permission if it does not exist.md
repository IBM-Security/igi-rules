# Process a New Rights Assignment from a Target - Step 2 - Add New Permission if it does not exist

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

This example code is for **Add New Permission if it does not exist**. This rule is for data consistency – you need a permission defined before you can associate it with a user, so if the permission is not found in IGI, create it.

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

```java
import com.engiweb.profilemanager.common.bean.FunctionalityTypeBean
import com.engiweb.profilemanager.common.ruleengine.action.ApplicationAction
```

## Rule Code
The code is:
```java
when
    event : EventTargetBean(  )
    syncBean : SyncStateBean(  )
then
if (event.getLevel() == EventTargetBean.PROFILE_NOT_FOUND) {

    // Create and publish the permission ...

    EntitlementBean entBean = syncBean.getProfile();
    // Not Administrative Role
    entBean.setAdministrative(0L);
    entBean.setExternalRef(event.getAttr3());
    if (entBean.getName() == null || entBean.getName().equals("")) {
      entBean.setName(event.getAttr3());
    }

    if (entBean.getFunctionalityType_name() == null) {
      // If Type is not set, equate it to the application name
      entBean.setFunctionalityType_name(entBean.getApplication_name() + "Type");
    }

    // Create permission type if it does not exist
    FunctionalityTypeBean ft = new FunctionalityTypeBean();
    ft.setApplication_id(entBean.getApplication_id());
    ft.setName(entBean.getFunctionalityType_name());

    BeanList bl = ApplicationAction.findFunctionalityType(sql, ft);
    if (bl.size() == 0) {
      ApplicationAction.addFunctionalityType(sql, ft);
    }

    // Create the entitlement
    try {
      entBean = EntitlementAction.insertEntitlement(sql, entBean, true);
    } catch (DBMSException e) {
      if (e.getErrorCode() == DBMSException.OBJECT_NOT_FOUND) {
        throw new Exception("Error creating permission");
      } else {
        throw e;
      }
    }

    logger.debug("Permission " + entBean.getName() + " created, application " + entBean.getApplication_name());

    // Update the current event object
    event.setLevel(EventTargetBean.OU_PROFILE_NOT_FOUND);
    syncBean.setProfile(entBean);

  }
```
