# Create Managers using specific attribute in the Organizational Unit

## Description
In this example, the existing list of managers are de-commissioned and instead the manager list is recreated using a specific attribute in the Organizational Unit.
For the sake of this example, it is assumed that the Managers userIDs are stored in the ATTR2 attribute of the OU's record.
In case needed, you could consolidate organizational units as well.

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
import java.util.ArrayList
import com.engiweb.profilemanager.common.bean.ExternalInfo
import com.engiweb.profilemanager.common.bean.PwdCfgBean
import com.crossideas.email.common.action.EmailTemplateAction
import com.crossideas.email.common.bean.EmailTemplateBean
import com.crossideas.email.common.bean.EmailDataBean
import com.crossideas.email.common.action.WebEmailAction
import com.engiweb.toolkit.common.enums.IdeasApplications
import java.sql.ResultSet
import java.sql.Statement
import com.engiweb.pm.dao.db.DAO
import com.engiweb.profilemanager.common.bean.ApplicationBean
import com.engiweb.profilemanager.common.bean.UserErcBean
import com.engiweb.profilemanager.common.bean.event.EventBean
import com.engiweb.profilemanager.common.bean.event.EventInBean
import com.engiweb.profilemanager.common.ruleengine.action.UtilAction
import com.engiweb.profilemanager.common.ruleengine.action.reorganize._AccountAction
import com.engiweb.pm.entity.Paging

global com.engiweb.profilemanager.backend.dao.db.SQLH sql
global com.engiweb.logger.impl.Log4JImpl logger
global com.engiweb.pm.dao.db.DAO sql
```

## Rule Code
The code is:
```java
when
    eval( true )
then
	// ATTR2 = Managers userid
	String SQL_QUERY_GET_MANAGERS = "select distinct OU, ATTR2 from " + sql.getCntSQL().dbUser + ".ORGANIZATIONAL_UNIT_ERC where attr2 is not null";
	String MANAGER_ROLE_NAME = "User Manager";

	logger.debug("RULE START");
	UtilAction.setCodOperation(sql, "MR_advanced_createManagersFromOU_" + System.currentTimeMillis());

	// Get manager role object
	EntitlementBean role = UtilAction.findEntitlementByName(sql, MANAGER_ROLE_NAME);
	if (role == null) {
		throw new Exception("Role with name " + MANAGER_ROLE_NAME + " not found!");
	}

	// Remove the role from all users
	UtilAction.removeEntitlementToAllUsers(sql, role);

	// Now reassign everything ...

	// Look for the managers
	ResultSet rs = sql.getCntSQL().getConnection().createStatement().executeQuery(SQL_QUERY_GET_MANAGERS);
	logger.debug("EXECUTE QUERY " + SQL_QUERY_GET_MANAGERS);

	// For each couple of "manager,ou", assign Role and Visibility
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
			logger.error("Error manager not found into IDEAS " + managerCode);
			continue;
		}

		// Assign the UM role
		OrgUnitBean currentOU = new OrgUnitBean();
		currentOU.setId(managerBean.getOrganizationalunit_id());

		BeanList roles = new BeanList();
		roles.add(role);

		// Add role to OU
		OrgUnitAction.addRoles(sql, currentOU, roles, false);

		// Add role to user
		UserAction.addRole(sql, managerBean, currentOU, roles, null, null, false, false);

		OrgUnitBean managedOU = UtilAction.findOrgUnitByCode(sql, managedOUCode);
		if (managedOU == null) {
			logger.error("Error OU not found into IDEAS " + managedOUCode);
			continue;
		}

		// Assign the visibility to the manager
		UtilAction.addResourcesToEmployment(sql, managerBean, role, managedOU);
		
		// Optionally, if you intend to create the OU Hierarchy based on the managers information
		try {
			OrgUnitAction.modifyOrgUnit(sql, managedOU, currentOU);
		} catch (Exception e) {
			logger.error("Unable to move OU " + managedOU.getCode() + " under OU with ID " + currentOU.getId() + " - " + e.getMessage());
			continue;
		}
	}
	rs.close();
	logger.debug("RULE END");
```
