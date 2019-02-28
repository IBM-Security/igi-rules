# Revoke User Entitlements and log the events in SQL Server

## Description
The SQL Server is hosting a custom table where events are being logged. This rule demonstrates how the events could be logged in the SQL Server, whereafter the unwanted entitlements are removed from the user.

For a detailed explanation of the rule code, see the Rules Guide.

## Package Imports
The imports for this rule:
```java
import com.engiweb.logger.impl.Log4JImpl
import com.engiweb.profilemanager.backend.dao.db.SQLH
import com.engiweb.profilemanager.common.bean.AccountBean
import com.engiweb.profilemanager.common.bean.ExternalInfo
import com.engiweb.profilemanager.common.bean.OrgUnitBean
import com.engiweb.profilemanager.common.bean.PwdCfgBean
import com.engiweb.profilemanager.common.bean.UserBean
import com.engiweb.profilemanager.common.bean.UserErcBean
import com.engiweb.profilemanager.common.bean.event.EventBean
import com.engiweb.profilemanager.common.bean.event.EventInBean
import com.engiweb.profilemanager.common.bean.event.EventOutBean
import com.engiweb.profilemanager.common.ruleengine.action.UserAction
import com.engiweb.profilemanager.common.ruleengine.action.UtilAction

global com.engiweb.profilemanager.backend.dao.db.SQLH sql
global com.engiweb.logger.impl.Log4JImpl logger
```

```java
import com.engiweb.pm.web.bean.AbstractBean
import com.engiweb.pm.entity.BeanList
import com.engiweb.pm.entity.Paging
import com.engiweb.profilemanager.common.bean.entitlement.EntitlementBean
import com.crossideas.certification.common.bean.TemplateBean
import com.crossideas.certification.backend.dao.TemplateDAO
import com.crossideas.certification.common.AttestationRes
import com.crossideas.certification.common.enumeration.AttestationTypes
import com.engiweb.profilemanager.common.ruleengine.action.reorganize._UserAction
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement
import java.sql.SQLException
```


## Rule Code
The code is:
```java
when
	userBean : UserBean(  )
	entBean : EntitlementBean(  )
then

	// [ V1.9 - 2015-09-11 ]
	Connection con = null;
	EventOutBean eventOut = new EventOutBean();
	String context = "Reading connection properties from property file.";

	try {
		String serverName = "";
		String portNumber ="";
		String serviceName="";
		String username="XXX";
		String password="XXX";
		String url="jdbc:sqlserver://XXX:1433;DatabaseName=XXX";

		context = "Loading SQL Server driver";
       String driverName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
       Class.forName(driverName);
       context = "Calling DriverManager.getConnection";
       con = DriverManager.getConnection(url, username, password);
       Statement st = con.createStatement();
		st.executeUpdate("INSERT INTO [dbo].[XXXDB_Access_Updates] ([Application_Name],[Role_Name],[Operation],[UserId],[XXX_Integration_Status],[Activity_Status],[Source_RequestID],[Remarks],[Target_RequestID])     VALUES   ('TEST123','TEST123','Remove','TEST123','Pending','Initiated','TEST123','TEST123','Test123')");
       con.close();

		System.out.println("entBean.getApplication_name() " +entBean.getApplication_name());
		eventOut.setApplication(entBean.getApplication_name());
								
		System.out.println("userBean.getCodfisc() "+userBean.getCodfisc());
		eventOut.setCodiceOperazione(userBean.getCodfisc());
								
		System.out.println("userBean.getUserErc() "+userBean.getUserErc());                
		eventOut.setErcStatus(userBean.getUserErc());
								
       System.out.println("entBean.getId() "+entBean.getId());
       eventOut.setId(entBean.getId());
								
       System.out.println("entBean.getPerson_id() "+entBean.getPerson_id());
       eventOut.setPerson(entBean.getPerson_id());
								
       System.out.println("entBean.getPerson_code() "+entBean.getPerson_code());
       eventOut.setPersonCode(entBean.getPerson_code());
								
		eventOut.setUserErc(userBean.getUserErc());
		eventOut.setUserID(userBean.getIdentityUID());

		BeanList<EntitlementBean> bl = new BeanList<EntitlementBean>();
		bl.add(entBean);
		UserBean newUB = new UserBean();
		newUB.setId(userBean.getId());
		_UserAction.removeEntitlements(sql, bl, newUB);
	} catch (SQLException se) {
		// Could not connect to the database
		System.out.println("getConnection: context:"+context+", Exception:"+se);
       se.printStackTrace();
	} catch (Exception e) {
		// Could not connect to the database
		System.out.println("getConnection: context:"+context+", Exception:"+e);
	}

```
