# Change Password and Notify Relevant Folks

## Description
This rule shows the steps on setting a random password to ideas account and then notify the relevant user of the change.

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
import java.util.ArrayList

import com.engiweb.profilemanager.common.bean.ExternalInfo
import com.engiweb.profilemanager.common.bean.PwdCfgBean
import com.crossideas.email.common.action.EmailTemplateAction
import com.crossideas.email.common.bean.EmailTemplateBean
import com.crossideas.email.common.bean.EmailDataBean
import com.crossideas.email.common.action.WebEmailAction
import com.engiweb.toolkit.common.enums.IdeasApplications
```

## Rule Code
The code is:
```java
when
    user : UserBean(  )  
    orgUnit : OrgUnitBean(  )  
    extInfo : ExternalInfo(  )
then
// [ V1.1 - 2016-09-05 ]
	// Get account cfg
	PwdCfgBean ideasAccountCfg = new PwdCfgBean();
	ideasAccountCfg.setId(1L);

	String pwd = UserAction.getRandomPwd(sql, ideasAccountCfg);

	AccountBean userAccount = new AccountBean();
	userAccount.setPwdcfg_id(ideasAccountCfg.getId());

	BeanList accounts = UserAction.findAccount(sql, user, userAccount);
	userAccount = (AccountBean) accounts.get(0);

	UserAction.changePwd(sql, userAccount, pwd);

	logger.debug("Random password set!!");
		  
	String userMail = user.getEmail();
		  
	if (userMail != null && !userMail.equals("")) {
		logger.debug("User email: " + userMail);  
		String dest = userMail;
		  
		String TEMPLATE_NAME = "Test Invio Mail";
		String LANG = "EN";
		String MAIL_FROM = "ideas@igidomain.com";
		String MAIL_DETAILS = "<b>Password = " + pwd + "</b>";
					
		EmailTemplateBean filter = new EmailTemplateBean();
		filter.setName(TEMPLATE_NAME);
		EmailTemplateBean template = EmailTemplateAction.findEmailTemplateDetails(sql, filter);
		//template.addBodyDetails("<b>Password = " + pwd + "</b>");  

		ArrayList<String> recipients = new ArrayList<String>();
		recipients.add(dest);

		EmailDataBean emailBean = new EmailDataBean(MAIL_FROM, recipients);
		WebEmailAction.submitEmail(sql,MAIL_DETAILS, "", LANG, IdeasApplications.EMAILSERVICE.getName(), template, emailBean);

		logger.debug("Mail sent!!");
		  
	} else  {
		logger.debug("User Email not defined, not sending email!");
	}
```
