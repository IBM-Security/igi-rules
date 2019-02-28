# Email New Password to User

## Description
The rule sends an email with the generated password to the user of a restored account.

Before you run the rule, you must have defined the "Password Reset template" email template that automatically notifies the user of the new password. Make sure that the placeholders that you use in the template correspond to the elements of the data map; that is, $P{givenName}, $P{surname}, and $P{password}.

This is a post-mapping rule. It is taken from the IGI product documentation. It's also a good example of creating and sending an email based on a template.

For a detailed explanation of the rule code, see the Rules Guide.

## Package Imports
The following package import is required:
```java
import com.engiweb.logger.impl.Log4JImpl
import com.engiweb.pm.entity.BeanList
import com.engiweb.profilemanager.backend.dao.db.SQLH
import com.engiweb.profilemanager.common.bean.AccountBean
import com.engiweb.profilemanager.common.bean.Block
import com.engiweb.profilemanager.common.enumerations.LockType
import com.engiweb.profilemanager.common.bean.entitlement.EntitlementBean
import com.engiweb.profilemanager.common.bean.ExternalInfo
import com.engiweb.profilemanager.common.bean.OrgUnitBean
import com.engiweb.profilemanager.common.bean.OrgUnitErcBean
import com.engiweb.profilemanager.common.bean.PwdCfgBean
import com.engiweb.profilemanager.common.bean.UserBean
import com.engiweb.profilemanager.common.bean.UserErcBean
import com.engiweb.profilemanager.common.bean.event.EventBean
import com.engiweb.profilemanager.common.bean.event.EventInBean
import com.engiweb.profilemanager.common.ruleengine.action.OrgUnitAction
import com.engiweb.profilemanager.common.ruleengine.action.UserAction
import com.engiweb.profilemanager.common.ruleengine.action.UtilAction
import com.engiweb.profilemanager.common.ruleengine.action.JobRoleAction
import com.engiweb.profilemanager.backend.business.direct.AccountDirect
import com.engiweb.toolkit.common.DBMSException

global com.engiweb.profilemanager.backend.dao.db.SQLH sql
global com.engiweb.logger.impl.Log4JImpl logger
```

```java
import com.crossideas.ideasconnector.core.databean.Event
import com.crossideas.email.common.action.WebEmailAction
import com.crossideas.email.common.bean.EmailDataBean
import com.engiweb.toolkit.common.enums.IdeasApplications
import java.util.ArrayList
import java.util.Map
import java.util.HashMap
```

## Rule Code
The code is:
```java
when
    event : Event(  )
then
//                               
  //----------- CONFIGURATION ---------------                                                   
  String TEMPLATE_NAME = "Password Reset template";                                
  String LANG = "EN";                                
  String MAIL_FROM = "igi@mycompany.com";                                
  String SYSTEM_NAME = "MyLDAP";                               
  //-----------------------------------------

  String newPassword = (String) event.getBean().getCurrentAttribute("erLdapPwdReset");

  if (newPassword != null) {                               

    //Get data from the event. A pre-mapping rule must fill this data.                                               
    String identityEmail = (String) event.getEventAttribute("EMAIL");                                               
    String givenName = (String) event.getEventAttribute("GIVEN_NAME");                                               
    String surname = (String) event.getEventAttribute("SURNAME");

    //Get data from current data bean (if available)                                               
    //String identityEmail = (String) event.getBean().getCurrentAttribute("EMAIL");                                               
    //String givenName = (String) event.getBean().getCurrentAttribute("GIVEN_NAME");                                               
    //String surname = (String) event.getBean().getCurrentAttribute("SURNAME");

    if (identityEmail != null) {

      ArrayList<String> recipients = new ArrayList<String>();                                                               
      recipients.add(identityEmail);

      Map<String, String> dataMap = new HashMap<String, String>();

      dataMap.put("password", newPassword);                                                               
      dataMap.put("givenName", givenName);                                                               
      dataMap.put("surname", surname);                                                               
      dataMap.put("system", SYSTEM_NAME);

      EmailDataBean emailBean = new EmailDataBean(MAIL_FROM, recipients);
      WebEmailAction.submitEmail(sql, dataMap, "", "Connector", LANG, "", IdeasApplications.EMAILSERVICE.getName(), TEMPLATE_NAME, emailBean);
    }
  }
```
