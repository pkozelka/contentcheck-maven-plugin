# ContentCheck Maven plugin

 is useful for checking content of project's output artifacts, like WAR or EAR files.
 
Sometimes you simply need to be confident that some files are present, others are not present, and some - typically 
those brought by your product - do not need checking.

Due to complex dependency graphs that tend to grow very quickly even for simple projects, it's hard to maintain 
the contents, and keep it in sync with your company's list of approved thirdparty artifacts.

This plugin helps you by detecting comparing actual content of your deliverable with list of restrictions that you write.
Failure to match can fail the build, thus attracting attention to potentially problematic newcoming artifacts.
You can resolve it by enabling in plugin configuration, or by excluding the dependency - whatever is best for matching 
both developer needs and your company policy.

Legal issues

* source file must contain only approved 3rd party libraries
* source file must contain a license file
* content completeness
* source file must/must not contain some additional files

Technical issues

* libraries provided by application server must not get into runtime (`servlet.jar`, `mail.jar` etc.)
* non-production libraries, used for testing, coverage, debugging and other development-time purpose, must stay away from the production archive
* libraries with conflicting classes can cause problems, and should be added carefully
