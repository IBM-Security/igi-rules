# Rule Code to Pass Arguments Between Rules

## Description
This is not a unique rule, it is showing how rules can pass arguments between each other in the same flow.

The key components here are:
- The ContainerBean object to pass the data
- The containerBean.put call to set a parameter value
- The containerBean.get call to get a parameter value

## Package Imports
The following should be added to the Package Imports section:
```
import com.engiweb.ruleengine.common.bean.ContainerBean;
```

## Rules
The first rule sets up an argument to be passed.
```
when
  user : UserBean( )
  orgUnit : OrgUnitBean( )
  extInfo : ExternalInfo( )
  containerBean : ContainerBean( )
then
  // [ V1.1 - 2014-05-26 ]
  // ContainerBean<String, Object> containerBean = new ContainerBean<String, Object>();
  YourObject yourObject = new YourObject();
  containerBean.put(“yourKey”, yourObject);
  ...
```

The second rule will retrieve the argument.
```
when
  user : UserBean( )
  orgUnit : OrgUnitBean( )
  extInfo : ExternalInfo( )
  containerBean : ContainerBean( )
then
  // [ V1.1 - 2014-05-26 ]
  YourObject yourObject = containerBean.get(“yourKey”);
  ...
```
