# Disable UME Account

## Description
This rule attempts to disable the UME account (ideas) of a given user.

For a detailed explanation of the rule code, see the Rules Guide.

## Package Imports
The imports needed for this rule:

```java
import com.engiweb.logger.impl.Log4JImpl
import com.engiweb.profilemanager.common.bean.AccountBean
import com.engiweb.profilemanager.common.bean.Block
import com.engiweb.profilemanager.common.bean.UserBean
import com.engiweb.profilemanager.common.enumerations.UmeType
import com.engiweb.profilemanager.common.ruleengine.action.UserAction
import com.engiweb.pm.entity.BeanList

global com.engiweb.profilemanager.backend.dao.db.SQLH sql
global com.engiweb.logger.impl.Log4JImpl logger

```



## Rule Code
The code is:
```java
when
    userBean : UserBean(  )
then
	if (userBean.getUmeType() != UmeType.MASTER) {
		return;
	}
  
	logger.info("Running Disable UME...");
	AccountBean masterIdeasAccount = new AccountBean();
	boolean isMasterIdeasAccountLocked = false;
	Block masterIdeasBlockCode = null;
  
	masterIdeasAccount.setPwdcfg_id(1L); // IDEAS cfg, ID=1
  
	BeanList masterAccounts = UserAction.findAccount(sql, userBean, masterIdeasAccount);
	if (!masterAccounts.isEmpty()) {
		masterIdeasAccount = (AccountBean) masterAccounts.get(0);
		masterIdeasBlockCode = masterIdeasAccount.getBlock();
		for (int i = 0; i < masterIdeasBlockCode.getStringBlocco().length; i++) {
			if (masterIdeasBlockCode.getStringBlocco()[i] != 0) {
				isMasterIdeasAccountLocked = true;
				logger.info("Master Ideas account locked, locking child account(s)...");
				break;
			}
		}
	}

	if (!isMasterIdeasAccountLocked) {
		logger.info("Master Ideas account not locked.");
		return;
	}
  
	UserBean userFilter = new UserBean();    
	//Once an UME is created, the Master_id (Parent ID in the UI) is populated
	userFilter.setMaster_id(userBean.getMaster_id());    

	BeanList userList = UserAction.find(sql, userFilter);

	if (!userList.isEmpty()) {
		for (int i = 0; i < userList.size(); i++) {
			UserBean umeUser = (UserBean)userList.get(i);
			if (umeUser.getUmeType() == UmeType.MASTER) {
				continue;
			}
			logger.info("User: "+ umeUser.getCode() + " found, with UME Type: "+ umeUser.getUmeType().name() + " and ParentID: "+ umeUser.getSwimUser() + " and MasterCode: "+ umeUser.getMaster_code() + " and MasterID: "+ umeUser.getMaster_id());

			AccountBean userAccount = new AccountBean();
			userAccount.setPwdcfg_id(1L); // IDEAS cfg, ID=1
			logger.info("Searching for Ideas account for: "+ umeUser.getCode());
			BeanList accounts = UserAction.findAccount(sql, umeUser, userAccount);
			if (!accounts.isEmpty()) {
				logger.info("Ideas account found for user: "+ umeUser.getCode());
				userAccount = (AccountBean) accounts.get(0);
 
 				Block blockCode = new Block();
				blockCode.setStringBlocco(masterIdeasBlockCode.getStringBlocco());

				userAccount.setBlock(blockCode);
				UserAction.updateAccount(sql, userAccount );
				logger.info("Ideas account found for user: "+ umeUser.getCode() +" disabled...");
			} else {
				logger.info("No Ideas account found for user: "+ umeUser.getCode());
			}
		}
	} else {
		logger.info("No UME found for the search criteria.");
	}
```
