import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.crossideas.ideasconnector.core.databean.DataBean;
import com.crossideas.ideasconnector.core.databean.Event;
import com.engiweb.logger.impl.Log4JImpl;
import com.engiweb.pm.entity.BeanList;
import com.engiweb.pm.entity.Paging;
import com.engiweb.profilemanager.backend.dao.db.SQLH;
import com.engiweb.profilemanager.common.bean.AccountBean;
import com.engiweb.profilemanager.common.bean.Block;
import com.engiweb.profilemanager.common.bean.UserBean;
import com.engiweb.profilemanager.common.bean.UserErcBean;
import com.engiweb.profilemanager.common.bean.event.EventTargetBean;
import com.engiweb.profilemanager.common.enumerations.LockType;
import com.engiweb.profilemanager.common.ruleengine.action.UserAction;
import com.engiweb.profilemanager.common.ruleengine.action.UtilAction;
import com.engiweb.profilemanager.common.ruleengine.action.reorganize._AccountAction;

public class IGIRules {

	private com.engiweb.logger.impl.Log4JImpl log = new Log4JImpl("");
	private com.engiweb.pm.dao.db.DAO sql = new SQLH(log);
	private com.engiweb.logger.impl.Log4JImpl logger = new Log4JImpl("");

	public void setIdeasPassword() throws Exception {
		String pwd = "testpassword";

		// Ideas Account
		AccountBean ideasAccountBean = new AccountBean();
		ideasAccountBean.setPwdcfg_id(1L);

		// Max 10000 Account
		Paging paging = new Paging(10000);

		BeanList<AccountBean> res = _AccountAction.findAccount(sql, ideasAccountBean, paging);
		for (AccountBean accountBean : res) {
			_AccountAction.changePwd(sql, "", pwd, accountBean);
			System.out.println(accountBean.getEmail());
		}
	}

	public void disableOrphanAccount(EventTargetBean event,AccountBean account) throws Exception {
		if (event.getTarget().equalsIgnoreCase("YYYPRD_ABCD") || event.getTarget().equalsIgnoreCase("ZZZPRD_ABCD")
				|| event.getTarget().equalsIgnoreCase("TEST-ABCD")) {
		
		Block blockCode = new Block();
		blockCode.setBlocco(0, 5);
		account.setBlock(blockCode);
		UserAction.updateAccount(sql, account);
	    logger.info("Account created!");
		event.setTrace("Unable to match Identity! - Auto Disabling Account!");
		}
		
	
	}

	public void matchAccount(EventTargetBean event, AccountBean accountBean) throws Exception {
		// ABCD Matching Rule(Username/Attr12 with UserId/Email)
		if (event.getTarget().equalsIgnoreCase("YYYPRD_ABCD") || event.getTarget().equalsIgnoreCase("ZZZPRD_ABCD")
				|| event.getTarget().equalsIgnoreCase("TEST-ABCD")) {
			if (accountBean.getPerson_id() != null) {
				log.info("!!! account is already matched");
			} else {

				// Gets the email. Email Attribute stores a copy of the user id
				String email = event.getEmail();
				if (email == null) {
					log.info("!!! Empty email, matching on email not applicable... exit !!!");
				} else {

					// Look for the User into IDEAS
					UserBean userFilter = new UserBean();
					userFilter.setPhoneNumber(email);
					BeanList ul = UserAction.find(sql, userFilter);

					boolean found = !ul.isEmpty();
					if (found) {
						log.info("!!! Account Matched by phone number on person & email on account !!!");

						// found
						userFilter = (UserBean) ul.get(0);
						log.info("!!! User Found" + userFilter.getId() + " !!!");
						accountBean.setPerson_id(userFilter.getId()); // !!

						String eventUserCode = event.getCode();
						if (eventUserCode != null) {
							accountBean.setCode(eventUserCode);
						}

						if (accountBean.getId() != null) {
							// the account already exist but it is
							// unmatched/orphan
							// Lock to Set

							UserAction.updateAccount(sql, accountBean);
							log.info("!!! Account exist but it is unmatched/orphan !!!");
						} else {
							UserAction.addAccount(sql, accountBean);
							log.info("!!! Account : " + accountBean.getCode() + " created !!!");
						}
					}
				}
			}
		}
	}

	public void setIdeasAccountExpiryFromUser(UserBean userBean, UserErcBean userErcBean) {
		logger.info("!!! Commencing Rule SetExpiration");
		logger.info("User to process : " + userBean.getCode());

		AccountBean userAccount = new AccountBean();
		userAccount.setPwdcfg_id(1L); // IDEAS account cfg, ID=1
		userAccount.getPwdcfg_id();
		BeanList accounts = null;
		try {
			accounts = UserAction.findAccount(sql, userBean, userAccount);
			userAccount = (AccountBean) accounts.get(0);

			Date expiryDate = (Date) userErcBean.getAttribute("ACCOUNT_EXPIRY_DATE");

			if (expiryDate != null) {
				String stringDate = new SimpleDateFormat("dd-MM-yyyy").format(expiryDate);
				logger.info("Exp2:  " + stringDate);

				if (userAccount != null) {
					userAccount.setExpire(expiryDate);
					logger.info("Expiration Date for Ideas set!!!!");
					UserAction.updateAccount(sql, userAccount);
				}
			} else {
				logger.info("!!! No expiry date found!!! ");
			}
		} catch (Exception e) {
			logger.error("!!! Error occured in SetExpiration rule !!!");
		}

	}

	public void SetDatesOnAccount(EventTargetBean event, AccountBean accountBean) throws Exception {
		// Set dates on Account [ABCD]
		if (accountBean.getPerson_id() != null) {
			if (event.getTarget().equalsIgnoreCase("YYYPRD_ABCD") || event.getTarget().equalsIgnoreCase("ZZZPRD_ABCD")
					|| event.getTarget().equalsIgnoreCase("TEST-ABCD")) {
				log.info("!!! Commencing Rule SetDatesOnAccount: " + event.getTarget());
				log.info("!!! User to process : " + accountBean.getCode() + " " + accountBean.getName());

				// Gets the change password date
				String stringDate = event.getAttr5();
				log.info("!!! " + stringDate);
				log.info("!!! event.getAttr1()" + event.getAttr1() + "-event.getAttr5()" + event.getAttr5());
				if (stringDate != null) {
					Date dateToSet = new SimpleDateFormat("yyyy-MM-dd").parse(stringDate);
					Timestamp timestamp = new java.sql.Timestamp(dateToSet.getTime());

					if (accountBean != null) {
						log.info("About to set Change Password Date for Account !!!!");
						accountBean.setLastChangePwd(timestamp);
						accountBean.setLastlogin(timestamp);
						log.info("Change Password Date set for Account !!!!");
						UserAction.updateAccount(sql, accountBean);
					}
				} else {
					log.info("!!! No date found to set !!! ");
				}
			}
		}

	}

	public void setVariablesUser(Event event) {
		// On User
		DataBean dBean = event.getBean();
		String id = (String) dBean.getCurrentAttribute("EmployeeID");
		String FirstName = (String) dBean.getCurrentAttribute("FirstName");
		String LastName = (String) dBean.getCurrentAttribute("LastName");
		String dateString = (String) dBean.getCurrentAttribute("EndContractDate");
		String action = (String) dBean.getCurrentAttribute("Activity");

		String var_FullName = "";
		String var_email = "generic-email@test.poc.com";
		String var_ou = "Active Users";
		String var_status = "0";

		if ((id != null) && !(id.equals(""))) {
			var_email = id + "@test.poc.com";
			event.getBean().setCurrentAttributeValue("var_email", var_email);
		}
		if (true) {
			if (FirstName == null) {
				ThFirstName = "";
			}
			if (ThLastName == null) {
				ThLastName = "";
			}
			var_FullName = FirstName + " " + LastName;
			event.getBean().setCurrentAttributeValue("var_FullName", var_FullName);
		}
		if (dateString != null) {
			SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

			try {
				Date var_contractEndDate = format.parse(dateString);
				Timestamp timestamp = new java.sql.Timestamp(var_contractEndDate.getTime());
				dBean.setCurrentAttributeValue("var_contractEndDate", timestamp);

			} catch (Exception e) {
				log.error("EndContractDate wrong fromat");
			}
		}
		
		if (action == null) {
			// ADD
			return;
		} else if (action.equalsIgnoreCase("Delete") || action.equalsIgnoreCase("PS-TERMINATE")) {
			event.setType(Event.TYPE_MODIFY);
			var_ou = "Terminated Users";
			var_status = "1";
			dBean.setCurrentAttributeValue("var_ou", var_ou);
			dBean.setCurrentAttributeValue("var_status", var_status);
		} else if (action.equalsIgnoreCase("NewAccount") || action.equalsIgnoreCase("PS-CREATE")) {
			event.setType(Event.TYPE_ADD);
		} else if (action.equalsIgnoreCase("PS-UPDATE")) {
			event.setType(Event.TYPE_MODIFY);
		}

	}

	public void setVariablesAccount(Event event) throws Exception {
		// On Account
		DataBean dBean = event.getBean();
		String dateString = (String) dBean.getCurrentAttribute("LAST_PW_CHANGE");
		log.info("!!! date received - " + dateString);

		if (dateString != null) {
			dateString = dateString.split(" ")[0];
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

			Date var_changePwdDate = format.parse(dateString);
			log.info("!!! date processed- " + var_changePwdDate);
			Timestamp timestamp = new java.sql.Timestamp(var_changePwdDate.getTime());
			dBean.setCurrentAttributeValue("var_changePwdDate", timestamp);

		}
	}

	public void sendEmailForTermination(UserBean userBean, UserErcBean userErcBean) throws Exception {
		String userMail = userBean.getEmail();
		String level1ManagerId = (String) userErcBean.getAttribute("Attr1");
		String level2ManagerId = (String) userErcBean.getAttribute("Attr2");
		String level1ManagerEmail = "level1Manager-generic@test.poc.com";
		String level2ManagerEmail = "level2Manager-generic@test.poc.com";
		logger.info("level1ManagerId: " + level1ManagerId + ", level2ManagerId: " + level2ManagerId);
		UserBean level1ManagerBean = UtilAction.findUserByCode(sql, level1ManagerId);
		UserBean level2ManagerBean = UtilAction.findUserByCode(sql, level2ManagerId);

		if (level1ManagerBean == null) {
			logger.error("Level 1 Manager not found!");
		} else {
			level1ManagerEmail = level1ManagerBean.getEmail();
		}
		if (level2ManagerBean == null) {
			logger.error("Level 2 Manager not found!");
		} else {
			level2ManagerEmail = level2ManagerBean.getEmail();
		}
		logger.info("User email: " + userMail + " level1ManagerEmail: " + level1ManagerEmail + " level2ManagerEmail: "
				+ level2ManagerEmail);

		String TEMPLATE_NAME = "NotifyTermination";
		String LANG = "EN";
		String MAIL_FROM = "admin@test.poc.com";
		String MAIL_SUBJECT = userBean.getSurname() + "[" + userBean.getCode() + "] has been terminated.";
		String MAIL_DETAILS = "All accounts for " + userBean.getName() + " " + userBean.getSurname() + "["
				+ userBean.getCode()
				+ "] have been suspended due to user Resignation/Termination.\n\n You are receiving this email as the Level 1/Level 2 Manager";

		ArrayList<String> recipients = new ArrayList<String>();
		recipients.add(level1ManagerEmail);
		recipients.add(level2ManagerEmail);

		final String SMTP = "x.x.x.x";
		final int SMTP_PORT = 0;
		String MAIL_TO = "" + level1ManagerEmail + "," + level2ManagerEmail;
		// Send email using specified SMTP
		UtilAction.sendMail(null, MAIL_FROM, MAIL_TO, null, MAIL_SUBJECT, MAIL_DETAILS, SMTP, SMTP_PORT);
		logger.info("Mail sent!!");

	}	
}
