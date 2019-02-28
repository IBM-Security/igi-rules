# Create Account and Match it to the User specified against Gecos

## Description
This rule attempts to match an account with a user, based on gecos comments specified against the account. 

For a detailed explanation of the rule code, see the Rules Guide.

## Package Imports
The imports needed for this rule:

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
import com.crossideas.certification.common.bean.TemplateBean
import com.crossideas.certification.backend.dao.TemplateDAO
import com.engiweb.pm.entity.Paging
import com.engiweb.pm.web.bean.AbstractBean
import com.crossideas.certification.common.AttestationRes
import com.crossideas.certification.common.enumeration.AttestationTypes

global com.engiweb.profilemanager.backend.dao.db.SQLH sql
global com.engiweb.logger.impl.Log4JImpl logger
```



## Rule Code
The code is:
```java
when 
	event : EventTargetBean( ) 
	account : AccountBean( ) 
	attributes : AccountAttrValueList( ) 
then // exit if already matched by a customized rule 

if (account.getPerson_id() != null) { 
	logger.info("Account already matched!"); 
	return; 
}

if(event.getTarget().startsWith("ILC_")) { 
	IAccountDirect accountDirect = (IAccountDirect) DirectFactory.getDirectInterface(JndiNames.accountEJB); 
	// Gets the attributes from ILC.

	BeanList<PwdManagementAttrValBean> beanList = accountDirect.findAttrValue(null, account, null, sql); 
	logger.info("AccountDirectList="+beanList); 

	//logger.info("attributes:="+attributes.toString()); 

	attributes.addAll(beanList); 

	//logger.info("attr at 0th location:- "+attributes.get(0)); 

	for (int i = 0; i < attributes.size(); i++) { 
		if(attributes.get(i).getPwdCfgAttrKey_id()==115L) { 
			String gecos = attributes.get(i).getValue(); 
			
			if(gecos != null) { 
				// Look for the User 
				UserBean userFilter = new UserBean(); 
				userFilter.setCode(gecos); 
				BeanList ul = UserAction.find(sql, userFilter); 
				logger.info("value of ul="+ul); 
				
				if (ul.size() == 0) { 
					// userNotFound logger.info("user not found into IDEAS"); 
					throw new Exception("User not found into IDEAS, description=" + gecos); 
				} else { 
					// found userFilter = (UserBean) ul.get(0); 
					logger.info("userFilter.getId()="+userFilter.getId()); 
					logger.info("account="+account); 
					account.setPerson_id(userFilter.getId()); 
					UserAction.updateAccount(sql,account); 
					logger.info("Account after Update="+account); 
					logger.info("Account ownership Updated Successfully"); 
					event.setTrace("Identity matched: Gecos!"); 
				} 
			} 
		} 
	} 
} 
```
