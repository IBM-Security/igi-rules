# Consolidate extended attribute values into Account Objects

## Description
This rule aims at extracting the values corresponding to extended attributes and patch them up along with the regular account attributes.

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
    event : EventTargetBean(  )
    account : AccountBean(  )
    attributes : AccountAttrValueList(  )
then
	// [ V2.0 - 2018-05-01, D.Chilovich, IBM ]
	String userTargetName = null;
	String userAccountCode = null;
	Long userAccountPwdManagement_id = null;
	Long userAccountPwdCfgAttrKey_id = null;
	String userAccountPwdCfgAttrKey_name = null;
	String accountAttr19 = null;
	String accountAttr20 = null;

	// Determine account code
	if ( event.getCode() != null ) {  
		userAccountCode = event.getCode(); 
	}

	// Determine what application account belongs to
	if ( event.getTarget() != null ) {  
		userTargetName = event.getTarget(); 
	}

	if ( userTargetName != null ) {
		if ( userTargetName.equals("Disconnected Application 6" ) ) {
			logger.info("Account " + userAccountCode + " belongs to application under IGI Marker " + userTargetName  + ". Ext. Attributes will be added.");
		} else {
			logger.info("Account " + userAccountCode + " belongs to application under IGI Marker " + userTargetName  + ". No attributes are added.");
			return;
		}
	} else {
		logger.info("Account belongs to null application! Exiting...");
		return;
	}

	// Determine extended attributes values
	if ( event.getAttr19() != null )  {  
		accountAttr19 = event.getAttr19(); 
	}
	
	if ( event.getAttr20() != null )  {  
		accountAttr20 = event.getAttr20(); 
	}

	// Log values
	logger.info("Account: " + userAccountCode + " extended attributes: " + accountAttr19 + "," + accountAttr20 );

	// Add attribute:value pairs to Account's PDWCONFIGURATION_ATTR_VAL table
	IAccountDirect accountDirect = (IAccountDirect) DirectFactory.getDirectInterface(JndiNames.accountEJB); 
	BeanList<AccountBean> beanList = accountDirect.find(account, null, sql); 
	logger.info("BEFORE: AccountBeanList = "+beanList );

	// Same for all accounts - App id , Account id
	userAccountPwdManagement_id = 192L;
	userAccountPwdCfgAttrKey_id = beanList.get(0).getId();

	logger.info("TO SET: Application: " + userAccountPwdManagement_id + ", account pwdcfg id: " + userAccountPwdCfgAttrKey_id );

	PwdManagementAttrValBean  yourattr19 = new PwdManagementAttrValBean ();
	yourattr19.setId(userAccountPwdCfgAttrKey_id);
	yourattr19.setValue(accountAttr19);
	yourattr19.setPwdManagement_id(192L);
	yourattr19.setPwdCfgAttrKey_id(1775L);
	yourattr19.setPwdCfgAttrKey_name("ATTR19");
	attributes.add(yourattr19);

	PwdManagementAttrValBean  yourattr20 = new PwdManagementAttrValBean ();
	yourattr20.setId(userAccountPwdCfgAttrKey_id);
	yourattr20.setValue(accountAttr20);
	yourattr20.setPwdManagement_id(192L);
	yourattr20.setPwdCfgAttrKey_id(1776L);
	yourattr20.setPwdCfgAttrKey_name("ATTR20");
	attributes.add(yourattr20);

	logger.info("AFTER: AccountDirectList change done." );
```
