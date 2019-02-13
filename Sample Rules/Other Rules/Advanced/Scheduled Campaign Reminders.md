# Certification Campaign Email Reminders and Expiration

## Description
The IGI certification campaign mechanism includes notifications. You can configure notifications for campaign start, campaign reaching some thresholds and campaign expiry.

However a customer needed a reminder mechanism as follows:
* If reviews are outstanding for five (5) days, send a reminder to the reviewer.
* If reviews are outstanding for ten (10) days, send another reminder to the reviewer.
* If reviews are outstanding for fifteen (15) days, send an escalation email.
* If reviews are outstanding for eighteen (18) days, revoke the access.
This could not be done with simple campaign notifications, it required a custom solution.

The solution revolves around a scheduled task that runs daily to go search for any outstanding certification campaign items, and if they fall into the five/ten/fifteen/eighteen day periods, send an email as a reminder or revoke the access.

It involves:
1. Rules and a Rule Flow to extract the campaign items, check dates and perform the email/revoke.
2. A Task and Job to run the rule flow, and
3. Notification Templates to be used by the emails from the rules.

The rules below show examples of pulling data directly from the IGI DB and sending emails using a pre-defined email template.

This file shows the rules used. For a detailed explanation of the rule code as well as the task/job and notification template configuration, see the Rules Guide.

## Package Imports
The following is a complete extract of the package imports for these rules (many of them will already be there):
```java
import com.crossideas.ap.common.ruleengine.action.RequestGenerationRule
import com.crossideas.certification.backend.dao.TemplateDAO
import com.crossideas.certification.common.AttestationRes
import com.crossideas.certification.common.bean.AttestationBean
import com.crossideas.certification.common.bean.TemplateBean
import com.crossideas.certification.common.enumeration.AttestationTypes
import com.engiweb.logger.impl.Log4JImpl
import com.engiweb.pm.dao.db.DAO
import com.engiweb.pm.entity.BeanList
import com.engiweb.pm.entity.Paging
import com.engiweb.pm.web.bean.AbstractBean
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
import com.engiweb.profilemanager.common.enumerations.LockType
import com.engiweb.profilemanager.common.ruleengine.action.JobRoleAction
import com.engiweb.profilemanager.common.ruleengine.action.OrgUnitAction
import com.engiweb.profilemanager.common.ruleengine.action.reorganize._AccountAction
import com.engiweb.profilemanager.common.ruleengine.action.reorganize._OrgUnitAction
import com.engiweb.profilemanager.common.ruleengine.action.reorganize._UserAction
import com.engiweb.profilemanager.common.ruleengine.action.UserAction
import com.engiweb.profilemanager.common.ruleengine.action.UtilAction
import com.engiweb.toolkit.common.DBMSException
import java.sql.ResultSet
import java.sql.Statement
import java.util.HashMap
import java.util.Iterator
import java.sql.ResultSet
import java.util.ArrayList
import java.util.HashMap
import java.util.Map
import com.crossideas.email.common.action.WebEmailAction
import com.crossideas.email.common.bean.EmailDataBean
import com.engiweb.toolkit.common.enums.IdeasApplications
import com.crossideas.certification.backend.business.direct.PersonReviewDirect
import com.crossideas.certification.common.bean.EmploymentReviewBean
global com.engiweb.logger.impl.Log4JImpl logger
global com.engiweb.pm.dao.db.DAO sql
global com.engiweb.profilemanager.backend.dao.db.SQLH sql
```

## Rule Code

### Rule 1 - CCReminder 05 days
To find outstanding items 5 days old and send email.

The code is:
```java
when
  eval( true )
then
//
  final String cc = "User Transfer Review";
  final String EMAIL_TEMPLATE = "CCReminder";
  final String MAIL_FROM = "CCReminder@acme.com";

  StringBuilder sb = new StringBuilder();
  sb.append("SELECT DISTINCT");
  sb.append(" rev.NAME AS REVIEWER_NAME,");
  sb.append(" rev.SURNAME AS REVIEWER_SURNAME,");
  sb.append(" rev.CODE AS REVIEWER_CODE,");
  sb.append(" rev.EMAIL AS REVIEWER_EMAIL,");
  sb.append(" u.NAME AS USER_NAME,");
  sb.append(" u.SURNAME AS USER_SURNAME,");
  sb.append(" u.CODE AS USER_CODE ");
  sb.append("FROM");
  sb.append(" IGACORE.ATTESTATION a,");
  sb.append(" IGACORE.EMPLOYMENT_REVIEW er,");
  sb.append(" IGACORE.EMPLOYMENT_REVIEWER err,");
  sb.append(" IGACORE.PERSON rev,");
  sb.append(" IGACORE.PERSON u ");
  sb.append("WHERE");
  sb.append(" a.NAME = '"+ cc +"'");
  sb.append(" AND a.ID = er.ATTESTATION");
  sb.append(" AND er.ID = err.EMPLOYMENT_REVIEW");
  sb.append(" AND err.CERT_FIRST_OWNER = rev.ID");
  sb.append(" AND er.PERSON = u.ID");
  sb.append(" AND er.REVIEW_STATE = 0");
  sb.append(" AND (CURRENT_DATE BETWEEN (er.CREATION_DATE + 5) AND (er.CREATION_DATE + 6))");

  String SQL_QUERY = sb.toString();
  logger.debug("REMINDER 5 DAYS QUERY:\n" + SQL_QUERY);

  ResultSet rs = sql.getCntSQL().getConnection().createStatement().executeQuery(SQL_QUERY);

  while (rs.next()) {

    String REVIEWER_NAME = rs.getString(1);
    String REVIEWER_SURNAME = rs.getString(2);
    String REVIEWER_CODE = rs.getString(3);
    String REVIEWER_EMAIL = rs.getString(4);
    String USER_NAME = rs.getString(5);
    String USER_SURNAME = rs.getString(6);
    String USER_CODE = rs.getString(7);

    // Add users to send out notifications
    ArrayList<String> recipients = new ArrayList<String>();
    recipients.add(REVIEWER_EMAIL);

    Map map = new HashMap();
    map.put("$P{attestation.campaign.recipient.name}", REVIEWER_NAME);
    map.put("$P{attestation.campaign.recipient.surname}", REVIEWER_SURNAME);
    map.put("$P{email}", REVIEWER_EMAIL);
    map.put("$P{details}", "Involved User: " + USER_NAME + " " + USER_SURNAME + " (" + USER_CODE + ")");

    EmailDataBean emailBean = new EmailDataBean(MAIL_FROM, recipients);
    WebEmailAction.submitEmail(sql, map, "", "admin", "EN", "EN", IdeasApplications.EMAILSERVICE.getName(), EMAIL_TEMPLATE, emailBean);

    logger.info("Sent an Email to: " + REVIEWER_NAME + "-" + REVIEWER_SURNAME + "-" + REVIEWER_CODE + "-" + REVIEWER_EMAIL);
    logger.info("5 days no action on: " + USER_NAME + "-" + USER_SURNAME + "-" + USER_CODE);

  }

  rs.close();  
```

### Rule 2 - CCReminder 10 days
To find outstanding items 10 days old and send email.

The code is:
```java
when
  eval( true )
then
//
  final String cc = "User Transfer Review";
  final String EMAIL_TEMPLATE = "CCReminder";
  final String MAIL_FROM = "CCReminder@acme.com";

  StringBuilder sb = new StringBuilder();
  sb.append("SELECT DISTINCT");
  sb.append(" rev.NAME AS REVIEWER_NAME,");
  sb.append(" rev.SURNAME AS REVIEWER_SURNAME,");
  sb.append(" rev.CODE AS REVIEWER_CODE,");
  sb.append(" rev.EMAIL AS REVIEWER_EMAIL,");
  sb.append(" u.NAME AS USER_NAME,");
  sb.append(" u.SURNAME AS USER_SURNAME,");
  sb.append(" u.CODE AS USER_CODE ");
  sb.append("FROM");
  sb.append(" IGACORE.ATTESTATION a,");
  sb.append(" IGACORE.EMPLOYMENT_REVIEW er,");
  sb.append(" IGACORE.EMPLOYMENT_REVIEWER err,");
  sb.append(" IGACORE.PERSON rev,");
  sb.append(" IGACORE.PERSON u ");
  sb.append("WHERE");
  sb.append(" a.NAME = '"+ cc +"'");
  sb.append(" AND a.ID = er.ATTESTATION");
  sb.append(" AND er.ID = err.EMPLOYMENT_REVIEW");
  sb.append(" AND err.CERT_FIRST_OWNER = rev.ID");
  sb.append(" AND er.PERSON = u.ID");
  sb.append(" AND er.REVIEW_STATE = 0");
  sb.append(" AND (CURRENT_DATE BETWEEN (er.CREATION_DATE + 10) AND (er.CREATION_DATE + 11))");

  String SQL_QUERY = sb.toString();
  logger.debug("REMINDER 5 DAYS QUERY:\n" + SQL_QUERY);

  ResultSet rs = sql.getCntSQL().getConnection().createStatement().executeQuery(SQL_QUERY);

  while (rs.next()) {

    String REVIEWER_NAME = rs.getString(1);
    String REVIEWER_SURNAME = rs.getString(2);
    String REVIEWER_CODE = rs.getString(3);
    String REVIEWER_EMAIL = rs.getString(4);
    String USER_NAME = rs.getString(5);
    String USER_SURNAME = rs.getString(6);
    String USER_CODE = rs.getString(7);

    // Add users to send out notifications
    ArrayList<String> recipients = new ArrayList<String>();
    recipients.add(REVIEWER_EMAIL);

    Map map = new HashMap();
    map.put("$P{attestation.campaign.recipient.name}", REVIEWER_NAME);
    map.put("$P{attestation.campaign.recipient.surname}", REVIEWER_SURNAME);
    map.put("$P{email}", REVIEWER_EMAIL);
    map.put("$P{details}", "Involved User: " + USER_NAME + " " + USER_SURNAME + " (" + USER_CODE + ")");

    EmailDataBean emailBean = new EmailDataBean(MAIL_FROM, recipients);
    WebEmailAction.submitEmail(sql, map, "", "admin", "EN", "EN", IdeasApplications.EMAILSERVICE.getName(), EMAIL_TEMPLATE, emailBean);

    logger.info("Sent an Email to: " + REVIEWER_NAME + "-" + REVIEWER_SURNAME + "-" + REVIEWER_CODE + "-" + REVIEWER_EMAIL);
    logger.info("5 days no action on: " + USER_NAME + "-" + USER_SURNAME + "-" + USER_CODE);

  }

  rs.close();  
```

### Rule 3 - CCReminder 15 Days (Escalate Email)
To find outstanding items 15 days old and send escalation email.

The code is:
```java
when
  eval( true )
then
//

  final String cc = "User Transfer Review";
  final String EMAIL_TEMPLATE = "CCReminderEscalate";
  final String MAIL_FROM = "CCReminder@acme.com";

  StringBuilder sb = new StringBuilder();
  sb.append("SELECT DISTINCT");
  sb.append(" rev.NAME AS REVIEWER_NAME,");
  sb.append(" rev.SURNAME AS REVIEWER_SURNAME,");
  sb.append(" rev.CODE AS REVIEWER_CODE,");
  sb.append(" rev.EMAIL AS REVIEWER_EMAIL,");
  sb.append(" mrev.NAME AS MREVIEWER_NAME,");
  sb.append(" mrev.SURNAME AS MREVIEWER_SURNAME,");
  sb.append(" ue.ATTR12 AS MREVIEWER_CODE,");
  sb.append(" ue.ATTR13 AS MREVIEWER_EMAIL,");
  sb.append(" u.NAME AS USER_NAME,");
  sb.append(" u.SURNAME AS USER_SURNAME,");
  sb.append(" u.CODE AS USER_CODE ");
  sb.append("FROM");
  sb.append(" IGACORE.ATTESTATION a,");
  sb.append(" IGACORE.EMPLOYMENT_REVIEW er,");
  sb.append(" IGACORE.EMPLOYMENT_REVIEWER err,");
  sb.append(" IGACORE.USER_ERC ue,");
  sb.append(" IGACORE.PERSON mrev,");
  sb.append(" IGACORE.PERSON rev,");
  sb.append(" IGACORE.PERSON u ");
  sb.append("WHERE");
  sb.append(" a.NAME = '"+ cc +"'");
  sb.append(" AND a.ID = er.ATTESTATION");
  sb.append(" AND er.ID = err.EMPLOYMENT_REVIEW");
  sb.append(" AND err.CERT_FIRST_OWNER = rev.ID");
  sb.append(" AND er.PERSON = u.ID");
  sb.append(" AND ue.ID = rev.USER_ERC");
  sb.append(" AND ue.ATTR12 = mrev.CODE");
  sb.append(" AND er.REVIEW_STATE = 0");
  sb.append(" AND (CURRENT_DATE BETWEEN (er.CREATION_DATE + 15) AND (er.CREATION_DATE + 16))");
  String SQL_QUERY = sb.toString();
  logger.debug("REMINDER TO Manager 10 DAYS QUERY:\n" + SQL_QUERY);

  ResultSet rs = sql.getCntSQL().getConnection().createStatement().executeQuery(SQL_QUERY);

  while (rs.next()) {

    String REVIEWER_NAME = rs.getString(1);
    String REVIEWER_SURNAME = rs.getString(2);
    String REVIEWER_CODE = rs.getString(3);
    String REVIEWER_EMAIL = rs.getString(4);
    String MREVIEWER_NAME = rs.getString(5);
    String MREVIEWER_SURNAME = rs.getString(6);
    String MREVIEWER_CODE = rs.getString(7);
    String MREVIEWER_EMAIL = rs.getString(8);
    String USER_NAME = rs.getString(9);
    String USER_SURNAME = rs.getString(10);
    String USER_CODE = rs.getString(11);

    // Add users to send out notifications
    ArrayList<String> recipients = new ArrayList<String>();
    recipients.add(MREVIEWER_EMAIL);

    Map<String, String> map = new HashMap<String, String>();
    map.put("$P{attestation.campaign.recipient.name}", REVIEWER_NAME);
    map.put("$P{attestation.campaign.recipient.surname}", REVIEWER_SURNAME);
    map.put("$P{mname}", MREVIEWER_NAME);
    map.put("$P{msurname}", MREVIEWER_SURNAME);
    map.put("$P{details}", "15 Days passed, and Reviewer did not processed tickets. Involved User: " + USER_NAME + " " + USER_SURNAME + " (" + USER_CODE + ")");

    EmailDataBean emailBean = new EmailDataBean(MAIL_FROM, recipients);

    WebEmailAction.submitEmail(sql, map, "", "admin", "EN", "EN", IdeasApplications.EMAILSERVICE.getName(), EMAIL_TEMPLATE, emailBean);
    // logger lines removed for clarity

  }

  rs.close();
```

### Rule 4 - CC Reminder 18 Days (Revoke)
To find outstanding items 18 days old and revoke.

The code is:
```java
when
  eval( true )
then
//
  final String cc = "User Transfer Review";

  StringBuilder sb = new StringBuilder();
  sb.append("SELECT DISTINCT");
  sb.append(" er.ID AS EMP_TO_REVIEW,");
  sb.append(" rev.CODE AS REVIEWER_CODE,");
  sb.append(" u.CODE AS USER_CODE,");
  sb.append(" ent.NAME AS ENT_NAME ");
  sb.append("FROM");
  sb.append(" IGACORE.ATTESTATION a,");
  sb.append(" IGACORE.EMPLOYMENT_REVIEW er,");
  sb.append(" IGACORE.EMPLOYMENT_REVIEWER err,");
  sb.append(" IGACORE.PERSON rev,");
  sb.append(" IGACORE.ENTITLEMENT ent,");
  sb.append(" IGACORE.PERSON u ");
  sb.append("WHERE");
  sb.append(" a.NAME = '"+ cc +"'");
  sb.append(" AND a.ID = er.ATTESTATION");
  sb.append(" AND er.ID = err.EMPLOYMENT_REVIEW");
  sb.append(" AND err.CERT_FIRST_OWNER = rev.ID");
  sb.append(" AND er.PERSON = u.ID");
  sb.append(" AND er.ENTITLEMENT = ent.ID");
  sb.append(" AND er.REVIEW_STATE = 0");
  sb.append(" AND (CURRENT_DATE BETWEEN (er.CREATION_DATE + 18) AND (er.CREATION_DATE + 20))");
  String SQL_QUERY = sb.toString();

  logger.debug("Revoke Entitlements More than 18 DAYS QUERY:\n" + SQL_QUERY);

  PersonReviewDirect pRD = new PersonReviewDirect();

  ResultSet rs = sql.getCntSQL().getConnection().createStatement().executeQuery(SQL_QUERY);

  while (rs.next()) {

    long EMP_TO_REVIEW = rs.getLong(1);
    String REVIEWER_CODE = rs.getString(2);
    String USER_CODE = rs.getString(3);
    String ENT_NAME = rs.getString(4);

    EmploymentReviewBean erb = new EmploymentReviewBean();
    erb.setId(EMP_TO_REVIEW);

    BeanList<EmploymentReviewBean> lerb = new BeanList<>();
    lerb.add(erb);

    logger.info("Revoking " + ENT_NAME + " to " + USER_CODE);

    pRD.revoke(lerb, "18 days passed: Revoked by System", REVIEWER_CODE, sql);

  }

  rs.close();

```
