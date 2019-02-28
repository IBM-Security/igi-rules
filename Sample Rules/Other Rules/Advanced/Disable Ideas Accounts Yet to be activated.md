# Disable Ideas Accounts Yet to be activated

## Description
This is an example of a rule that may be useful to identify all the ideas accounts that are carrying an activation date of Future and disable the same.

For a detailed explanation of the rule code, see the Rules Guide.

## Package Imports
```java 
import com.crossideas.ap.common.bean.SwimEntitlementBean
import com.crossideas.ap.common.ruleengine.action.RequestGenerationRule
import com.engiweb.logger.impl.Log4JImpl
import com.engiweb.pm.dao.db.DAO
import com.engiweb.pm.entity.BeanList
import com.engiweb.pm.entity.Paging
import com.engiweb.profilemanager.backend.dao.db.SQLH
import com.engiweb.profilemanager.common.bean.AccountBean
import com.engiweb.profilemanager.common.bean.ApplicationBean
import com.engiweb.profilemanager.common.bean.Block
import com.engiweb.profilemanager.common.bean.entitlement.EntitlementBean
import com.engiweb.profilemanager.common.bean.event.EventBean
import com.engiweb.profilemanager.common.bean.event.EventInBean
import com.engiweb.profilemanager.common.bean.ExternalInfo
import com.engiweb.profilemanager.common.bean.OrgUnitBean
import com.engiweb.profilemanager.common.bean.OrgUnitErcBean
import com.engiweb.profilemanager.common.bean.PwdCfgBean
import com.engiweb.profilemanager.common.bean.UserBean
import com.engiweb.profilemanager.common.bean.UserErcBean
import com.engiweb.profilemanager.common.bean.UsrErcBean
import com.engiweb.profilemanager.common.enumerations.EntitlementType
import com.engiweb.profilemanager.common.enumerations.LockType
import com.engiweb.profilemanager.common.ruleengine.action.JobRoleAction
import com.engiweb.profilemanager.common.ruleengine.action.OrgUnitAction
import com.engiweb.profilemanager.common.ruleengine.action.reorganize._AccountAction
import com.engiweb.profilemanager.common.ruleengine.action.UserAction
import com.engiweb.profilemanager.common.ruleengine.action.UtilAction
import com.engiweb.toolkit.common.DBMSException
import java.sql.ResultSet
import java.sql.Statement
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.List
import java.util.TimeZone
import com.engiweb.profilemanager.common.ruleengine.action.reorganize._EntitlementAction
import com.engiweb.profilemanager.common.ruleengine.action.reorganize._UserAction
import com.engiweb.profilemanager.common.ruleengine.action.reorganize._RightsAction
import com.engiweb.profilemanager.common.ruleengine.action.reorganize.UserTypeAction

global com.engiweb.logger.impl.Log4JImpl logger
global com.engiweb.pm.dao.db.DAO sql
global com.engiweb.profilemanager.backend.dao.db.SQLH sql
```

## Rule Code
The code is:
```java 
when
    eval( true )
then
logger.info("Disable Ideas Accounts Yet to be activated - RULE START");

<<<<<<< HEAD
// get current date, IGI supports only date and no time in timestamp
SimpleDateFormat cd = new SimpleDateFormat("yyyy-MM-dd");
Date currentDate = new Date();
String currentDateString = cd.format(currentDate);
logger.info("Current Date is: " + currentDateString);

// find active ERC users with a future activation date
String SQL_QUERY_PENDING_ACTIVATION = "select distinct PM_CODE, ATTR15 from " + sql.getCntSQL().dbUser + ".USER_ERC where ATTR15 is not null and DATE(ATTR15) > '" + currentDateString + "'";

ResultSet rs = sql.getCntSQL().getConnection().createStatement().executeQuery(SQL_QUERY_PENDING_ACTIVATION);
logger.info("EXECUTE QUERY " + SQL_QUERY_PENDING_ACTIVATION);

String userPMCode = null;
Date activationDate = null;
	
while (rs.next()) {

	activationDate = new SimpleDateFormat("yyyy-MM-dd").parse(rs.getString(2));
	logger.info("Set IDEAS Account inactive for USER_ERC User " + userPMCode + " with Activation Date: " + activationDate);
			
	// Get the userBean by userCode from USER_ERC table
	String SQL_QUERY_GET_USERBEAN = "select distinct CODE from " + sql.getCntSQL().dbUser + ".PERSON where CODE = '" + userPMCode + "'";
			
	ResultSet rs2 = sql.getCntSQL().getConnection().createStatement().executeQuery(SQL_QUERY_GET_USERBEAN);
	logger.info("EXECUTE QUERY " + SQL_QUERY_GET_USERBEAN);
										
	// Now switch to users in PERSON IGI table
	String userCode = null;
	UserBean userBean = null;
			
	while (rs2.next()) {	
		userCode = rs2.getString(1);
		userBean = UtilAction.findUserByCode(sql, userCode);
			
		if (userBean == null) {
			logger.error("Error - User not found by Code: " + userCode);
			continue;
		}
	
		AccountBean userAccount = new AccountBean();
		userAccount.setPwdcfg_id(1L); // IDEAS cfg, ID=1

		BeanList accounts = UserAction.findAccount(sql, userBean, userAccount);
		userAccount = (AccountBean) accounts.get(0);
          
		// Disable IDEAS Account for User who has Activation date in future
					
		Block blockCode = new Block();
		blockCode.setLock(LockType.AUTHORITATIVE, 1);

		// Lock IDEAS Account
		userAccount.setBlock(blockCode);
		UserAction.updateAccount(sql, userAccount );
		logger.info("User IDEAS Account: " + userBean.getCode() + " was disabled due to activation date in future!");
					
		// Update PERSON record for such user, set USER STATUS to inactive
		userBean.setState(1L);
		logger.info("User " + userBean.getCode() + " PERSON State is set to inactive due to activation date in future!");
		
	}
	rs2.close();
}
rs.close();
=======
	// get current date, IGI supports only date and no time in timestamp
    SimpleDateFormat cd = new SimpleDateFormat("yyyy-MM-dd");
    Date currentDate = new Date();
	String currentDateString = cd.format(currentDate);
    logger.info("Current Date is: " + currentDateString);

  // find active ERC users with a future activation date
  String SQL_QUERY_PENDING_ACTIVATION = "select distinct PM_CODE, ATTR15 from " + sql.getCntSQL().dbUser + ".USER_ERC where ATTR15 is not null and DATE(ATTR15) > '" + currentDateString + "'";

  ResultSet rs = sql.getCntSQL().getConnection().createStatement().executeQuery(SQL_QUERY_PENDING_ACTIVATION);
  logger.info("EXECUTE QUERY " + SQL_QUERY_PENDING_ACTIVATION);

  String userPMCode = null;
	
	while (rs.next()) {

			activationDate = new SimpleDateFormat("yyyy-MM-dd").parse(rs.getString(2));
			logger.info("Set IDEAS Account inactive for USER_ERC User " + userPMCode + " with Activation Date: " + activationDate);
			
			// Get the userBean by userCode from USER_ERC table
			String SQL_QUERY_GET_USERBEAN = "select distinct CODE from " + sql.getCntSQL().dbUser + ".PERSON where CODE = '" + userPMCode + "'";
			
			ResultSet rs2 = sql.getCntSQL().getConnection().createStatement().executeQuery(SQL_QUERY_GET_USERBEAN);
			logger.info("EXECUTE QUERY " + SQL_QUERY_GET_USERBEAN);
										
			// Now switch to users in PERSON IGI table
			String userCode = null;
			UserBean userBean = null;
			
			while (rs2.next()) {
			
					userCode = rs2.getString(1);
					userBean = UtilAction.findUserByCode(sql, userCode);
			
					if (userBean == null) {
						logger.error("Error - User not found by Code: " + userCode);
						continue;
					}
	
					AccountBean userAccount = new AccountBean();
					userAccount.setPwdcfg_id(1L); // IDEAS cfg, ID=1

					BeanList accounts = UserAction.findAccount(sql, userBean, userAccount);
					userAccount = (AccountBean) accounts.get(0);
          
					// Disable IDEAS Account for User who has Activation date in future
					
					Block blockCode = new Block();
					blockCode.setLock(LockType.AUTHORITATIVE, 1);

					// Lock IDEAS Account
					userAccount.setBlock(blockCode);
					UserAction.updateAccount(sql, userAccount );
					logger.info("User IDEAS Account: " + userBean.getCode() + " was disabled due to activation date in future!");
					
					// Update PERSON record for such user, set USER STATUS to inactive
					userBean.setState(1L);
					logger.info("User " + userBean.getCode() + " PERSON State is set to inactive due to activation date in future!");
		
		    }
			rs2.close();
 
    }
    rs.close();
>>>>>>> 10ec9ea... Initial change for rule pertaining to disabling accounts

logger.info("Disable Ideas Accounts by User Account Activation Date - RULE END");
```
