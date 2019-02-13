# Date Manipulation in a Pre-Mapping Rule

## Description
This example demonstrates a rule that was used with a CSV Connector. It formats a date coming from the CSV file into the format needed by IGI.

For a detailed explanation of the rule code, see the Rules Guide.

## Package Imports
The following package imports are required:
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
import com.crossideas.ideasconnector.core.databean.DataBean
import java.util.ArrayList
import com.crossideas.ideasconnector.drivers.ideas.IdeasDriver
import java.text.SimpleDateFormat
import java.util.Date
import java.sql.Timestamp
```

## Rule Code
The code is:
```java
when
    event : Event(  )
then
//Pre Mapping Rule to parse DATE_OF_BIRTH
//based on example of a Pre Mapping Rule parse DATE_OF_BIRTH in IGI 5.2.2 Admin Guide

  logger.debug("Entered DATE_OF_BIRTH pre-mapping rule");

  String attributeName = "DATE_OF_BIRTH";
  DataBean dBean = event.getBean();

  String dateString = (String) dBean.getCurrentAttribute(attributeName);
  logger.debug("DATE_OF_BIRTH Pre-Mapping rule: dateString: " + dateString);
  if (dateString != null) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    try {
      Date date = dateFormat.parse(dateString);
      Timestamp timestamp = new java.sql.Timestamp(date.getTime());
      dBean.setCurrentAttributeValue(attributeName, timestamp);
      logger.debug("DATE_OF_BIRTH pre-mapping rule: updated attribute " + attributeName + "with date");
    } catch (Exception e) {
      dBean.stripCurrentAttribute(attributeName);

      String errorMessage = "DATE_OF_BIRTH wrong format: " + dateString;
      String userid = (String) dBean.getCurrentAttribute("USERID");

      if (userid != null) {
        errorMessage = errorMessage + " for userid: " + userid;
      }
      logger.error(errorMessage);
    }
  }
  logger.debug("Leaving DATE_OF_BIRTH pre-mapping rule");
```
