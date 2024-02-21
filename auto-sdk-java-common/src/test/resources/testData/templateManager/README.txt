The files in this directory are processed automatically by the unit test for TemplateManager
(com.applause.auto.framework.TemplateManagerTest).  All the files in this directory that conform
to these naming pattern are processed (files that don't conform, like this one, are ignored)

<methodName>_<descr>_<result>_input.text
<methodName>_<descr>_<result>_data.text
<methodName>_<descr>_<result>_output.text

There are 3 different types of files
   input :  REQUIRED The data is read and passed into the system for processing
            This should be a freemarker template file, but for negative testing
            we allow anything
   data  :  Optional.  A JSON file representing a Map of String --> Object data
            This is read and used as input when processing the input file
   output:  REQUIRED.  What should be output by the system
            The test infrastructure does a straight String.equals(...) call

Each file has 3 parts:

  methodName:  determines which of the unit test methods will process this file
               The methodName _should_ match a method in the com.applause.auto.framework.TemplateManager class
  descr     :  Some easy to understand description of the test.  Keep it to a dozen characters or so
  result    :  The value returned with the 'input' file is processed with the data
               Usually 'true' (for success) or 'false' (for failure)

NOTE WELL:  The '_' is forbidden in the file name unless it's separating one of the 4 fields
            described above
